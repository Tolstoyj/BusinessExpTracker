package com.dps.businessexpensetracker

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.dps.businessexpensetracker.data.AppDataMigration
import com.dps.businessexpensetracker.data.Expense
import com.dps.businessexpensetracker.data.ExpenseCategory
import com.dps.businessexpensetracker.data.ExpenseRepository
import com.dps.businessexpensetracker.data.ExpenseStatus
import com.dps.businessexpensetracker.data.PaymentMethod
import com.dps.businessexpensetracker.data.Sale
import com.dps.businessexpensetracker.data.SaleStatus
import com.dps.businessexpensetracker.data.SalesChannel
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppDataMigrationTest {
    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }
    private val preferences by lazy {
        context.getSharedPreferences("business_expenses", Context.MODE_PRIVATE)
    }

    @Before
    fun clearBefore() {
        preferences.edit().clear().commit()
    }

    @After
    fun clearAfter() {
        preferences.edit().clear().commit()
    }

    @Test
    fun versionOneExpensesAreSnapshottedAndPreserved() {
        val original = JSONArray().put(sampleExpense().toJson()).toString()
        preferences.edit().putString("expenses_json", original).commit()

        val result = AppDataMigration.migrate(context)
        val restored = ExpenseRepository(context).loadExpenses()

        assertTrue(result.migrated)
        assertEquals(1, result.fromVersion)
        assertEquals(2, result.toVersion)
        assertEquals(1, result.preservedExpenseCount)
        assertEquals(original, preferences.getString("pre_migration_expenses_v1", null))
        assertEquals(2, preferences.getInt("data_schema_version", 0))
        assertEquals("Legacy Supplier", restored.single().vendor)
        assertEquals(1250.0, restored.single().amount, 0.001)

        assertFalse(AppDataMigration.migrate(context).migrated)
        assertEquals(original, preferences.getString("expenses_json", null))
    }

    @Test
    fun corruptedPrimaryUsesValidPreviousSnapshotWithoutDeletingEither() {
        val previous = JSONArray().put(sampleExpense().toJson()).toString()
        preferences.edit()
            .putString("expenses_json", "not-json")
            .putString("expenses_json_backup", previous)
            .commit()

        val result = AppDataMigration.migrate(context)

        assertTrue(result.migrated)
        assertEquals(previous, preferences.getString("pre_migration_expenses_v1", null))
        assertEquals("not-json", preferences.getString("expenses_json", null))
        assertEquals("Legacy Supplier", ExpenseRepository(context).loadExpenses().single().vendor)
    }

    @Test
    fun existingVersionTwoSalesAreNotRewrittenDuringFirstVersionBookkeeping() {
        val expenseJson = JSONArray().put(sampleExpense().toJson()).toString()
        val salesJson = JSONArray().put(sampleSale().toJson()).toString()
        preferences.edit()
            .putString("expenses_json", expenseJson)
            .putString("sales_json", salesJson)
            .commit()

        AppDataMigration.migrate(context)
        val repository = ExpenseRepository(context)

        assertEquals("Legacy Supplier", repository.loadExpenses().single().vendor)
        assertEquals("Existing counter sale", repository.loadSales().single().customer)
        assertEquals(salesJson, preferences.getString("sales_json", null))
    }

    private fun sampleExpense() = Expense(
        id = "legacy-expense",
        vendor = "Legacy Supplier",
        amount = 1250.0,
        category = ExpenseCategory.INVENTORY,
        paymentMethod = PaymentMethod.UPI,
        date = "2026-07-01",
        status = ExpenseStatus.PAID,
        submittedBy = "Owner",
        invoiceNumber = "OLD-001",
        attachmentUri = null,
        attachmentName = null,
        notes = "Created in version 1"
    )

    private fun sampleSale() = Sale(
        id = "existing-sale",
        customer = "Existing counter sale",
        amount = 500.0,
        channel = SalesChannel.STORE,
        paymentMethod = PaymentMethod.CASH,
        date = "2026-07-13",
        status = SaleStatus.RECEIVED,
        reference = "SALE-001",
        soldBy = "Owner",
        quantity = 1,
        taxAmount = null,
        discountAmount = null,
        notes = "Created in version 2.0.0"
    )
}
