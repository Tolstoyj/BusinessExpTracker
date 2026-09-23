package com.dps.businessexpensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class QuickStartProgress(
    val profileDone: Boolean,
    val firstSaleDone: Boolean,
    val firstExpenseDone: Boolean,
    val backupDone: Boolean
) {
    val completedCount: Int = listOf(profileDone, firstSaleDone, firstExpenseDone, backupDone).count { it }
    val isComplete: Boolean = completedCount == 4
}

fun quickStartProgress(
    businessName: String,
    currencyConfirmed: Boolean,
    saleCount: Int,
    expenseCount: Int,
    backupConfigured: Boolean
): QuickStartProgress = QuickStartProgress(
    profileDone = currencyConfirmed || businessName.trim().isNotEmpty(),
    firstSaleDone = saleCount > 0,
    firstExpenseDone = expenseCount > 0,
    backupDone = backupConfigured
)

/**
 * Installations that finished language onboarding before currency confirmation
 * was stored already accepted the INR default. A missing flag must not send
 * them through setup again. An explicit stored value always wins.
 */
fun currencyConfirmationForExistingInstall(
    storedConfirmation: Boolean?,
    languageOnboardingAlreadyComplete: Boolean
): Boolean = storedConfirmation ?: languageOnboardingAlreadyComplete

fun confirmedOnboardingProfile(businessName: String, currency: BusinessCurrency): BusinessProfile =
    BusinessProfile(businessName = businessName.trim(), currency = currency)

@Composable
fun QuickStartChecklist(
    progress: QuickStartProgress,
    onOpenProfile: () -> Unit,
    onAddSale: () -> Unit,
    onAddExpense: () -> Unit,
    onChooseBackup: () -> Unit
) {
    if (progress.isComplete) return
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(tr("Quick start"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${progress.completedCount}/4",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            QuickStartRow(tr("Set business profile"), progress.profileDone, onOpenProfile)
            QuickStartRow(tr("Add your first sale"), progress.firstSaleDone, onAddSale)
            QuickStartRow(tr("Add your first expense"), progress.firstExpenseDone, onAddExpense)
            QuickStartRow(tr("Choose a backup file"), progress.backupDone, onChooseBackup)
        }
    }
}

@Composable
private fun QuickStartRow(label: String, done: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .then(if (done) Modifier else Modifier.clickable(onClick = onClick))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (done) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (done) tr("Done") else tr("Not done"),
            modifier = Modifier.size(20.dp),
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
