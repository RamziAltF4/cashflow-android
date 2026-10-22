package com.ramzi.cashflow.presentation.dashboard.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext  
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.presentation.dashboard.report.components.DonutChart
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.util.CurrencyFormatter

@Composable
fun ReportScreen(
   viewModel: ReportViewModel = viewModel(
    factory = ReportViewModelFactory(LocalContext.current)
)
) {
    val state by viewModel.state.collectAsState()
    val accent = LocalAccent.current

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // ⭐ Currency code
    val currencyCode = state.currencyCode

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

    val selectedSlice = state.categoryBreakdown.firstOrNull {
        it.category == selectedCategory
    }

    val savingsRate = if (state.totalIncome > 0L) {
        val net = state.totalIncome - state.totalExpense
        ((net.toFloat() / state.totalIncome) * 100).toInt().coerceIn(-100, 100)
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = 20.dp, bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // HEADER
        item {
            Column {
                Text(
                    text = "Laporan Keuangan",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Analisis arus kas, rasio tabungan, dan distribusi beban",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // HERO FINANCIAL HEALTH & SAVINGS RATE CARD
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accent.base.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Savings,
                                    contentDescription = null,
                                    tint = accent.base,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RASIO TABUNGAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = if (savingsRate >= 20) "Keuangan Sehat" else if (savingsRate >= 0) "Arus Kas Cukup" else "Defisit Bulanan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (savingsRate >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (savingsRate >= 0) Color(0xFF10B981).copy(alpha = 0.12f)
                                    else Color(0xFFF43F5E).copy(alpha = 0.12f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$savingsRate%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (savingsRate >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    val totalVolume = state.totalIncome + state.totalExpense
                    val incomeFrac = if (totalVolume > 0) state.totalIncome.toFloat() / totalVolume else 0.5f
                    val expenseFrac = if (totalVolume > 0) state.totalExpense.toFloat() / totalVolume else 0.5f

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Masuk: ${CurrencyFormatter.format(state.totalIncome, currencyCode)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Keluar: ${CurrencyFormatter.format(state.totalExpense, currencyCode)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF43F5E)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (state.totalIncome > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(incomeFrac.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(Color(0xFF10B981))
                                )
                            }
                            if (state.totalExpense > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(expenseFrac.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(Color(0xFFF43F5E))
                                )
                            }
                        }
                    }
                }
            }
        }

        // DONUT CHART CARD
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pengeluaran per Kategori",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedCategory == null) "Tap slice chart untuk fokus kategori"
                                else "Fokus: $selectedCategory (Tap lagi untuk reset)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (selectedCategory != null) {
                            Surface(
                                onClick = { selectedCategory = null },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Reset",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (state.categoryBreakdown.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PieChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada catatan pengeluaran",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(230.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                modifier = Modifier.fillMaxSize(),
                                slices = state.categoryBreakdown,
                                strokeWidthDp = 34,
                                selectedCategory = selectedCategory,
                                onSliceSelected = { cat ->
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                }
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 40.dp)
                            ) {
                                if (selectedSlice != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(selectedSlice.color)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = selectedSlice.category,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(selectedSlice.amount, currencyCode),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = selectedSlice.color
                                    )
                                    Text(
                                        text = "${selectedSlice.percentage.toInt()}% dari total",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "TOTAL KELUAR",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(state.totalExpense, currencyCode),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${state.categoryBreakdown.size} kategori",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // DETAIL KATEGORI
        if (state.categoryBreakdown.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Distribusi Kategori",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pangsa (%)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.categoryBreakdown.forEach { slice ->
                                val isSelected = slice.category == selectedCategory
                                CategoryProgressRow(
                                    slice = slice,
                                    currencyCode = currencyCode,
                                    selected = isSelected,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == slice.category) null else slice.category
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // RINGKASAN METRIK FINANSIAL
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Ringkasan Finansial",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinancialMetricRow(
                            icon = Icons.Filled.ArrowUpward,
                            iconColor = Color(0xFF10B981),
                            label = "Total Pemasukan",
                            value = CurrencyFormatter.format(state.totalIncome, currencyCode),
                            valueColor = Color(0xFF10B981)
                        )
                        FinancialMetricRow(
                            icon = Icons.Filled.ArrowDownward,
                            iconColor = Color(0xFFF43F5E),
                            label = "Total Pengeluaran",
                            value = CurrencyFormatter.format(state.totalExpense, currencyCode),
                            valueColor = Color(0xFFF43F5E)
                        )
                        FinancialMetricRow(
                            icon = Icons.Filled.TrendingUp,
                            iconColor = if (state.balance >= 0) Color(0xFF6366F1) else Color(0xFFDC2626),
                            label = "Saldo Akhir Periode",
                            value = CurrencyFormatter.format(state.balance, currencyCode),
                            valueColor = if (state.balance >= 0) Color(0xFF6366F1) else Color(0xFFDC2626)
                        )
                        FinancialMetricRow(
                            icon = Icons.Filled.PieChart,
                            iconColor = Color(0xFF8B5CF6),
                            label = "Beban Terbesar",
                            value = state.topCategory.ifBlank { "Belum ada" },
                            valueColor = Color(0xFF8B5CF6)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Category Progress Row
// ============================================================
@Composable
private fun CategoryProgressRow(
    slice: CategorySlice,
    currencyCode: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) slice.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val border = if (selected) BorderStroke(1.5.dp, slice.color) else null

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(slice.color)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = slice.category,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${slice.percentage.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = slice.color
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = CurrencyFormatter.format(slice.amount, currencyCode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (slice.percentage / 100f).coerceIn(0.01f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(slice.color)
                )
            }
        }
    }
}

// ============================================================
// Financial Metric Row
// ============================================================
@Composable
private fun FinancialMetricRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}