package com.dps.businessexpensetracker.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseModelsTest {
    @Test
    fun duplicateInvoiceComparisonIgnoresCaseAndWhitespace() {
        assertTrue(
            isDuplicateInvoiceNumber(
                candidate = " inv-1001 ",
                existingNumbers = listOf("INV-1001", "INV-1002")
            )
        )
    }

    @Test
    fun blankInvoiceNumberIsNeverDuplicate() {
        assertFalse(isDuplicateInvoiceNumber("  ", listOf("", "INV-1001")))
    }

    @Test
    fun differentInvoiceNumberIsNotDuplicate() {
        assertFalse(isDuplicateInvoiceNumber("INV-2001", listOf("INV-1001")))
    }

    @Test
    fun nonFiniteAmountsAreRejected() {
        assertTrue(validateExpenseDraft(validDraft(amount = "NaN")) != null)
        assertTrue(validateExpenseDraft(validDraft(amount = "Infinity")) != null)
    }

    @Test
    fun draftStateRoundTripPreservesUnsavedFields() {
        val original = validDraft(amount = "1,234.50").copy(
            id = "draft-id",
            invoiceNumber = "INV-42",
            notes = "Line one\nLine two",
            attachmentUri = "content://receipt/42",
            attachmentName = "receipt.pdf"
        )

        assertTrue(expenseDraftFromState(original.toStateString()) == original)
    }

    private fun validDraft(amount: String) = ExpenseDraft(
        vendor = "Vendor",
        amount = amount,
        submittedBy = "Accountant"
    )
}
