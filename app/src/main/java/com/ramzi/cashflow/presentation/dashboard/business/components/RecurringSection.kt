package com.ramzi.cashflow.presentation.dashboard.business.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.Recurring
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.util.Formatting

/**
 * Section yang menampilkan daftar transaksi berulang.
 * Empty state + item + tombol tambah.
 */
@Composable
fun RecurringSection(
    recurringList: List<Recurring>,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
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
            Icon(
                imageVector = Icons.Filled.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Transaksi Berulang",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAdd() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "Tambah",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "Atur langganan, gaji, tagihan bulanan agar otomatis tercatat.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // ============ LIST ============
        if (recurringList.isEmpty()) {
            EmptyRecurringState()
        } else {
            recurringList.forEach { rec ->
                RecurringItem(
                    rec = rec,
                    onEdit = { onEdit(rec.id) },
                    onToggle = { onToggle(rec.id, !rec.active) },
                    onDelete = { onDelete(rec.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/* ============================================================
   EMPTY STATE
   ============================================================ */
@Composable
private fun EmptyRecurringState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Belum ada transaksi berulang",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tambahkan langganan, gaji, tagihan bulanan.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ============================================================
   SINGLE ITEM
   ============================================================ */
@Composable
private fun RecurringItem(
    rec: Recurring,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = rec.type == TxType.INCOME
    val isDue = rec.isDue

    val accent = if (isIncome) Color(0xFF059669) else Color(0xFFE11D48)
    val accentSoft = if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)

    val cardAlpha = if (rec.active) 1f else 0.6f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.size(12.dp))

        // Middle: title + meta
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = rec.desc,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (isDue) {
                    Spacer(Modifier.size(6.dp))
                    Badge(text = "JATUH TEMPO", color = Color(0xFFF59E0B))
                }
                if (!rec.active) {
                    Spacer(Modifier.size(6.dp))
                    Badge(text = "NONAKTIF", color = Color(0xFF94A3B8))
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = rec.frequency.label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "  ·  ${rec.category}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(2.dp))
            Text(
                text = "Next: ${rec.nextRun}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (rec.note.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "\"${rec.note}\"",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.size(8.dp))

        // Right: amount + actions
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (isIncome) "+" else "-") + Formatting.formatRupiah(rec.amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = accent
            )

            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButtonMini(
                    icon = Icons.Filled.Edit,
                    onClick = onEdit
                )
                IconButtonMini(
                    icon = if (rec.active) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    tint = if (rec.active) Color(0xFFF59E0B) else Color(0xFF10B981),
                    onClick = onToggle
                )
                IconButtonMini(
                    icon = Icons.Filled.Delete,
                    tint = Color(0xFFE11D48),
                    onClick = onDelete
                )
            }
        }
    }
}

/* ============================================================
   SMALL HELPERS
   ============================================================ */
@Composable
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
private fun IconButtonMini(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
    }
}