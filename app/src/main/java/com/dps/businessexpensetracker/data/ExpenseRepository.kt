package com.dps.businessexpensetracker.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray

class ExpenseRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "business_expenses",
        Context.MODE_PRIVATE
    )

    fun loadExpenses(): List<Expense> {
        val primary = preferences.getString(KEY_EXPENSES, null)
        val backup = preferences.getString(KEY_EXPENSES_BACKUP, null)
        return parseExpenses(primary) ?: parseExpenses(backup) ?: emptyList()
    }

    fun saveExpenses(expenses: List<Expense>) {
        val array = JSONArray()
        expenses.forEach { array.put(it.toJson()) }
        val newValue = array.toString()
        val currentValue = preferences.getString(KEY_EXPENSES, null)
        preferences.edit {
            when {
                parseExpenses(currentValue) != null -> {
                    putString(KEY_EXPENSES_BACKUP, currentValue)
                }
                !preferences.contains(KEY_EXPENSES_BACKUP) -> {
                    putString(KEY_EXPENSES_BACKUP, newValue)
                }
            }
            putString(KEY_EXPENSES, newValue)
        }
    }

    fun loadSales(): List<Sale> {
        val primary = preferences.getString(KEY_SALES, null)
        val backup = preferences.getString(KEY_SALES_BACKUP, null)
        return parseSales(primary) ?: parseSales(backup) ?: emptyList()
    }

    fun saveSales(sales: List<Sale>) {
        val array = JSONArray()
        sales.forEach { array.put(it.toJson()) }
        val newValue = array.toString()
        val currentValue = preferences.getString(KEY_SALES, null)
        preferences.edit {
            when {
                parseSales(currentValue) != null -> putString(KEY_SALES_BACKUP, currentValue)
                !preferences.contains(KEY_SALES_BACKUP) -> putString(KEY_SALES_BACKUP, newValue)
            }
            putString(KEY_SALES, newValue)
        }
    }

    private fun parseExpenses(raw: String?): List<Expense>? {
        if (raw == null) return null
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    add(Expense.fromJson(array.getJSONObject(index)))
                }
            }
        }.getOrNull()
    }

    private fun parseSales(raw: String?): List<Sale>? {
        if (raw == null) return null
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    add(Sale.fromJson(array.getJSONObject(index)))
                }
            }
        }.getOrNull()
    }

    private companion object {
        const val KEY_EXPENSES = "expenses_json"
        const val KEY_EXPENSES_BACKUP = "expenses_json_backup"
        const val KEY_SALES = "sales_json"
        const val KEY_SALES_BACKUP = "sales_json_backup"
    }
}
