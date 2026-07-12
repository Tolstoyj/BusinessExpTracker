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

enum class SalesChannel(val label: String) {
    STORE("Store / counter"),
    ONLINE("Online"),
    WHOLESALE("Wholesale"),
    SERVICE("Service"),
    OTHER("Other")
}

enum class SaleStatus(val label: String) {
    RECEIVED("Received"),
    PENDING("Pending"),
    REFUNDED("Refunded")
}

data class Sale(
    val id: String = UUID.randomUUID().toString(),
    val customer: String,
    val amount: Double,
    val channel: SalesChannel,
    val paymentMethod: PaymentMethod,
    val date: String,
    val status: SaleStatus,
    val reference: String,
    val soldBy: String,
    val quantity: Int,
    val taxAmount: Double?,
    val discountAmount: Double?,
    val notes: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("customer", customer)
        .put("amount", amount)
        .put("channel", channel.name)
        .put("paymentMethod", paymentMethod.name)
        .put("date", date)
        .put("status", status.name)
        .put("reference", reference)
        .put("soldBy", soldBy)
        .put("quantity", quantity)
        .put("taxAmount", taxAmount)
        .put("discountAmount", discountAmount)
        .put("notes", notes)
        .put("updatedAt", updatedAt)

    companion object {
        fun fromJson(json: JSONObject): Sale = Sale(
            id = json.optString("id", UUID.randomUUID().toString()),
            customer = json.optString("customer"),
            amount = json.optDouble("amount", 0.0),
            channel = parseSaleEnum(json.optString("channel"), SalesChannel.OTHER),
            paymentMethod = parseSaleEnum(
                json.optString("paymentMethod"),
                PaymentMethod.OTHER
            ),
            date = json.optString("date", LocalDate.now().toString()),
            status = parseSaleEnum(json.optString("status"), SaleStatus.RECEIVED),
            reference = json.optString("reference"),
            soldBy = json.optString("soldBy"),
            quantity = json.optInt("quantity", 1),
            taxAmount = json.optionalDouble("taxAmount"),
            discountAmount = json.optionalDouble("discountAmount"),
            notes = json.optString("notes"),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
        )
    }
}

data class SaleDraft(
    val id: String? = null,
    val customer: String = "Walk-in customer",
    val amount: String = "",
    val channel: SalesChannel = SalesChannel.STORE,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val date: String = LocalDate.now().toString(),
    val status: SaleStatus = SaleStatus.RECEIVED,
    val reference: String = "",
    val soldBy: String = "",
    val quantity: String = "1",
    val taxAmount: String = "",
    val discountAmount: String = "",
    val notes: String = ""
) {
    fun toSale(): Sale = Sale(
        id = id ?: UUID.randomUUID().toString(),
        customer = customer.trim(),
        amount = normalizedAmount(amount),
        channel = channel,
        paymentMethod = paymentMethod,
        date = date.trim(),
        status = status,
        reference = reference.trim(),
        soldBy = soldBy.trim(),
        quantity = quantity.trim().toInt(),
        taxAmount = optionalAmount(taxAmount),
        discountAmount = optionalAmount(discountAmount),
        notes = notes.trim()
    )

    companion object {
        fun fromSale(sale: Sale): SaleDraft = SaleDraft(
            id = sale.id,
            customer = sale.customer,
            amount = sale.amount.toPlainAmount(),
            channel = sale.channel,
            paymentMethod = sale.paymentMethod,
            date = sale.date,
            status = sale.status,
            reference = sale.reference,
            soldBy = sale.soldBy,
            quantity = sale.quantity.toString(),
            taxAmount = sale.taxAmount?.toPlainAmount().orEmpty(),
            discountAmount = sale.discountAmount?.toPlainAmount().orEmpty(),
            notes = sale.notes
        )
    }
}

fun validateSaleDraft(draft: SaleDraft): String? {
    if (draft.customer.isBlank()) return "Customer or sale label is required."
    val amount = draft.amount.toFinitePositiveAmount()
        ?: return "Enter a valid sale amount greater than zero."
    if (runCatching { LocalDate.parse(draft.date.trim()) }.getOrNull() == null) {
        return "Use date format YYYY-MM-DD."
    }
    if (draft.soldBy.isBlank()) return "Sold by is required."
    val quantity = draft.quantity.trim().toIntOrNull()
    if (quantity == null || quantity <= 0 || quantity > 1_000_000) {
        return "Enter a valid quantity greater than zero."
    }
    val taxError = validateOptionalSaleAmount(draft.taxAmount, amount, "Tax")
    if (taxError != null) return taxError
    val discountError = validateOptionalSaleAmount(draft.discountAmount, amount, "Discount")
    if (discountError != null) return discountError
    return null
}

fun SaleDraft.toStateString(): String {
    val bytes = ByteArrayOutputStream()
    DataOutputStream(bytes).use { output ->
        listOf(
            id, customer, amount, channel.name, paymentMethod.name, date, status.name,
            reference, soldBy, quantity, taxAmount, discountAmount, notes
        ).forEach(output::writeSaleStateString)
    }
    return Base64.getEncoder().encodeToString(bytes.toByteArray())
}

fun saleDraftFromState(value: String): SaleDraft = runCatching {
    DataInputStream(ByteArrayInputStream(Base64.getDecoder().decode(value))).use { input ->
        SaleDraft(
            id = input.readSaleStateString(),
            customer = input.readSaleStateString().orEmpty(),
            amount = input.readSaleStateString().orEmpty(),
            channel = parseSaleEnum(input.readSaleStateString().orEmpty(), SalesChannel.STORE),
            paymentMethod = parseSaleEnum(
                input.readSaleStateString().orEmpty(),
                PaymentMethod.UPI
            ),
            date = input.readSaleStateString() ?: LocalDate.now().toString(),
            status = parseSaleEnum(input.readSaleStateString().orEmpty(), SaleStatus.RECEIVED),
            reference = input.readSaleStateString().orEmpty(),
            soldBy = input.readSaleStateString().orEmpty(),
            quantity = input.readSaleStateString().orEmpty(),
            taxAmount = input.readSaleStateString().orEmpty(),
            discountAmount = input.readSaleStateString().orEmpty(),
            notes = input.readSaleStateString().orEmpty()
        )
    }
}.getOrElse { SaleDraft() }

private fun validateOptionalSaleAmount(value: String, total: Double, label: String): String? {
    if (value.isBlank()) return null
    val parsed = value.replace(",", "").trim().toDoubleOrNull()
    if (parsed == null || !parsed.isFinite() || parsed < 0.0) {
        return "Enter a valid ${label.lowercase()} amount."
    }
    if (parsed > total) return "$label amount cannot exceed the total sale."
    return null
}

private fun String.toFinitePositiveAmount(): Double? =
    replace(",", "").trim().toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }

private fun normalizedAmount(value: String): Double =
    value.replace(",", "").trim().toDouble()

private fun optionalAmount(value: String): Double? =
    value.replace(",", "").trim().toDoubleOrNull()

private fun Double.toPlainAmount(): String =
    BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()

private inline fun <reified T : Enum<T>> parseSaleEnum(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback

private fun JSONObject.optionalDouble(key: String): Double? =
    if (isNull(key)) null else optDouble(key).takeIf(Double::isFinite)

private fun DataOutputStream.writeSaleStateString(value: String?) {
    if (value == null) {
        writeInt(-1)
        return
    }
    val bytes = value.toByteArray(Charsets.UTF_8)
    writeInt(bytes.size)
    write(bytes)
}

private fun DataInputStream.readSaleStateString(): String? {
    val length = readInt()
    if (length < 0) return null
    require(length <= 1_000_000) { "Draft state field is too large." }
    return ByteArray(length).also(::readFully).toString(Charsets.UTF_8)
}
