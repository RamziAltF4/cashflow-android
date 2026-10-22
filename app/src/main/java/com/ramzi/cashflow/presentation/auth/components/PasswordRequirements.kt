package com.ramzi.cashflow.presentation.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.presentation.theme.LocalAccent

/**
 * Checklist syarat password — live centang.
 *
 * 4 syarat:
 *  1. Minimal 8 karakter
 *  2. Ada huruf besar (A-Z)
 *  3. Ada angka (0-9)
 *  4. Ada simbol (!@#$%)
 */
@Composable
fun PasswordRequirements(
    modifier: Modifier = Modifier,
    minLength: Boolean,
    hasUpper: Boolean,
    hasDigit: Boolean,
    hasSymbol: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
    ) {
        RequirementRow("Minimal 8 karakter", minLength)
        RequirementRow("Ada huruf besar (A-Z)", hasUpper)
        RequirementRow("Ada angka (0-9)", hasDigit)
        RequirementRow("Ada simbol (!@#\$%)", hasSymbol)
    }
}

@Composable
private fun RequirementRow(label: String, ok: Boolean) {
    val accent = LocalAccent.current
    val doneColor = Color(0xFF10B981)

    val dotColor by animateColorAsState(
        targetValue = if (ok) doneColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "reqDot"
    )
    val textColor by animateColorAsState(
        targetValue = if (ok) doneColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "reqText"
    )
    val scale by animateFloatAsState(
        targetValue = if (ok) 1f else 0.85f,
        animationSpec = tween(200),
        label = "reqScale"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(dotColor),
            contentAlignment = Alignment.Center
        ) {
            if (ok) {
                Text(
                    text = "✓",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (ok) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Strength meter — 4 bar horizontal.
 * Warna berubah sesuai jumlah syarat terpenuhi.
 */
@Composable
fun PasswordStrengthMeter(
    modifier: Modifier = Modifier,
    strength: Float
) {
    val color = when {
        strength < 0.25f -> Color(0xFFF43F5E)   // rose-500
        strength < 0.5f -> Color(0xFFF97316)     // orange-500
        strength < 0.75f -> Color(0xFFF59E0B)   // amber-500
        strength < 1f -> Color(0xFF10B981)      // emerald-500
        else -> Color(0xFF059669)                // emerald-600
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
    ) {
        repeat(4) { i ->
            val active = (i + 1) / 4f <= strength
            val animatedColor by animateColorAsState(
                targetValue = if (active) color else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200),
                label = "barColor$i"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(animatedColor)
            )
        }
    }
}