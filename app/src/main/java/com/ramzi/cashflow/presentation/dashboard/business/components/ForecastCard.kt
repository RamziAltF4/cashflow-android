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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import com.ramzi.cashflow.domain.model.ForecastResult
import com.ramzi.cashflow.util.Formatting

@Composable
fun ForecastCard(forecast: ForecastResult) {
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
                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShowChart,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prediksi Saldo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (forecast.hasEnoughData)
                        "Berdasarkan ${forecast.daysAnalyzed} hari terakhir"
                    else
                        "Butuh minimal beberapa transaksi",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ============ FORECAST TILES ============
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ForecastTile(
                modifier = Modifier.weight(1f),
                label = "7 Hari",
                value = forecast.forecast7,
                accent = Color(0xFF6366F1)
            )
            ForecastTile(
                modifier = Modifier.weight(1f),
                label = "30 Hari",
                value = forecast.forecast30,
                accent = Color(0xFF8B5CF6)
            )
        }

        Spacer(Modifier.height(12.dp))

        // ============ TREND BANNER ============
        val isPositive = forecast.isPositive
        val trendColor = if (isPositive) Color(0xFF059669) else Color(0xFFE11D48)
        val trendBg = if (isPositive)
            Brush.horizontalGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0)))
        else
            Brush.horizontalGradient(listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(trendBg)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                contentDescription = null,
                tint = trendColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isPositive) "Tren positif" else "Tren negatif",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = trendColor
                )
                Text(
                    text = "${if (isPositive) "+" else ""}${Formatting.formatRupiah(forecast.dailyNet)}/hari",
                    fontSize = 10.sp,
                    color = trendColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ForecastTile(
    label: String,
    value: Long,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = Formatting.formatRupiah(value),
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = accent
        )
    }
}