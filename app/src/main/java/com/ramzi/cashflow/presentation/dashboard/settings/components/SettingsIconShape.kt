package com.ramzi.cashflow.presentation.dashboard.settings.components


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
 * Enum section di Settings — dipakai untuk menentukan icon shape.
 */
enum class SettingsSectionIcon {
    PROFILE,        // 👤 user
    SECURITY,       // 🔒 shield
    APPEARANCE,     // 🎨 palette
    PLAN,           // ⭐ crown
    DANGER,         // ⚠️ warning
    LOGOUT          // 🚪 logout
}

/**
 * Icon Canvas untuk section header Settings.
 * Nol dependency — digambar manual dengan Canvas.
 */
@Composable
fun SettingsIconShape(
    icon: SettingsSectionIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.09f).coerceAtLeast(1.5f)
        val style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

        fun x(v: Float) = (v / 24f) * w
        fun y(v: Float) = (v / 24f) * h

        when (icon) {
            SettingsSectionIcon.PROFILE -> drawProfile(color, ::x, ::y, style)
            SettingsSectionIcon.SECURITY -> drawShield(color, ::x, ::y, style)
            SettingsSectionIcon.APPEARANCE -> drawPalette(color, ::x, ::y, style)
            SettingsSectionIcon.PLAN -> drawCrown(color, ::x, ::y, style)
            SettingsSectionIcon.DANGER -> drawWarning(color, ::x, ::y, style)
            SettingsSectionIcon.LOGOUT -> drawLogout(color, ::x, ::y, style)
        }
    }
}

// ============================================================
// PROFILE — user (kepala + badan)
// ============================================================
private fun DrawScope.drawProfile(
    color: Color,
    x: (Float) -> Float,
    y: (Float) -> Float,
    style: Stroke
) {
    // Kepala
    drawCircle(
        color = color,
        radius = (x(5f) - x(0f)),
        center = Offset(x(12f), y(8f)),
        style = style
    )
    // Badan (arc setengah lingkaran)
    val path = Path().apply {
        moveTo(x(5f), y(20f))
        cubicTo(
            x(5f), y(14f),
            x(19f), y(14f),
            x(19f), y(20f)
        )
    }
    drawPath(path, color, style = style)
}

// ============================================================
// SECURITY — shield
// ============================================================
private fun DrawScope.drawShield(
    color: Color,
    x: (Float) -> Float,
    y: (Float) -> Float,
    style: Stroke
) {
    val path = Path().apply {
        moveTo(x(12f), y(3f))
        lineTo(x(20f), y(6f))
        lineTo(x(20f), y(13f))
        cubicTo(
            x(20f), y(18f),
            x(16f), y(21f),
            x(12f), y(22f)
        )
        cubicTo(
            x(8f), y(21f),
            x(4f), y(18f),
            x(4f), y(13f)
        )
        lineTo(x(4f), y(6f))
        close()
    }
    drawPath(path, color, style = style)
}

// ============================================================
// APPEARANCE — palette
// ============================================================
private fun DrawScope.drawPalette(
    color: Color,
    x: (Float) -> Float,
    y: (Float) -> Float,
    style: Stroke
) {
    // Oval palette
    drawOval(
        color = color,
        topLeft = Offset(x(3f), y(4f)),
        size = Size(x(18f) - x(0f), y(16f) - y(0f)),
        style = style
    )
    // Titik-titik cat
    drawCircle(color, radius = (x(2f) - x(0f)), center = Offset(x(8f), y(9f)))
    drawCircle(color, radius = (x(2f) - x(0f)), center = Offset(x(12f), y(8f)))
    drawCircle(color, radius = (x(2f) - x(0f)), center = Offset(x(15f), y(11f)))
    drawCircle(color, radius = (x(2f) - x(0f)), center = Offset(x(10f), y(14f)))
}

// ============================================================
// PLAN — crown (mahkota)
// ============================================================
private fun DrawScope.drawCrown(
    color: Color,
    x: (Float) -> Float,
    y: (Float) -> Float,
    style: Stroke
) {
    val path = Path().apply {
        moveTo(x(3f), y(18f))
        lineTo(x(5f), y(8f))
        lineTo(x(9f), y(13f))
        lineTo(x(12f), y(6f))
        lineTo(x(15f), y(13f))
        lineTo(x(19f), y(8f))
        lineTo(x(21f), y(18f))
        close()
    }
    drawPath(path, color, style = style)
    // Base
    drawLine(
        color = color,
        start = Offset(x(4f), y(20f)),
        end = Offset(x(20f), y(20f)),
        strokeWidth = style.width,
        cap = StrokeCap.Round
    )
}

// ============================================================
// DANGER — warning triangle
// ============================================================
private fun DrawScope.drawWarning(
    color: Color,
    x: (Float) -> Float,
    y: (Float) -> Float,
    style: Stroke
) {
    val path = Path().apply {
        moveTo(x(12f), y(3f))
        lineTo(x(22f), y(20f))
        lineTo(x(2f), y(20f))
        close()
    }
    drawPath(path, color, style = style)
    // Exclamation
    drawLine(
        color = color,
        start = Offset(x(12f), y(9f)),
        end = Offset(x(12f), y(15f)),
        strokeWidth = style.width,
        cap = StrokeCap.Round
    )
    drawCircle(color, radius = (x(1.2f) - x(0f)), center = Offset(x(12f), y(17.5f)))
}

// ============================================================
// LOGOUT — pintu + panah
// ============================================================
private fun DrawScope.drawLogout(
    color: Color,
    x: (Float) -> Float,
    y: (Float) -> Float,
    style: Stroke
) {
    // Pintu
    val path = Path().apply {
        moveTo(x(14f), y(4f))
        lineTo(x(6f), y(4f))
        lineTo(x(6f), y(20f))
        lineTo(x(14f), y(20f))
    }
    drawPath(path, color, style = style)

    // Panah keluar
    drawLine(
        color = color,
        start = Offset(x(11f), y(12f)),
        end = Offset(x(21f), y(12f)),
        strokeWidth = style.width,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(x(17f), y(8f)),
        end = Offset(x(21f), y(12f)),
        strokeWidth = style.width,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(x(17f), y(16f)),
        end = Offset(x(21f), y(12f)),
        strokeWidth = style.width,
        cap = StrokeCap.Round
    )
}
