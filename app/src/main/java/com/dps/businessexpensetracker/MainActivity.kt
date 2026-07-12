package com.dps.businessexpensetracker

import android.content.Context
import android.content.Intent
import android.app.DatePickerDialog
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.dps.businessexpensetracker.data.Expense
import com.dps.businessexpensetracker.data.ExpenseExport
import com.dps.businessexpensetracker.data.ExpenseExportFormat
import com.dps.businessexpensetracker.data.ExpenseExporter
import com.dps.businessexpensetracker.data.ExpenseCategory
import com.dps.businessexpensetracker.data.ExpenseBackupManager
import com.dps.businessexpensetracker.data.BackupRestoreResult
import com.dps.businessexpensetracker.data.ExpenseDraft
import com.dps.businessexpensetracker.data.ExpenseRepository
import com.dps.businessexpensetracker.data.ExpenseStatus
import com.dps.businessexpensetracker.data.PaymentMethod
import com.dps.businessexpensetracker.data.Sale
import com.dps.businessexpensetracker.data.SaleDraft
import com.dps.businessexpensetracker.data.SaleStatus
import com.dps.businessexpensetracker.data.SalesChannel
import com.dps.businessexpensetracker.data.SalesExporter
import com.dps.businessexpensetracker.data.ExtractionConfidence
import com.dps.businessexpensetracker.data.InvoiceExtractionResult
import com.dps.businessexpensetracker.data.InvoiceScanProcessor
import com.dps.businessexpensetracker.data.inrCurrencyFormatter
import com.dps.businessexpensetracker.data.isDuplicateInvoiceNumber
import com.dps.businessexpensetracker.data.expenseDraftFromState
import com.dps.businessexpensetracker.data.toStateString
import com.dps.businessexpensetracker.data.validateExpenseDraft
import com.dps.businessexpensetracker.data.saleDraftFromState
import com.dps.businessexpensetracker.data.validateSaleDraft
import com.dps.businessexpensetracker.ui.GuidedTourOverlay
import com.dps.businessexpensetracker.ui.GuidedTourPrefs
import com.dps.businessexpensetracker.ui.TourTargets
import com.dps.businessexpensetracker.ui.expenseTourSteps
import com.dps.businessexpensetracker.ui.tourTarget
import com.dps.businessexpensetracker.ui.theme.BusinessExpenseTrackerTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusinessExpenseTrackerTheme {
                BusinessExpenseTrackerApp()
            }
        }
    }
}

private val ExpenseDraftStateSaver = Saver<ExpenseDraft, String>(
    save = { it.toStateString() },
    restore = { expenseDraftFromState(it) }
)

private val SaleDraftStateSaver = Saver<SaleDraft, String>(
    save = { it.toStateString() },
    restore = { saleDraftFromState(it) }
)

@Composable
private fun BusinessExpenseTrackerApp() {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(context) }
    var expenses by remember { mutableStateOf(sortedExpenses(repository.loadExpenses())) }
    var sales by remember { mutableStateOf(sortedSales(repository.loadSales())) }
    var editingExpenseId by rememberSaveable { mutableStateOf<String?>(null) }
    var creatingDraftState by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingSaleId by rememberSaveable { mutableStateOf<String?>(null) }
    var creatingSaleDraftState by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSaleDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedLedgerName by rememberSaveable { mutableStateOf(LedgerView.EXPENSES.name) }
    var scanInProgress by remember { mutableStateOf(false) }
    var extractionReview by remember { mutableStateOf<InvoiceExtractionResult?>(null) }
    var automaticBackupUri by remember {
        mutableStateOf(ExpenseBackupManager.configuredBackupUri(context))
    }
    var pendingRestore by remember { mutableStateOf<BackupRestoreResult?>(null) }
    val editingExpense = expenses.firstOrNull { it.id == editingExpenseId }
    val creatingDraft = creatingDraftState?.let(::expenseDraftFromState)
    val pendingDelete = expenses.firstOrNull { it.id == pendingDeleteId }
    val editingSale = sales.firstOrNull { it.id == editingSaleId }
    val creatingSaleDraft = creatingSaleDraftState?.let(::saleDraftFromState)
    val pendingSaleDelete = sales.firstOrNull { it.id == pendingSaleDeleteId }

    fun persist(updated: List<Expense>) {
        val sorted = sortedExpenses(updated)
        repository.saveExpenses(sorted)
        expenses = sorted
        ExpenseBackupManager.writeAutomaticBackup(context, sorted, sales) {
            automaticBackupUri = null
            Toast.makeText(
                context,
                "Automatic backup stopped because the backup file is no longer writable.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun persistSales(updated: List<Sale>) {
        val sorted = sortedSales(updated)
        repository.saveSales(sorted)
        sales = sorted
        ExpenseBackupManager.writeAutomaticBackup(context, expenses, sorted) {
            automaticBackupUri = null
            Toast.makeText(
                context,
                "Automatic backup stopped because the backup file is no longer writable.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun writeBackup(uri: Uri, enableAutomaticUpdates: Boolean) {
        if (enableAutomaticUpdates) {
            ExpenseBackupManager.configureAutomaticBackup(context, uri)
            automaticBackupUri = uri
        }
        ExpenseBackupManager.writeBackup(
            context = context,
            targetUri = uri,
            expenses = expenses,
            sales = sales,
            onSuccess = { result ->
                val attachmentNote = if (result.missingAttachmentCount == 0) {
                    "${result.attachmentCount} attachments included"
                } else {
                    "${result.attachmentCount} attachments included; ${result.missingAttachmentCount} unavailable"
                }
                Toast.makeText(
                    context,
                    "Backup saved: ${result.expenseCount} expenses, ${result.salesCount} sales, " +
                        "$attachmentNote.",
                    Toast.LENGTH_LONG
                ).show()
            },
            onFailure = { error ->
                if (enableAutomaticUpdates) {
                    ExpenseBackupManager.disableAutomaticBackup(context)
                    automaticBackupUri = null
                }
                Toast.makeText(
                    context,
                    "Backup failed: ${error.message ?: "unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    val backupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val permissionPersisted = runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }.isSuccess
        writeBackup(uri, enableAutomaticUpdates = permissionPersisted)
        if (!permissionPersisted) {
            Toast.makeText(
                context,
                "Backup created, but this storage provider cannot keep it updated automatically.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        ExpenseBackupManager.restoreBackup(
            context = context,
            sourceUri = uri,
            onSuccess = { pendingRestore = it },
            onFailure = { error ->
                Toast.makeText(
                    context,
                    "Restore failed: ${error.message ?: "invalid backup file"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    fun applyLearnedDefaults(result: InvoiceExtractionResult): InvoiceExtractionResult {
        val vendor = result.draft.vendor.trim()
        val previous = expenses
            .filter { vendor.isNotBlank() && it.vendor.equals(vendor, ignoreCase = true) }
            .maxByOrNull { it.updatedAt }
        return if (previous == null) {
            result
        } else {
            result.copy(
                draft = result.draft.copy(
                    category = previous.category,
                    paymentMethod = previous.paymentMethod,
                    submittedBy = previous.submittedBy
                )
            )
        }
    }

    fun processInvoicePage(uri: Uri) {
        scanInProgress = true
        InvoiceScanProcessor.process(
            context = context,
            sourceUri = uri,
            onSuccess = { rawResult ->
                val result = applyLearnedDefaults(rawResult)
                extractionReview = result
                creatingDraftState = result.draft.toStateString()
                scanInProgress = false
            },
            onFailure = { error ->
                scanInProgress = false
                Toast.makeText(
                    context,
                    "Couldn't read this invoice: ${error.message ?: "unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(true)
                .setPageLimit(1)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .build()
        )
    }
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val scannedPage = GmsDocumentScanningResult
            .fromActivityResultIntent(activityResult.data)
            ?.pages
            ?.firstOrNull()
            ?.imageUri
        if (scannedPage == null) {
            Toast.makeText(context, "No scanned page was returned.", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        processInvoicePage(scannedPage)
    }
    val invoiceImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let(::processInvoicePage)
    }

    fun startInvoiceScan() {
        val activity = context as? Activity ?: return
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Scanner unavailable. Try importing an invoice image instead.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    when {
        scanInProgress -> ScanningInvoiceScreen()

        creatingDraft != null || editingExpense != null -> {
            val initialDraft = editingExpense?.let(ExpenseDraft::fromExpense) ?: creatingDraft!!
            ExpenseEditorScreen(
                initialDraft = initialDraft,
                existingInvoiceNumbers = expenses
                    .filterNot { it.id == editingExpense?.id }
                    .map { it.invoiceNumber },
                extractionReview = extractionReview,
                onCancel = {
                    if (editingExpense == null) {
                        releaseAttachmentPermission(context, creatingDraft?.attachmentUri)
                    }
                    creatingDraftState = null
                    editingExpenseId = null
                    extractionReview = null
                },
                onSave = { savedExpense ->
                    val withoutPrevious = expenses.filterNot { it.id == savedExpense.id }
                    persist(withoutPrevious + savedExpense)
                    editingExpense?.attachmentUri
                        ?.takeIf { it != savedExpense.attachmentUri }
                        ?.let { releaseAttachmentPermission(context, it) }
                    creatingDraft?.attachmentUri
                        ?.takeIf { it != savedExpense.attachmentUri }
                        ?.let { releaseAttachmentPermission(context, it) }
                    creatingDraftState = null
                    editingExpenseId = null
                    extractionReview = null
                }
            )
        }

        creatingSaleDraft != null || editingSale != null -> {
            SaleEditorScreen(
                initialDraft = editingSale?.let(SaleDraft::fromSale) ?: creatingSaleDraft!!,
                existingReferences = sales
                    .filterNot { it.id == editingSale?.id }
                    .map { it.reference },
                onCancel = {
                    creatingSaleDraftState = null
                    editingSaleId = null
                },
                onSave = { savedSale ->
                    persistSales(sales.filterNot { it.id == savedSale.id } + savedSale)
                    creatingSaleDraftState = null
                    editingSaleId = null
                }
            )
        }

        else -> {
            ExpenseHomeScreen(
                expenses = expenses,
                sales = sales,
                selectedLedgerName = selectedLedgerName,
                onLedgerSelected = { selectedLedgerName = it.name },
                onAddExpense = {
                    extractionReview = null
                    creatingDraftState = ExpenseDraft().toStateString()
                },
                onScanInvoice = ::startInvoiceScan,
                onImportInvoice = { invoiceImportLauncher.launch(arrayOf("image/*")) },
                automaticBackupEnabled = automaticBackupUri != null,
                onChooseBackupFile = {
                    val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    backupFileLauncher.launch("business-data-$date.betbackup.zip")
                },
                onBackupNow = {
                    automaticBackupUri?.let { writeBackup(it, enableAutomaticUpdates = true) }
                },
                onRestoreBackup = {
                    restoreFileLauncher.launch(
                        arrayOf("application/zip", "application/octet-stream")
                    )
                },
                onDisableAutomaticBackup = {
                    ExpenseBackupManager.disableAutomaticBackup(context)
                    automaticBackupUri = null
                    Toast.makeText(context, "Automatic backup turned off.", Toast.LENGTH_SHORT)
                        .show()
                },
                onEditExpense = { editingExpenseId = it.id },
                onDuplicateExpense = { expense ->
                    extractionReview = null
                    creatingDraftState = ExpenseDraft.fromExpense(expense).copy(
                        id = null,
                        date = LocalDate.now().toString(),
                        status = ExpenseStatus.DRAFT,
                        invoiceNumber = "",
                        attachmentUri = null,
                        attachmentName = null
                    ).toStateString()
                },
                onStatusChange = { expense, status ->
                    persist(
                        expenses.map {
                            if (it.id == expense.id) {
                                it.copy(status = status, updatedAt = System.currentTimeMillis())
                            } else {
                                it
                            }
                        }
                    )
                },
                onDeleteExpense = { pendingDeleteId = it.id },
                onOpenAttachment = { openAttachment(context, it.attachmentUri) },
                onAddSale = { creatingSaleDraftState = SaleDraft().toStateString() },
                onEditSale = { editingSaleId = it.id },
                onDuplicateSale = { sale ->
                    creatingSaleDraftState = SaleDraft.fromSale(sale).copy(
                        id = null,
                        date = LocalDate.now().toString(),
                        status = SaleStatus.RECEIVED,
                        reference = ""
                    ).toStateString()
                },
                onSaleStatusChange = { sale, status ->
                    persistSales(
                        sales.map {
                            if (it.id == sale.id) {
                                it.copy(status = status, updatedAt = System.currentTimeMillis())
                            } else {
                                it
                            }
                        }
                    )
                },
                onDeleteSale = { pendingSaleDeleteId = it.id }
            )
        }
    }

    pendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete expense") },
            text = { Text("Delete ${expense.vendor} from the expense register?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        releaseAttachmentPermission(context, expense.attachmentUri)
                        persist(expenses.filterNot { it.id == expense.id })
                        pendingDeleteId = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingSaleDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { pendingSaleDeleteId = null },
            title = { Text("Delete sale") },
            text = { Text("Delete ${sale.customer} from the sales register?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        persistSales(sales.filterNot { it.id == sale.id })
                        pendingSaleDeleteId = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingSaleDeleteId = null }) { Text("Cancel") }
            }
        )
    }

    pendingRestore?.let { restore ->
        AlertDialog(
            onDismissRequest = {
                ExpenseBackupManager.discardRestore(restore)
                pendingRestore = null
            },
            title = { Text("Restore business data?") },
            text = {
                Text(
                    "The backup contains ${restore.expenses.size} expenses, " +
                        "${restore.sales.size} sales and " +
                        "${restore.attachmentCount} attachments. Merge keeps current records; " +
                        "Replace removes current records first."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val restoredById = restore.expenses.associateBy { it.id }
                        expenses.forEach { existing ->
                            restoredById[existing.id]
                                ?.takeIf { it.attachmentUri != existing.attachmentUri }
                                ?.let { releaseAttachmentPermission(context, existing.attachmentUri) }
                        }
                        val merged = expenses.associateBy { it.id }.toMutableMap()
                        restore.expenses.forEach { merged[it.id] = it }
                        val mergedSales = sales.associateBy { it.id }.toMutableMap()
                        restore.sales.forEach { mergedSales[it.id] = it }
                        val restoredExpenses = sortedExpenses(merged.values.toList())
                        val restoredSales = sortedSales(mergedSales.values.toList())
                        repository.saveExpenses(restoredExpenses)
                        repository.saveSales(restoredSales)
                        expenses = restoredExpenses
                        sales = restoredSales
                        ExpenseBackupManager.writeAutomaticBackup(
                            context,
                            restoredExpenses,
                            restoredSales
                        )
                        pendingRestore = null
                        Toast.makeText(context, "Backup merged successfully.", Toast.LENGTH_LONG)
                            .show()
                    }
                ) {
                    Text("Merge")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            expenses.forEach {
                                releaseAttachmentPermission(context, it.attachmentUri)
                            }
                            val restoredExpenses = sortedExpenses(restore.expenses)
                            val restoredSales = sortedSales(restore.sales)
                            repository.saveExpenses(restoredExpenses)
                            repository.saveSales(restoredSales)
                            expenses = restoredExpenses
                            sales = restoredSales
                            ExpenseBackupManager.writeAutomaticBackup(
                                context,
                                restoredExpenses,
                                restoredSales
                            )
                            pendingRestore = null
                            Toast.makeText(
                                context,
                                "Backup restored successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    ) {
                        Text("Replace", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(
                        onClick = {
                            ExpenseBackupManager.discardRestore(restore)
                            pendingRestore = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseHomeScreen(
    expenses: List<Expense>,
    sales: List<Sale>,
    selectedLedgerName: String,
    onLedgerSelected: (LedgerView) -> Unit,
    onAddExpense: () -> Unit,
    onScanInvoice: () -> Unit,
    onImportInvoice: () -> Unit,
    automaticBackupEnabled: Boolean,
    onChooseBackupFile: () -> Unit,
    onBackupNow: () -> Unit,
    onRestoreBackup: () -> Unit,
    onDisableAutomaticBackup: () -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDuplicateExpense: (Expense) -> Unit,
    onStatusChange: (Expense, ExpenseStatus) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onOpenAttachment: (Expense) -> Unit,
    onAddSale: () -> Unit,
    onEditSale: (Sale) -> Unit,
    onDuplicateSale: (Sale) -> Unit,
    onSaleStatusChange: (Sale, SaleStatus) -> Unit,
    onDeleteSale: (Sale) -> Unit
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var statusFilterName by rememberSaveable { mutableStateOf("") }
    var categoryFilterName by rememberSaveable { mutableStateOf("") }
    var sortOptionName by rememberSaveable { mutableStateOf(ExpenseSortOption.NEWEST.name) }
    var saleQuery by rememberSaveable { mutableStateOf("") }
    var saleStatusFilterName by rememberSaveable { mutableStateOf("") }
    var saleChannelFilterName by rememberSaveable { mutableStateOf("") }
    var saleSortOptionName by rememberSaveable { mutableStateOf(SaleSortOption.NEWEST.name) }
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var dataMenuExpanded by remember { mutableStateOf(false) }
    var addSheetVisible by remember { mutableStateOf(false) }
    val tourSteps = remember { expenseTourSteps() }
    val tourTargetBounds = remember { mutableStateMapOf<String, Rect>() }
    // Deliberately not saveable: after recreation the preference is the source
    // of truth, so a finished tour stays dismissed and an unfinished one returns.
    var tourActive by remember { mutableStateOf(!GuidedTourPrefs.isTourSeen(context)) }
    var tourStepIndex by rememberSaveable { mutableStateOf(0) }

    fun finishTour() {
        tourActive = false
        tourStepIndex = 0
        GuidedTourPrefs.markTourSeen(context)
    }
    val currencyFormatter = remember { inrCurrencyFormatter() }
    val selectedLedger = LedgerView.entries.firstOrNull { it.name == selectedLedgerName }
        ?: LedgerView.EXPENSES
    val selectedStatus = ExpenseStatus.entries.firstOrNull { it.name == statusFilterName }
    val selectedCategory = ExpenseCategory.entries.firstOrNull { it.name == categoryFilterName }
    val selectedSort = ExpenseSortOption.entries
        .firstOrNull { it.name == sortOptionName } ?: ExpenseSortOption.NEWEST
    val hasActiveFilters = query.isNotBlank() || selectedStatus != null || selectedCategory != null
    val filteredExpenses = remember(
        expenses,
        query,
        statusFilterName,
        categoryFilterName,
        sortOptionName
    ) {
        expenses.filter { expense ->
            val searchText = listOf(
                expense.vendor,
                expense.invoiceNumber,
                expense.supplierGstin,
                expense.submittedBy,
                expense.notes,
                expense.category.label,
                expense.status.label
            ).joinToString(" ").lowercase(Locale.ROOT)
            val matchesSearch = query.isBlank() ||
                searchText.contains(query.trim().lowercase(Locale.ROOT))
            val matchesStatus = selectedStatus == null || expense.status == selectedStatus
            val matchesCategory = selectedCategory == null || expense.category == selectedCategory
            matchesSearch && matchesStatus && matchesCategory
        }.sortedWith(selectedSort.comparator)
    }
    val selectedSaleStatus = SaleStatus.entries.firstOrNull { it.name == saleStatusFilterName }
    val selectedSaleChannel = SalesChannel.entries.firstOrNull { it.name == saleChannelFilterName }
    val selectedSaleSort = SaleSortOption.entries.firstOrNull { it.name == saleSortOptionName }
        ?: SaleSortOption.NEWEST
    val hasActiveSaleFilters = saleQuery.isNotBlank() || selectedSaleStatus != null ||
        selectedSaleChannel != null
    val filteredSales = remember(
        sales,
        saleQuery,
        saleStatusFilterName,
        saleChannelFilterName,
        saleSortOptionName
    ) {
        sales.filter { sale ->
            val searchText = listOf(
                sale.customer,
                sale.reference,
                sale.soldBy,
                sale.notes,
                sale.channel.label,
                sale.paymentMethod.label,
                sale.status.label
            ).joinToString(" ").lowercase(Locale.ROOT)
            val matchesSearch = saleQuery.isBlank() ||
                searchText.contains(saleQuery.trim().lowercase(Locale.ROOT))
            val matchesStatus = selectedSaleStatus == null || sale.status == selectedSaleStatus
            val matchesChannel = selectedSaleChannel == null || sale.channel == selectedSaleChannel
            matchesSearch && matchesStatus && matchesChannel
        }.sortedWith(selectedSaleSort.comparator)
    }
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ExpenseExportFormat.CSV.mimeType),
        onResult = { uri ->
            saveExportToUri(
                context,
                uri,
                if (selectedLedger == LedgerView.EXPENSES) {
                    ExpenseExporter.create(filteredExpenses, ExpenseExportFormat.CSV)
                } else {
                    SalesExporter.create(filteredSales, ExpenseExportFormat.CSV)
                }
            )
        }
    )
    val htmlExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ExpenseExportFormat.HTML.mimeType),
        onResult = { uri ->
            saveExportToUri(
                context,
                uri,
                if (selectedLedger == LedgerView.EXPENSES) {
                    ExpenseExporter.create(filteredExpenses, ExpenseExportFormat.HTML)
                } else {
                    SalesExporter.create(filteredSales, ExpenseExportFormat.HTML)
                }
            )
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Business Tracker",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        Box(modifier = Modifier.tourTarget(tourTargetBounds, TourTargets.EXPORT)) {
                            IconButton(
                                onClick = { exportMenuExpanded = true },
                                enabled = if (selectedLedger == LedgerView.EXPENSES) {
                                    filteredExpenses.isNotEmpty()
                                } else {
                                    filteredSales.isNotEmpty()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = "Export records"
                                )
                            }
                            DropdownMenu(
                                expanded = exportMenuExpanded,
                                onDismissRequest = { exportMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export current view as CSV") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        val export = if (selectedLedger == LedgerView.EXPENSES) {
                                            ExpenseExporter.create(filteredExpenses, ExpenseExportFormat.CSV)
                                        } else {
                                            SalesExporter.create(filteredSales, ExpenseExportFormat.CSV)
                                        }
                                        csvExportLauncher.launch(export.fileName)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export current view as HTML") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        val export = if (selectedLedger == LedgerView.EXPENSES) {
                                            ExpenseExporter.create(filteredExpenses, ExpenseExportFormat.HTML)
                                        } else {
                                            SalesExporter.create(filteredSales, ExpenseExportFormat.HTML)
                                        }
                                        htmlExportLauncher.launch(export.fileName)
                                    }
                                )
                            }
                        }
                        Box(modifier = Modifier.tourTarget(tourTargetBounds, TourTargets.BACKUP)) {
                            IconButton(onClick = { dataMenuExpanded = true }) {
                                Icon(Icons.Outlined.MoreVert, contentDescription = "Backup and restore")
                            }
                            DropdownMenu(
                                expanded = dataMenuExpanded,
                                onDismissRequest = { dataMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Choose backup file") },
                                    leadingIcon = { Icon(Icons.Outlined.Backup, contentDescription = null) },
                                    onClick = {
                                        dataMenuExpanded = false
                                        onChooseBackupFile()
                                    }
                                )
                                if (automaticBackupEnabled) {
                                    DropdownMenuItem(
                                        text = { Text("Update backup now") },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                                        },
                                        onClick = {
                                            dataMenuExpanded = false
                                            onBackupNow()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Restore from backup") },
                                    leadingIcon = { Icon(Icons.Outlined.Restore, contentDescription = null) },
                                    onClick = {
                                        dataMenuExpanded = false
                                        onRestoreBackup()
                                    }
                                )
                                if (automaticBackupEnabled) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Turn off automatic backup") },
                                        onClick = {
                                            dataMenuExpanded = false
                                            onDisableAutomaticBackup()
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Replay app tour") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.HelpOutline,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        dataMenuExpanded = false
                                        tourStepIndex = 0
                                        tourActive = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (selectedLedger == LedgerView.EXPENSES) addSheetVisible = true
                        else onAddSale()
                    },
                    modifier = Modifier.tourTarget(tourTargetBounds, TourTargets.ADD_EXPENSE)
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = if (selectedLedger == LedgerView.EXPENSES) {
                            "Add expense"
                        } else {
                            "Add sale"
                        }
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.tourTarget(tourTargetBounds, TourTargets.DASHBOARD)
                    ) {
                        DashboardSummary(
                            expenses = expenses,
                            sales = sales,
                            currencyFormatter = currencyFormatter
                        )
                    }
                }

                item {
                    LedgerSelector(
                        selected = selectedLedger,
                        onSelected = onLedgerSelected,
                        expenseCount = expenses.size,
                        saleCount = sales.size
                    )
                }

                if (selectedLedger == LedgerView.EXPENSES) {
                item {
                    Box(
                        modifier = Modifier.tourTarget(
                            tourTargetBounds,
                            TourTargets.SEARCH_FILTERS
                        )
                    ) {
                        SearchAndFilters(
                            query = query,
                            onQueryChange = { query = it },
                            statusFilterName = statusFilterName,
                            onStatusFilterChange = { statusFilterName = it },
                            categoryFilterName = categoryFilterName,
                            onCategoryFilterChange = { categoryFilterName = it },
                            sortOptionName = sortOptionName,
                            onSortOptionChange = { sortOptionName = it },
                            hasActiveFilters = hasActiveFilters,
                            onClearFilters = {
                                query = ""
                                statusFilterName = ""
                                categoryFilterName = ""
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = recordCountLabel(filteredExpenses.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (filteredExpenses.isEmpty()) {
                    item {
                        EmptyExpenseState(
                            hasExpenses = expenses.isNotEmpty(),
                            onAddExpense = { addSheetVisible = true },
                            onClearFilters = {
                                query = ""
                                statusFilterName = ""
                                categoryFilterName = ""
                            }
                        )
                    }
                } else {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseListItem(
                            expense = expense,
                            currencyFormatter = currencyFormatter,
                            onEdit = { onEditExpense(expense) },
                            onDuplicate = { onDuplicateExpense(expense) },
                            onStatusChange = { onStatusChange(expense, it) },
                            onDelete = { onDeleteExpense(expense) },
                            onOpenAttachment = { onOpenAttachment(expense) }
                        )
                    }
                }
                } else {
                    item {
                        SalesSearchAndFilters(
                            query = saleQuery,
                            onQueryChange = { saleQuery = it },
                            statusFilterName = saleStatusFilterName,
                            onStatusFilterChange = { saleStatusFilterName = it },
                            channelFilterName = saleChannelFilterName,
                            onChannelFilterChange = { saleChannelFilterName = it },
                            sortOptionName = saleSortOptionName,
                            onSortOptionChange = { saleSortOptionName = it },
                            hasActiveFilters = hasActiveSaleFilters,
                            onClearFilters = {
                                saleQuery = ""
                                saleStatusFilterName = ""
                                saleChannelFilterName = ""
                            }
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Daily sales", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(recordCountLabel(filteredSales.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (filteredSales.isEmpty()) {
                        item {
                            EmptySalesState(
                                hasSales = sales.isNotEmpty(),
                                onAddSale = onAddSale,
                                onClearFilters = {
                                    saleQuery = ""
                                    saleStatusFilterName = ""
                                    saleChannelFilterName = ""
                                }
                            )
                        }
                    } else {
                        items(filteredSales, key = { "sale-${it.id}" }) { sale ->
                            SaleListItem(
                                sale = sale,
                                currencyFormatter = currencyFormatter,
                                onEdit = { onEditSale(sale) },
                                onDuplicate = { onDuplicateSale(sale) },
                                onStatusChange = { onSaleStatusChange(sale, it) },
                                onDelete = { onDeleteSale(sale) }
                            )
                        }
                    }
                }
            }
        }

        if (tourActive) {
            GuidedTourOverlay(
                steps = tourSteps,
                stepIndex = tourStepIndex,
                targetBounds = tourTargetBounds,
                onStepChange = { tourStepIndex = it },
                onFinish = ::finishTour
            )
        }
    }

    if (addSheetVisible) {
        ModalBottomSheet(onDismissRequest = { addSheetVisible = false }) {
            AddExpenseOptions(
                onScanInvoice = {
                    addSheetVisible = false
                    onScanInvoice()
                },
                onImportInvoice = {
                    addSheetVisible = false
                    onImportInvoice()
                },
                onManualEntry = {
                    addSheetVisible = false
                    onAddExpense()
                }
            )
        }
    }
}

@Composable
private fun ScanningInvoiceScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Reading invoice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Extracting fields, checking totals and looking for QR data…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddExpenseOptions(
    onScanInvoice: () -> Unit,
    onImportInvoice: () -> Unit,
    onManualEntry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add an expense",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Scan a bill to prefill the details, or enter them yourself.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onScanInvoice),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Scan invoice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Camera or gallery · processed on device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onImportInvoice)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Image, contentDescription = null)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Import invoice image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Use an existing photo from this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onManualEntry)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Keyboard, contentDescription = null)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Enter manually",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Use the standard expense form",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSummary(
    expenses: List<Expense>,
    sales: List<Sale>,
    currencyFormatter: NumberFormat
) {
    val summary = remember(expenses, sales) { BusinessSummary.from(expenses, sales) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Operating cashflow",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currencyFormatter.format(summary.operatingCashflow),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "This month cashflow",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currencyFormatter.format(summary.thisMonthCashflow),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryPill("Today sales", currencyFormatter.format(summary.todaySales))
            SummaryPill("Received", currencyFormatter.format(summary.receivedSales))
            SummaryPill("Paid expenses", currencyFormatter.format(summary.paidSpend))
            SummaryPill("Pending sales", currencyFormatter.format(summary.pendingSales))
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    statusFilterName: String,
    onStatusFilterChange: (String) -> Unit,
    categoryFilterName: String,
    onCategoryFilterChange: (String) -> Unit,
    sortOptionName: String,
    onSortOptionChange: (String) -> Unit,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("expense_search"),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                    }
                }
            },
            label = { Text("Search vendor, invoice, notes") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterMenuButton(
                label = "Status",
                selectedText = ExpenseStatus.entries
                    .firstOrNull { it.name == statusFilterName }
                    ?.label ?: "All",
                options = listOf("" to "All") + ExpenseStatus.entries.map { it.name to it.label },
                onSelected = onStatusFilterChange
            )
            FilterMenuButton(
                label = "Category",
                selectedText = ExpenseCategory.entries
                    .firstOrNull { it.name == categoryFilterName }
                    ?.label ?: "All",
                options = listOf("" to "All") + ExpenseCategory.entries.map { it.name to it.label },
                onSelected = onCategoryFilterChange
            )
            FilterMenuButton(
                label = "Sort",
                selectedText = ExpenseSortOption.entries
                    .firstOrNull { it.name == sortOptionName }
                    ?.label ?: ExpenseSortOption.NEWEST.label,
                options = ExpenseSortOption.entries.map { it.name to it.label },
                onSelected = onSortOptionChange,
                showSortIcon = true
            )
            if (hasActiveFilters) {
                TextButton(onClick = onClearFilters) { Text("Clear") }
            }
        }
    }
}

@Composable
private fun FilterMenuButton(
    label: String,
    selectedText: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    showSortIcon: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("filter_button_$label")
        ) {
            if (showSortIcon) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text("$label: $selectedText", maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    modifier = Modifier.testTag("filter_option_${label}_$value"),
                    text = { Text(text) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LedgerSelector(
    selected: LedgerView,
    onSelected: (LedgerView) -> Unit,
    expenseCount: Int,
    saleCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            Triple(LedgerView.EXPENSES, "Expenses", expenseCount),
            Triple(LedgerView.SALES, "Sales", saleCount)
        ).forEach { (view, label, count) ->
            val isSelected = selected == view
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(view) }
                    .testTag("ledger_${view.name.lowercase()}"),
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (view == LedgerView.EXPENSES) {
                            Icons.AutoMirrored.Outlined.ReceiptLong
                        } else {
                            Icons.Outlined.ShoppingCart
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text("$label  $count", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SalesSearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    statusFilterName: String,
    onStatusFilterChange: (String) -> Unit,
    channelFilterName: String,
    onChannelFilterChange: (String) -> Unit,
    sortOptionName: String,
    onSortOptionChange: (String) -> Unit,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().testTag("sale_search"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear sales search")
                    }
                }
            },
            label = { Text("Search customer, reference") }
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.FilterList, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterMenuButton(
                label = "Sale status",
                selectedText = SaleStatus.entries.firstOrNull { it.name == statusFilterName }
                    ?.label ?: "All",
                options = listOf("" to "All") + SaleStatus.entries.map { it.name to it.label },
                onSelected = onStatusFilterChange
            )
            FilterMenuButton(
                label = "Channel",
                selectedText = SalesChannel.entries.firstOrNull { it.name == channelFilterName }
                    ?.label ?: "All",
                options = listOf("" to "All") + SalesChannel.entries.map { it.name to it.label },
                onSelected = onChannelFilterChange
            )
            FilterMenuButton(
                label = "Sale sort",
                selectedText = SaleSortOption.entries.firstOrNull { it.name == sortOptionName }
                    ?.label ?: SaleSortOption.NEWEST.label,
                options = SaleSortOption.entries.map { it.name to it.label },
                onSelected = onSortOptionChange,
                showSortIcon = true
            )
            if (hasActiveFilters) TextButton(onClick = onClearFilters) { Text("Clear") }
        }
    }
}

@Composable
private fun EmptySalesState(
    hasSales: Boolean,
    onAddSale: () -> Unit,
    onClearFilters: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.ShoppingCart, contentDescription = null,
                modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Text(
                if (hasSales) "No matching sales" else "No sales yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (hasSales) "Try another search or clear the sales filters."
                else "Record today's first sale to start tracking revenue and cashflow.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasSales) OutlinedButton(onClick = onClearFilters) { Text("Clear filters") }
            else Button(onClick = onAddSale) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add sale")
            }
        }
    }
}

@Composable
private fun EmptyExpenseState(
    hasExpenses: Boolean,
    onAddExpense: () -> Unit,
    onClearFilters: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (hasExpenses) "No matching expenses" else "No expenses yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (hasExpenses) {
                    "Try a different search or clear your filters."
                } else {
                    "Add your first bill or receipt to start tracking spend."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasExpenses) {
                OutlinedButton(onClick = onClearFilters) { Text("Clear filters") }
            } else {
                Button(onClick = onAddExpense) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add expense")
                }
            }
        }
    }
}

@Composable
private fun ExpenseListItem(
    expense: Expense,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onStatusChange: (ExpenseStatus) -> Unit,
    onDelete: () -> Unit,
    onOpenAttachment: () -> Unit
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    val nextStatus = expense.status.nextAction()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.vendor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${expense.category.label} • ${formatExpenseDate(expense.date)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(expense.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    StatusBadge(status = expense.status)
                }
            }

            if (expense.invoiceNumber.isNotBlank() || expense.submittedBy.isNotBlank()) {
                Text(
                    text = buildList {
                        if (expense.invoiceNumber.isNotBlank()) add("Invoice ${expense.invoiceNumber}")
                        if (expense.submittedBy.isNotBlank()) add("By ${expense.submittedBy}")
                    }.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (expense.attachmentUri != null) {
                    TextButton(onClick = onOpenAttachment) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = expense.attachmentName ?: "Attachment",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = "No attachment",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    nextStatus?.let { (label, status) ->
                        TextButton(onClick = { onStatusChange(status) }) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(label)
                        }
                    }
                    Box {
                        IconButton(onClick = { actionsExpanded = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "Expense actions")
                        }
                        DropdownMenu(
                            expanded = actionsExpanded,
                            onDismissRequest = { actionsExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                                onClick = {
                                    actionsExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onDuplicate()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleListItem(
    sale: Sale,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onStatusChange: (SaleStatus) -> Unit,
    onDelete: () -> Unit
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(sale.customer, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(
                        "${sale.channel.label} • ${formatExpenseDate(sale.date)} • Qty ${sale.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(currencyFormatter.format(sale.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    SaleStatusBadge(sale.status)
                }
            }
            Text(
                buildList {
                    if (sale.reference.isNotBlank()) add("Ref ${sale.reference}")
                    add(sale.paymentMethod.label)
                    if (sale.soldBy.isNotBlank()) add("By ${sale.soldBy}")
                }.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sale.status == SaleStatus.PENDING) {
                    TextButton(onClick = { onStatusChange(SaleStatus.RECEIVED) }) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark received")
                    }
                }
                Box {
                    IconButton(onClick = { actionsExpanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Sale actions")
                    }
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = { actionsExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                            },
                            onClick = { actionsExpanded = false; onDuplicate() }
                        )
                        if (sale.status != SaleStatus.REFUNDED) {
                            DropdownMenuItem(
                                text = { Text("Mark refunded") },
                                onClick = {
                                    actionsExpanded = false
                                    onStatusChange(SaleStatus.REFUNDED)
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = { actionsExpanded = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleStatusBadge(status: SaleStatus) {
    val colors = when (status) {
        SaleStatus.RECEIVED -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
        SaleStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer
        SaleStatus.REFUNDED -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(shape = CircleShape, color = colors.first, contentColor = colors.second) {
        Text(status.label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusBadge(status: ExpenseStatus) {
    val (containerColor, contentColor) = when (status) {
        ExpenseStatus.DRAFT -> MaterialTheme.colorScheme.surfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
        ExpenseStatus.FOR_REVIEW -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer
        ExpenseStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        ExpenseStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
        ExpenseStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleEditorScreen(
    initialDraft: SaleDraft,
    existingReferences: List<String>,
    onCancel: () -> Unit,
    onSave: (Sale) -> Unit
) {
    val context = LocalContext.current
    var draft by rememberSaveable(initialDraft.id, stateSaver = SaleDraftStateSaver) {
        mutableStateOf(initialDraft)
    }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }
    val duplicateReference = isDuplicateInvoiceNumber(draft.reference, existingReferences)

    fun requestCancel() {
        if (draft != initialDraft) showDiscardConfirmation = true else onCancel()
    }

    fun validateAndSave() {
        val error = if (duplicateReference) {
            "This sale reference is already used by another sale."
        } else {
            validateSaleDraft(draft)
        }
        if (error == null) onSave(draft.toSale()) else validationMessage = error
    }

    BackHandler(onBack = ::requestCancel)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (initialDraft.id == null) "Add sale" else "Edit sale") },
                navigationIcon = {
                    IconButton(onClick = ::requestCancel) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = ::validateAndSave) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save sale")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Record daily revenue", fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            "Use the final invoiced total. Tax and discount are optional details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = draft.customer,
                    onValueChange = { draft = draft.copy(customer = it); validationMessage = null },
                    modifier = Modifier.fillMaxWidth().testTag("sale_customer_field"),
                    singleLine = true,
                    label = { Text("Customer or sale label") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.amount,
                    onValueChange = { draft = draft.copy(amount = it); validationMessage = null },
                    modifier = Modifier.fillMaxWidth().testTag("sale_amount_field"),
                    singleLine = true,
                    label = { Text("Total sale (INR)") },
                    leadingIcon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnumDropdownField(
                        label = "Channel",
                        selected = draft.channel,
                        options = SalesChannel.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(channel = it) },
                        modifier = Modifier.weight(1f)
                    )
                    EnumDropdownField(
                        label = "Status",
                        selected = draft.status,
                        options = SaleStatus.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(status = it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            showDatePicker(context, draft.date) { draft = draft.copy(date = it) }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Date", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(draft.date, maxLines = 1)
                        }
                        Icon(Icons.Outlined.CalendarToday, contentDescription = "Choose sale date")
                    }
                    EnumDropdownField(
                        label = "Payment",
                        selected = draft.paymentMethod,
                        options = PaymentMethod.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(paymentMethod = it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.quantity,
                        onValueChange = { draft = draft.copy(quantity = it); validationMessage = null },
                        modifier = Modifier.weight(1f).testTag("sale_quantity_field"),
                        singleLine = true,
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = draft.soldBy,
                        onValueChange = { draft = draft.copy(soldBy = it); validationMessage = null },
                        modifier = Modifier.weight(1f).testTag("sale_sold_by_field"),
                        singleLine = true,
                        label = { Text("Sold by") }
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = draft.reference,
                    onValueChange = { draft = draft.copy(reference = it); validationMessage = null },
                    modifier = Modifier.fillMaxWidth().testTag("sale_reference_field"),
                    singleLine = true,
                    isError = duplicateReference,
                    supportingText = {
                        if (duplicateReference) Text("Already used by another sale")
                    },
                    label = { Text("Invoice / order reference (optional)") }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.taxAmount,
                        onValueChange = { draft = draft.copy(taxAmount = it); validationMessage = null },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Tax (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = draft.discountAmount,
                        onValueChange = {
                            draft = draft.copy(discountAmount = it)
                            validationMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Discount (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = { draft = draft.copy(notes = it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Notes (optional)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) }
                )
            }
            validationMessage?.let { message ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(message, modifier = Modifier.fillMaxWidth().padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            item {
                Button(onClick = ::validateAndSave, modifier = Modifier.fillMaxWidth()) {
                    Text(if (initialDraft.id == null) "Add sale" else "Save changes")
                }
            }
        }
    }

    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmation = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved sale changes will be lost.") },
            confirmButton = {
                TextButton(onClick = onCancel) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmation = false }) { Text("Keep editing") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseEditorScreen(
    initialDraft: ExpenseDraft,
    existingInvoiceNumbers: List<String>,
    extractionReview: InvoiceExtractionResult? = null,
    onCancel: () -> Unit,
    onSave: (Expense) -> Unit
) {
    val context = LocalContext.current
    var draft by rememberSaveable(initialDraft.id, stateSaver = ExpenseDraftStateSaver) {
        mutableStateOf(initialDraft)
    }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }
    val duplicateInvoice = isDuplicateInvoiceNumber(draft.invoiceNumber, existingInvoiceNumbers)
    val isScannedDraft = initialDraft.id == null &&
        initialDraft.attachmentUri?.toUri()?.authority == "${context.packageName}.fileprovider"

    fun requestCancel() {
        if (draft != initialDraft) {
            showDiscardConfirmation = true
        } else {
            onCancel()
        }
    }

    fun releaseTransientAttachment() {
        draft.attachmentUri
            ?.takeIf { it != initialDraft.attachmentUri }
            ?.let { releaseAttachmentPermission(context, it) }
    }

    fun validateAndSave() {
        val error = when {
            duplicateInvoice -> "This invoice number is already used by another expense."
            else -> validateExpenseDraft(draft)
        }
        if (error == null) onSave(draft.toExpense()) else validationMessage = error
    }
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                draft.attachmentUri
                    ?.takeIf { it != initialDraft.attachmentUri && it != uri.toString() }
                    ?.let { releaseAttachmentPermission(context, it) }
                val permissionTaken = runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }.isSuccess
                if (!permissionTaken) {
                    Toast.makeText(
                        context,
                        "This file may need to be selected again after a device restart.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                draft = draft.copy(
                    attachmentUri = uri.toString(),
                    attachmentName = displayNameForUri(context, uri) ?: "Attached bill"
                )
            }
        }
    )

    BackHandler(onBack = ::requestCancel)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (initialDraft.id == null) "Add expense" else "Edit expense")
                },
                navigationIcon = {
                    IconButton(onClick = ::requestCancel) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = ::validateAndSave
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save expense")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (extractionReview != null || isScannedDraft) {
                item {
                    ExtractionReviewCard(extractionReview)
                }
            }
            item {
                OutlinedTextField(
                    value = draft.vendor,
                    onValueChange = {
                        draft = draft.copy(vendor = it)
                        validationMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vendor_field"),
                    singleLine = true,
                    label = { Text("Vendor") },
                    leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.amount,
                    onValueChange = {
                        draft = draft.copy(amount = it)
                        validationMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_field"),
                    singleLine = true,
                    label = { Text("Amount (INR)") },
                    leadingIcon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnumDropdownField(
                        label = "Category",
                        selected = draft.category,
                        options = ExpenseCategory.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(category = it) },
                        modifier = Modifier.weight(1f)
                    )
                    EnumDropdownField(
                        label = "Status",
                        selected = draft.status,
                        options = ExpenseStatus.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(status = it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.date,
                        onValueChange = {},
                        modifier = Modifier
                            .weight(1f)
                            .testTag("date_field"),
                        singleLine = true,
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showDatePicker(context, draft.date) {
                                        draft = draft.copy(date = it)
                                        validationMessage = null
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.CalendarToday,
                                    contentDescription = "Choose date"
                                )
                            }
                        }
                    )
                    EnumDropdownField(
                        label = "Payment",
                        selected = draft.paymentMethod,
                        options = PaymentMethod.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(paymentMethod = it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = draft.submittedBy,
                    onValueChange = {
                        draft = draft.copy(submittedBy = it)
                        validationMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submitted_by_field"),
                    singleLine = true,
                    label = { Text("Submitted by") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.invoiceNumber,
                    onValueChange = {
                        draft = draft.copy(invoiceNumber = it)
                        validationMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_field"),
                    singleLine = true,
                    isError = duplicateInvoice,
                    label = { Text("Invoice number") },
                    supportingText = {
                        if (duplicateInvoice) Text("Already used by another expense")
                    },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.supplierGstin,
                    onValueChange = {
                        draft = draft.copy(supplierGstin = it.uppercase().take(15))
                        validationMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Supplier GSTIN") },
                    supportingText = { Text("Optional · 15 characters") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.taxAmount,
                    onValueChange = {
                        draft = draft.copy(taxAmount = it)
                        validationMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Tax amount (INR)") },
                    leadingIcon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                AttachmentField(
                    attachmentName = draft.attachmentName,
                    onOpen = draft.attachmentUri?.let { uri ->
                        { openAttachment(context, uri) }
                    },
                    onPick = { documentPicker.launch(arrayOf("application/pdf", "image/*")) },
                    onClear = {
                        draft.attachmentUri
                            ?.takeIf { it != initialDraft.attachmentUri }
                            ?.let { releaseAttachmentPermission(context, it) }
                        draft = draft.copy(attachmentUri = null, attachmentName = null)
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = { draft = draft.copy(notes = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .testTag("notes_field"),
                    label = { Text("Notes") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = ::requestCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel")
                    }
                    Button(
                        onClick = ::validateAndSave,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_expense")
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmation = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved expense details will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirmation = false
                        releaseTransientAttachment()
                        onCancel()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmation = false }) {
                    Text("Keep editing")
                }
            }
        )
    }
    validationMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { validationMessage = null },
            title = { Text("Check expense details") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { validationMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ExtractionReviewCard(result: InvoiceExtractionResult?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Review scanned details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Nothing is saved until you confirm.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (result == null) {
                Text(
                    text = "The extracted draft was restored. Check every field before saving.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else if (result.fields.isEmpty()) {
                Text(
                    text = "No fields were confidently extracted. Complete the form below.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                result.fields.forEach { field ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = field.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = field.value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        ConfidenceBadge(field.confidence)
                    }
                }
            }
            result?.warnings?.forEach { warning ->
                Text(
                    text = "• $warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (result?.qrCodeDetected == true) {
                Text(
                    text = "QR data detected and included in the checks.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: ExtractionConfidence) {
    val label = when (confidence) {
        ExtractionConfidence.HIGH -> "High"
        ExtractionConfidence.MEDIUM -> "Check"
        ExtractionConfidence.LOW -> "Low"
    }
    val color = when (confidence) {
        ExtractionConfidence.HIGH -> MaterialTheme.colorScheme.secondaryContainer
        ExtractionConfidence.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
        ExtractionConfidence.LOW -> MaterialTheme.colorScheme.errorContainer
    }
    Surface(shape = CircleShape, color = color) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AttachmentField(
    attachmentName: String?,
    onOpen: (() -> Unit)?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(if (onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Bill or invoice",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = attachmentName ?: "No file selected",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (onOpen != null) {
                        Text(
                            text = "Tap to view",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (attachmentName != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Outlined.Close, contentDescription = "Remove attachment")
                }
            }
            OutlinedButton(onClick = onPick) {
                Text(if (attachmentName == null) "Attach" else "Replace")
            }
        }
    }
}

@Composable
private fun <T : Enum<T>> EnumDropdownField(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = optionLabel(selected),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Outlined.ExpandMore, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class BusinessSummary(
    val operatingCashflow: Double,
    val thisMonthCashflow: Double,
    val todaySales: Double,
    val receivedSales: Double,
    val pendingSales: Double,
    val paidSpend: Double,
) {
    companion object {
        fun from(expenses: List<Expense>, sales: List<Sale>): BusinessSummary {
            val currentMonth = YearMonth.now()
            val paidExpenses = expenses.filter { it.status == ExpenseStatus.PAID }
            val receivedSales = sales.filter { it.status == SaleStatus.RECEIVED }
            val paidSpend = paidExpenses.sumOf { it.amount }
            val receivedTotal = receivedSales.sumOf { it.amount }
            val monthSpend = paidExpenses
                    .filter { parsedYearMonth(it.date) == currentMonth }
                    .sumOf { it.amount }
            val monthSales = receivedSales
                .filter { parsedYearMonth(it.date) == currentMonth }
                .sumOf { it.amount }
            return BusinessSummary(
                operatingCashflow = receivedTotal - paidSpend,
                thisMonthCashflow = monthSales - monthSpend,
                todaySales = receivedSales.filter { it.date == LocalDate.now().toString() }
                    .sumOf { it.amount },
                receivedSales = receivedTotal,
                pendingSales = sales.filter { it.status == SaleStatus.PENDING }
                    .sumOf { it.amount },
                paidSpend = paidSpend
            )
        }
    }
}

private enum class LedgerView { EXPENSES, SALES }

private enum class SaleSortOption(
    val label: String,
    val comparator: Comparator<Sale>
) {
    NEWEST(
        "Newest",
        compareByDescending<Sale> { parsedDate(it.date) ?: LocalDate.MIN }
            .thenByDescending { it.updatedAt }
    ),
    OLDEST(
        "Oldest",
        compareBy<Sale> { parsedDate(it.date) ?: LocalDate.MAX }
            .thenBy { it.updatedAt }
    ),
    AMOUNT_HIGH("Amount: high to low", compareByDescending { it.amount }),
    AMOUNT_LOW("Amount: low to high", compareBy { it.amount })
}

private enum class ExpenseSortOption(
    val label: String,
    val comparator: Comparator<Expense>
) {
    NEWEST(
        "Newest",
        compareByDescending<Expense> { parsedDate(it.date) ?: LocalDate.MIN }
            .thenByDescending { it.updatedAt }
    ),
    OLDEST(
        "Oldest",
        compareBy<Expense> { parsedDate(it.date) ?: LocalDate.MAX }
            .thenBy { it.updatedAt }
    ),
    AMOUNT_HIGH("Amount: high to low", compareByDescending { it.amount }),
    AMOUNT_LOW("Amount: low to high", compareBy { it.amount })
}

private fun ExpenseStatus.nextAction(): Pair<String, ExpenseStatus>? = when (this) {
    ExpenseStatus.DRAFT -> "Submit" to ExpenseStatus.FOR_REVIEW
    ExpenseStatus.FOR_REVIEW -> "Approve" to ExpenseStatus.APPROVED
    ExpenseStatus.APPROVED -> "Mark paid" to ExpenseStatus.PAID
    ExpenseStatus.PAID, ExpenseStatus.REJECTED -> null
}

private fun sortedExpenses(expenses: List<Expense>): List<Expense> =
    expenses.sortedWith(
        compareByDescending<Expense> { parsedDate(it.date) ?: LocalDate.MIN }
            .thenByDescending { it.updatedAt }
    )

private fun sortedSales(sales: List<Sale>): List<Sale> =
    sales.sortedWith(SaleSortOption.NEWEST.comparator)

private fun parsedDate(date: String): LocalDate? =
    runCatching { LocalDate.parse(date) }.getOrNull()

private fun parsedYearMonth(date: String): YearMonth? =
    parsedDate(date)?.let { YearMonth.from(it) }

private fun formatExpenseDate(date: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    return parsedDate(date)?.format(formatter) ?: date
}

private fun recordCountLabel(count: Int): String =
    "$count ${if (count == 1) "record" else "records"}"

private fun showDatePicker(
    context: Context,
    currentValue: String,
    onDateSelected: (String) -> Unit
) {
    val initialDate = parsedDate(currentValue) ?: LocalDate.now()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            onDateSelected(LocalDate.of(year, month + 1, day).toString())
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).show()
}

private fun displayNameForUri(context: Context, uri: Uri): String? =
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }

private fun openAttachment(context: Context, attachmentUri: String?) {
    if (attachmentUri == null) return
    val uri = attachmentUri.toUri()
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(
            context,
            "Couldn't open this attachment. Try attaching the file again.",
            Toast.LENGTH_LONG
        )
            .show()
    }
}

private fun releaseAttachmentPermission(context: Context, attachmentUri: String?) {
    if (attachmentUri == null) return
    val uri = attachmentUri.toUri()
    if (uri.authority == "${context.packageName}.fileprovider") {
        InvoiceScanProcessor.deleteManagedScan(context, uri)
        return
    }
    runCatching {
        context.contentResolver.releasePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

private fun saveExportToUri(context: Context, uri: Uri?, export: ExpenseExport) {
    if (uri == null) return
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(export.content.toByteArray(Charsets.UTF_8))
        } ?: error("Unable to open selected file.")
    }.onSuccess {
        Toast.makeText(context, "Exported ${export.fileName}", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "Could not export records.", Toast.LENGTH_SHORT).show()
    }
}
