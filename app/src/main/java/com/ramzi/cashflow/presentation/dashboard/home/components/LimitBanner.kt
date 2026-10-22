package com.ramzi.cashflow.presentation.dashboard.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LimitBanner(
    modifier: Modifier = Modifier,
    used: Int,
    limit: Int,
    planLabel: String,
    onUpgrade: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFBEB))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Text("!", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF92400E))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "$used/$limit transaksi paket $planLabel",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF92400E),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Upgrade",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF92400E),
            modifier = Modifier
                .clickable { onUpgrade() }
                .padding(start = 4.dp)
        )
    }
}