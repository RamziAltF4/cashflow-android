package com.ramzi.cashflow.presentation.dashboard.business

import com.ramzi.cashflow.domain.model.ActivityLog
import com.ramzi.cashflow.domain.model.BankConfig
import com.ramzi.cashflow.domain.model.Customer
import com.ramzi.cashflow.domain.model.CustomerType
import com.ramzi.cashflow.domain.model.ForecastResult
import com.ramzi.cashflow.domain.model.HealthScore
import com.ramzi.cashflow.domain.model.Invoice
import com.ramzi.cashflow.domain.model.InvoiceStatus
import com.ramzi.cashflow.domain.model.Recurring
import com.ramzi.cashflow.domain.model.RecurringFrequency
import com.ramzi.cashflow.domain.model.TaxResult
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.model.User
import com.ramzi.cashflow.domain.model.Workspace

enum class BusinessHubTab(val id: String, val label: String) {
    OVERVIEW("overview", "Ringkasan"),
    INVOICES("invoices", "Invoices"),
    RECURRING("recurring", "Langganan"),
    WORKSPACES("workspaces", "Workspace"),
    TAX_CUSTOMERS("tax_customers", "Pajak & Kontak")
}

data class BusinessState(

    /* ---------- LOADING ---------- */
    val loading: Boolean = true,
    val errorMessage: String? = null,

    /* ---------- USER ---------- */
    val user: User? = null,

    /* ---------- DATA ---------- */
    val txs: List<Transaction> = emptyList(),
    val recurring: List<Recurring> = emptyList(),
    val workspaces: List<Workspace> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val activities: List<ActivityLog> = emptyList(),
    val bankConfig: BankConfig = BankConfig.EMPTY,

    /* ---------- WORKSPACE ---------- */
    val currentWorkspaceId: String = Workspace.DEFAULT_ID,

    /* ---------- BUDGET ---------- */
    val budgetMonthlyTarget: Long = 0L,
    val budgetExpense: Long = 0L,
    val budgetIncome: Long = 0L,
    val budgetModalOpen: Boolean = false,
    val budgetInputText: String = "",
    val budgetSaving: Boolean = false,
    val budgetError: String? = null,

    /* ---------- TAX CALCULATOR ---------- */
    val taxDppText: String = "",
    val taxPpnEnabled: Boolean = true,
    val taxPphEnabled: Boolean = false,

    /* ---------- RECURRING MODAL ---------- */
    val recurringModalOpen: Boolean = false,
    val editingRecurringId: String? = null,
    val recDesc: String = "",
    val recAmountText: String = "",
    val recType: TxType = TxType.EXPENSE,
    val recCategory: String = "Tagihan",
    val recFrequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val recStartDate: String = "",
    val recEndDate: String = "",
    val recNote: String = "",
    val recActive: Boolean = true,
    val recError: String? = null,
    val recSaving: Boolean = false,

    /* ---------- CUSTOMER MODAL ---------- */
    val customerModalOpen: Boolean = false,
    val custName: String = "",
    val custPhone: String = "",
    val custType: CustomerType = CustomerType.CUSTOMER,
    val custSaving: Boolean = false,
    val custError: String? = null,

    /* ---------- WORKSPACE MODAL ---------- */
    val workspaceModalOpen: Boolean = false,
    val wsNameInput: String = "",
    val wsSaving: Boolean = false,
    val wsError: String? = null,

    /* ---------- BUSINESS HUB TAB ---------- */
    val selectedTab: BusinessHubTab = BusinessHubTab.OVERVIEW,

    /* ---------- INVOICE MODAL & BATCH ---------- */
    val invoiceModalOpen: Boolean = false,
    val editingInvoice: Invoice? = null,
    val viewingInvoiceDetail: Invoice? = null,
    val deleteInvoiceConfirmId: String? = null,
    val invoiceSearchQuery: String = "",
    val invoiceStatusFilter: InvoiceStatus? = null,
    val invoiceClientName: String = "",
    val invoiceNumberInput: String = "",
    val invoiceAmountText: String = "",
    val invoiceSaving: Boolean = false,
    val invoiceError: String? = null,
    val selectedInvoiceIds: Set<String> = emptySet(),
    val isBatchUpdatingInvoices: Boolean = false,

    /* ---------- UI TOAST ---------- */
    val toastMessage: String? = null,
    val toastType: String = "success"
) {

    val filteredInvoices: List<Invoice>
        get() = invoices.filter { inv ->
            val matchesQuery = invoiceSearchQuery.isBlank() ||
                inv.invoiceNumber.contains(invoiceSearchQuery, ignoreCase = true) ||
                inv.clientName.contains(invoiceSearchQuery, ignoreCase = true)
            val matchesStatus = invoiceStatusFilter == null || inv.status == invoiceStatusFilter
            matchesQuery && matchesStatus
        }

    val totalPendingInvoiceAmount: Long
        get() = invoices.filter { it.status == InvoiceStatus.PENDING }.sumOf { it.amount }

    val totalPaidInvoiceAmount: Long
        get() = invoices.filter { it.status == InvoiceStatus.PAID }.sumOf { it.amount }

    /* ============================================================
       COMPUTED: workspace filter
       ============================================================ */
    val txsInWorkspace: List<Transaction>
        get() = if (currentWorkspaceId == Workspace.DEFAULT_ID) {
            txs.filter { it.workspaceId.isBlank() || it.workspaceId == Workspace.DEFAULT_ID }
        } else {
            txs.filter { it.workspaceId == currentWorkspaceId }
        }

    val income: Long
        get() = txsInWorkspace.filter { it.type == TxType.INCOME }.sumOf { it.amount }

    val expense: Long
        get() = txsInWorkspace.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }

    val balance: Long
        get() = income - expense

    /* ============================================================
       COMPUTED: recurring
       ============================================================ */
    val pendingRecurringCount: Int
        get() = recurring.count { it.isDue }

    val activeRecurringCount: Int
        get() = recurring.count { it.active }

    /* ============================================================
       COMPUTED: tax result (dari taxDppText)
       ============================================================ */
    val taxResult: TaxResult
        get() {
            val dpp = taxDppText.filter { it.isDigit() }.toLongOrNull() ?: 0L
            return TaxResult(
                dpp = dpp,
                ppnEnabled = taxPpnEnabled,
                pphEnabled = taxPphEnabled
            )
        }

    /* ============================================================
       COMPUTED: forecast (dari txsInWorkspace)
       ============================================================ */
    val forecast: ForecastResult
        get() = ForecastResult.calculate(txsInWorkspace, lookbackDays = 30)

    /* ============================================================
       COMPUTED: health score (dari txsInWorkspace)
       ============================================================ */
    val healthScore: HealthScore
        get() {
            val totalIncome = txsInWorkspace
                .filter { it.type == TxType.INCOME }
                .sumOf { it.amount }
            val totalExpense = txsInWorkspace
                .filter { it.type == TxType.EXPENSE }
                .sumOf { it.amount }
            val incomeDays = txsInWorkspace
                .filter { it.type == TxType.INCOME }
                .map { it.date }
                .distinct()
                .size
            return HealthScore.calculate(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                incomeDays = incomeDays,
                txCount = txsInWorkspace.size
            )
        }

    /* ============================================================
       COMPUTED: workspaces
       ============================================================ */
    val currentWorkspace: Workspace
        get() = if (currentWorkspaceId == Workspace.DEFAULT_ID) {
            Workspace.DEFAULT
        } else {
            workspaces.firstOrNull { it.id == currentWorkspaceId } ?: Workspace.DEFAULT
        }

    val allWorkspaces: List<Workspace>
        get() = listOf(Workspace.DEFAULT) + workspaces
}