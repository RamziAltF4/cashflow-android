package com.ramzi.cashflow.presentation.dashboard.business.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.util.Formatting

/**
 * Card budget bulanan — menampilkan progress pengeluaran vs target.
 * Klik "Edit" untuk buka input.
 */
@Composable
fun BudgetCard(
    monthlyTarget: Long,
    currentExpense: Long,
    currentIncome: Long,
    onEditClick: () -> Unit
) {
    val hasTarget = monthlyTarget > 0
    val pct = if (hasTarget) {
        ((currentExpense.toFloat() / monthlyTarget.toFloat()) * 100f).coerceIn(0f, 999f)
    } else 0f

    val remaining = monthlyTarget - currentExpense
    val isOver = remaining < 0

    // Warna bar & aksen berdasarkan level
    val (barColor, labelColor, labelText) = when {
        !hasTarget -> Triple(
            Color(0xFF94A3B8),
            Color(0xFF64748B),
            "Belum ada target"
        )
        pct >= 100f -> Triple(
            Color(0xFFE11D48),
            Color(0xFFE11D48),
            "Terlampaui!"
        )
        pct >= 80f -> Triple(
            Color(0xFFF59E0B),
            Color(0xFFD97706),
            "Hampir penuh"
        )
        pct >= 50f -> Triple(
            Color(0xFFEAB308),
            Color(0xFFCA8A04),
            "Setengah jalan"
        )
        else -> Triple(
            Color(0xFF10B981),
            Color(0xFF059669),
            "Sehat"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // ============ HEADER ============
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Savings,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Budget Bulanan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = labelText,
                    fontSize = 11.sp,
                    color = labelColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onEditClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (hasTarget) "Edit" else "Set",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ============ AMOUNT ============
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = Formatting.formatRupiah(currentExpense),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hasTarget) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "/ ${Formatting.formatRupiah(monthlyTarget)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ============ PROGRESS BAR ============
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (hasTarget) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (pct / 100f).coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    barColor.copy(alpha = 0.7f),
                                    barColor
                                )
                            )
                        )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ============ FOOTER ============
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (hasTarget) "${pct.toInt()}% terpakai" else "Tap \"Set\" untuk atur target",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasTarget) {
                Text(
                    text = if (isOver)
                        "Lebih ${Formatting.formatRupiah(-remaining)}"
                    else
                        "Sisa ${Formatting.formatRupiah(remaining)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) Color(0xFFE11D48) else Color(0xFF059669)
                )
            }
        }

        // Info pemasukan bulan ini
        if (currentIncome > 0) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Pemasukan bulan ini: ${Formatting.formatRupiah(currentIncome)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
