package com.dps.businessexpensetracker.data

import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

enum class ExpenseCategory(val label: String) {
    OFFICE("Office"),
    TRAVEL("Travel"),
    MEALS("Meals"),
    UTILITIES("Utilities"),
    SOFTWARE("Software"),
    INVENTORY("Inventory"),
    MARKETING("Marketing"),
    PROFESSIONAL_FEES("Professional fees"),
    TAXES("Taxes"),
    OTHER("Other")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CARD("Card"),
    BANK_TRANSFER("Bank transfer"),
    UPI("UPI"),
    CHEQUE("Cheque"),
    OTHER("Other")
}

enum class ExpenseStatus(val label: String) {
    DRAFT("Draft"),
    FOR_REVIEW("For review"),
    APPROVED("Approved"),
    PAID("Paid"),
    REJECTED("Rejected")
}

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val vendor: String,
    val amount: Double,
    val category: ExpenseCategory,
    val paymentMethod: PaymentMethod,
    val date: String,
    val status: ExpenseStatus,
    val submittedBy: String,
    val invoiceNumber: String,
    val attachmentUri: String?,
    val attachmentName: String?,
    val notes: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val supplierGstin: String = "",
    val taxAmount: Double? = null
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("vendor", vendor)
        .put("amount", amount)
        .put("category", category.name)
        .put("paymentMethod", paymentMethod.name)
        .put("date", date)
        .put("status", status.name)
        .put("submittedBy", submittedBy)
        .put("invoiceNumber", invoiceNumber)
        .put("attachmentUri", attachmentUri)
        .put("attachmentName", attachmentName)
        .put("notes", notes)
        .put("updatedAt", updatedAt)
        .put("supplierGstin", supplierGstin)
        .put("taxAmount", taxAmount)

    companion object {
        fun fromJson(json: JSONObject): Expense = Expense(
            id = json.optString("id", UUID.randomUUID().toString()),
            vendor = json.optString("vendor"),
            amount = json.optDouble("amount", 0.0),
            category = parseEnum(json.optString("category"), ExpenseCategory.OTHER),
            paymentMethod = parseEnum(json.optString("paymentMethod"), PaymentMethod.OTHER),
            date = json.optString("date", LocalDate.now().toString()),
            status = parseEnum(json.optString("status"), ExpenseStatus.DRAFT),
            submittedBy = json.optString("submittedBy"),
            invoiceNumber = json.optString("invoiceNumber"),
            attachmentUri = json.nullableString("attachmentUri"),
            attachmentName = json.nullableString("attachmentName"),
            notes = json.optString("notes"),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
            supplierGstin = json.optString("supplierGstin"),
            taxAmount = if (json.isNull("taxAmount")) null else json.optDouble("taxAmount")
        )
    }
}

data class ExpenseDraft(
    val id: String? = null,
    val vendor: String = "",
    val amount: String = "",
    val category: ExpenseCategory = ExpenseCategory.OFFICE,
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
    val date: String = LocalDate.now().toString(),
    val status: ExpenseStatus = ExpenseStatus.FOR_REVIEW,
    val submittedBy: String = "",
    val invoiceNumber: String = "",
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val notes: String = "",
    val supplierGstin: String = "",
    val taxAmount: String = ""
) {
    fun toExpense(): Expense = Expense(
        id = id ?: UUID.randomUUID().toString(),
        vendor = vendor.trim(),
        amount = normalizedAmount(),
        category = category,
        paymentMethod = paymentMethod,
        date = date.trim(),
        status = status,
        submittedBy = submittedBy.trim(),
        invoiceNumber = invoiceNumber.trim(),
        attachmentUri = attachmentUri,
        attachmentName = attachmentName,
        notes = notes.trim(),
        supplierGstin = supplierGstin.trim().uppercase(),
        taxAmount = taxAmount.replace(",", "").trim().toDoubleOrNull()
    )

    fun normalizedAmount(): Double = amount.replace(",", "").trim().toDouble()

    companion object {
        fun fromExpense(expense: Expense): ExpenseDraft = ExpenseDraft(
            id = expense.id,
            vendor = expense.vendor,
            amount = BigDecimal.valueOf(expense.amount).stripTrailingZeros().toPlainString(),
            category = expense.category,
            paymentMethod = expense.paymentMethod,
            date = expense.date,
            status = expense.status,
            submittedBy = expense.submittedBy,
            invoiceNumber = expense.invoiceNumber,
            attachmentUri = expense.attachmentUri,
            attachmentName = expense.attachmentName,
            notes = expense.notes,
            supplierGstin = expense.supplierGstin,
            taxAmount = expense.taxAmount?.let {
                BigDecimal.valueOf(it).stripTrailingZeros().toPlainString()
            }.orEmpty()
        )
    }
}

fun ExpenseDraft.toStateString(): String {
    val bytes = ByteArrayOutputStream()
    DataOutputStream(bytes).use { output ->
        output.writeStateString(id)
        output.writeStateString(vendor)
        output.writeStateString(amount)
        output.writeStateString(category.name)
        output.writeStateString(paymentMethod.name)
        output.writeStateString(date)
        output.writeStateString(status.name)
        output.writeStateString(submittedBy)
        output.writeStateString(invoiceNumber)
        output.writeStateString(attachmentUri)
        output.writeStateString(attachmentName)
        output.writeStateString(notes)
        output.writeStateString(supplierGstin)
        output.writeStateString(taxAmount)
    }
    return Base64.getEncoder().encodeToString(bytes.toByteArray())
}

fun expenseDraftFromState(value: String): ExpenseDraft = runCatching {
    DataInputStream(ByteArrayInputStream(Base64.getDecoder().decode(value))).use { input ->
        ExpenseDraft(
            id = input.readStateString(),
            vendor = input.readStateString().orEmpty(),
            amount = input.readStateString().orEmpty(),
            category = parseEnum(input.readStateString().orEmpty(), ExpenseCategory.OFFICE),
            paymentMethod = parseEnum(
                input.readStateString().orEmpty(),
                PaymentMethod.BANK_TRANSFER
            ),
            date = input.readStateString() ?: LocalDate.now().toString(),
            status = parseEnum(
                input.readStateString().orEmpty(),
                ExpenseStatus.FOR_REVIEW
            ),
            submittedBy = input.readStateString().orEmpty(),
            invoiceNumber = input.readStateString().orEmpty(),
            attachmentUri = input.readStateString(),
            attachmentName = input.readStateString(),
            notes = input.readStateString().orEmpty(),
            supplierGstin = input.readStateString().orEmpty(),
            taxAmount = input.readStateString().orEmpty()
        )
    }
}.getOrElse { ExpenseDraft() }

fun validateExpenseDraft(draft: ExpenseDraft): String? {
    if (draft.vendor.isBlank()) return "Vendor is required."
    val amount = draft.amount.replace(",", "").trim().toDoubleOrNull()
    if (amount == null || !amount.isFinite() || amount <= 0.0) {
        return "Enter a valid amount greater than zero."
    }
    val parsedDate = runCatching { LocalDate.parse(draft.date.trim()) }.getOrNull()
    if (parsedDate == null) return "Use date format YYYY-MM-DD."
    if (draft.submittedBy.isBlank()) return "Submitted by is required."
    if (draft.supplierGstin.isNotBlank() && !GSTIN_REGEX.matches(draft.supplierGstin.trim())) {
        return "Enter a valid 15-character GSTIN."
    }
    if (draft.taxAmount.isNotBlank()) {
        val tax = draft.taxAmount.replace(",", "").trim().toDoubleOrNull()
        if (tax == null || !tax.isFinite() || tax < 0.0) return "Enter a valid tax amount."
        if (tax > amount) return "Tax amount cannot exceed the total amount."
    }
    return null
}

fun isDuplicateInvoiceNumber(candidate: String, existingNumbers: List<String>): Boolean {
    val normalizedCandidate = candidate.trim()
    if (normalizedCandidate.isBlank()) return false
    return existingNumbers.any {
        it.isNotBlank() && it.trim().equals(normalizedCandidate, ignoreCase = true)
    }
}

private inline fun <reified T : Enum<T>> parseEnum(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback

private fun JSONObject.nullableString(key: String): String? =
    if (isNull(key)) null else optString(key).ifBlank { null }

private fun DataOutputStream.writeStateString(value: String?) {
    if (value == null) {
        writeInt(-1)
        return
    }
    val bytes = value.toByteArray(Charsets.UTF_8)
    writeInt(bytes.size)
    write(bytes)
}

private fun DataInputStream.readStateString(): String? {
    val length = readInt()
    if (length < 0) return null
    require(length <= MAX_STATE_FIELD_BYTES) { "Draft state field is too large." }
    return ByteArray(length).also(::readFully).toString(Charsets.UTF_8)
}

private const val MAX_STATE_FIELD_BYTES = 1_000_000
private val GSTIN_REGEX = Regex("\\d{2}[A-Za-z]{5}\\d{4}[A-Za-z][A-Za-z0-9]Z[A-Za-z0-9]")
