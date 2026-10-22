package com.ramzi.cashflow.presentation.dashboard.tx.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Icon kategori digambar manual dengan Canvas.
 * Tidak butuh dependency (bukan emoji, bukan image, bukan font).
 *
 * Semua icon didesain di grid 24x24 (dipetakan ke ukuran canvas).
 */
@Composable
fun CategoryIconShape(
    category: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val stroke = s * 0.08f
        val pad = s * 0.18f
        val w = size.width
        val h = size.height

        // Fungsi bantu: scale koordinat (0..24) ke ukuran canvas
        fun sx(v: Float) = pad + (v / 24f) * (w - pad * 2)
        fun sy(v: Float) = pad + (v / 24f) * (h - pad * 2)
        fun sw(v: Float) = (v / 24f) * (w - pad * 2)

        val strokeStyle = Stroke(
            width = stroke,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (category) {
            "Gaji" -> drawGaji(color, ::sx, ::sy, ::sw, strokeStyle)
            "Bonus" -> drawBonus(color, ::sx, ::sy, ::sw, strokeStyle)
            "Investasi" -> drawInvestasi(color, ::sx, ::sy, ::sw, strokeStyle)
            "Penjualan" -> drawPenjualan(color, ::sx, ::sy, ::sw, strokeStyle)
            "Hadiah" -> drawHadiah(color, ::sx, ::sy, ::sw, strokeStyle)
            "Makanan" -> drawMakanan(color, ::sx, ::sy, ::sw, strokeStyle)
            "Transportasi" -> drawTransportasi(color, ::sx, ::sy, ::sw, strokeStyle)
            "Belanja" -> drawBelanja(color, ::sx, ::sy, ::sw, strokeStyle)
            "Tagihan" -> drawTagihan(color, ::sx, ::sy, ::sw, strokeStyle)
            "Hiburan" -> drawHiburan(color, ::sx, ::sy, ::sw, strokeStyle)
            "Kesehatan" -> drawKesehatan(color, ::sx, ::sy, ::sw, strokeStyle)
            "Pendidikan" -> drawPendidikan(color, ::sx, ::sy, ::sw, strokeStyle)
            else -> drawLainnya(color, ::sx, ::sy, ::sw, strokeStyle)
        }
    }
}

// ============================================================
// Gaji — uang kertas + lingkaran
// ============================================================
private fun DrawScope.drawGaji(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(sx(2f), sy(6f)),
        size = Size(sw(20f), sw(12f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(sw(2f), sw(2f)),
        style = style
    )
    drawCircle(
        color = color,
        radius = sw(3.5f),
        center = Offset(sx(12f), sy(12f)),
        style = style
    )
    drawLine(color, Offset(sx(6f), sy(9f)), Offset(sx(6f), sy(15f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(18f), sy(9f)), Offset(sx(18f), sy(15f)), strokeWidth = style.width, cap = StrokeCap.Round)
}

// ============================================================
// Bonus — kotak hadiah + pita
// ============================================================
private fun DrawScope.drawBonus(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Box body
    drawRoundRect(
        color = color,
        topLeft = Offset(sx(4f), sy(9f)),
        size = Size(sw(16f), sw(11f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(sw(1.5f), sw(1.5f)),
        style = style
    )
    // Lid
    drawRoundRect(
        color = color,
        topLeft = Offset(sx(3f), sy(6f)),
        size = Size(sw(18f), sw(4f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(sw(1.5f), sw(1.5f)),
        style = style
    )
    // Vertical ribbon
    drawLine(color, Offset(sx(12f), sy(6f)), Offset(sx(12f), sy(20f)), strokeWidth = style.width, cap = StrokeCap.Round)
    // Bow (two small arcs)
    drawArc(
        color = color,
        startAngle = 180f, sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(sx(8f), sy(2.5f)),
        size = Size(sw(4f), sw(4f)),
        style = style
    )
    drawArc(
        color = color,
        startAngle = 180f, sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(sx(12f), sy(2.5f)),
        size = Size(sw(4f), sw(4f)),
        style = style
    )
}

// ============================================================
// Investasi — panah naik
// ============================================================
private fun DrawScope.drawInvestasi(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    val path = Path().apply {
        moveTo(sx(4f), sy(17f))
        lineTo(sx(9f), sy(12f))
        lineTo(sx(13f), sy(15f))
        lineTo(sx(20f), sy(7f))
    }
    drawPath(path, color, style = style)
    // Arrow head
    drawLine(color, Offset(sx(20f), sy(7f)), Offset(sx(15f), sy(7f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(20f), sy(7f)), Offset(sx(20f), sy(12f)), strokeWidth = style.width, cap = StrokeCap.Round)
}

// ============================================================
// Penjualan — tas belanja
// ============================================================
private fun DrawScope.drawPenjualan(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Bag body
    drawRoundRect(
        color = color,
        topLeft = Offset(sx(5f), sy(8f)),
        size = Size(sw(14f), sw(12f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(sw(2f), sw(2f)),
        style = style
    )
    // Handle
    drawArc(
        color = color,
        startAngle = 180f, sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(sx(8f), sy(4f)),
        size = Size(sw(8f), sw(7f)),
        style = style
    )
}

// ============================================================
// Hadiah — medali
// ============================================================
private fun DrawScope.drawHadiah(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Circle medal
    drawCircle(
        color = color,
        radius = sw(6f),
        center = Offset(sx(12f), sy(14f)),
        style = style
    )
    // Ribbon (V shape above)
    drawLine(color, Offset(sx(8f), sy(2f)), Offset(sx(12f), sy(8f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(16f), sy(2f)), Offset(sx(12f), sy(8f)), strokeWidth = style.width, cap = StrokeCap.Round)
    // Star inside
    drawCircle(
        color = color,
        radius = sw(1.5f),
        center = Offset(sx(12f), sy(14f))
    )
}

// ============================================================
// Makanan — garpu + sendok
// ============================================================
private fun DrawScope.drawMakanan(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Fork (left)
    drawLine(color, Offset(sx(8f), sy(3f)), Offset(sx(8f), sy(11f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(6f), sy(3f)), Offset(sx(6f), sy(7f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(10f), sy(3f)), Offset(sx(10f), sy(7f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(8f), sy(11f)), Offset(sx(8f), sy(21f)), strokeWidth = style.width, cap = StrokeCap.Round)

    // Spoon (right)
    drawArc(
        color = color,
        startAngle = 0f, sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(sx(14f), sy(3f)),
        size = Size(sw(6f), sw(8f)),
        style = style
    )
    drawLine(color, Offset(sx(17f), sy(11f)), Offset(sx(17f), sy(21f)), strokeWidth = style.width, cap = StrokeCap.Round)
}

// ============================================================
// Transportasi — mobil
// ============================================================
private fun DrawScope.drawTransportasi(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Body
    val path = Path().apply {
        moveTo(sx(3f), sy(16f))
        lineTo(sx(3f), sy(11f))
        lineTo(sx(6f), sy(11f))
        lineTo(sx(8f), sy(7f))
        lineTo(sx(16f), sy(7f))
        lineTo(sx(18f), sy(11f))
        lineTo(sx(21f), sy(11f))
        lineTo(sx(21f), sy(16f))
        lineTo(sx(3f), sy(16f))
        close()
    }
    drawPath(path, color, style = style)

    // Wheels
    drawCircle(color, radius = sw(2f), center = Offset(sx(7f), sy(17f)), style = style)
    drawCircle(color, radius = sw(2f), center = Offset(sx(17f), sy(17f)), style = style)
}

// ============================================================
// Belanja — keranjang
// ============================================================
private fun DrawScope.drawBelanja(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Basket
    drawLine(color, Offset(sx(3f), sy(6f)), Offset(sx(21f), sy(6f)), strokeWidth = style.width, cap = StrokeCap.Round)
    val path = Path().apply {
        moveTo(sx(5f), sy(6f))
        lineTo(sx(7f), sy(20f))
        lineTo(sx(17f), sy(20f))
        lineTo(sx(19f), sy(6f))
    }
    drawPath(path, color, style = style)
    // Handle
    drawArc(
        color = color,
        startAngle = 0f, sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(sx(9f), sy(2f)),
        size = Size(sw(6f), sw(6f)),
        style = style
    )
}

// ============================================================
// Tagihan — struk
// ============================================================
private fun DrawScope.drawTagihan(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Receipt body with zigzag bottom
    val path = Path().apply {
        moveTo(sx(6f), sy(3f))
        lineTo(sx(18f), sy(3f))
        lineTo(sx(18f), sy(21f))
        lineTo(sx(16f), sy(19f))
        lineTo(sx(14f), sy(21f))
        lineTo(sx(12f), sy(19f))
        lineTo(sx(10f), sy(21f))
        lineTo(sx(8f), sy(19f))
        lineTo(sx(6f), sy(21f))
        close()
    }
    drawPath(path, color, style = style)
    // Lines inside
    drawLine(color, Offset(sx(9f), sy(8f)), Offset(sx(15f), sy(8f)), strokeWidth = style.width * 0.7f, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(9f), sy(11f)), Offset(sx(15f), sy(11f)), strokeWidth = style.width * 0.7f, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(9f), sy(14f)), Offset(sx(13f), sy(14f)), strokeWidth = style.width * 0.7f, cap = StrokeCap.Round)
}

// ============================================================
// Hiburan — gamepad
// ============================================================
private fun DrawScope.drawHiburan(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Gamepad body
    drawRoundRect(
        color = color,
        topLeft = Offset(sx(3f), sy(9f)),
        size = Size(sw(18f), sw(9f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(sw(4.5f), sw(4.5f)),
        style = style
    )
    // D-pad
    drawLine(color, Offset(sx(7f), sy(13f)), Offset(sx(9f), sy(13f)), strokeWidth = style.width * 0.8f, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(8f), sy(12f)), Offset(sx(8f), sy(14f)), strokeWidth = style.width * 0.8f, cap = StrokeCap.Round)
    // Buttons
    drawCircle(color, radius = sw(0.8f), center = Offset(sx(16f), sy(12f)))
    drawCircle(color, radius = sw(0.8f), center = Offset(sx(18f), sy(14f)))
}

// ============================================================
// Kesehatan — hati
// ============================================================
private fun DrawScope.drawKesehatan(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    val path = Path().apply {
        moveTo(sx(12f), sy(20f))
        // Left lobe
        cubicTo(
            sx(2f), sy(14f),
            sx(2f), sy(5f),
            sx(12f), sy(9f)
        )
        // Right lobe
        cubicTo(
            sx(22f), sy(5f),
            sx(22f), sy(14f),
            sx(12f), sy(20f)
        )
        close()
    }
    drawPath(path, color, style = style)
}

// ============================================================
// Pendidikan — topi wisuda
// ============================================================
private fun DrawScope.drawPendidikan(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    // Top triangle (flat diamond)
    val path = Path().apply {
        moveTo(sx(12f), sy(4f))
        lineTo(sx(22f), sy(9f))
        lineTo(sx(12f), sy(14f))
        lineTo(sx(2f), sy(9f))
        close()
    }
    drawPath(path, color, style = style)
    // Body (trapezoid under)
    drawLine(color, Offset(sx(6f), sy(11f)), Offset(sx(6f), sy(17f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(18f), sy(11f)), Offset(sx(18f), sy(17f)), strokeWidth = style.width, cap = StrokeCap.Round)
    drawLine(color, Offset(sx(6f), sy(17f)), Offset(sx(18f), sy(17f)), strokeWidth = style.width, cap = StrokeCap.Round)
    // Tassel
    drawLine(color, Offset(sx(22f), sy(9f)), Offset(sx(22f), sy(14f)), strokeWidth = style.width * 0.8f, cap = StrokeCap.Round)
}

// ============================================================
// Lainnya — 3 titik
// ============================================================
private fun DrawScope.drawLainnya(
    color: Color,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    sw: (Float) -> Float,
    style: Stroke
) {
    drawCircle(color, radius = sw(1.8f), center = Offset(sx(6f), sy(12f)))
    drawCircle(color, radius = sw(1.8f), center = Offset(sx(12f), sy(12f)))
    drawCircle(color, radius = sw(1.8f), center = Offset(sx(18f), sy(12f)))
}