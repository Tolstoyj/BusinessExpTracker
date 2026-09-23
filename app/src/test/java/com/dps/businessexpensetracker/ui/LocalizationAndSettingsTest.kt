package com.dps.businessexpensetracker.ui

import com.dps.businessexpensetracker.data.currencyFormatter
import com.dps.businessexpensetracker.data.inrCurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationAndSettingsTest {
    @Test
    fun languageTagsFallBackToEnglishAndKeepRtlForArabic() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("zh"))
        assertEquals(AppLanguage.CHINESE, AppLanguage.fromTag("zh-CN"))
        assertEquals(AppLanguage.SPANISH, storedLanguageSettings("es", false).language)
        assertTrue(AppLanguage.ARABIC.isRtl)
        assertFalse(AppLanguage.ENGLISH.isRtl)
    }

    @Test
    fun storedLanguageSettingsPreserveOnboardingAndLanguageChanges() {
        val completed = storedLanguageSettings("en", false)
            .completingOnboarding(AppLanguage.SPANISH)
        assertEquals(AppLanguage.SPANISH, completed.language)
        assertEquals("es", completed.language.tag)
        assertTrue(completed.onboardingComplete)

        val changed = completed.withLanguage(AppLanguage.GERMAN)
        assertEquals(AppLanguage.GERMAN, changed.language)
        assertTrue(changed.onboardingComplete)
    }

    @Test
    fun translateUsesPrimaryAndExtendedCatalogsAndKeepsMissingEnglish() {
        assertEquals("Expenses", translate(AppLanguage.ENGLISH, "Expenses"))
        assertEquals("Gastos", translate(AppLanguage.SPANISH, "Expenses"))
        assertEquals("Einstellungen", translate(AppLanguage.GERMAN, "Settings"))
        assertEquals("Hauptwährung", translate(AppLanguage.GERMAN, "Home currency"))
        assertEquals("Not translated", translate(AppLanguage.FRENCH, "Not translated"))
    }

    @Test
    fun storedBusinessProfileDefaultsToInrAndTrimsTheSavedName() {
        assertEquals(BusinessCurrency.INR, BusinessCurrency.fromCode(null))
        assertEquals(BusinessCurrency.INR, BusinessCurrency.fromCode("usd"))
        assertEquals(BusinessCurrency.TRY, BusinessCurrency.fromCode("TRY"))

        val loaded = businessProfileFromStored(null, "GBP")
        assertEquals("", loaded.businessName)
        assertEquals(BusinessCurrency.GBP, loaded.currency)

        val profile = BusinessProfile(businessName = "  North Shop  ", currency = BusinessCurrency.EUR)
        assertEquals("North Shop", profile.storedBusinessName())
        assertEquals("EUR", profile.storedCurrencyCode())
        assertEquals(
            currencyFormatter("EUR").format(10.0),
            businessCurrencyFormatter(profile.currency).format(10.0)
        )
        assertEquals(
            inrCurrencyFormatter().format(10.0),
            businessCurrencyFormatter(BusinessCurrency.INR).format(10.0)
        )
    }

    @Test
    fun quickStartProgressCompletesOneStepAtATimeAndHidesWhenFinished() {
        val empty = quickStartProgress("", currencyConfirmed = false, saleCount = 0, expenseCount = 0, backupConfigured = false)
        assertEquals(0, empty.completedCount)
        assertFalse(empty.isComplete)

        val namedOnly = quickStartProgress("  Shop  ", currencyConfirmed = false, saleCount = 0, expenseCount = 0, backupConfigured = false)
        assertTrue(namedOnly.profileDone)
        assertEquals(1, namedOnly.completedCount)

        val confirmedCurrency = quickStartProgress(" ", currencyConfirmed = true, saleCount = 0, expenseCount = 0, backupConfigured = false)
        assertTrue(confirmedCurrency.profileDone)
        assertFalse(confirmedCurrency.firstSaleDone)

        val partial = quickStartProgress("", currencyConfirmed = false, saleCount = 2, expenseCount = 1, backupConfigured = false)
        assertTrue(partial.firstSaleDone)
        assertTrue(partial.firstExpenseDone)
        assertFalse(partial.backupDone)
        assertEquals(2, partial.completedCount)

        val done = quickStartProgress("Shop", currencyConfirmed = true, saleCount = 1, expenseCount = 1, backupConfigured = true)
        assertEquals(4, done.completedCount)
        assertTrue(done.isComplete)
    }

    @Test
    fun existingInstallsKeepCurrencyConfirmationWithoutRepeatingSetup() {
        assertFalse(currencyConfirmationForExistingInstall(null, languageOnboardingAlreadyComplete = false))
        assertTrue(currencyConfirmationForExistingInstall(null, languageOnboardingAlreadyComplete = true))
        assertFalse(currencyConfirmationForExistingInstall(false, languageOnboardingAlreadyComplete = true))
        assertTrue(currencyConfirmationForExistingInstall(true, languageOnboardingAlreadyComplete = false))

        val confirmed = confirmedOnboardingProfile("  North Shop  ", BusinessCurrency.USD)
        assertEquals("North Shop", confirmed.businessName)
        assertEquals(BusinessCurrency.USD, confirmed.currency)
        assertEquals("", confirmedOnboardingProfile("   ", BusinessCurrency.INR).businessName)
    }

    @Test
    fun newUsabilityCopyIsTranslatedForGermanAndTurkishOnly() {
        assertEquals("Schnellstart", translate(AppLanguage.GERMAN, "Quick start"))
        assertEquals("Zorunlu", translate(AppLanguage.TURKISH, "Required"))
        assertEquals(
            "Eingegangene Verkäufe minus bezahlte Ausgaben. Das ist kein buchhalterischer Gewinn.",
            translate(AppLanguage.GERMAN, "Received sales minus paid expenses. Not accounting profit.")
        )
        assertEquals("Quick start", translate(AppLanguage.FRENCH, "Quick start"))
        assertEquals("Required", translate(AppLanguage.JAPANESE, "Required"))
    }
}
