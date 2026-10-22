package com.ramzi.cashflow.presentation.dashboard.plans.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.ramzi.cashflow.domain.model.BillingMode
import com.ramzi.cashflow.domain.model.Plan
import com.ramzi.cashflow.domain.model.PlanConfig
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.util.Formatting

@Composable
fun PlanCard(
    modifier: Modifier = Modifier,
    plan: PlanConfig,
    billingMode: BillingMode,
    isCurrent: Boolean,
    onUpgrade: () -> Unit
) {
    val accent = LocalAccent.current
    val isBusiness = plan.plan == Plan.BUSINESS
    val isPro = plan.plan == Plan.PRO

    val cardBorderColor = when {
        isCurrent -> Color(0xFF10B981)
        isBusiness -> Color(0xFF8B5CF6)
        isPro -> accent.base
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val badgeColor = when {
        isBusiness -> Color(0xFF8B5CF6)
        isPro -> accent.base
        else -> Color(0xFF64748B)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isCurrent || isBusiness || isPro) 2.dp else 1.dp,
            color = cardBorderColor
        ),
        shadowElevation = if (isPro || isBusiness) 3.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(22.dp)) {

            // BADGE & CURRENT PLAN INDICATOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (plan.badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = plan.badge.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (isPro) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accent.base.copy(alpha = 0.14f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "POPULER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = accent.base,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PAKET AKTIF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981),
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // PLAN NAME
            Text(
                text = plan.label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(8.dp))

            // PRICE
            val price = plan.priceFor(billingMode)
            val priceLabel = if (price == 0L) "Gratis" else Formatting.formatRupiah(price)
            val period = plan.periodLabel(billingMode)

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = priceLabel,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isBusiness) Color(0xFF8B5CF6) else accent.base
                )
                if (period.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = period,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // FEATURE LIST
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plan.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ACTION BUTTON
            if (isCurrent) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Sedang Digunakan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (plan.plan == Plan.FREE) {
                Spacer(Modifier.height(8.dp))
            } else {
                Button(
                    onClick = onUpgrade,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBusiness) Color(0xFF8B5CF6) else accent.base,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Upgrade ke ${plan.label}",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
