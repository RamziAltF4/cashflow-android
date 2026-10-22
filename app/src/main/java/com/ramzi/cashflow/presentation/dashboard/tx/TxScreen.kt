package com.ramzi.cashflow.presentation.dashboard.tx

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.presentation.dashboard.tx.components.TxRowItem
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.util.CurrencyFormatter
import kotlinx.coroutines.launch

@Composable
fun TxScreen(viewModel: TxViewModel) {
    val state by viewModel.state.collectAsState()
    val accent = LocalAccent.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val monthScrollState = rememberScrollState()

    // ⭐ Currency code
    val currencyCode = state.currencyCode

    LaunchedEffect(state.sortDesc, state.filterType, state.searchQuery, state.filterMonth) {
        listState.scrollToItem(0)
    }

    if (state.deleteConfirmId != null) {
        val deletingTx = state.transactions.firstOrNull { it.id == state.deleteConfirmId }
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Hapus Transaksi?",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus catatan transaksi ini?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = state.deleteConfirmTitle.ifBlank { "Transaksi" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (deletingTx != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${deletingTx.category} · ${CurrencyFormatter.format(deletingTx.amount, currencyCode)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (deletingTx.type == TxType.INCOME) Color(0xFF10B981) else Color(0xFFF43F5E)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tindakan ini permanen dan akan langsung memperbarui saldo serta laporan keuangan.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Hapus Permanen",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelDelete() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (state.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = accent.base)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Transaksi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Kelola semua catatan keuanganmu",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = {
                Text("Cari transaksi...", fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { scope.launch { listState.scrollToItem(0) } }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent.base,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "Semua",
                active = state.filterType == null,
                onClick = { viewModel.onFilterTypeChange(null) }
            )
            FilterChip(
                label = "Pemasukan",
                active = state.filterType == TxType.INCOME,
                onClick = { viewModel.onFilterTypeChange(TxType.INCOME) }
            )
            FilterChip(
                label = "Pengeluaran",
                active = state.filterType == TxType.EXPENSE,
                onClick = { viewModel.onFilterTypeChange(TxType.EXPENSE) }
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Bulan",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp
            )

            Spacer(Modifier.weight(1f))

            if (state.filterMonth != null) {
                Surface(
                    onClick = { viewModel.onMonthFilterChange(null) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(monthScrollState)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MonthPill(
                label = "Semua",
                active = state.filterMonth == null,
                accent = accent.base,
                onClick = { viewModel.onMonthFilterChange(null) }
            )

            state.availableMonths.forEach { monthKey ->
                val label = formatMonthLabel(monthKey)
                MonthPill(
                    label = label,
                    active = state.filterMonth == monthKey,
                    accent = accent.base,
                    onClick = { viewModel.onMonthFilterChange(monthKey) }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "Pemasukan",
                value = state.totalIncome,
                color = Color(0xFF059669),
                currencyCode = currencyCode
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "Pengeluaran",
                value = state.totalExpense,
                color = Color(0xFFE11D48),
                currencyCode = currencyCode
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DAFTAR",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${state.filtered.size} transaksi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            SortToggleButton(
                isDesc = state.sortDesc,
                accent = accent.base,
                onClick = { viewModel.toggleSort() }
            )
        }

        Spacer(Modifier.height(10.dp))

        if (state.filtered.isEmpty()) {
            EmptyTxState(hasFilter = state.hasActiveFilter)
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(
                    items = state.filtered,
                    key = { it.id }
                ) { tx ->
                    TxRowItem(
                        tx = tx,
                        currencyCode = currencyCode,
                        onClick = { viewModel.openEditForm(tx.id) },
                        onDeleteClick = { viewModel.requestDelete(tx.id) }
                    )
                }
            }
        }
    }
}

/* ============================================================
   MONTH PILL
   ============================================================ */
@Composable
private fun MonthPill(
    label: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (active) accent
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ============================================================
   FORMAT "2026-01" → "Jan 2026"
   ============================================================ */
private fun formatMonthLabel(monthKey: String): String {
    return try {
        val parts = monthKey.split("-")
        val year = parts[0]
        val month = parts[1].toInt()
        val names = listOf(
            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
        )
        "${names[month - 1]} $year"
    } catch (e: Exception) {
        monthKey
    }
}

/* ============================================================
   FILTER CHIP
   ============================================================ */
@Composable
private fun FilterChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val accent = LocalAccent.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (active) accent.base
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ============================================================
   SUMMARY CARD
   ============================================================ */
@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: Long,
    color: Color,
    currencyCode: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = CurrencyFormatter.format(value, currencyCode),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

/* ============================================================
   SORT TOGGLE
   ============================================================ */
@Composable
private fun SortToggleButton(
    isDesc: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDesc) Icons.Filled.ArrowDownward
                else Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isDesc) "Terbaru" else "Terlama",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = accent
            )
        }
    }
}

/* ============================================================
   EMPTY STATE
   ============================================================ */
@Composable
private fun EmptyTxState(hasFilter: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasFilter) Icons.Filled.Search else Icons.Filled.SortByAlpha,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (hasFilter) "Tidak ditemukan" else "Belum ada transaksi",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (hasFilter) "Coba ubah filter atau kata kunci pencarian"
                else "Tap tombol tambah untuk mencatat transaksi pertama",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}