package com.dps.businessexpensetracker.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class SalesExporterTest {
    private val sale = Sale(
        id = "sale-1",
        customer = "=Unsafe Customer",
        amount = 1180.0,
        channel = SalesChannel.STORE,
        paymentMethod = PaymentMethod.UPI,
        date = "2026-07-13",
        status = SaleStatus.RECEIVED,
        reference = "ORD-1",
        soldBy = "Owner",
        quantity = 2,
        taxAmount = 180.0,
        discountAmount = 20.0,
        notes = "<paid>"
    )

    @Test
    fun csvContainsSalesFieldsAndBlocksFormulaInjection() {
        val export = SalesExporter.create(
            listOf(sale),
            ExpenseExportFormat.CSV,
            LocalDateTime.of(2026, 7, 13, 10, 30)
        )
        assertTrue(export.fileName.startsWith("business-sales-"))
        assertTrue(export.content.contains("Sale Amount (INR)"))
        assertTrue(export.content.contains("'=Unsafe Customer"))
        assertTrue(export.content.contains("1180.00"))
    }

    @Test
    fun htmlEscapesUserContentAndShowsRevenueSummary() {
        val export = SalesExporter.create(listOf(sale), ExpenseExportFormat.HTML)
        assertTrue(export.content.contains("Business Sales Report"))
        assertTrue(export.content.contains("&lt;paid&gt;"))
        assertFalse(export.content.contains("<paid>"))
        assertTrue(export.content.contains("Total sales"))
        assertTrue(export.content.contains("Currency: INR"))
    }

    @Test
    fun exportsUseTheRequestedCurrencyOnCsvAndHtml() {
        val generatedAt = LocalDateTime.of(2026, 7, 13, 10, 30)
        val csv = SalesExporter.create(
            listOf(sale),
            ExpenseExportFormat.CSV,
            generatedAt,
            currencyCode = "USD"
        )
        assertTrue(csv.content.contains("Sale Amount (USD)"))
        assertTrue(csv.content.contains("Tax Amount (USD)"))
        assertTrue(csv.content.contains("Discount Amount (USD)"))
        assertTrue(csv.content.contains("Sale Amount (INR)").not())

        val html = SalesExporter.create(
            sales = listOf(sale),
            format = ExpenseExportFormat.HTML,
            generatedAt = generatedAt,
            currencyCode = "eur"
        )
        assertTrue(html.content.contains("Currency: EUR"))
        assertTrue(html.content.contains(currencyFormatter("EUR").format(sale.amount)))
        assertTrue(html.content.contains("Currency: INR").not())
    }
}
