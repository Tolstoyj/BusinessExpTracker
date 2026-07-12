package com.dps.businessexpensetracker.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray

data class DataMigrationResult(
    val migrated: Boolean,
    val fromVersion: Int,
    val toVersion: Int,
    val preservedExpenseCount: Int
)

/**
 * Performs additive, idempotent migrations before repositories read app data.
 *
 * Version 1 stored only expenses in the `business_expenses` preferences file.
 * Version 2 adds the sales ledger without rewriting or removing any v1 keys.
 * A validated pre-migration expense snapshot is retained so a later failure can
 * still recover the exact register that existed before the app update.
 */
object AppDataMigration {
    const val CURRENT_SCHEMA_VERSION = 2

    fun migrate(context: Context): DataMigrationResult {
        val preferences = context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        val storedVersion = preferences.getInt(KEY_SCHEMA_VERSION, 0)
        val hasExistingLedger = preferences.contains(KEY_EXPENSES) ||
            preferences.contains(KEY_EXPENSES_BACKUP) || preferences.contains(KEY_SALES)
        val inferredVersion = when {
            storedVersion > 0 -> storedVersion
            hasExistingLedger -> 1
            else -> CURRENT_SCHEMA_VERSION
        }

        if (inferredVersion >= CURRENT_SCHEMA_VERSION) {
            if (storedVersion == 0) {
                preferences.edit { putInt(KEY_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION) }
            }
            return DataMigrationResult(
                migrated = false,
                fromVersion = inferredVersion,
                toVersion = CURRENT_SCHEMA_VERSION,
                preservedExpenseCount = validatedExpenseCount(
                    preferences.getString(KEY_EXPENSES, null)
                ) ?: 0
            )
        }

        val primary = preferences.getString(KEY_EXPENSES, null)
        val previousSnapshot = preferences.getString(KEY_EXPENSES_BACKUP, null)
        val validatedSource = primary.takeIf { validatedExpenseCount(it) != null }
            ?: previousSnapshot.takeIf { validatedExpenseCount(it) != null }
        val preservedCount = validatedExpenseCount(validatedSource) ?: 0

        preferences.edit(commit = true) {
            if (!preferences.contains(KEY_PRE_MIGRATION_EXPENSES) && validatedSource != null) {
                putString(KEY_PRE_MIGRATION_EXPENSES, validatedSource)
            }
            putInt(KEY_MIGRATED_FROM_VERSION, inferredVersion)
            putInt(KEY_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION)
        }

        return DataMigrationResult(
            migrated = true,
            fromVersion = inferredVersion,
            toVersion = CURRENT_SCHEMA_VERSION,
            preservedExpenseCount = preservedCount
        )
    }

    private fun validatedExpenseCount(raw: String?): Int? {
        if (raw == null) return null
        return runCatching {
            val array = JSONArray(raw)
            for (index in 0 until array.length()) {
                Expense.fromJson(array.getJSONObject(index))
            }
            array.length()
        }.getOrNull()
    }

    private const val PREFERENCES_NAME = "business_expenses"
    private const val KEY_SCHEMA_VERSION = "data_schema_version"
    private const val KEY_MIGRATED_FROM_VERSION = "migrated_from_schema_version"
    private const val KEY_PRE_MIGRATION_EXPENSES = "pre_migration_expenses_v1"
    private const val KEY_EXPENSES = "expenses_json"
    private const val KEY_EXPENSES_BACKUP = "expenses_json_backup"
    private const val KEY_SALES = "sales_json"
}
