package com.dps.businessexpensetracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceExtractorTest {
    @Test
    fun extractsCoreIndianInvoiceFields() {
        val lines = linesOf(
            "TAX INVOICE",
            "ACME PRIVATE LIMITED",
            "GSTIN: 27ABCDE1234F1Z5",
            "Invoice No: INV-2026/1042",
            "Invoice Date: 11/07/2026",
            "Subtotal 1,000.00",
            "CGST 9% 90.00",
            "SGST 9% 90.00",
            "Grand Total ₹ 1,180.00"
        )

        val result = InvoiceExtractor.extract(
            fullText = lines.joinToString("\n") { it.text },
            lines = lines
        )

        assertEquals("ACME PRIVATE LIMITED", result.draft.vendor)
        assertEquals("INV-2026/1042", result.draft.invoiceNumber)
        assertEquals("2026-07-11", result.draft.date)
        assertEquals("1180", result.draft.amount)
        assertEquals("180", result.draft.taxAmount)
        assertEquals("27ABCDE1234F1Z5", result.draft.supplierGstin)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun missingRequiredFieldsProduceReviewWarnings() {
        val result = InvoiceExtractor.extract(
            fullText = "Receipt\nThank you",
            lines = linesOf("Receipt", "Thank you")
        )

        assertTrue(result.warnings.any { it.contains("Total amount") })
        assertTrue(result.warnings.any { it.contains("Invoice number") })
        assertTrue(result.warnings.any { it.contains("Invoice date") })
    }

    @Test
    fun reportsQrDetectionWithoutTreatingItAsAutomaticConfirmation() {
        val result = InvoiceExtractor.extract(
            fullText = "Vendor Ltd\nGrand Total 500.00",
            lines = linesOf("Vendor Ltd", "Grand Total 500.00"),
            qrValues = listOf("signed-e-invoice-payload")
        )

        assertTrue(result.qrCodeDetected)
        assertTrue(result.draft.notes.contains("QR detected"))
    }

    private fun linesOf(vararg values: String): List<OcrLine> = values.mapIndexed { index, value ->
        OcrLine(text = value, top = index * 40, bottom = index * 40 + 30)
    }
}
