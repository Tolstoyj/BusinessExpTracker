package com.dps.businessexpensetracker

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import com.dps.businessexpensetracker.data.Expense
import com.dps.businessexpensetracker.data.ExpenseCategory
import com.dps.businessexpensetracker.data.ExpenseRepository
import com.dps.businessexpensetracker.data.ExpenseStatus
import com.dps.businessexpensetracker.data.PaymentMethod

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dps.businessexpensetracker", appContext.packageName)
    }

    @Test
    fun repositoryRecoversPreviousSnapshotWhenPrimaryDataIsCorrupt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("business_expenses", 0)
        preferences.edit().clear().commit()
        val repository = ExpenseRepository(context)
        repository.saveExpenses(listOf(sampleExpense("previous")))
        repository.saveExpenses(listOf(sampleExpense("current")))
        preferences.edit().putString("expenses_json", "{broken-json").commit()

        assertEquals("previous", repository.loadExpenses().single().vendor)
    }

    private fun sampleExpense(vendor: String) = Expense(
        vendor = vendor,
        amount = 10.0,
        category = ExpenseCategory.OFFICE,
        paymentMethod = PaymentMethod.CARD,
        date = "2026-07-11",
        status = ExpenseStatus.PAID,
        submittedBy = "Test",
        invoiceNumber = "",
        attachmentUri = null,
        attachmentName = null,
        notes = ""
    )
}
