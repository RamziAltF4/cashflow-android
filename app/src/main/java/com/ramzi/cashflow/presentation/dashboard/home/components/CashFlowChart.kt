package com.ramzi.cashflow.presentation.dashboard.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.presentation.dashboard.home.ChartPoint

/**
 * Chart arus kas modern (line + area chart) untuk 7 hari terakhir.
 * Menampilkan garis pemasukan (emerald) dan pengeluaran (rose) dengan gradient fill
 * serta label hari di bawah grafik.
 */
@Composable
fun CashFlowChart(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L
    val incomeColor = Color(0xFF10B981)
    val expenseColor = Color(0xFFF43F5E)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val w = size.width
            val h = size.height
            val padLeft = 12.dp.toPx()
            val padRight = 12.dp.toPx()
            val padTop = 14.dp.toPx()
            val padBottom = 8.dp.toPx()

            val chartW = w - padLeft - padRight
            val chartH = h - padTop - padBottom

            val stepX = if (data.size > 1) chartW / (data.size - 1) else chartW

            fun pointFor(index: Int, value: Long): Offset {
                val x = padLeft + stepX * index
                val ratio = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
                val y = padTop + chartH * (1f - ratio)
                return Offset(x, y)
            }

            // Grid lines
            for (i in 0..3) {
                val y = padTop + chartH * (i / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(padLeft, y),
                    end = Offset(w - padRight, y),
                    strokeWidth = 1f
                )
            }

            // Income gradient fill
            val incomeFillPath = Path().apply {
                data.forEachIndexed { i, p ->
                    val pt = pointFor(i, p.income)
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
                lineTo(padLeft + stepX * (data.size - 1), padTop + chartH)
                lineTo(padLeft, padTop + chartH)
                close()
            }
            drawPath(
                path = incomeFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(incomeColor.copy(alpha = 0.22f), Color.Transparent),
                    startY = padTop,
                    endY = padTop + chartH
                )
            )

            // Expense gradient fill
            val expenseFillPath = Path().apply {
                data.forEachIndexed { i, p ->
                    val pt = pointFor(i, p.expense)
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
                lineTo(padLeft + stepX * (data.size - 1), padTop + chartH)
                lineTo(padLeft, padTop + chartH)
                close()
            }
            drawPath(
                path = expenseFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(expenseColor.copy(alpha = 0.16f), Color.Transparent),
                    startY = padTop,
                    endY = padTop + chartH
                )
            )

            // Income line
            val incomePath = Path().apply {
                data.forEachIndexed { i, p ->
                    val pt = pointFor(i, p.income)
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = incomePath,
                color = incomeColor,
                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Expense line
            val expensePath = Path().apply {
                data.forEachIndexed { i, p ->
                    val pt = pointFor(i, p.expense)
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = expensePath,
                color = expenseColor,
                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dots
            data.forEachIndexed { i, p ->
                val ptIn = pointFor(i, p.income)
                drawCircle(color = incomeColor, radius = 3.5.dp.toPx(), center = ptIn)
                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = ptIn)

                val ptEx = pointFor(i, p.expense)
                drawCircle(color = expenseColor, radius = 3.5.dp.toPx(), center = ptEx)
                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = ptEx)
            }
        }

        Spacer(Modifier.height(6.dp))

        // X-Axis Day Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { pt ->
                Text(
                    text = pt.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )
            }
        }
    }
}