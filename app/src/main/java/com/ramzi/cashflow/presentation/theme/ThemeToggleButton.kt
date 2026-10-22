package com.ramzi.cashflow.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tombol toggle tema — persegi bulat dengan ikon.
 * Dipakai di pojok kanan atas Login & Register.
 */
@Composable
fun ThemeToggleButton(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = if (isDark) "Ubah ke Light Mode" else "Ubah ke Dark Mode",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
