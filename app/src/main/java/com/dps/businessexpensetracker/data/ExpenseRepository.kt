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
        val raw = preferences.getString(KEY_EXPENSES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    add(Expense.fromJson(array.getJSONObject(index)))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveExpenses(expenses: List<Expense>) {
        val array = JSONArray()
        expenses.forEach { array.put(it.toJson()) }
        preferences.edit { putString(KEY_EXPENSES, array.toString()) }
    }

    private companion object {
        const val KEY_EXPENSES = "expenses_json"
    }
}
