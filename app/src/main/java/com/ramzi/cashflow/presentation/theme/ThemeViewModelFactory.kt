package com.ramzi.cashflow.presentation.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ramzi.cashflow.data.local.AccentPreference
import com.ramzi.cashflow.data.local.ThemePreference

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(
                themePreference = ThemePreference(context.applicationContext),
                accentPreference = AccentPreference(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}