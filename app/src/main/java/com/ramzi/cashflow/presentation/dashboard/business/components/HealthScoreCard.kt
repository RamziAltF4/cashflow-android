package com.ramzi.cashflow.presentation.dashboard.business.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
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
import com.ramzi.cashflow.domain.model.HealthScore

@Composable
fun HealthScoreCard(health: HealthScore) {
    val (bgGrad, barGrad, textColor) = when {
        health.score >= 80 -> Triple(
            Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
            Brush.horizontalGradient(listOf(Color(0xFF34D399), Color(0xFF059669))),
            Color(0xFF059669)
        )
        health.score >= 60 -> Triple(
            Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4338CA))),
            Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFF4F46E5))),
            Color(0xFF4F46E5)
        )
        health.score >= 40 -> Triple(
            Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309))),
            Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
            Color(0xFFD97706)
        )
        else -> Triple(
            Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C))),
            Brush.horizontalGradient(listOf(Color(0xFFFB7185), Color(0xFFE11D48))),
            Color(0xFFE11D48)
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
                    .background(Color(0xFFE11D48).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Skor Kesehatan Keuangan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Berdasarkan kebiasaan finansialmu",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ============ SCORE DISPLAY ============
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgGrad),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = health.grade,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "GRADE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = health.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${health.score}/100",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = health.score / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barGrad)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Saving rate: ${(health.savingRate * 100).toInt()}% · ${health.incomeDays} hari pemasukan",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}