package com.dps.businessexpensetracker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dps.businessexpensetracker.R

@Composable
fun LanguageOnboarding(
    selectedLanguage: AppLanguage,
    initialProfile: BusinessProfile = BusinessProfile(),
    onLanguageSelected: (AppLanguage) -> Unit,
    onComplete: (BusinessProfile) -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    var businessName by remember { mutableStateOf(initialProfile.businessName) }
    var currency by remember { mutableStateOf(initialProfile.currency) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_art),
                contentDescription = null,
                modifier = Modifier.size(82.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (page) {
                    0 -> tr("Welcome")
                    1 -> tr("Ready to begin?")
                    else -> tr("Your business")
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = when (page) {
                    0 -> tr("Choose your language")
                    1 -> tr("Track business with confidence")
                    else -> tr("Confirm your home currency")
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))

            if (page == 0) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppLanguage.entries) { language ->
                        val selected = language == selectedLanguage
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().clickable { onLanguageSelected(language) },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Language, contentDescription = null)
                                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                    Text(language.nativeName, fontWeight = FontWeight.SemiBold)
                                    if (language.englishName != language.nativeName) {
                                        Text(language.englishName, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (selected) Icon(Icons.Outlined.CheckCircle, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Button(onClick = { page = 1 }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(tr("Continue"))
                }
            } else if (page == 1) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Outlined.Lock, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.padding(start = 16.dp)) {
                                Text(tr("Your data stays on this device"), fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text(tr("No account, no ads and no automatic cloud upload."),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(tr("You can change language anytime from the menu."),
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { page = 0 }) { Text(tr("Back")) }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { page = 2 }) { Text(tr("Continue")) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(tr("Business name (optional)")) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            tr("Business name is optional. You can change it later in Settings."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            tr("Currency changes how amounts are displayed; it does not convert saved values."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(BusinessCurrency.entries) { option ->
                        val selected = option == currency
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().clickable { currency = option },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Payments, contentDescription = null)
                                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                    Text(option.code, fontWeight = FontWeight.SemiBold)
                                    Text(option.label, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (selected) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = tr("Home currency"),
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { page = 1 }) { Text(tr("Back")) }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { onComplete(confirmedOnboardingProfile(businessName, currency)) }) {
                        Text(tr("Confirm and start"))
                    }
                }
            }
        }
    }
}

@Composable
fun LanguagePickerDialog(
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Choose your language")) },
        text = {
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                items(AppLanguage.entries) { language ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(language) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(language.nativeName, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        if (language == selectedLanguage) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Cancel")) } }
    )
}
