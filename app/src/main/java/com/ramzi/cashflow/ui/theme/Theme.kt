package com.ramzi.cashflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.ramzi.cashflow.data.local.ThemePreference
import com.ramzi.cashflow.domain.model.AccentColors
import com.ramzi.cashflow.presentation.theme.DashboardTheme

@Composable
fun CashflowTheme(
    themeMode: String = ThemePreference.MODE_SYSTEM,
    accentId: String = AccentColors.DEFAULT.id,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemePreference.MODE_DARK -> true
        ThemePreference.MODE_LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    val accent = AccentColors.byId(accentId)

    DashboardTheme(
        accent = accent,
        darkTheme = darkTheme,
        content = content
    )
}