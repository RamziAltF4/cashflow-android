package com.ramzi.cashflow.presentation.dashboard.home.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.presentation.dashboard.tx.components.CategoryIconShape
import com.ramzi.cashflow.util.CurrencyFormatter
import com.ramzi.cashflow.util.Formatting

@Composable
fun RecentTransactions(
    modifier: Modifier = Modifier,
    transactions: List<Transaction>,
    currencyCode: String,
    onSeeAll: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (transactions.isEmpty()) {
            EmptyState()
        } else {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                transactions.forEachIndexed { i, tx ->
                    if (i > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 68.dp)
                                .height(1.dp)
                                .background(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    RoundedCornerShape(1.dp)
                                )
                        )
                    }
                    TxRow(tx, currencyCode)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("📭", fontSize = 24.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Belum ada transaksi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Mulai catat dengan tombol di atas",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TxRow(tx: Transaction, currencyCode: String) {
    val isIncome = tx.type.id == "income"
    val iconBg = if (isIncome) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFF43F5E).copy(alpha = 0.12f)
    val iconColor = if (isIncome) Color(0xFF10B981) else Color(0xFFF43F5E)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            CategoryIconShape(
                category = tx.category,
                color = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.desc,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${tx.category} · ${Formatting.formatDate(tx.date)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            text = (if (isIncome) "+" else "-") + CurrencyFormatter.format(tx.amount, currencyCode),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = iconColor
        )
    }
}