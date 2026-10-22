package com.ramzi.cashflow.presentation.dashboard.business

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.presentation.dashboard.business.components.BudgetCard
import com.ramzi.cashflow.presentation.dashboard.business.components.BudgetModal
import com.ramzi.cashflow.presentation.dashboard.business.components.CustomerModal
import com.ramzi.cashflow.presentation.dashboard.business.components.CustomerSection
import com.ramzi.cashflow.presentation.dashboard.business.components.ForecastCard
import com.ramzi.cashflow.presentation.dashboard.business.components.HealthScoreCard
import com.ramzi.cashflow.presentation.dashboard.business.components.InvoiceDeleteConfirmDialog
import com.ramzi.cashflow.presentation.dashboard.business.components.InvoiceDetailDialog
import com.ramzi.cashflow.presentation.dashboard.business.components.InvoiceModal
import com.ramzi.cashflow.presentation.dashboard.business.components.InvoiceSection
import com.ramzi.cashflow.presentation.dashboard.business.components.RecurringModal
import com.ramzi.cashflow.presentation.dashboard.business.components.RecurringSection
import com.ramzi.cashflow.presentation.dashboard.business.components.TaxCalculatorCard
import com.ramzi.cashflow.presentation.dashboard.business.components.WorkspaceModal
import com.ramzi.cashflow.presentation.dashboard.business.components.WorkspaceSection

@Composable
fun BusinessScreen(
    viewModel: BusinessViewModel = viewModel(factory = BusinessViewModelFactory)
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadAll() }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.loading && state.user == null) {
            BusinessLoadingState()
            return@Box
        }

        if (state.errorMessage != null && state.user == null) {
            BusinessErrorState(
                message = state.errorMessage!!,
                onRetry = { viewModel.loadAll() }
            )
            return@Box
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 16.dp, bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /* ============ HEADER ============ */
            item {
                BusinessHeader(
                    userName = state.user?.name ?: "",
                    workspaceName = state.currentWorkspace.name
                )
            }

            /* ============ TAB BAR ============ */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BusinessHubTab.entries.forEach { tab ->
                        val active = state.selectedTab == tab
                        val badge = when (tab) {
                            BusinessHubTab.INVOICES -> if (state.invoices.isNotEmpty()) "${state.invoices.size}" else null
                            BusinessHubTab.RECURRING -> if (state.pendingRecurringCount > 0) "${state.pendingRecurringCount}" else null
                            BusinessHubTab.WORKSPACES -> if (state.allWorkspaces.size > 1) "${state.allWorkspaces.size}" else null
                            BusinessHubTab.TAX_CUSTOMERS -> if (state.customers.isNotEmpty()) "${state.customers.size}" else null
                            else -> null
                        }
                        BusinessTabChip(
                            label = tab.label,
                            active = active,
                            badge = badge,
                            onClick = { viewModel.selectTab(tab) }
                        )
                    }
                }
            }

            /* ============ TAB CONTENT ============ */
            when (state.selectedTab) {
                BusinessHubTab.OVERVIEW -> {
                    /* ============ QUICK STATS ============ */
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Filled.Repeat,
                                label = "Recurring",
                                value = "${state.activeRecurringCount}",
                                subValue = "${state.pendingRecurringCount} jatuh tempo",
                                accent = Color(0xFF6366F1),
                                onClick = { viewModel.selectTab(BusinessHubTab.RECURRING) }
                            )
                            QuickStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Filled.TrendingUp,
                                label = "Transaksi",
                                value = "${state.txsInWorkspace.size}",
                                subValue = "Tercatat",
                                accent = Color(0xFF0891B2),
                                onClick = { }
                            )
                        }
                    }

                    /* ============ BUDGET ============ */
                    item {
                        BudgetCard(
                            monthlyTarget = state.budgetMonthlyTarget,
                            currentExpense = state.budgetExpense,
                            currentIncome = state.budgetIncome,
                            onEditClick = { viewModel.openBudgetModal() }
                        )
                    }

                    /* ============ HEALTH SCORE ============ */
                    item {
                        HealthScoreCard(health = state.healthScore)
                    }

                    /* ============ FORECAST ============ */
                    item {
                        ForecastCard(forecast = state.forecast)
                    }

                    /* ============ QUICK ACTIONS ============ */
                    item {
                        QuickActionBar(
                            onAddRecurring = { viewModel.openRecurringModal() },
                            onRefresh = { viewModel.loadAll() }
                        )
                    }
                }

                BusinessHubTab.INVOICES -> {
                    item {
                        InvoiceSection(
                            invoices = state.invoices,
                            filteredInvoices = state.filteredInvoices,
                            selectedIds = state.selectedInvoiceIds,
                            isBatchUpdating = state.isBatchUpdatingInvoices,
                            searchQuery = state.invoiceSearchQuery,
                            statusFilter = state.invoiceStatusFilter,
                            totalPendingAmount = state.totalPendingInvoiceAmount,
                            totalPaidAmount = state.totalPaidInvoiceAmount,
                            onSearchChange = { viewModel.setInvoiceSearchQuery(it) },
                            onFilterStatusChange = { viewModel.setInvoiceStatusFilter(it) },
                            onAdd = { viewModel.openInvoiceModal(null) },
                            onEdit = { inv -> viewModel.openInvoiceModal(inv) },
                            onViewDetail = { inv -> viewModel.viewInvoiceDetail(inv) },
                            onToggleSelect = { viewModel.toggleInvoiceSelection(it) },
                            onSelectAll = { viewModel.selectAllInvoices() },
                            onClearSelection = { viewModel.clearInvoiceSelection() },
                            onUpdateStatus = { id, status -> viewModel.updateInvoiceStatus(id, status) },
                            onDelete = { viewModel.requestDeleteInvoice(it) },
                            onBatchUpdate = { viewModel.batchUpdateInvoiceStatus(it) }
                        )
                    }
                }

                BusinessHubTab.RECURRING -> {
                    item {
                        QuickActionBar(
                            onAddRecurring = { viewModel.openRecurringModal() },
                            onRefresh = { viewModel.loadAll() }
                        )
                    }

                    item {
                        SectionHeader(
                            icon = Icons.Filled.Repeat,
                            title = "Transaksi Berulang",
                            subtitle = "Langganan, gaji, tagihan otomatis",
                            badge = if (state.pendingRecurringCount > 0)
                                "${state.pendingRecurringCount} jatuh tempo" else null,
                            onAction = { viewModel.openRecurringModal() },
                            actionLabel = "Tambah"
                        )
                    }

                    item {
                        RecurringSection(
                            recurringList = state.recurring,
                            onAdd = { viewModel.openRecurringModal() },
                            onEdit = { id -> viewModel.openRecurringModal(id) },
                            onToggle = { id, active -> viewModel.toggleRecurring(id, active) },
                            onDelete = { id -> viewModel.deleteRecurring(id) }
                        )
                    }
                }

                BusinessHubTab.WORKSPACES -> {
                    item {
                        WorkspaceSection(
                            workspaces = state.allWorkspaces,
                            currentWorkspaceId = state.currentWorkspaceId,
                            onSelect = { viewModel.setCurrentWorkspace(it) },
                            onAdd = { viewModel.openWorkspaceModal() },
                            onDelete = { viewModel.deleteWorkspace(it) }
                        )
                    }
                }

                BusinessHubTab.TAX_CUSTOMERS -> {
                    item {
                        TaxCalculatorCard(
                            dppText = state.taxDppText,
                            ppnEnabled = state.taxPpnEnabled,
                            pphEnabled = state.taxPphEnabled,
                            onDppChange = viewModel::setTaxDpp,
                            onPpnChange = viewModel::setTaxPpn,
                            onPphChange = viewModel::setTaxPph
                        )
                    }

                    item {
                        CustomerSection(
                            customers = state.customers,
                            onAdd = { viewModel.openCustomerModal() },
                            onDelete = { viewModel.deleteCustomer(it) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }

        /* ============ MODAL RECURRING ============ */
        if (state.recurringModalOpen) {
            RecurringModal(
                state = state,
                onDescChange = viewModel::setRecDesc,
                onAmountChange = viewModel::setRecAmountText,
                onTypeChange = viewModel::setRecType,
                onCategoryChange = viewModel::setRecCategory,
                onFrequencyChange = viewModel::setRecFrequency,
                onStartDateChange = viewModel::setRecStartDate,
                onEndDateChange = viewModel::setRecEndDate,
                onNoteChange = viewModel::setRecNote,
                onActiveChange = viewModel::setRecActive,
                onSave = viewModel::saveRecurring,
                onDismiss = viewModel::closeRecurringModal
            )
        }

        /* ============ MODAL BUDGET ============ */
        if (state.budgetModalOpen) {
            BudgetModal(
                currentInput = state.budgetInputText,
                isSaving = state.budgetSaving,
                errorMessage = state.budgetError,
                onInputChange = viewModel::setBudgetInput,
                onSave = viewModel::saveBudget,
                onDismiss = viewModel::closeBudgetModal
            )
        }

        /* ============ MODAL CUSTOMER ============ */
        if (state.customerModalOpen) {
            CustomerModal(
                name = state.custName,
                phone = state.custPhone,
                type = state.custType,
                isSaving = state.custSaving,
                errorMessage = state.custError,
                onNameChange = viewModel::setCustName,
                onPhoneChange = viewModel::setCustPhone,
                onTypeChange = viewModel::setCustType,
                onSave = viewModel::saveCustomer,
                onDismiss = viewModel::closeCustomerModal
            )
        }

        /* ============ MODAL WORKSPACE ============ */
        if (state.workspaceModalOpen) {
            WorkspaceModal(
                nameInput = state.wsNameInput,
                isSaving = state.wsSaving,
                errorMessage = state.wsError,
                onNameChange = viewModel::setWsNameInput,
                onSave = viewModel::saveWorkspace,
                onDismiss = viewModel::closeWorkspaceModal
            )
        }

        /* ============ MODAL INVOICE ============ */
        if (state.invoiceModalOpen) {
            InvoiceModal(
                clientName = state.invoiceClientName,
                invoiceNumber = state.invoiceNumberInput,
                amountText = state.invoiceAmountText,
                isSaving = state.invoiceSaving,
                errorMessage = state.invoiceError,
                isEditing = state.editingInvoice != null,
                onClientNameChange = viewModel::setInvoiceClientName,
                onInvoiceNumberChange = viewModel::setInvoiceNumberInput,
                onAmountChange = viewModel::setInvoiceAmountText,
                onSave = viewModel::saveInvoice,
                onDismiss = viewModel::closeInvoiceModal
            )
        }

        /* ============ MODAL INVOICE DETAIL ============ */
        state.viewingInvoiceDetail?.let { inv ->
            InvoiceDetailDialog(
                invoice = inv,
                onDismiss = { viewModel.viewInvoiceDetail(null) },
                onEdit = {
                    viewModel.viewInvoiceDetail(null)
                    viewModel.openInvoiceModal(inv)
                },
                onUpdateStatus = { viewModel.updateInvoiceStatus(inv.id, it) },
                onDelete = {
                    viewModel.viewInvoiceDetail(null)
                    viewModel.requestDeleteInvoice(inv.id)
                }
            )
        }

        /* ============ CONFIRM DELETE INVOICE ============ */
        state.deleteInvoiceConfirmId?.let { id ->
            val inv = state.invoices.firstOrNull { it.id == id }
            if (inv != null) {
                InvoiceDeleteConfirmDialog(
                    invoice = inv,
                    onConfirm = { viewModel.confirmDeleteInvoice() },
                    onDismiss = { viewModel.cancelDeleteInvoice() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(12.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface
            )
        }
    }
}

/* ============================================================
   HEADER
   ============================================================ */
@Composable
private fun BusinessHeader(
    userName: String,
    workspaceName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Business Hub",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${userName.split(' ').firstOrNull() ?: "User"} / $workspaceName",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "BUSINESS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

/* ============================================================
   QUICK STAT CARD
   ============================================================ */
@Composable
private fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subValue,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ============================================================
   QUICK ACTION BAR
   ============================================================ */
@Composable
private fun QuickActionBar(
    onAddRecurring: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                    )
                )
                .clickable { onAddRecurring() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Tambah Recurring",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/* ============================================================
   SECTION HEADER
   ============================================================ */
@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onAction: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onAction != null && actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )
            }
        }
    }
}

/* ============================================================
   COMING SOON
   ============================================================ */
@Composable
private fun ComingSoonCard(
    title: String,
    description: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

/* ============================================================
   LOADING
   ============================================================ */
@Composable
private fun BusinessLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF6366F1),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/* ============================================================
   ERROR
   ============================================================ */
@Composable
private fun BusinessErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Gagal Memuat Data",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                        )
                    )
                    .clickable { onRetry() }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Coba Lagi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BusinessTabChip(
    label: String,
    active: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (active) Color(0xFF6366F1)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (active) Color(0xFF6366F1) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
            )
            if (badge != null) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (active) Color.White.copy(alpha = 0.25f)
                            else Color(0xFF6366F1).copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (active) Color.White else Color(0xFF6366F1)
                    )
                }
            }
        }
    }
}