package com.ramzi.cashflow.presentation.dashboard.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.presentation.dashboard.home.components.BalanceHero
import com.ramzi.cashflow.presentation.dashboard.home.components.CashFlowChart
import com.ramzi.cashflow.presentation.dashboard.home.components.LimitBanner
import com.ramzi.cashflow.presentation.dashboard.home.components.MiniStat
import com.ramzi.cashflow.presentation.dashboard.home.components.QuickActionsRow
import com.ramzi.cashflow.presentation.dashboard.home.components.RecentTransactions
import com.ramzi.cashflow.presentation.dashboard.home.components.SectionHeader
import com.ramzi.cashflow.presentation.dashboard.home.components.StatTone
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.util.CurrencyFormatter
import java.util.Calendar

@Composable
fun HomeScreen(
    onAddTransaction: (type: String) -> Unit = {},
    onNavigateToTx: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToPlans: () -> Unit = {},
    onNavigateToImports: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory)
) {
    val state by viewModel.state.collectAsState()
    val accent = LocalAccent.current

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

    // ⭐ Currency code dari user (fallback IDR)
    val currencyCode = state.user?.displayCurrency ?: "IDR"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = 20.dp, bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        /* ============ HEADER ============ */
        item {
            HeaderSection(
                userName = state.user?.name.orEmpty(),
                pendingImportsCount = state.pendingImportsCount,
                onImportsClick = onNavigateToImports
            )
        }

        val isBusinessPlan = state.planConfig?.plan?.id == "business" || state.user?.plan?.id == "business"

        /* ============ LIMIT BANNER ============ */
        if (state.showLimitWarning && !isBusinessPlan) {
            item {
                LimitBanner(
                    used = state.txCount,
                    limit = state.planConfig?.limit ?: 10,
                    planLabel = state.planConfig?.label ?: "Free",
                    onUpgrade = onNavigateToPlans
                )
            }
        }

        /* ============ HERO BALANCE ============ */
        item {
            BalanceHero(
                balance = state.balance,
                hidden = state.balanceHidden,
                currencyCode = currencyCode,
                onToggle = viewModel::toggleBalanceVisibility
            )
        }

        /* ============ MINI STATS ============ */
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStat(
                    modifier = Modifier.weight(1f),
                    title = "Pemasukan",
                    value = state.totalIncome,
                    tone = StatTone.INCOME,
                    currencyCode = currencyCode,
                    hidden = state.balanceHidden
                )
                MiniStat(
                    modifier = Modifier.weight(1f),
                    title = "Pengeluaran",
                    value = state.totalExpense,
                    tone = StatTone.EXPENSE,
                    currencyCode = currencyCode,
                    hidden = state.balanceHidden
                )
            }
        }

        /* ============ QUICK ACTIONS ============ */
        item {
            QuickActionsRow(
                onAddIncome = { onAddTransaction("income") },
                onAddExpense = { onAddTransaction("expense") }
            )
        }

        /* ============ ARUS KAS HEADER ============ */
        item {
            SectionHeader(
                title = "Arus Kas",
                subtitle = "Ringkasan 7 hari terakhir",
                actionLabel = "Laporan",
                onAction = onNavigateToReport
            )
        }

        /* ============ ARUS KAS CARD ============ */
        item {
            CashFlowSection(
                chartData = state.chartData,
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense,
                hidden = state.balanceHidden,
                currencyCode = currencyCode,
                onOpenReport = onNavigateToReport
            )
        }

        /* ============ TRANSAKSI TERBARU HEADER ============ */
        item {
            SectionHeader(
                title = "Transaksi Terbaru",
                subtitle = if (state.recentTransactions.isEmpty()) "Belum ada aktivitas"
                else "${state.txCount} transaksi total",
                actionLabel = "Lihat semua",
                onAction = onNavigateToTx
            )
        }

        item {
            RecentTransactions(
                transactions = state.recentTransactions,
                currencyCode = currencyCode,
                onSeeAll = onNavigateToTx
            )
        }

        /* ============ UPGRADE CARD ============ */
        if (!isBusinessPlan) {
            item {
                UpgradeCard(
                    currentPlan = state.planConfig?.label ?: "Free",
                    onClick = onNavigateToPlans
                )
            }
        }
    }
}

/* ============================================================
   HEADER
   ============================================================ */
@Composable
private fun HeaderSection(
    userName: String,
    pendingImportsCount: Int,
    onImportsClick: () -> Unit
) {
    val accent = LocalAccent.current
    val firstName = userName.split(" ").firstOrNull()?.ifBlank { "User" } ?: "User"
    val initials = userName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "U" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greetingByTime(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent.base,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = firstName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.6).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Semoga harimu produktif",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (pendingImportsCount > 0) accent.base.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onImportsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Transaksi Terdeteksi",
                    tint = if (pendingImportsCount > 0) accent.base
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (pendingImportsCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE11D48))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.background,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (pendingImportsCount > 99) "99+"
                        else pendingImportsCount.toString(),
                        fontSize = if (pendingImportsCount > 9) 8.sp else 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(accent.base, accent.hover)
                    )
                )
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accent.text
                )
            }
        }
    }
}

private fun greetingByTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 11 -> "SELAMAT PAGI"
        hour < 15 -> "SELAMAT SIANG"
        hour < 18 -> "SELAMAT SORE"
        else -> "SELAMAT MALAM"
    }
}

/* ============================================================
   CASH FLOW SECTION
   ============================================================ */
@Composable
private fun CashFlowSection(
    chartData: List<ChartPoint>,
    totalIncome: Long,
    totalExpense: Long,
    hidden: Boolean,
    currencyCode: String,
    onOpenReport: () -> Unit
) {
    val accent = LocalAccent.current

    val incomeSum = if (totalIncome > 0) totalIncome else chartData.sumOf { it.income }
    val expenseSum = if (totalExpense > 0) totalExpense else chartData.sumOf { it.expense }
    val netSum = incomeSum - expenseSum
    val isPositive = netSum >= 0

    val maxVal = chartData.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L
    val peakLabel = chartData.maxByOrNull { it.income + it.expense }?.label ?: "-"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            CashFlowChart(
                modifier = Modifier.fillMaxSize(),
                data = chartData
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.ArrowUpward,
                label = "Masuk",
                value = incomeSum,
                color = Color(0xFF059669),
                hidden = hidden,
                currencyCode = currencyCode
            )
            InsightChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.ArrowDownward,
                label = "Keluar",
                value = expenseSum,
                color = Color(0xFFE11D48),
                hidden = hidden,
                currencyCode = currencyCode
            )
            InsightChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.TrendingUp,
                label = "Net",
                value = netSum,
                color = if (isPositive) Color(0xFF4F46E5) else Color(0xFFE11D48),
                hidden = hidden,
                currencyCode = currencyCode,
                isSigned = true
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(accent.soft.copy(alpha = 0.6f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.base),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPositive) "Kondisi keuangan sehat" else "Pengeluaran lebih tinggi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = accent.text
                )
                Text(
                    text = if (maxVal > 0) "Puncak: $peakLabel" else "Belum ada aktivitas minggu ini",
                    fontSize = 10.sp,
                    color = accent.text.copy(alpha = 0.75f)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.base)
                    .clickable { onOpenReport() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Detail",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun InsightChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Long,
    color: Color,
    hidden: Boolean,
    currencyCode: String,
    modifier: Modifier = Modifier,
    isSigned: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.4.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (hidden) "•••••"
            else buildString {
                if (isSigned && value >= 0) append("+")
                append(CurrencyFormatter.format(value, currencyCode))
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

/* ============================================================
   UPGRADE CARD
   ============================================================ */
@Composable
private fun UpgradeCard(
    currentPlan: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Upgrade ke Pro",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "100 transaksi, export CSV, tema kustom",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Mulai",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4F46E5)
                    )
                }
            }
        }
    }
}