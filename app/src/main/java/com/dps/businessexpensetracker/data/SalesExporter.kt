package com.dps.businessexpensetracker.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object SalesExporter {
    fun create(
        sales: List<Sale>,
        format: ExpenseExportFormat,
        generatedAt: LocalDateTime = LocalDateTime.now()
    ): ExpenseExport {
        val timestamp = generatedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        return ExpenseExport(
            fileName = "business-sales-$timestamp.${format.extension}",
            mimeType = format.mimeType,
            content = when (format) {
                ExpenseExportFormat.CSV -> sales.toSalesCsv()
                ExpenseExportFormat.HTML -> sales.toSalesHtml(generatedAt)
            }
        )
    }
}

private fun List<Sale>.toSalesCsv(): String {
    val rows = buildList {
        add(
            listOf(
                "Date", "Customer / Sale", "Reference", "Channel", "Payment Method",
                "Status", "Sold By", "Quantity", "Sale Amount (INR)",
                "Tax Amount (INR)", "Discount Amount (INR)", "Notes"
            )
        )
        this@toSalesCsv.forEach { sale ->
            add(
                listOf(
                    sale.date,
                    sale.customer,
                    sale.reference,
                    sale.channel.label,
                    sale.paymentMethod.label,
                    sale.status.label,
                    sale.soldBy,
                    sale.quantity.toString(),
                    sale.amount.asExportAmount(),
                    sale.taxAmount?.asExportAmount().orEmpty(),
                    sale.discountAmount?.asExportAmount().orEmpty(),
                    sale.notes
                )
            )
        }
    }
    return rows.joinToString(separator = "\n", postfix = "\n") { row ->
        row.joinToString(",") { it.toSafeCsvCell() }
    }
}

private fun List<Sale>.toSalesHtml(generatedAt: LocalDateTime): String {
    val currencyFormatter = inrCurrencyFormatter()
    val currentMonth = YearMonth.now()
    val total = filterNot { it.status == SaleStatus.REFUNDED }.sumOf { it.amount }
    val received = filter { it.status == SaleStatus.RECEIVED }.sumOf { it.amount }
    val pending = filter { it.status == SaleStatus.PENDING }.sumOf { it.amount }
    val month = filter {
        runCatching { YearMonth.from(LocalDate.parse(it.date)) }.getOrNull() == currentMonth &&
            it.status != SaleStatus.REFUNDED
    }.sumOf { it.amount }
    val rows = joinToString("\n") { sale ->
        """
        <tr>
          <td>${sale.date.salesHtml()}</td>
          <td>${sale.customer.salesHtml()}</td>
          <td>${sale.reference.salesHtml()}</td>
          <td>${sale.channel.label.salesHtml()}</td>
          <td>${sale.paymentMethod.label.salesHtml()}</td>
          <td>${sale.status.label.salesHtml()}</td>
          <td>${sale.soldBy.salesHtml()}</td>
          <td>${sale.quantity}</td>
          <td class="amount">${currencyFormatter.format(sale.amount).salesHtml()}</td>
          <td class="amount">${sale.taxAmount?.let(currencyFormatter::format).orEmpty().salesHtml()}</td>
          <td class="amount">${sale.discountAmount?.let(currencyFormatter::format).orEmpty().salesHtml()}</td>
          <td>${sale.notes.salesHtml()}</td>
        </tr>
        """.trimIndent()
    }
    val generatedText = generatedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
    val countText = "$size ${if (size == 1) "record" else "records"}"
    return """
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Business Sales Report</title>
          <style>
            body { margin: 0; font-family: Arial, sans-serif; color: #17201d; background: #f5f7f6; }
            main { max-width: 1180px; margin: 0 auto; padding: 32px 20px; }
            h1 { margin: 0 0 8px; } .meta { color: #52615b; margin: 0 0 24px; }
            .summary { display: grid; grid-template-columns: repeat(auto-fit,minmax(170px,1fr)); gap: 12px; margin-bottom: 24px; }
            .metric { background: white; border: 1px solid #d9e3df; border-radius: 8px; padding: 14px; }
            .metric span { display: block; color: #52615b; font-size: 12px; text-transform: uppercase; }
            .metric strong { display: block; margin-top: 8px; font-size: 18px; }
            .table-wrap { overflow-x: auto; background: white; border: 1px solid #d9e3df; border-radius: 8px; }
            table { width: 100%; border-collapse: collapse; min-width: 1080px; }
            th,td { padding: 11px 12px; border-bottom: 1px solid #e5ece9; text-align: left; font-size: 13px; }
            th { background: #edf5f2; } .amount { text-align: right; white-space: nowrap; }
          </style>
        </head>
        <body><main>
          <h1>Business Sales Report</h1>
          <p class="meta">Generated ${generatedText.salesHtml()} · Currency: INR · $countText</p>
          <section class="summary">
            <div class="metric"><span>Total sales</span><strong>${currencyFormatter.format(total).salesHtml()}</strong></div>
            <div class="metric"><span>This month</span><strong>${currencyFormatter.format(month).salesHtml()}</strong></div>
            <div class="metric"><span>Received</span><strong>${currencyFormatter.format(received).salesHtml()}</strong></div>
            <div class="metric"><span>Pending</span><strong>${currencyFormatter.format(pending).salesHtml()}</strong></div>
          </section>
          <section class="table-wrap"><table><thead><tr>
            <th>Date</th><th>Customer / Sale</th><th>Reference</th><th>Channel</th>
            <th>Payment</th><th>Status</th><th>Sold By</th><th>Qty</th>
            <th class="amount">Sale</th><th class="amount">Tax</th>
            <th class="amount">Discount</th><th>Notes</th>
          </tr></thead><tbody>$rows</tbody></table></section>
        </main></body></html>
    """.trimIndent()
}

private fun Double.asExportAmount(): String = "%.2f".format(Locale.US, this)

private fun String.toSafeCsvCell(): String {
    val first = trimStart().firstOrNull()
    val safe = if (first in listOf('=', '+', '-', '@')) "'$this" else this
    val escaped = safe.replace("\"", "\"\"")
    return if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"$escaped\""
    } else {
        escaped
    }
}

private fun String.salesHtml(): String = buildString {
    this@salesHtml.forEach {
        append(
            when (it) {
                '&' -> "&amp;"
                '<' -> "&lt;"
                '>' -> "&gt;"
                '"' -> "&quot;"
                '\'' -> "&#39;"
                else -> it
            }
        )
    }
}
