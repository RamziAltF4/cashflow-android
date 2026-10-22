package com.ramzi.cashflow.presentation.broadcast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.Broadcast
import com.ramzi.cashflow.domain.model.BroadcastType
import kotlinx.coroutines.delay

@Composable
fun BroadcastBanner(
    broadcast: Broadcast?,
    onDismiss: () -> Unit,
    autoDismissMs: Long = 5000L,
    modifier: Modifier = Modifier
) {
    // ═══ AUTO-DISMISS 5 DETIK ═══
    // Key = broadcast.id → timer reset tiap broadcast baru
    LaunchedEffect(broadcast?.id) {
        if (broadcast != null) {
            delay(autoDismissMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = broadcast != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (broadcast != null) {
            BroadcastBannerContent(broadcast, onDismiss)
        }
    }
}

@Composable
private fun BroadcastBannerContent(
    broadcast: Broadcast,
    onDismiss: () -> Unit
) {
    val palette = paletteFor(broadcast.type)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.bg)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(palette.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = palette.icon,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = broadcast.title.ifBlank { defaultTitle(broadcast.type) },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    letterSpacing = (-0.2).sp
                )
                if (broadcast.message.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = broadcast.message,
                        fontSize = 12.5.sp,
                        color = palette.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private data class BannerPalette(
    val bg: Color,
    val iconBg: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val icon: ImageVector
)

private fun paletteFor(type: BroadcastType): BannerPalette = when (type) {
    BroadcastType.INFO -> BannerPalette(
        bg = Color(0xFFEFF6FF),
        iconBg = Color(0xFFDBEAFE),
        accent = Color(0xFF2563EB),
        textPrimary = Color(0xFF1E3A8A),
        textSecondary = Color(0xFF1E40AF),
        icon = Icons.Filled.Info
    )
    BroadcastType.SUCCESS -> BannerPalette(
        bg = Color(0xFFECFDF5),
        iconBg = Color(0xFFD1FAE5),
        accent = Color(0xFF059669),
        textPrimary = Color(0xFF064E3B),
        textSecondary = Color(0xFF065F46),
        icon = Icons.Filled.CheckCircle
    )
    BroadcastType.WARNING -> BannerPalette(
        bg = Color(0xFFFFFBEB),
        iconBg = Color(0xFFFEF3C7),
        accent = Color(0xFFD97706),
        textPrimary = Color(0xFF78350F),
        textSecondary = Color(0xFF92400E),
        icon = Icons.Filled.Warning
    )
    BroadcastType.PROMO -> BannerPalette(
        bg = Color(0xFFFDF4FF),
        iconBg = Color(0xFFFAE8FF),
        accent = Color(0xFFC026D3),
        textPrimary = Color(0xFF701A75),
        textSecondary = Color(0xFF86198F),
        icon = Icons.Filled.LocalOffer
    )
}

private fun defaultTitle(type: BroadcastType): String = when (type) {
    BroadcastType.INFO -> "Info"
    BroadcastType.SUCCESS -> "Success"
    BroadcastType.WARNING -> "Warning"
    BroadcastType.PROMO -> "Promo"
}