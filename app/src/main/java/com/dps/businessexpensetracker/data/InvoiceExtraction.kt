package com.dps.businessexpensetracker.data

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import java.util.concurrent.Executors

enum class ExtractionConfidence { HIGH, MEDIUM, LOW }

data class ExtractedInvoiceField(
    val label: String,
    val value: String,
    val confidence: ExtractionConfidence
)

data class InvoiceExtractionResult(
    val draft: ExpenseDraft,
    val fields: List<ExtractedInvoiceField>,
    val warnings: List<String>,
    val qrCodeDetected: Boolean
)

data class OcrLine(
    val text: String,
    val top: Int,
    val bottom: Int
)

object InvoiceScanProcessor {
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun process(
        context: Context,
        sourceUri: Uri,
        onSuccess: (InvoiceExtractionResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val appContext = context.applicationContext
        ioExecutor.execute {
            runCatching {
                val managedUri = copyScanToAppStorage(appContext, sourceUri)
                managedUri to InputImage.fromFilePath(appContext, managedUri)
            }.onSuccess { (managedUri, image) ->
                runRecognition(appContext, managedUri, image, onSuccess, onFailure)
            }.onFailure { error ->
                mainHandler.post { onFailure(error) }
            }
        }
    }

    private fun runRecognition(
        context: Context,
        managedUri: Uri,
        image: InputImage,
        onSuccess: (InvoiceExtractionResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_PDF417
                )
                .build()
        )

        textRecognizer.process(image)
            .addOnSuccessListener { recognizedText ->
                val lines = recognizedText.textBlocks
                    .flatMap { it.lines }
                    .map { line ->
                        OcrLine(
                            text = line.text,
                            top = line.boundingBox?.top ?: 0,
                            bottom = line.boundingBox?.bottom ?: 0
                        )
                    }
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        finishRecognition(
                            context = context,
                            managedUri = managedUri,
                            text = recognizedText.text,
                            lines = lines,
                            qrValues = barcodes.mapNotNull { it.rawValue },
                            onSuccess = onSuccess
                        )
                    }
                    .addOnFailureListener {
                        finishRecognition(
                            context = context,
                            managedUri = managedUri,
                            text = recognizedText.text,
                            lines = lines,
                            qrValues = emptyList(),
                            onSuccess = onSuccess
                        )
                    }
                    .addOnCompleteListener { barcodeScanner.close() }
            }
            .addOnFailureListener { error ->
                barcodeScanner.close()
                deleteManagedScan(context, managedUri)
                onFailure(error)
            }
            .addOnCompleteListener { textRecognizer.close() }
    }

    private fun finishRecognition(
        context: Context,
        managedUri: Uri,
        text: String,
        lines: List<OcrLine>,
        qrValues: List<String>,
        onSuccess: (InvoiceExtractionResult) -> Unit
    ) {
        val fileName = managedUri.lastPathSegment?.substringAfterLast('/') ?: "Scanned invoice"
        onSuccess(
            InvoiceExtractor.extract(
                fullText = text,
                lines = lines,
                qrValues = qrValues,
                attachmentUri = managedUri.toString(),
                attachmentName = fileName
            )
        )
    }

    private fun copyScanToAppStorage(context: Context, sourceUri: Uri): Uri {
        val directory = File(context.filesDir, SCAN_DIRECTORY).apply { mkdirs() }
        val extension = when (context.contentResolver.getType(sourceUri)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> sourceUri.lastPathSegment
                ?.substringAfterLast('.', "")
                ?.takeIf { it.lowercase(Locale.ROOT) in setOf("jpg", "jpeg", "png", "webp") }
                ?: "jpg"
        }
        val target = File(directory, "invoice-scan-${System.currentTimeMillis()}.$extension")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            target.outputStream().use(input::copyTo)
        } ?: error("Unable to read the scanned invoice.")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            target
        )
    }

    fun deleteManagedScan(context: Context, uri: Uri) {
        if (uri.authority != "${context.packageName}.fileprovider") return
        val segments = uri.pathSegments
        if (segments.size < 2) return
        val root = when (segments.first()) {
            "scanned_invoices" -> File(context.filesDir, SCAN_DIRECTORY)
            "expense_attachments" -> File(context.filesDir, "expense_attachments")
            else -> return
        }
        val target = File(root, segments.drop(1).joinToString(File.separator))
        val canonicalRoot = root.canonicalFile
        val canonicalTarget = target.canonicalFile
        if (!canonicalTarget.path.startsWith(canonicalRoot.path + File.separator)) return
        canonicalTarget.delete()
        var parent = canonicalTarget.parentFile
        while (parent != null && parent != canonicalRoot && parent.list()?.isEmpty() == true) {
            parent.delete()
            parent = parent.parentFile
        }
    }

    private const val SCAN_DIRECTORY = "scanned_invoices"
}

object InvoiceExtractor {
    private val gstinRegex = Regex("\\b\\d{2}[A-Z]{5}\\d{4}[A-Z][A-Z0-9]Z[A-Z0-9]\\b")
    private val irnRegex = Regex("\\b[A-Fa-f0-9]{64}\\b")
    private val invoicePatterns = listOf(
        Regex("(?i)\\b(?:invoice|inv|bill)\\s*(?:no|number|num|#)\\s*[:#.-]?\\s*([A-Z0-9][A-Z0-9/_-]{2,})"),
        Regex("(?i)\\b(?:invoice|inv|bill)\\s*[:#-]\\s*([A-Z0-9][A-Z0-9/_-]{2,})")
    )
    private val numericDateRegex = Regex("\\b(?:\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}|\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4})\\b")
    private val namedDateRegex = Regex("(?i)\\b\\d{1,2}[ -](?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[ -]\\d{2,4}\\b")
    private val amountRegex = Regex("(?i)(?:₹|INR|Rs\\.?)?\\s*(-?\\d[\\d,]*(?:\\.\\d{1,2})?)")
    private val dateFormatters = listOf(
        "uuuu-M-d",
        "d-M-uuuu",
        "d/M/uuuu",
        "d.M.uuuu",
        "d-M-uu",
        "d/M/uu",
        "d MMM uuuu",
        "d-MMM-uuuu",
        "d MMMM uuuu"
    ).map {
        DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(it)
            .toFormatter(Locale.ENGLISH)
    }

    fun extract(
        fullText: String,
        lines: List<OcrLine>,
        qrValues: List<String> = emptyList(),
        attachmentUri: String? = null,
        attachmentName: String? = null
    ): InvoiceExtractionResult {
        val normalizedText = (fullText + "\n" + qrValues.joinToString("\n")).uppercase(Locale.ROOT)
        val sortedLines = lines.filter { it.text.isNotBlank() }.sortedBy { it.top }
        val vendor = findVendor(sortedLines)
        val invoiceNumber = findInvoiceNumber(sortedLines, normalizedText)
        val date = findInvoiceDate(sortedLines)
        val amount = findTotalAmount(sortedLines)
        val gstin = gstinRegex.find(normalizedText)?.value.orEmpty()
        val taxAmount = findTaxAmount(sortedLines)
        val irn = irnRegex.find(normalizedText)?.value

        val notes = buildList {
            if (irn != null) add("IRN: $irn")
            if (qrValues.isNotEmpty()) add("GST/e-invoice QR detected")
        }.joinToString("\n")

        val draft = ExpenseDraft(
            vendor = vendor.orEmpty(),
            amount = amount?.value?.toPlainAmount().orEmpty(),
            date = date?.value ?: LocalDate.now().toString(),
            invoiceNumber = invoiceNumber.orEmpty(),
            attachmentUri = attachmentUri,
            attachmentName = attachmentName,
            notes = notes,
            supplierGstin = gstin,
            taxAmount = taxAmount?.toPlainAmount().orEmpty()
        )

        val fields = buildList {
            vendor?.let { add(ExtractedInvoiceField("Vendor", it, ExtractionConfidence.MEDIUM)) }
            invoiceNumber?.let {
                add(ExtractedInvoiceField("Invoice number", it, ExtractionConfidence.HIGH))
            }
            date?.let { add(ExtractedInvoiceField("Invoice date", it.value, it.confidence)) }
            amount?.let {
                add(ExtractedInvoiceField("Total", "₹${it.value.toPlainAmount()}", it.confidence))
            }
            if (gstin.isNotBlank()) {
                add(ExtractedInvoiceField("Supplier GSTIN", gstin, ExtractionConfidence.HIGH))
            }
            taxAmount?.let {
                add(ExtractedInvoiceField("Tax", "₹${it.toPlainAmount()}", ExtractionConfidence.MEDIUM))
            }
        }

        val warnings = buildList {
            if (vendor == null) add("Vendor could not be identified.")
            if (amount == null) add("Total amount could not be identified.")
            if (invoiceNumber == null) add("Invoice number was not found.")
            if (date == null) add("Invoice date was not found; today's date is selected.")
            if (taxAmount != null && amount != null && taxAmount > amount.value) {
                add("Extracted tax is greater than the invoice total.")
            }
        }

        return InvoiceExtractionResult(
            draft = draft,
            fields = fields,
            warnings = warnings,
            qrCodeDetected = qrValues.isNotEmpty()
        )
    }

    private fun findVendor(lines: List<OcrLine>): String? {
        val pageBottom = lines.maxOfOrNull { it.bottom }?.coerceAtLeast(1) ?: 1
        val excluded = listOf(
            "tax invoice", "invoice", "original for recipient", "gstin", "phone",
            "mobile", "email", "address", "bill to", "ship to"
        )
        return lines.asSequence()
            .filter { it.top <= pageBottom * 0.38 }
            .map { it.text.trim() }
            .filter { it.length in 3..100 }
            .filter { candidate -> candidate.any(Char::isLetter) }
            .filter { candidate -> excluded.none { candidate.lowercase(Locale.ROOT) == it } }
            .filterNot { gstinRegex.containsMatchIn(it.uppercase(Locale.ROOT)) }
            .filterNot { amountRegex.matches(it) }
            .firstOrNull()
    }

    private fun findInvoiceNumber(lines: List<OcrLine>, normalizedText: String): String? {
        lines.forEach { line ->
            invoicePatterns.forEach { pattern ->
                pattern.find(line.text)?.groupValues?.getOrNull(1)?.let { candidate ->
                    if (!candidate.equals("date", ignoreCase = true)) return candidate.trim()
                }
            }
        }
        return invoicePatterns.asSequence()
            .mapNotNull { it.find(normalizedText)?.groupValues?.getOrNull(1) }
            .firstOrNull()
    }

    private fun findInvoiceDate(lines: List<OcrLine>): DateCandidate? {
        val preferred = lines.filter {
            val value = it.text.lowercase(Locale.ROOT)
            (value.contains("invoice date") || value.startsWith("date")) &&
                !value.contains("due")
        }
        (preferred + lines.filterNot { it in preferred }).forEach { line ->
            val raw = numericDateRegex.find(line.text)?.value
                ?: namedDateRegex.find(line.text)?.value
                ?: return@forEach
            parseDate(raw)?.let { parsed ->
                return DateCandidate(
                    value = parsed.toString(),
                    confidence = if (line in preferred) {
                        ExtractionConfidence.HIGH
                    } else {
                        ExtractionConfidence.MEDIUM
                    }
                )
            }
        }
        return null
    }

    private fun parseDate(value: String): LocalDate? {
        val normalized = value.trim().replace('/', '-').replace('.', '-')
        return dateFormatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(normalized, formatter) }.getOrNull()
        }
    }

    private fun findTotalAmount(lines: List<OcrLine>): AmountCandidate? {
        val strongLabels = listOf("grand total", "amount payable", "net amount", "invoice total", "total due")
        val strongLines = lines.filter { line ->
            strongLabels.any { line.text.lowercase(Locale.ROOT).contains(it) }
        }
        strongLines.asReversed().forEach { line ->
            amountsIn(line.text).maxOrNull()?.let {
                return AmountCandidate(it, ExtractionConfidence.HIGH)
            }
        }
        lines.asReversed().filter { line ->
            val value = line.text.lowercase(Locale.ROOT)
            value.contains("total") &&
                !value.contains("subtotal") &&
                !value.contains("tax")
        }.forEach { line ->
            amountsIn(line.text).maxOrNull()?.let {
                return AmountCandidate(it, ExtractionConfidence.MEDIUM)
            }
        }
        return null
    }

    private fun findTaxAmount(lines: List<OcrLine>): Double? {
        lines.asReversed().firstOrNull {
            it.text.lowercase(Locale.ROOT).contains("total tax")
        }?.let { line -> amountsIn(line.text).lastOrNull()?.let { return it } }

        return listOf("cgst", "sgst", "igst").mapNotNull { taxLabel ->
            lines.asReversed().firstOrNull {
                it.text.lowercase(Locale.ROOT).contains(taxLabel)
            }?.let { amountsIn(it.text).lastOrNull() }
        }.takeIf { it.isNotEmpty() }?.sum()
    }

    private fun amountsIn(value: String): List<Double> = amountRegex.findAll(value)
        .mapNotNull { match ->
            match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
        }
        .filter { it.isFinite() && it >= 0.0 }
        .toList()

    private fun Double.toPlainAmount(): String =
        java.math.BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()

    private data class AmountCandidate(
        val value: Double,
        val confidence: ExtractionConfidence
    )

    private data class DateCandidate(
        val value: String,
        val confidence: ExtractionConfidence
    )
}
