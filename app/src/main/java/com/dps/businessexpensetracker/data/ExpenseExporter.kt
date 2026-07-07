package com.dps.businessexpensetracker.data

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

private val IndiaLocale: Locale = Locale.Builder()
    .setLanguage("en")
    .setRegion("IN")
    .build()

fun inrCurrencyFormatter(): NumberFormat =
    NumberFormat.getCurrencyInstance(IndiaLocale).apply {
        currency = Currency.getInstance("INR")
    }

enum class ExpenseExportFormat(
    val label: String,
    val mimeType: String,
    val extension: String
) {
    CSV("CSV spreadsheet", "text/csv", "csv"),
    HTML("HTML report", "text/html", "html")
}

data class ExpenseExport(
    val fileName: String,
    val mimeType: String,
    val content: String
)

object ExpenseExporter {
    fun create(
        expenses: List<Expense>,
        format: ExpenseExportFormat,
        generatedAt: LocalDateTime = LocalDateTime.now()
    ): ExpenseExport {
        val timestamp = generatedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        val fileName = "business-expenses-$timestamp.${format.extension}"
        val content = when (format) {
            ExpenseExportFormat.CSV -> expenses.toCsv()
            ExpenseExportFormat.HTML -> expenses.toHtmlReport(generatedAt)
        }
        return ExpenseExport(
            fileName = fileName,
            mimeType = format.mimeType,
            content = content
        )
    }
}

private fun List<Expense>.toCsv(): String {
    val rows = buildList {
        add(
            listOf(
                "Date",
                "Vendor",
                "Invoice Number",
                "Category",
                "Payment Method",
                "Status",
                "Submitted By",
                "Amount (INR)",
                "Notes",
                "Attachment Name",
                "Attachment URI"
            )
        )
        this@toCsv.forEach { expense ->
            add(
                listOf(
                    expense.date,
                    expense.vendor,
                    expense.invoiceNumber,
                    expense.category.label,
                    expense.paymentMethod.label,
                    expense.status.label,
                    expense.submittedBy,
                    "%.2f".format(Locale.US, expense.amount),
                    expense.notes,
                    expense.attachmentName.orEmpty(),
                    expense.attachmentUri.orEmpty()
                )
            )
        }
    }
    return rows.joinToString(separator = "\n", postfix = "\n") { row ->
        row.joinToString(",") { it.toCsvCell() }
    }
}

private fun List<Expense>.toHtmlReport(generatedAt: LocalDateTime): String {
    val currencyFormatter = inrCurrencyFormatter()
    val summary = ExportSummary.from(this)
    val generatedText = generatedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
    val rows = joinToString("\n") { expense ->
        """
        <tr>
          <td>${expense.date.escapeHtml()}</td>
          <td>${expense.vendor.escapeHtml()}</td>
          <td>${expense.invoiceNumber.escapeHtml()}</td>
          <td>${expense.category.label.escapeHtml()}</td>
          <td>${expense.paymentMethod.label.escapeHtml()}</td>
          <td>${expense.status.label.escapeHtml()}</td>
          <td>${expense.submittedBy.escapeHtml()}</td>
          <td class="amount">${currencyFormatter.format(expense.amount).escapeHtml()}</td>
          <td>${expense.notes.escapeHtml()}</td>
          <td>${expense.attachmentName.orEmpty().escapeHtml()}</td>
        </tr>
        """.trimIndent()
    }

    return """
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Business Expense Report</title>
          <style>
            body {
              margin: 0;
              font-family: Arial, Helvetica, sans-serif;
              color: #17201d;
              background: #f5f7f6;
            }
            main {
              max-width: 1180px;
              margin: 0 auto;
              padding: 32px 20px;
            }
            h1 {
              margin: 0 0 8px;
              font-size: 28px;
            }
            .meta {
              margin: 0 0 24px;
              color: #52615b;
            }
            .summary {
              display: grid;
              grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
              gap: 12px;
              margin-bottom: 24px;
            }
            .metric {
              background: #ffffff;
              border: 1px solid #d9e3df;
              border-radius: 8px;
              padding: 14px;
            }
            .metric span {
              display: block;
              color: #52615b;
              font-size: 12px;
              text-transform: uppercase;
            }
            .metric strong {
              display: block;
              margin-top: 8px;
              font-size: 18px;
            }
            .table-wrap {
              overflow-x: auto;
              background: #ffffff;
              border: 1px solid #d9e3df;
              border-radius: 8px;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              min-width: 980px;
            }
            th, td {
              padding: 11px 12px;
              border-bottom: 1px solid #e5ece9;
              text-align: left;
              vertical-align: top;
              font-size: 13px;
            }
            th {
              background: #edf5f2;
              color: #25332f;
              font-weight: 700;
              position: sticky;
              top: 0;
            }
            tr:last-child td {
              border-bottom: 0;
            }
            .amount {
              text-align: right;
              white-space: nowrap;
            }
          </style>
        </head>
        <body>
          <main>
            <h1>Business Expense Report</h1>
            <p class="meta">Generated $generatedText · Currency: INR · ${size} records</p>
            <section class="summary" aria-label="Summary">
              <div class="metric"><span>Total spend</span><strong>${currencyFormatter.format(summary.totalSpend).escapeHtml()}</strong></div>
              <div class="metric"><span>This month</span><strong>${currencyFormatter.format(summary.thisMonthSpend).escapeHtml()}</strong></div>
              <div class="metric"><span>Paid</span><strong>${currencyFormatter.format(summary.paidSpend).escapeHtml()}</strong></div>
              <div class="metric"><span>Pending review</span><strong>${summary.pendingCount}</strong></div>
              <div class="metric"><span>Top category</span><strong>${summary.topCategory.escapeHtml()}</strong></div>
            </section>
            <section class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Vendor</th>
                    <th>Invoice</th>
                    <th>Category</th>
                    <th>Payment</th>
                    <th>Status</th>
                    <th>Submitted By</th>
                    <th class="amount">Amount</th>
                    <th>Notes</th>
                    <th>Attachment</th>
                  </tr>
                </thead>
                <tbody>
                  $rows
                </tbody>
              </table>
            </section>
          </main>
        </body>
        </html>
    """.trimIndent()
}

private data class ExportSummary(
    val totalSpend: Double,
    val thisMonthSpend: Double,
    val paidSpend: Double,
    val pendingCount: Int,
    val topCategory: String
) {
    companion object {
        fun from(expenses: List<Expense>): ExportSummary {
            val currentMonth = YearMonth.now()
            val topCategory = expenses
                .groupBy { it.category }
                .maxByOrNull { (_, categoryExpenses) -> categoryExpenses.sumOf { it.amount } }
                ?.key
                ?.label ?: "None"

            return ExportSummary(
                totalSpend = expenses.sumOf { it.amount },
                thisMonthSpend = expenses
                    .filter { runCatching { YearMonth.from(java.time.LocalDate.parse(it.date)) }.getOrNull() == currentMonth }
                    .sumOf { it.amount },
                paidSpend = expenses
                    .filter { it.status == ExpenseStatus.PAID }
                    .sumOf { it.amount },
                pendingCount = expenses.count {
                    it.status == ExpenseStatus.DRAFT || it.status == ExpenseStatus.FOR_REVIEW
                },
                topCategory = topCategory
            )
        }
    }
}

private fun String.toCsvCell(): String {
    val escaped = replace("\"", "\"\"")
    return if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"$escaped\""
    } else {
        escaped
    }
}

private fun String.escapeHtml(): String = buildString {
    this@escapeHtml.forEach { character ->
        when (character) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(character)
        }
    }
}
