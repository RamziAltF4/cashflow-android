package com.ramzi.cashflow.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.ramzi.cashflow.domain.model.AccentColor

val LocalAccent = compositionLocalOf<AccentColor> {
    error("LocalAccent belum di-provide")
}

@Composable
fun DashboardTheme(
    accent: AccentColor,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme(
        primary = accent.light,
        onPrimary = Color.White,
        primaryContainer = accent.soft,
        onPrimaryContainer = accent.text,
        surface = Color(0xFF0D1424),
        onSurface = Color(0xFFF1F5F9),
        surfaceVariant = Color(0xFF131A2E),
        onSurfaceVariant = Color(0xFF94A3B8),
        background = Color(0xFF070B17),
        onBackground = Color(0xFFF1F5F9),
        outline = Color(0xFF1A2236),
        outlineVariant = Color(0xFF2A3450),
        error = Color(0xFFF43F5E),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA)
    ) else lightColorScheme(
        primary = accent.base,
        onPrimary = Color.White,
        primaryContainer = accent.soft,
        onPrimaryContainer = accent.text,
        surface = Color.White,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF8FAFC),
        onSurfaceVariant = Color(0xFF64748B),
        background = Color(0xFFF6F7FB),
        onBackground = Color(0xFF0F172A),
        outline = Color(0xFFE5E9F0),
        outlineVariant = Color(0xFFCBD5E1),
        error = Color(0xFFE11D48),
        errorContainer = Color(0xFFFFE4E6),
        onErrorContainer = Color(0xFF9F1239)
    )

    CompositionLocalProvider(LocalAccent provides accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}