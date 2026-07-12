package com.dps.businessexpensetracker

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import com.dps.businessexpensetracker.data.Expense
import com.dps.businessexpensetracker.data.ExpenseCategory
import com.dps.businessexpensetracker.data.ExpenseRepository
import com.dps.businessexpensetracker.data.ExpenseStatus
import com.dps.businessexpensetracker.data.PaymentMethod
import com.dps.businessexpensetracker.data.Sale
import com.dps.businessexpensetracker.data.SaleStatus
import com.dps.businessexpensetracker.data.SalesChannel
import com.dps.businessexpensetracker.ui.GuidedTourPrefs
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExpenseUserJourneyTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun resetAppData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("business_expenses", 0).edit().clear().commit()
        // Skip the first-run guided tour so it doesn't block these journeys.
        GuidedTourPrefs.markTourSeen(context)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitForIdle()
    }

    @After
    fun closeActivity() {
        scenario.close()
    }

    @Test
    fun emptyStateRejectsNonFiniteAmount() {
        composeRule.onNodeWithText("No expenses yet").assertIsDisplayed()
        openManualEntry()
        fillRequiredFields(vendor = "Edge Vendor", amount = "NaN")

        composeRule.onNodeWithContentDescription("Save expense").performClick()

        composeRule.onNodeWithText("Enter a valid amount greater than zero.")
            .assertIsDisplayed()
    }

    @Test
    fun addMenuMakesSmartScanThePrimaryOption() {
        composeRule.onNodeWithContentDescription("Add expense").performClick()

        composeRule.onNodeWithText("Scan invoice").assertIsDisplayed()
        composeRule.onNodeWithText("Camera or gallery · processed on device")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Import invoice image").assertIsDisplayed()
        composeRule.onNodeWithText("Enter manually").assertIsDisplayed()
    }

    @Test
    fun backupAndRestoreOptionsAreDiscoverable() {
        composeRule.onNodeWithContentDescription("Backup and restore").performClick()

        composeRule.onNodeWithText("Choose backup file").assertIsDisplayed()
        composeRule.onNodeWithText("Restore from backup").assertIsDisplayed()
    }

    @Test
    fun addDailySaleUpdatesLedgerAndDashboard() {
        composeRule.onNodeWithTag("ledger_sales").performClick()
        composeRule.onNodeWithText("No sales yet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Add sale").performClick()

        composeRule.onNodeWithTag("sale_customer_field").performTextClearance()
        composeRule.onNodeWithTag("sale_customer_field").performTextInput("Counter sale")
        composeRule.onNodeWithTag("sale_amount_field").performTextInput("3500")
        composeRule.onNodeWithTag("sale_sold_by_field").performTextInput("Owner")
        composeRule.onNodeWithContentDescription("Save sale").performClick()

        composeRule.onNodeWithText("Counter sale").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("₹3,500.00").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun cashflowDashboardUsesReceivedSalesMinusPaidExpenses() {
        scenario.onActivity { activity ->
            ExpenseRepository(activity).apply {
                saveExpenses(
                    listOf(
                        sampleExpense(
                            id = "paid-expense",
                            vendor = "Stock Supplier",
                            amount = 1_000.0,
                            date = "2026-06-30",
                            status = ExpenseStatus.PAID
                        )
                    )
                )
                saveSales(
                    listOf(
                        sampleSale("received-sale", "Counter sales", 3_500.0, SaleStatus.RECEIVED),
                        sampleSale("pending-sale", "Wholesale order", 500.0, SaleStatus.PENDING)
                    )
                )
            }
        }
        scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Operating cashflow").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("₹2,500.00").fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithTag("ledger_sales").performClick()
        composeRule.onNodeWithText("Counter sales").assertIsDisplayed()
        composeRule.onNodeWithText("Wholesale order").assertIsDisplayed()
    }

    @Test
    fun unsavedDraftSurvivesActivityRecreation() {
        openManualEntry()
        fillRequiredFields(vendor = "Rotation-safe vendor", amount = "999.50")

        scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Add expense").assertIsDisplayed()
        composeRule.onNodeWithTag("vendor_field")
            .assertTextContains("Rotation-safe vendor")
        composeRule.onNodeWithTag("amount_field").assertTextContains("999.50")
    }

    @Test
    fun createApprovePayAndDuplicateJourney() {
        openManualEntry()
        fillRequiredFields(
            vendor = "Repeat Vendor",
            amount = "1250",
            invoice = "INV-REPEAT"
        )
        composeRule.onNodeWithContentDescription("Save expense").performClick()

        composeRule.onNodeWithText("Repeat Vendor").assertIsDisplayed()
        composeRule.onNodeWithText("Approve").performClick()
        composeRule.onNodeWithText("Approved").assertIsDisplayed()
        composeRule.onNodeWithText("Mark paid").performClick()
        assertTrue(composeRule.onAllNodesWithText("Paid").fetchSemanticsNodes().isNotEmpty())

        composeRule.onNodeWithContentDescription("Expense actions").performClick()
        composeRule.onNodeWithText("Duplicate").performClick()

        composeRule.onNodeWithText("Add expense").assertIsDisplayed()
        composeRule.onNodeWithTag("vendor_field").assertTextContains("Repeat Vendor")
        composeRule.onNodeWithTag("amount_field").assertTextContains("1250")
    }

    @Test
    fun backButtonProtectsUnsavedChanges() {
        openManualEntry()
        composeRule.onNodeWithTag("vendor_field").performTextInput("Unsaved vendor")

        scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText("Discard changes?").assertIsDisplayed()
        composeRule.onNodeWithText("Keep editing").performClick()
        composeRule.onNodeWithTag("vendor_field").assertTextContains("Unsaved vendor")
    }

    @Test
    fun duplicateInvoiceIsExplainedAndBlocked() {
        saveRecords(
            sampleExpense(
                id = "existing",
                vendor = "Existing Vendor",
                amount = 100.0,
                date = "2026-07-10",
                status = ExpenseStatus.PAID
            )
        )

        openManualEntry()
        fillRequiredFields(
            vendor = "Second Vendor",
            amount = "200",
            invoice = " inv-EXISTING "
        )

        composeRule.onNodeWithText("Already used by another expense").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Save expense").performClick()
        composeRule.onNodeWithText("This invoice number is already used by another expense.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Add expense").assertIsDisplayed()
    }

    @Test
    fun deleteCanBeCancelledAndThenConfirmed() {
        saveRecords(
            sampleExpense(
                id = "delete",
                vendor = "Delete Vendor",
                amount = 100.0,
                date = "2026-07-10",
                status = ExpenseStatus.DRAFT
            )
        )

        composeRule.onNodeWithContentDescription("Expense actions").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Delete expense").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Delete Vendor").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Expense actions").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("No expenses yet").assertIsDisplayed()
    }

    @Test
    fun searchFilterAndSortOperateOnPersistedRecords() {
        scenario.onActivity { activity ->
            ExpenseRepository(activity).saveExpenses(
                listOf(
                    sampleExpense(
                        id = "acme",
                        vendor = "Acme Office",
                        amount = 500.0,
                        date = "2026-07-10",
                        status = ExpenseStatus.FOR_REVIEW
                    ),
                    sampleExpense(
                        id = "beta",
                        vendor = "Beta Travel",
                        amount = 2_000.0,
                        date = "2026-07-01",
                        status = ExpenseStatus.PAID
                    )
                )
            )
        }
        scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("expense_search").performTextInput("Acme")
        composeRule.onNodeWithText("Acme Office").assertIsDisplayed()
        composeRule.onNodeWithText("Beta Travel").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Clear search").performClick()

        composeRule.onNodeWithTag("filter_button_Status")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("filter_option_Status_PAID").performClick()
        composeRule.onNodeWithText("Beta Travel").assertIsDisplayed()
        composeRule.onNodeWithText("Acme Office").assertDoesNotExist()

        composeRule.onNodeWithText("Clear")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("filter_button_Sort")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("filter_option_Sort_AMOUNT_HIGH").performClick()

        val betaTop = composeRule.onNodeWithText("Beta Travel")
            .fetchSemanticsNode().boundsInRoot.top
        val acmeTop = composeRule.onNodeWithText("Acme Office")
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue(betaTop < acmeTop)
    }

    private fun fillRequiredFields(
        vendor: String,
        amount: String,
        invoice: String = ""
    ) {
        composeRule.onNodeWithTag("vendor_field").performTextInput(vendor)
        composeRule.onNodeWithTag("amount_field").performTextInput(amount)
        composeRule.onNodeWithTag("submitted_by_field").performTextInput("Accountant")
        if (invoice.isNotEmpty()) {
            composeRule.onNodeWithTag("invoice_field").performTextInput(invoice)
        }
    }

    private fun openManualEntry() {
        composeRule.onNodeWithContentDescription("Add expense").performClick()
        composeRule.onNodeWithText("Enter manually").performClick()
    }

    private fun saveRecords(vararg expenses: Expense) {
        scenario.onActivity {
            ExpenseRepository(it).saveExpenses(expenses.toList())
        }
        scenario.recreate()
        composeRule.waitForIdle()
    }

    private fun sampleExpense(
        id: String,
        vendor: String,
        amount: Double,
        date: String,
        status: ExpenseStatus
    ) = Expense(
        id = id,
        vendor = vendor,
        amount = amount,
        category = ExpenseCategory.OFFICE,
        paymentMethod = PaymentMethod.CARD,
        date = date,
        status = status,
        submittedBy = "Accountant",
        invoiceNumber = "INV-$id",
        attachmentUri = null,
        attachmentName = null,
        notes = "",
        updatedAt = 1L
    )

    private fun sampleSale(
        id: String,
        customer: String,
        amount: Double,
        status: SaleStatus
    ) = Sale(
        id = id,
        customer = customer,
        amount = amount,
        channel = if (status == SaleStatus.PENDING) SalesChannel.WHOLESALE else SalesChannel.STORE,
        paymentMethod = PaymentMethod.UPI,
        date = "2026-07-13",
        status = status,
        reference = "REF-$id",
        soldBy = "Owner",
        quantity = if (status == SaleStatus.PENDING) 10 else 4,
        taxAmount = null,
        discountAmount = null,
        notes = ""
    )
}
