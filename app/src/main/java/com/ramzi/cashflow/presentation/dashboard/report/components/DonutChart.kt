package com.ramzi.cashflow.presentation.dashboard.report.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ramzi.cashflow.presentation.dashboard.report.CategorySlice
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Donut chart interaktif.
 *
 * - Tap slice → onSliceSelected(category)
 * - Slice yang dipilih di-"pop" (sedikit keluar dari pusat).
 * - Sudut mulai dari -90° (jam 12).
 * - Gap 2° antar slice biar rapi.
 * - Sudut slice pakai rounded cap di ujungnya.
 */
@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    slices: List<CategorySlice>,
    strokeWidthDp: Int = 40,
    selectedCategory: String? = null,
    onSliceSelected: (String) -> Unit = {}
) {
    if (slices.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = strokeWidthDp.dp.toPx()
                val diameter = minOf(size.width, size.height) - stroke
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    ),
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke)
                )
            }
        }
        return
    }

    // Precompute angle ranges untuk hit-testing
    val sliceRanges = buildAngleRanges(slices)

    Box(
        modifier = modifier.pointerInput(slices, selectedCategory) {
            detectTapGestures { tapOffset ->
                // Hitung sudut tap relatif ke pusat
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = tapOffset.x - center.x
                val dy = tapOffset.y - center.y
                val dist = sqrt(dx * dx + dy * dy)

                // Radius dalam (inner) & luar (outer)
                val strokePx = strokeWidthDp.dp.toPx()
                val outerR = minOf(size.width, size.height) / 2f
                val innerR = outerR - strokePx

                // Cek apakah tap di area ring
                if (dist < innerR || dist > outerR) return@detectTapGestures

                // Hitung sudut dari jam 12, clockwise
                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                angle = (angle + 90f + 360f) % 360f

                // Cari slice
                val hit = sliceRanges.firstOrNull { angle >= it.start && angle < it.end }
                if (hit != null) {
                    onSliceSelected(hit.category)
                }
            }
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidthDp.dp.toPx()
            val diameter = minOf(size.width, size.height) - stroke
            val baseTopLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            val gapDegrees = 2f

            slices.forEachIndexed { idx, slice ->
                val range = sliceRanges[idx]
                val isSelected = slice.category == selectedCategory

                // "Pop" effect: geser keluar dari pusat kalau dipilih
                val popOffset = if (isSelected) stroke * 0.35f else 0f
                val midAngle = Math.toRadians((range.start + (range.end - range.start) / 2f - 90f).toDouble())
                val popX = (popOffset * Math.cos(midAngle)).toFloat()
                val popY = (popOffset * Math.sin(midAngle)).toFloat()

                val topLeft = Offset(
                    baseTopLeft.x + popX,
                    baseTopLeft.y + popY
                )

                val startAngle = range.start - 90f + gapDegrees / 2f
                val sweepAngle = (range.end - range.start) - gapDegrees

                if (sweepAngle <= 0f) return@forEachIndexed

                // Glow saat dipilih
                if (isSelected) {
                    drawArc(
                        color = slice.color.copy(alpha = 0.25f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = stroke + stroke * 0.25f,
                            cap = StrokeCap.Round
                        )
                    )
                }

                drawArc(
                    color = if (selectedCategory == null || isSelected) slice.color
                    else slice.color.copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// ============================================================
// Data class untuk rentang sudut slice
// ============================================================
private data class AngleRange(
    val category: String,
    val start: Float,
    val end: Float
)

private fun buildAngleRanges(slices: List<CategorySlice>): List<AngleRange> {
    var start = 0f
    return slices.map { slice ->
        val sweep = (slice.percentage / 100f) * 360f
        val range = AngleRange(slice.category, start, start + sweep)
        start += sweep
        range
    }
}