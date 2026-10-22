package com.ramzi.cashflow.presentation.dashboard.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.util.CurrencyFormatter

enum class StatTone { INCOME, EXPENSE }

@Composable
fun MiniStat(
    modifier: Modifier = Modifier,
    title: String,
    value: Long,
    tone: StatTone,
    currencyCode: String,
    hidden: Boolean = false
) {
    val accentColor = when (tone) {
        StatTone.INCOME -> Color(0xFF10B981)
        StatTone.EXPENSE -> Color(0xFFF43F5E)
    }
    val bgColor = accentColor.copy(alpha = 0.12f)
    val arrow = when (tone) {
        StatTone.INCOME -> "↓"
        StatTone.EXPENSE -> "↑"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = arrow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (hidden) "${CurrencyFormatter.symbolOf(currencyCode)} ••••••"
                else CurrencyFormatter.format(value, currencyCode),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = (-0.5).sp
            )
        }
    }
}