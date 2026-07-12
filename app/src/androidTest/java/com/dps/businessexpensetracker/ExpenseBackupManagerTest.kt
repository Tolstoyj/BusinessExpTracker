package com.dps.businessexpensetracker

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.dps.businessexpensetracker.data.BackupRestoreResult
import com.dps.businessexpensetracker.data.Expense
import com.dps.businessexpensetracker.data.ExpenseBackupManager
import com.dps.businessexpensetracker.data.ExpenseCategory
import com.dps.businessexpensetracker.data.ExpenseStatus
import com.dps.businessexpensetracker.data.InvoiceScanProcessor
import com.dps.businessexpensetracker.data.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ExpenseBackupManagerTest {
    @Test
    fun backupRoundTripRestoresExpenseAndAttachment() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val attachment = File(context.filesDir, "scanned_invoices/backup-test.txt").apply {
            parentFile?.mkdirs()
            writeText("invoice attachment")
        }
        val attachmentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            attachment
        )
        val expense = sampleExpense(attachmentUri.toString())
        val backupFile = File(context.cacheDir, "round-trip.betbackup").apply { delete() }
        val backupUri = Uri.fromFile(backupFile)

        val writeError = AtomicReference<Throwable>()
        val writeLatch = CountDownLatch(1)
        ExpenseBackupManager.writeBackup(
            context,
            backupUri,
            listOf(expense),
            onSuccess = {
                assertEquals(1, it.expenseCount)
                assertEquals(1, it.attachmentCount)
                writeLatch.countDown()
            },
            onFailure = {
                writeError.set(it)
                writeLatch.countDown()
            }
        )
        assertTrue(writeLatch.await(10, TimeUnit.SECONDS))
        assertEquals(null, writeError.get())
        InvoiceScanProcessor.deleteManagedScan(context, attachmentUri)

        val restored = AtomicReference<BackupRestoreResult>()
        val restoreError = AtomicReference<Throwable>()
        val restoreLatch = CountDownLatch(1)
        ExpenseBackupManager.restoreBackup(
            context,
            backupUri,
            onSuccess = {
                restored.set(it)
                restoreLatch.countDown()
            },
            onFailure = {
                restoreError.set(it)
                restoreLatch.countDown()
            }
        )
        assertTrue(restoreLatch.await(10, TimeUnit.SECONDS))
        assertEquals(null, restoreError.get())
        val result = restored.get()
        assertNotNull(result)
        assertEquals(1, result.expenses.size)
        assertEquals("Portable Vendor", result.expenses.single().vendor)
        assertEquals("27ABCDE1234F1Z5", result.expenses.single().supplierGstin)
        val restoredUri = Uri.parse(result.expenses.single().attachmentUri)
        val restoredText = context.contentResolver.openInputStream(restoredUri)
            ?.bufferedReader()
            ?.use { it.readText() }
        assertEquals("invoice attachment", restoredText)

        ExpenseBackupManager.discardRestore(result)
        backupFile.delete()
    }

    @Test
    fun invalidArchiveIsRejectedWithoutProducingRecords() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val invalidFile = File(context.cacheDir, "invalid.betbackup").apply {
            writeText("not a backup")
        }
        val error = AtomicReference<Throwable>()
        val latch = CountDownLatch(1)

        ExpenseBackupManager.restoreBackup(
            context,
            Uri.fromFile(invalidFile),
            onSuccess = { latch.countDown() },
            onFailure = {
                error.set(it)
                latch.countDown()
            }
        )

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        assertNotNull(error.get())
        invalidFile.delete()
    }

    private fun sampleExpense(attachmentUri: String) = Expense(
        id = "portable-expense",
        vendor = "Portable Vendor",
        amount = 1180.0,
        category = ExpenseCategory.OFFICE,
        paymentMethod = PaymentMethod.CARD,
        date = "2026-07-11",
        status = ExpenseStatus.PAID,
        submittedBy = "Accountant",
        invoiceNumber = "INV-PORTABLE",
        attachmentUri = attachmentUri,
        attachmentName = "invoice.txt",
        notes = "Portable backup test",
        supplierGstin = "27ABCDE1234F1Z5",
        taxAmount = 180.0
    )
}
