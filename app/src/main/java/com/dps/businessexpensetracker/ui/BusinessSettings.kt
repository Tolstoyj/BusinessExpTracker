package com.dps.businessexpensetracker.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dps.businessexpensetracker.data.currencyFormatter
import java.text.NumberFormat

enum class BusinessCurrency(val code: String, val label: String) {
    INR("INR", "Indian Rupee"), USD("USD", "US Dollar"), EUR("EUR", "Euro"),
    GBP("GBP", "British Pound"), TRY("TRY", "Turkish Lira"), AED("AED", "UAE Dirham"),
    SAR("SAR", "Saudi Riyal"), CNY("CNY", "Chinese Yuan"), JPY("JPY", "Japanese Yen"),
    BRL("BRL", "Brazilian Real");

    companion object {
        fun fromCode(code: String?): BusinessCurrency = entries.firstOrNull { it.code == code } ?: INR
    }
}

data class BusinessProfile(
    val businessName: String = "",
    val currency: BusinessCurrency = BusinessCurrency.INR
)

val LocalBusinessProfile = staticCompositionLocalOf { BusinessProfile() }

fun businessProfileFromStored(businessName: String?, currencyCode: String?): BusinessProfile =
    BusinessProfile(
        businessName = businessName.orEmpty(),
        currency = BusinessCurrency.fromCode(currencyCode)
    )

fun BusinessProfile.storedBusinessName(): String = businessName.trim()

fun BusinessProfile.storedCurrencyCode(): String = currency.code

object BusinessProfilePrefs {
    private const val PREFS = "business_profile_preferences"
    private const val CURRENCY_CONFIRMED = "currency_confirmed"

    fun load(context: Context): BusinessProfile {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return businessProfileFromStored(
            prefs.getString("business_name", ""),
            prefs.getString("currency", BusinessCurrency.INR.code)
        )
    }

    fun save(context: Context, profile: BusinessProfile) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("business_name", profile.storedBusinessName())
            .putString("currency", profile.storedCurrencyCode())
            .apply()
    }

    fun isCurrencyConfirmed(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = if (prefs.contains(CURRENCY_CONFIRMED)) {
            prefs.getBoolean(CURRENCY_CONFIRMED, false)
        } else {
            null
        }
        return currencyConfirmationForExistingInstall(
            stored,
            AppLanguagePrefs.isOnboardingComplete(context)
        )
    }

    fun confirmHomeCurrency(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(CURRENCY_CONFIRMED, true)
            .apply()
    }
}

fun businessCurrencyFormatter(currency: BusinessCurrency): NumberFormat =
    currencyFormatter(currency.code)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    language: AppLanguage,
    profile: BusinessProfile,
    automaticBackupEnabled: Boolean,
    onBack: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onProfileChange: (BusinessProfile) -> Unit,
    onReplayTour: () -> Unit
) {
    var businessName by remember(profile.businessName) { mutableStateOf(profile.businessName) }
    var showLanguages by remember { mutableStateOf(false) }
    var showCurrencies by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tr("Settings")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = tr("Back"))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { SectionTitle(tr("Business profile")) }
            item {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = {
                        businessName = it
                        onProfileChange(profile.copy(businessName = it))
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(tr("Business name (optional)")) },
                    leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) },
                    singleLine = true
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(tr("Home currency")) },
                    supportingContent = { Text("${profile.currency.code} · ${profile.currency.label}") },
                    leadingContent = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                    modifier = Modifier.clickable { showCurrencies = true }
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { SectionTitle(tr("App preferences")) }
            item {
                ListItem(
                    headlineContent = { Text(tr("Language")) },
                    supportingContent = { Text(language.nativeName) },
                    leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
                    modifier = Modifier.clickable { showLanguages = true }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(tr("Replay app tour")) },
                    supportingContent = { Text(tr("Review the dashboard and key actions")) },
                    modifier = Modifier.clickable(onClick = onReplayTour)
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { SectionTitle(tr("Data and privacy")) }
            item {
                ListItem(
                    headlineContent = { Text(tr("Local-first storage")) },
                    supportingContent = { Text(tr("Expenses, sales and scans stay on this device.")) },
                    leadingContent = { Icon(Icons.Outlined.Lock, contentDescription = null) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(tr("Automatic backup")) },
                    supportingContent = {
                        Text(if (automaticBackupEnabled) tr("On") else tr("Off — choose a backup file from the dashboard menu"))
                    }
                )
            }
            item {
                Text(
                    tr("Currency changes how amounts are displayed; it does not convert saved values."),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showLanguages) {
        LanguagePickerDialog(
            selectedLanguage = language,
            onDismiss = { showLanguages = false },
            onSelect = {
                showLanguages = false
                onLanguageChange(it)
            }
        )
    }
    if (showCurrencies) {
        AlertDialog(
            onDismissRequest = { showCurrencies = false },
            title = { Text(tr("Home currency")) },
            text = {
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(BusinessCurrency.entries) { currency ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                showCurrencies = false
                                onProfileChange(
                                    profile.copy(
                                        businessName = businessName,
                                        currency = currency
                                    )
                                )
                            }.padding(vertical = 12.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(currency.code, fontWeight = FontWeight.SemiBold)
                                Text(currency.label, style = MaterialTheme.typography.bodySmall)
                            }
                            if (currency == profile.currency) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencies = false }) { Text(tr("Cancel")) }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}
