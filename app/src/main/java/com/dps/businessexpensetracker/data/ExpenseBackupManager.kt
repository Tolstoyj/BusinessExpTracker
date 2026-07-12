package com.dps.businessexpensetracker.data

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupWriteResult(
    val expenseCount: Int,
    val salesCount: Int,
    val attachmentCount: Int,
    val missingAttachmentCount: Int
)

data class BackupRestoreResult(
    val expenses: List<Expense>,
    val sales: List<Sale>,
    val attachmentCount: Int,
    val createdAt: String,
    internal val restoreDirectory: File
)

object ExpenseBackupManager {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun configuredBackupUri(context: Context): Uri? =
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AUTOMATIC_BACKUP_URI, null)
            ?.let(Uri::parse)

    fun configureAutomaticBackup(context: Context, uri: Uri) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_AUTOMATIC_BACKUP_URI, uri.toString()) }
    }

    fun disableAutomaticBackup(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_AUTOMATIC_BACKUP_URI) }
    }

    fun writeAutomaticBackup(
        context: Context,
        expenses: List<Expense>,
        sales: List<Sale> = emptyList(),
        onFailure: ((Throwable) -> Unit)? = null
    ) {
        val uri = configuredBackupUri(context) ?: return
        writeBackup(context, uri, expenses, sales, onSuccess = {}, onFailure = { error ->
            disableAutomaticBackup(context)
            onFailure?.invoke(error)
        })
    }

    fun writeBackup(
        context: Context,
        targetUri: Uri,
        expenses: List<Expense>,
        sales: List<Sale> = emptyList(),
        onSuccess: (BackupWriteResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val appContext = context.applicationContext
        executor.execute {
            val temporaryFile = File.createTempFile("expense-backup-", ".tmp", appContext.cacheDir)
            runCatching {
                val result = createArchive(appContext, temporaryFile, expenses, sales)
                require(temporaryFile.length() <= MAX_BACKUP_FILE_BYTES) {
                    "Backup is larger than the supported limit."
                }
                appContext.contentResolver.openOutputStream(targetUri, "rwt")?.use { output ->
                    temporaryFile.inputStream().use { input -> input.copyTo(output) }
                } ?: error("The selected backup file cannot be written.")
                result
            }.onSuccess { result ->
                mainHandler.post { onSuccess(result) }
            }.onFailure { error ->
                mainHandler.post { onFailure(error) }
            }
            temporaryFile.delete()
        }
    }

    fun restoreBackup(
        context: Context,
        sourceUri: Uri,
        onSuccess: (BackupRestoreResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val appContext = context.applicationContext
        executor.execute {
            runCatching { readArchive(appContext, sourceUri) }
                .onSuccess { result -> mainHandler.post { onSuccess(result) } }
                .onFailure { error -> mainHandler.post { onFailure(error) } }
        }
    }

    fun discardRestore(result: BackupRestoreResult) {
        result.restoreDirectory.deleteRecursively()
    }

    private fun createArchive(
        context: Context,
        target: File,
        expenses: List<Expense>,
        sales: List<Sale>
    ): BackupWriteResult {
        require(expenses.size <= MAX_EXPENSES) { "Too many expenses for one backup." }
        require(sales.size <= MAX_SALES) { "Too many sales for one backup." }
        val expenseArray = JSONArray()
        val salesArray = JSONArray()
        var includedAttachments = 0
        var missingAttachments = 0
        var totalAttachmentBytes = 0L

        ZipOutputStream(BufferedOutputStream(target.outputStream())).use { zip ->
            expenses.forEachIndexed { index, expense ->
                val json = expense.toJson()
                    .put("attachmentUri", JSONObject.NULL)
                val attachmentUri = expense.attachmentUri?.let(Uri::parse)
                if (attachmentUri != null) {
                    val extension = safeExtension(expense.attachmentName)
                    val entryName = "attachments/${safeIdentifier(expense.id)}-$index.$extension"
                    val copied = runCatching {
                        context.contentResolver.openInputStream(attachmentUri)?.use { input ->
                            zip.putNextEntry(ZipEntry(entryName))
                            try {
                                val copiedBytes = input.copyToWithLimit(
                                    output = zip,
                                    remainingBytes = MAX_BACKUP_BYTES - totalAttachmentBytes
                                )
                                totalAttachmentBytes += copiedBytes
                            } finally {
                                zip.closeEntry()
                            }
                        } ?: error("Attachment is no longer readable.")
                    }.isSuccess
                    if (copied) {
                        json.put("backupAttachmentEntry", entryName)
                        includedAttachments++
                    } else {
                        json.put("attachmentName", JSONObject.NULL)
                        missingAttachments++
                    }
                }
                expenseArray.put(json)
            }

            sales.forEach { sale -> salesArray.put(sale.toJson()) }

            zip.writeTextEntry(EXPENSES_ENTRY, expenseArray.toString())
            zip.writeTextEntry(SALES_ENTRY, salesArray.toString())
            zip.writeTextEntry(
                MANIFEST_ENTRY,
                JSONObject()
                    .put("format", FORMAT_NAME)
                    .put("schemaVersion", SCHEMA_VERSION)
                    .put("createdAt", OffsetDateTime.now().toString())
                    .put("expenseCount", expenses.size)
                    .put("salesCount", sales.size)
                    .put("attachmentCount", includedAttachments)
                    .toString()
            )
        }

        return BackupWriteResult(
            expenseCount = expenses.size,
            salesCount = sales.size,
            attachmentCount = includedAttachments,
            missingAttachmentCount = missingAttachments
        )
    }

    private fun readArchive(context: Context, sourceUri: Uri): BackupRestoreResult {
        val restoreDirectory = File(
            context.filesDir,
            "$RESTORE_DIRECTORY/restore-${System.currentTimeMillis()}"
        ).apply { mkdirs() }
        return runCatching {
            var manifestText: String? = null
            var expensesText: String? = null
            var salesText: String? = null
            var entryCount = 0
            var restoredBytes = 0L
            val attachmentFiles = mutableMapOf<String, File>()

            context.contentResolver.openInputStream(sourceUri)?.use { rawInput ->
                ZipInputStream(BufferedInputStream(rawInput)).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        entryCount++
                        require(entryCount <= MAX_ZIP_ENTRIES) { "Backup contains too many files." }
                        require(isSafeEntryName(entry.name)) { "Backup contains an unsafe file path." }
                        when (entry.name) {
                            MANIFEST_ENTRY -> manifestText = zip.readTextWithLimit(MAX_MANIFEST_BYTES)
                            EXPENSES_ENTRY -> expensesText = zip.readTextWithLimit(MAX_EXPENSES_JSON_BYTES)
                            SALES_ENTRY -> salesText = zip.readTextWithLimit(MAX_SALES_JSON_BYTES)
                            else -> if (entry.name.startsWith("attachments/") && !entry.isDirectory) {
                                require(entry.name !in attachmentFiles) { "Duplicate attachment entry." }
                                val destination = File(restoreDirectory, "attachment-$entryCount.bin")
                                destination.outputStream().use { output ->
                                    val copied = zip.copyToWithLimit(
                                        output,
                                        MAX_BACKUP_BYTES - restoredBytes
                                    )
                                    restoredBytes += copied
                                }
                                attachmentFiles[entry.name] = destination
                            }
                        }
                        zip.closeEntry()
                    }
                }
            } ?: error("The selected backup file cannot be read.")

            val manifest = JSONObject(manifestText ?: error("Backup manifest is missing."))
            require(manifest.optString("format") == FORMAT_NAME) { "This is not an expense backup." }
            val schemaVersion = manifest.optInt("schemaVersion")
            require(schemaVersion in MIN_SUPPORTED_SCHEMA_VERSION..SCHEMA_VERSION) {
                "This backup version is not supported."
            }
            val expenseArray = JSONArray(expensesText ?: error("Backup expense data is missing."))
            require(expenseArray.length() <= MAX_EXPENSES) { "Backup contains too many expenses." }

            val usedAttachments = mutableSetOf<String>()
            val restoredIds = mutableSetOf<String>()
            val expenses = buildList {
                for (index in 0 until expenseArray.length()) {
                    val json = expenseArray.getJSONObject(index)
                    val expense = Expense.fromJson(json)
                    validateRestoredExpense(expense)
                    require(restoredIds.add(expense.id)) { "Backup contains duplicate expense IDs." }
                    val attachmentEntry = json.optString("backupAttachmentEntry")
                    val attachmentFile = attachmentFiles[attachmentEntry]
                    if (attachmentFile != null) {
                        usedAttachments += attachmentEntry
                        val finalName = "${safeIdentifier(expense.id)}-${safeFileName(expense.attachmentName)}"
                        val finalFile = File(restoreDirectory, finalName)
                        require(attachmentFile.renameTo(finalFile)) { "Could not restore an attachment." }
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            finalFile
                        )
                        add(expense.copy(attachmentUri = uri.toString()))
                    } else {
                        add(expense.copy(attachmentUri = null, attachmentName = null))
                    }
                }
            }
            val sales = if (schemaVersion >= 2) {
                val salesArray = JSONArray(salesText ?: error("Backup sales data is missing."))
                require(salesArray.length() <= MAX_SALES) { "Backup contains too many sales." }
                val restoredSaleIds = mutableSetOf<String>()
                buildList {
                    for (index in 0 until salesArray.length()) {
                        val sale = Sale.fromJson(salesArray.getJSONObject(index))
                        validateRestoredSale(sale)
                        require(restoredSaleIds.add(sale.id)) {
                            "Backup contains duplicate sale IDs."
                        }
                        add(sale)
                    }
                }
            } else {
                emptyList()
            }
            attachmentFiles
                .filterKeys { it !in usedAttachments }
                .values
                .forEach(File::delete)

            BackupRestoreResult(
                expenses = expenses,
                sales = sales,
                attachmentCount = usedAttachments.size,
                createdAt = manifest.optString("createdAt"),
                restoreDirectory = restoreDirectory
            )
        }.getOrElse { error ->
            restoreDirectory.deleteRecursively()
            throw error
        }
    }

    private fun validateRestoredExpense(expense: Expense) {
        require(expense.id.isNotBlank()) { "Backup contains an expense without an ID." }
        require(expense.vendor.length <= 500) { "Backup contains an invalid vendor." }
        require(expense.amount.isFinite() && expense.amount > 0.0) {
            "Backup contains an invalid amount."
        }
        require(runCatching { LocalDate.parse(expense.date) }.isSuccess) {
            "Backup contains an invalid date."
        }
        require(expense.taxAmount == null ||
            (expense.taxAmount.isFinite() && expense.taxAmount >= 0.0 && expense.taxAmount <= expense.amount)) {
            "Backup contains an invalid tax amount."
        }
    }

    private fun validateRestoredSale(sale: Sale) {
        require(sale.id.isNotBlank()) { "Backup contains a sale without an ID." }
        require(sale.customer.isNotBlank() && sale.customer.length <= 500) {
            "Backup contains an invalid customer."
        }
        require(sale.amount.isFinite() && sale.amount > 0.0) {
            "Backup contains an invalid sale amount."
        }
        require(runCatching { LocalDate.parse(sale.date) }.isSuccess) {
            "Backup contains an invalid sale date."
        }
        require(sale.quantity in 1..1_000_000) { "Backup contains an invalid quantity." }
        require(sale.taxAmount == null ||
            (sale.taxAmount.isFinite() && sale.taxAmount >= 0.0 && sale.taxAmount <= sale.amount)) {
            "Backup contains an invalid sale tax amount."
        }
        require(sale.discountAmount == null ||
            (sale.discountAmount.isFinite() && sale.discountAmount >= 0.0 &&
                sale.discountAmount <= sale.amount)) {
            "Backup contains an invalid discount amount."
        }
    }

    private fun ZipOutputStream.writeTextEntry(name: String, value: String) {
        putNextEntry(ZipEntry(name))
        write(value.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun InputStream.copyToWithLimit(
        output: java.io.OutputStream,
        remainingBytes: Long
    ): Long {
        require(remainingBytes > 0) { "Backup is larger than the supported limit." }
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            total += count
            require(total <= remainingBytes) { "Backup is larger than the supported limit." }
            output.write(buffer, 0, count)
        }
        return total
    }

    private fun InputStream.readTextWithLimit(maxBytes: Long): String {
        val output = ByteArrayOutputStream()
        copyToWithLimit(output, maxBytes)
        return output.toString(Charsets.UTF_8.name())
    }

    private fun isSafeEntryName(name: String): Boolean =
        name.isNotBlank() && !name.startsWith('/') && !name.contains("..") && !name.contains('\\')

    private fun safeIdentifier(value: String): String =
        value.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.take(80).ifBlank { "expense" }

    private fun safeExtension(name: String?): String = name
        ?.substringAfterLast('.', "")
        ?.lowercase()
        ?.filter(Char::isLetterOrDigit)
        ?.take(8)
        ?.ifBlank { null }
        ?: "bin"

    private fun safeFileName(name: String?): String {
        val safe = name.orEmpty()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(120)
        return safe.ifBlank { "attachment.bin" }
    }

    private const val PREFERENCES_NAME = "expense_backup_preferences"
    private const val KEY_AUTOMATIC_BACKUP_URI = "automatic_backup_uri"
    private const val FORMAT_NAME = "business-expense-tracker-backup"
    private const val SCHEMA_VERSION = 2
    private const val MIN_SUPPORTED_SCHEMA_VERSION = 1
    private const val MANIFEST_ENTRY = "manifest.json"
    private const val EXPENSES_ENTRY = "expenses.json"
    private const val SALES_ENTRY = "sales.json"
    private const val RESTORE_DIRECTORY = "expense_attachments"
    private const val MAX_EXPENSES = 50_000
    private const val MAX_SALES = 100_000
    private const val MAX_ZIP_ENTRIES = 55_000
    private const val MAX_MANIFEST_BYTES = 1_000_000L
    private const val MAX_EXPENSES_JSON_BYTES = 25_000_000L
    private const val MAX_SALES_JSON_BYTES = 35_000_000L
    private const val MAX_BACKUP_BYTES = 250_000_000L
    private const val MAX_BACKUP_FILE_BYTES = 275_000_000L
}
