package com.ramzi.cashflow.presentation.dashboard.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.Customer
import com.ramzi.cashflow.domain.model.CustomerType
import com.ramzi.cashflow.domain.model.Recurring
import com.ramzi.cashflow.domain.model.RecurringFrequency
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.model.Workspace
import com.ramzi.cashflow.domain.model.Invoice
import com.ramzi.cashflow.domain.model.InvoiceStatus
import com.ramzi.cashflow.domain.repository.ActivityLogRepository
import com.ramzi.cashflow.domain.repository.ConfigRepository
import com.ramzi.cashflow.domain.repository.CustomerRepository
import com.ramzi.cashflow.domain.repository.InvoiceRepository
import com.ramzi.cashflow.domain.repository.RecurringRepository
import com.ramzi.cashflow.domain.repository.UserRepository
import com.ramzi.cashflow.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BusinessViewModel(
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository,
    private val recurringRepository: RecurringRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val activityRepository: ActivityLogRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(BusinessState())
    val state: StateFlow<BusinessState> = _state.asStateFlow()

    private val uid: String?
        get() = firebaseAuth.currentUser?.uid

    /* ============================================================
       INIT
       ============================================================ */
    init {
        loadAll()
    }

    fun loadAll() {
        val userId = uid ?: run {
            _state.update { it.copy(loading = false, errorMessage = "Sesi tidak valid.") }
            return
        }

        _state.update { it.copy(loading = true, errorMessage = null) }

        // 1) User
        viewModelScope.launch {
            try {
                val user = userRepository.getById(userId)
                _state.update {
                    it.copy(
                        user = user,
                        budgetMonthlyTarget = user?.budget ?: 0L
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }

        // 2) Bank config
        viewModelScope.launch {
            val bank = configRepository.getBankConfig()
            _state.update { it.copy(bankConfig = bank) }
        }

        // 3) Recurring
        viewModelScope.launch {
            recurringRepository.observeAll(userId).collect { list ->
                _state.update { it.copy(recurring = list) }
            }
        }

        // 4) Workspaces
        viewModelScope.launch {
            workspaceRepository.observeAll(userId).collect { list ->
                _state.update { it.copy(workspaces = list) }
            }
        }

        // 5) Customers
        viewModelScope.launch {
            customerRepository.observeAll(userId).collect { list ->
                _state.update { it.copy(customers = list) }
            }
        }

        // 5b) Invoices
        viewModelScope.launch {
            invoiceRepository.observeAll(userId).collect { list ->
                _state.update { it.copy(invoices = list) }
            }
        }

        // 6) Transactions + budget snapshot
        viewModelScope.launch {
            loadTransactionsInternal(userId)
        }

        // 7) Activity logs
        viewModelScope.launch {
            val list = activityRepository.getRecent(userId, 20)
            _state.update { it.copy(activities = list) }
        }

        // 8) Proses recurring yang due
        viewModelScope.launch {
            try {
                val today = todayIso()
                val result = recurringRepository.processDue(userId, today)
                if (result.createdCount > 0) {
                    showToast("${result.createdCount} transaksi berulang dibuat.", "success")
                    loadTransactionsInternal(userId)
                }
            } catch (e: Exception) {
                android.util.Log.w("BusinessVM", "processDue error", e)
            }
        }
    }

    private suspend fun loadTransactionsInternal(userId: String) {
        try {
            val snap = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val txs = snap.documents.mapNotNull { d ->
                val data = d.data ?: return@mapNotNull null
                Transaction(
                    id = d.id,
                    userId = data["userId"] as? String ?: "",
                    type = TxType.fromId(data["type"] as? String),
                    desc = data["desc"] as? String ?: "",
                    amount = (data["amount"] as? Number)?.toLong() ?: 0L,
                    category = data["category"] as? String ?: "",
                    date = data["date"] as? String ?: "",
                    note = data["note"] as? String ?: "",
                    workspaceId = data["workspaceId"] as? String ?: ""
                )
            }
            val monthKey = monthKey()
            val monthExpense = txs
                .filter { it.type == TxType.EXPENSE && it.date.startsWith(monthKey) }
                .sumOf { it.amount }
            val monthIncome = txs
                .filter { it.type == TxType.INCOME && it.date.startsWith(monthKey) }
                .sumOf { it.amount }

            _state.update {
                it.copy(
                    txs = txs,
                    budgetExpense = monthExpense,
                    budgetIncome = monthIncome,
                    loading = false
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, errorMessage = e.message) }
        }
    }

    /* ============================================================
       WORKSPACE
       ============================================================ */
    fun setCurrentWorkspace(id: String) {
        _state.update { it.copy(currentWorkspaceId = id) }
    }

    fun openWorkspaceModal() {
        _state.update {
            it.copy(
                workspaceModalOpen = true,
                wsNameInput = "",
                wsError = null
            )
        }
    }

    fun closeWorkspaceModal() {
        if (_state.value.wsSaving) return
        _state.update { it.copy(workspaceModalOpen = false, wsError = null) }
    }

    fun setWsNameInput(v: String) =
        _state.update { it.copy(wsNameInput = v, wsError = null) }

    fun saveWorkspace() {
        val userId = uid ?: return
        val name = _state.value.wsNameInput.trim()
        if (name.isBlank()) {
            _state.update { it.copy(wsError = "Nama workspace tidak boleh kosong.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(wsSaving = true, wsError = null) }
            try {
                workspaceRepository.create(userId, name)
                activityRepository.log(userId, "create_workspace", "Workspace: $name")
                _state.update { it.copy(wsSaving = false, workspaceModalOpen = false) }
                showToast("Workspace \"$name\" dibuat.", "success")
            } catch (e: Exception) {
                _state.update { it.copy(wsSaving = false, wsError = e.message) }
            }
        }
    }

    fun deleteWorkspace(id: String) {
        if (id == Workspace.DEFAULT_ID) return
        val userId = uid ?: return
        viewModelScope.launch {
            try {
                workspaceRepository.delete(id)
                if (_state.value.currentWorkspaceId == id) {
                    _state.update { it.copy(currentWorkspaceId = Workspace.DEFAULT_ID) }
                }
                activityRepository.log(userId, "delete_workspace", "Hapus workspace $id")
                showToast("Workspace dihapus.", "warn")
            } catch (e: Exception) {
                showToast("Gagal: ${e.message}", "error")
            }
        }
    }

    /* ============================================================
       BUDGET
       ============================================================ */
    fun openBudgetModal() {
        val current = _state.value.budgetMonthlyTarget
        _state.update {
            it.copy(
                budgetModalOpen = true,
                budgetInputText = if (current > 0) formatThousands(current) else "",
                budgetError = null
            )
        }
    }

    fun closeBudgetModal() {
        if (_state.value.budgetSaving) return
        _state.update { it.copy(budgetModalOpen = false, budgetError = null) }
    }

    fun setBudgetInput(v: String) {
        val digits = v.filter { it.isDigit() }.take(15)
        val formatted = if (digits.isBlank()) "" else formatThousands(digits.toLong())
        _state.update { it.copy(budgetInputText = formatted, budgetError = null) }
    }

    fun saveBudget() {
        val userId = uid ?: return
        val amount = _state.value.budgetInputText.filter { it.isDigit() }.toLongOrNull() ?: 0L

        viewModelScope.launch {
            _state.update { it.copy(budgetSaving = true, budgetError = null) }
            try {
                userRepository.updateBudget(userId, amount)
                activityRepository.log(
                    userId,
                    "update_budget",
                    if (amount > 0) "Set budget: $amount" else "Hapus budget"
                )
                _state.update {
                    it.copy(
                        budgetSaving = false,
                        budgetModalOpen = false,
                        budgetMonthlyTarget = amount
                    )
                }
                showToast(
                    if (amount > 0) "Budget disimpan." else "Budget dinonaktifkan.",
                    "success"
                )
            } catch (e: Exception) {
                _state.update { it.copy(budgetSaving = false, budgetError = e.message) }
            }
        }
    }

    /* ============================================================
       TAX CALCULATOR
       ============================================================ */
    fun setTaxDpp(v: String) {
        val digits = v.filter { it.isDigit() }.take(15)
        val formatted = if (digits.isBlank()) "" else formatThousands(digits.toLong())
        _state.update { it.copy(taxDppText = formatted) }
    }

    fun setTaxPpn(enabled: Boolean) =
        _state.update { it.copy(taxPpnEnabled = enabled) }

    fun setTaxPph(enabled: Boolean) =
        _state.update { it.copy(taxPphEnabled = enabled) }

    /* ============================================================
       CUSTOMER
       ============================================================ */
    fun openCustomerModal() {
        _state.update {
            it.copy(
                customerModalOpen = true,
                custName = "",
                custPhone = "",
                custType = CustomerType.CUSTOMER,
                custError = null
            )
        }
    }

    fun closeCustomerModal() {
        if (_state.value.custSaving) return
        _state.update { it.copy(customerModalOpen = false, custError = null) }
    }

    fun setCustName(v: String) = _state.update { it.copy(custName = v, custError = null) }
    fun setCustPhone(v: String) = _state.update { it.copy(custPhone = v) }
    fun setCustType(t: CustomerType) = _state.update { it.copy(custType = t) }

    fun saveCustomer() {
        val userId = uid ?: return
        val name = _state.value.custName.trim()
        if (name.isBlank()) {
            _state.update { it.copy(custError = "Nama tidak boleh kosong.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(custSaving = true, custError = null) }
            try {
                customerRepository.create(
                    Customer(
                        userId = userId,
                        name = name,
                        phone = _state.value.custPhone.trim(),
                        type = _state.value.custType
                    )
                )
                activityRepository.log(userId, "create_customer", name)
                _state.update { it.copy(custSaving = false, customerModalOpen = false) }
                showToast("Kontak ditambahkan.", "success")
            } catch (e: Exception) {
                _state.update { it.copy(custSaving = false, custError = e.message) }
            }
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch {
            try {
                customerRepository.delete(id)
                showToast("Kontak dihapus.", "warn")
            } catch (e: Exception) {
                showToast("Gagal: ${e.message}", "error")
            }
        }
    }

    /* ============================================================
       TAB NAVIGATION
       ============================================================ */
    fun selectTab(tab: BusinessHubTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    /* ============================================================
       INVOICE & BATCH ACTIONS
       ============================================================ */
    fun setInvoiceSearchQuery(q: String) {
        _state.update { it.copy(invoiceSearchQuery = q) }
    }

    fun setInvoiceStatusFilter(status: InvoiceStatus?) {
        _state.update { it.copy(invoiceStatusFilter = status) }
    }

    fun viewInvoiceDetail(invoice: Invoice?) {
        _state.update { it.copy(viewingInvoiceDetail = invoice) }
    }

    fun openInvoiceModal(invoice: Invoice? = null) {
        _state.update {
            it.copy(
                invoiceModalOpen = true,
                editingInvoice = invoice,
                invoiceClientName = invoice?.clientName ?: "",
                invoiceNumberInput = invoice?.invoiceNumber ?: "INV-${System.currentTimeMillis().toString().takeLast(6)}",
                invoiceAmountText = if (invoice != null && invoice.amount > 0) formatThousands(invoice.amount) else "",
                invoiceError = null,
                invoiceSaving = false
            )
        }
    }

    fun closeInvoiceModal() {
        if (_state.value.invoiceSaving) return
        _state.update {
            it.copy(
                invoiceModalOpen = false,
                editingInvoice = null,
                invoiceError = null
            )
        }
    }

    fun setInvoiceClientName(v: String) = _state.update { it.copy(invoiceClientName = v, invoiceError = null) }
    fun setInvoiceNumberInput(v: String) = _state.update { it.copy(invoiceNumberInput = v, invoiceError = null) }
    fun setInvoiceAmountText(v: String) {
        val digits = v.filter { it.isDigit() }.take(15)
        val formatted = if (digits.isBlank()) "" else formatThousands(digits.toLong())
        _state.update { it.copy(invoiceAmountText = formatted, invoiceError = null) }
    }

    fun saveInvoice() {
        val userId = uid ?: return
        val client = _state.value.invoiceClientName.trim()
        val num = _state.value.invoiceNumberInput.trim()
        val amount = _state.value.invoiceAmountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        val editing = _state.value.editingInvoice

        if (client.isBlank()) {
            _state.update { it.copy(invoiceError = "Nama klien tidak boleh kosong.") }
            return
        }
        if (num.isBlank()) {
            _state.update { it.copy(invoiceError = "Nomor invoice tidak boleh kosong.") }
            return
        }
        if (amount <= 0) {
            _state.update { it.copy(invoiceError = "Nominal harus lebih dari 0.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(invoiceSaving = true, invoiceError = null) }
            try {
                if (editing != null) {
                    val updated = editing.copy(
                        clientName = client,
                        invoiceNumber = num,
                        amount = amount
                    )
                    invoiceRepository.update(updated)
                    activityRepository.log(userId, "update_invoice", "Edit Invoice: $num ($client)")
                    _state.update {
                        it.copy(
                            invoiceSaving = false,
                            invoiceModalOpen = false,
                            editingInvoice = null,
                            viewingInvoiceDetail = if (it.viewingInvoiceDetail?.id == updated.id) updated else it.viewingInvoiceDetail
                        )
                    }
                    showToast("Invoice \"$num\" berhasil diperbarui.", "success")
                } else {
                    invoiceRepository.create(
                        Invoice(
                            userId = userId,
                            invoiceNumber = num,
                            clientName = client,
                            amount = amount,
                            status = InvoiceStatus.PENDING,
                            date = System.currentTimeMillis()
                        )
                    )
                    activityRepository.log(userId, "create_invoice", "Invoice: $num ($client)")
                    _state.update { it.copy(invoiceSaving = false, invoiceModalOpen = false) }
                    showToast("Invoice \"$num\" berhasil dibuat.", "success")
                }
            } catch (e: Exception) {
                _state.update { it.copy(invoiceSaving = false, invoiceError = e.message) }
            }
        }
    }

    fun updateInvoiceStatus(id: String, status: InvoiceStatus) {
        viewModelScope.launch {
            try {
                invoiceRepository.updateStatus(id, status.id)
                _state.update { s ->
                    val updatedDetail = if (s.viewingInvoiceDetail?.id == id) {
                        s.viewingInvoiceDetail.copy(status = status)
                    } else s.viewingInvoiceDetail
                    s.copy(viewingInvoiceDetail = updatedDetail)
                }
                showToast("Status invoice diubah ke ${status.label}.", "info")
            } catch (e: Exception) {
                showToast("Gagal update status: ${e.message}", "error")
            }
        }
    }

    fun requestDeleteInvoice(id: String) {
        _state.update { it.copy(deleteInvoiceConfirmId = id) }
    }

    fun cancelDeleteInvoice() {
        _state.update { it.copy(deleteInvoiceConfirmId = null) }
    }

    fun confirmDeleteInvoice() {
        val id = _state.value.deleteInvoiceConfirmId ?: return
        _state.update { it.copy(deleteInvoiceConfirmId = null) }

        viewModelScope.launch {
            try {
                invoiceRepository.delete(id)
                _state.update { s ->
                    s.copy(
                        selectedInvoiceIds = s.selectedInvoiceIds - id,
                        viewingInvoiceDetail = if (s.viewingInvoiceDetail?.id == id) null else s.viewingInvoiceDetail
                    )
                }
                showToast("Invoice berhasil dihapus.", "warn")
            } catch (e: Exception) {
                showToast("Gagal hapus invoice: ${e.message}", "error")
            }
        }
    }

    fun toggleInvoiceSelection(id: String) {
        _state.update { s ->
            val set = s.selectedInvoiceIds.toMutableSet()
            if (set.contains(id)) set.remove(id) else set.add(id)
            s.copy(selectedInvoiceIds = set)
        }
    }

    fun selectAllInvoices() {
        _state.update { s ->
            val allIds = s.invoices.map { it.id }.toSet()
            s.copy(selectedInvoiceIds = allIds)
        }
    }

    fun clearInvoiceSelection() {
        _state.update { it.copy(selectedInvoiceIds = emptySet()) }
    }

    fun batchUpdateInvoiceStatus(targetStatus: InvoiceStatus) {
        val selected = _state.value.selectedInvoiceIds.toList()
        if (selected.isEmpty()) {
            showToast("Pilih setidaknya 1 invoice untuk diperbarui.", "warn")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isBatchUpdatingInvoices = true) }
            try {
                invoiceRepository.batchUpdateStatus(selected, targetStatus.id)
                val count = selected.size
                _state.update {
                    it.copy(
                        isBatchUpdatingInvoices = false,
                        selectedInvoiceIds = emptySet()
                    )
                }
                showToast("Batch Update Sukses: $count invoice ditandai ${targetStatus.label}.", "success")
                uid?.let { u ->
                    activityRepository.log(u, "batch_update_invoice", "Batch update $count invoice ke ${targetStatus.label}")
                }
            } catch (e: Exception) {
                _state.update { it.copy(isBatchUpdatingInvoices = false) }
                showToast("Batch Update gagal: ${e.message}", "error")
            }
        }
    }

    /* ============================================================
       RECURRING
       ============================================================ */
    fun openRecurringModal(editId: String? = null) {
        val existing = if (editId != null) _state.value.recurring.find { it.id == editId } else null

        _state.update {
            it.copy(
                recurringModalOpen = true,
                editingRecurringId = editId,
                recDesc = existing?.desc ?: "",
                recAmountText = existing?.amount?.let { v ->
                    if (v > 0) formatThousands(v) else ""
                } ?: "",
                recType = existing?.type ?: TxType.EXPENSE,
                recCategory = existing?.category ?: "Tagihan",
                recFrequency = existing?.frequency ?: RecurringFrequency.MONTHLY,
                recStartDate = existing?.startDate ?: todayIso(),
                recEndDate = existing?.endDate ?: "",
                recNote = existing?.note ?: "",
                recActive = existing?.active ?: true,
                recError = null,
                recSaving = false
            )
        }
    }

    fun closeRecurringModal() {
        if (_state.value.recSaving) return
        _state.update { it.copy(recurringModalOpen = false, recError = null) }
    }

    fun setRecDesc(v: String) = _state.update { it.copy(recDesc = v) }

    fun setRecAmountText(v: String) {
        val digits = v.filter { it.isDigit() }.take(15)
        val formatted = if (digits.isBlank()) "" else formatThousands(digits.toLong())
        _state.update { it.copy(recAmountText = formatted, recError = null) }
    }

    fun setRecType(t: TxType) = _state.update { it.copy(recType = t) }
    fun setRecCategory(c: String) = _state.update { it.copy(recCategory = c) }
    fun setRecFrequency(f: RecurringFrequency) = _state.update { it.copy(recFrequency = f) }
    fun setRecStartDate(d: String) = _state.update { it.copy(recStartDate = d) }
    fun setRecEndDate(d: String) = _state.update { it.copy(recEndDate = d) }
    fun setRecNote(n: String) = _state.update { it.copy(recNote = n) }
    fun setRecActive(a: Boolean) = _state.update { it.copy(recActive = a) }

    fun saveRecurring() {
        val s = _state.value
        val userId = uid ?: return

        val desc = s.recDesc.trim()
        val amount = s.recAmountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        val start = s.recStartDate.trim()

        if (desc.isBlank()) {
            _state.update { it.copy(recError = "Isi deskripsi transaksi.") }
            return
        }
        if (amount <= 0) {
            _state.update { it.copy(recError = "Nominal harus lebih dari 0.") }
            return
        }
        if (start.isBlank()) {
            _state.update { it.copy(recError = "Pilih tanggal mulai.") }
            return
        }
        if (s.recEndDate.isNotBlank() && s.recEndDate < start) {
            _state.update { it.copy(recError = "Tanggal berhenti tidak boleh sebelum tanggal mulai.") }
            return
        }

        val today = todayIso()
        var nextRun = start
        if (start <= today) {
            nextRun = nextRunDate(start, s.recFrequency)
            var guard = 0
            while (nextRun <= today && guard < 500) {
                nextRun = nextRunDate(nextRun, s.recFrequency)
                guard++
            }
        }

        val editingId = s.editingRecurringId

        viewModelScope.launch {
            _state.update { it.copy(recSaving = true, recError = null) }
            try {
                if (editingId != null) {
                    val old = s.recurring.find { it.id == editingId }
                    val updated = Recurring(
                        id = editingId,
                        userId = old?.userId ?: userId,
                        desc = desc,
                        amount = amount,
                        type = s.recType,
                        category = s.recCategory,
                        frequency = s.recFrequency,
                        startDate = start,
                        endDate = s.recEndDate.ifBlank { null },
                        nextRun = old?.nextRun ?: nextRun,
                        lastRun = old?.lastRun,
                        active = s.recActive,
                        note = s.recNote.trim()
                    )
                    recurringRepository.update(updated)
                    activityRepository.log(userId, "update_recurring", desc)
                    showToast("Transaksi berulang diperbarui.", "success")
                } else {
                    val newRec = Recurring(
                        userId = userId,
                        desc = desc,
                        amount = amount,
                        type = s.recType,
                        category = s.recCategory,
                        frequency = s.recFrequency,
                        startDate = start,
                        endDate = s.recEndDate.ifBlank { null },
                        nextRun = nextRun,
                        active = true,
                        note = s.recNote.trim()
                    )
                    recurringRepository.create(newRec)
                    activityRepository.log(
                        userId,
                        "create_recurring",
                        "$desc - ${s.recFrequency.label}"
                    )
                    showToast("Transaksi berulang dibuat.", "success")
                }
                _state.update { it.copy(recSaving = false, recurringModalOpen = false) }
            } catch (e: Exception) {
                _state.update { it.copy(recSaving = false, recError = e.message) }
            }
        }
    }

    fun toggleRecurring(id: String, active: Boolean) {
        viewModelScope.launch {
            try {
                recurringRepository.toggleActive(id, active)
                showToast(if (active) "Diaktifkan." else "Dinonaktifkan.", "info")
            } catch (e: Exception) {
                showToast("Gagal: ${e.message}", "error")
            }
        }
    }

    fun deleteRecurring(id: String) {
        val userId = uid ?: return
        viewModelScope.launch {
            try {
                recurringRepository.delete(id)
                activityRepository.log(userId, "delete_recurring", "Hapus $id")
                showToast("Dihapus.", "warn")
            } catch (e: Exception) {
                showToast("Gagal: ${e.message}", "error")
            }
        }
    }

    /* ============================================================
       TOAST
       ============================================================ */
    fun showToast(msg: String, type: String = "success") {
        _state.update { it.copy(toastMessage = msg, toastType = type) }
    }

    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }

    /* ============================================================
       HELPER
       ============================================================ */
    private fun todayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun monthKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${String.format("%02d", cal.get(Calendar.MONTH) + 1)}"
    }

    private fun formatThousands(v: Long): String {
        return v.toString().reversed().chunked(3).joinToString(".").reversed()
    }

    private fun nextRunDate(fromDate: String, freq: RecurringFrequency): String {
        val parts = fromDate.split("-").map { it.toInt() }
        val cal = Calendar.getInstance()
        cal.set(parts[0], parts[1] - 1, parts[2])
        when (freq) {
            RecurringFrequency.DAILY -> cal.add(Calendar.DAY_OF_MONTH, 1)
            RecurringFrequency.WEEKLY -> cal.add(Calendar.DAY_OF_MONTH, 7)
            RecurringFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            RecurringFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
}