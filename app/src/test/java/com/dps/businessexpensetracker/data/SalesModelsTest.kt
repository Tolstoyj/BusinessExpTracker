package com.dps.businessexpensetracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SalesModelsTest {
    @Test
    fun validDraftRoundTripsThroughSaleAndJson() {
        val draft = SaleDraft(
            customer = "Acme Customer",
            amount = "1,250.50",
            channel = SalesChannel.ONLINE,
            paymentMethod = PaymentMethod.UPI,
            date = "2026-07-13",
            status = SaleStatus.RECEIVED,
            reference = "ORD-100",
            soldBy = "Priya",
            quantity = "2",
            taxAmount = "190.75",
            discountAmount = "50",
            notes = "Website order"
        )

        assertNull(validateSaleDraft(draft))
        val restored = draft.toSale()

        assertEquals("Acme Customer", restored.customer)
        assertEquals(1250.50, restored.amount, 0.001)
        assertEquals(SalesChannel.ONLINE, restored.channel)
        assertEquals(2, restored.quantity)
        assertEquals(50.0, restored.discountAmount!!, 0.001)
    }

    @Test
    fun rejectsInvalidAmountsQuantityAndDate() {
        val valid = SaleDraft(amount = "100", soldBy = "Owner")

        assertTrue(validateSaleDraft(valid.copy(amount = "NaN"))!!.contains("sale amount"))
        assertTrue(validateSaleDraft(valid.copy(quantity = "0"))!!.contains("quantity"))
        assertTrue(validateSaleDraft(valid.copy(date = "13/07/2026"))!!.contains("YYYY-MM-DD"))
        assertTrue(validateSaleDraft(valid.copy(discountAmount = "101"))!!.contains("cannot exceed"))
    }

    @Test
    fun draftStateSurvivesRecreation() {
        val draft = SaleDraft(
            customer = "Saved draft",
            amount = "999.99",
            soldBy = "Owner",
            reference = "REF-1"
        )
        assertEquals(draft, saleDraftFromState(draft.toStateString()))
    }
}
