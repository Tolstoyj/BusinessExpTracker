package com.dps.businessexpensetracker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.dps.businessexpensetracker.data.InvoiceExtractionResult
import com.dps.businessexpensetracker.data.InvoiceScanProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class InvoiceScanProcessorTest {
    @Test
    fun bundledOcrProcessesInvoiceImageEndToEnd() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val imageFile = File(context.cacheDir, "generated-invoice.png")
        imageFile.outputStream().use {
            createInvoiceBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        val result = AtomicReference<InvoiceExtractionResult>()
        val error = AtomicReference<Throwable>()
        val latch = CountDownLatch(1)

        InvoiceScanProcessor.process(
            context = context,
            sourceUri = Uri.fromFile(imageFile),
            onSuccess = {
                result.set(it)
                latch.countDown()
            },
            onFailure = {
                error.set(it)
                latch.countDown()
            }
        )

        assertTrue("OCR timed out", latch.await(20, TimeUnit.SECONDS))
        assertEquals(null, error.get())
        val extraction = result.get()
        assertNotNull(extraction)
        assertEquals("ACME PRIVATE LIMITED", extraction.draft.vendor)
        assertEquals("INV-2026-1042", extraction.draft.invoiceNumber)
        assertEquals("2026-07-11", extraction.draft.date)
        assertEquals("1180", extraction.draft.amount)
        assertEquals("27ABCDE1234F1Z5", extraction.draft.supplierGstin)

        extraction.draft.attachmentUri?.let {
            InvoiceScanProcessor.deleteManagedScan(context, Uri.parse(it))
        }
        imageFile.delete()
    }

    private fun createInvoiceBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(1200, 1600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 52f
        }
        listOf(
            "ACME PRIVATE LIMITED",
            "TAX INVOICE",
            "GSTIN: 27ABCDE1234F1Z5",
            "Invoice No: INV-2026-1042",
            "Invoice Date: 11/07/2026",
            "Subtotal INR 1000.00",
            "CGST INR 90.00",
            "SGST INR 90.00",
            "Grand Total INR 1180.00"
        ).forEachIndexed { index, line ->
            canvas.drawText(line, 70f, 120f + index * 100f, paint)
        }
        return bitmap
    }
}
