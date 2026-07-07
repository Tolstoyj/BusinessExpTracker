package com.dps.businessexpensetracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.util.Currency

class ExpenseExporterTest {
    @Test
    fun csvExportUsesStableFileNameAndEscapesCells() {
        val export = ExpenseExporter.create(
            expenses = listOf(
                sampleExpense(
                    vendor = "ACME, India",
                    notes = "Need \"approval\" before payment"
                )
            ),
            format = ExpenseExportFormat.CSV,
            generatedAt = LocalDateTime.of(2026, 7, 8, 10, 30)
        )

        assertEquals("business-expenses-20260708-1030.csv", export.fileName)
        assertEquals("text/csv", export.mimeType)
        assertTrue(export.content.startsWith("Date,Vendor,Invoice Number"))
        assertTrue(export.content.contains("2026-07-08,\"ACME, India\""))
        assertTrue(export.content.contains("\"Need \"\"approval\"\" before payment\""))
        assertTrue(export.content.contains("Amount (INR)"))
    }

    @Test
    fun htmlExportEscapesValuesAndLabelsCurrencyAsInr() {
        val export = ExpenseExporter.create(
            expenses = listOf(
                sampleExpense(
                    vendor = "<script>Vendor</script>",
                    notes = "Tax & freight"
                )
            ),
            format = ExpenseExportFormat.HTML,
            generatedAt = LocalDateTime.of(2026, 7, 8, 10, 30)
        )

        assertEquals("business-expenses-20260708-1030.html", export.fileName)
        assertEquals("text/html", export.mimeType)
        assertTrue(export.content.contains("Currency: INR"))
        assertTrue(export.content.contains("&lt;script&gt;Vendor&lt;/script&gt;"))
        assertTrue(export.content.contains("Tax &amp; freight"))
    }

    @Test
    fun inrFormatterUsesIndianCurrency() {
        assertEquals(Currency.getInstance("INR"), inrCurrencyFormatter().currency)
    }

    private fun sampleExpense(
        vendor: String = "DPS Supplies",
        notes: String = "Monthly office supplies"
    ): Expense = Expense(
        id = "expense-1",
        vendor = vendor,
        amount = 1234.5,
        category = ExpenseCategory.OFFICE,
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        date = "2026-07-08",
        status = ExpenseStatus.FOR_REVIEW,
        submittedBy = "Accountant",
        invoiceNumber = "INV-1001",
        attachmentUri = "content://invoice/1001",
        attachmentName = "invoice-1001.pdf",
        notes = notes,
        updatedAt = 1L
    )
}
