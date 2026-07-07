package com.dps.businessexpensetracker.data

import org.json.JSONObject
import java.time.LocalDate
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
    val updatedAt: Long = System.currentTimeMillis()
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
            attachmentUri = json.optString("attachmentUri").ifBlank { null },
            attachmentName = json.optString("attachmentName").ifBlank { null },
            notes = json.optString("notes"),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
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
    val notes: String = ""
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
        notes = notes.trim()
    )

    fun normalizedAmount(): Double = amount.replace(",", "").trim().toDouble()

    companion object {
        fun fromExpense(expense: Expense): ExpenseDraft = ExpenseDraft(
            id = expense.id,
            vendor = expense.vendor,
            amount = expense.amount.toString(),
            category = expense.category,
            paymentMethod = expense.paymentMethod,
            date = expense.date,
            status = expense.status,
            submittedBy = expense.submittedBy,
            invoiceNumber = expense.invoiceNumber,
            attachmentUri = expense.attachmentUri,
            attachmentName = expense.attachmentName,
            notes = expense.notes
        )
    }
}

fun validateExpenseDraft(draft: ExpenseDraft): String? {
    if (draft.vendor.isBlank()) return "Vendor is required."
    val amount = draft.amount.replace(",", "").trim().toDoubleOrNull()
    if (amount == null || amount <= 0.0) return "Enter a valid amount greater than zero."
    val parsedDate = runCatching { LocalDate.parse(draft.date.trim()) }.getOrNull()
    if (parsedDate == null) return "Use date format YYYY-MM-DD."
    if (draft.submittedBy.isBlank()) return "Submitted by is required."
    return null
}

private inline fun <reified T : Enum<T>> parseEnum(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback
