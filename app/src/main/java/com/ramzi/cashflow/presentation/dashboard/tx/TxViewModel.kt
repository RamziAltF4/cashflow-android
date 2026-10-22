package com.ramzi.cashflow.presentation.dashboard.tx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramzi.cashflow.data.local.CurrencyPreference
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.repository.TxRepository
import com.ramzi.cashflow.util.Formatting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/* ============================================================
   STATE
   ============================================================ */
data class TxState(
    val loading: Boolean = true,

    // Data
    val transactions: List<Transaction> = emptyList(),
    val filtered: List<Transaction> = emptyList(),

    // Filter
    val searchQuery: String = "",
    val filterType: TxType? = null,
    val filterMonth: String? = null,
    val availableMonths: List<String> = emptyList(),
    val sortDesc: Boolean = true,

    // Form
    val formOpen: Boolean = false,
    val formEditingId: String? = null,
    val formType: TxType = TxType.INCOME,
    val formDesc: String = "",
    val formAmount: String = "",
    val formCategory: String = "",
    val formDate: String = "",
    val formNote: String = "",
    val formError: String? = null,
    val formSaving: Boolean = false,
    val datePickerOpen: Boolean = false,

    // Delete confirm
    val deleteConfirmId: String? = null,
    val deleteConfirmTitle: String = "",

    // ⭐ BARU — display currency
    val currencyCode: String = "IDR"
) {
    val totalIncome: Long
        get() = filtered.filter { it.type == TxType.INCOME }.sumOf { it.amount }

    val totalExpense: Long
        get() = filtered.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }

    val hasActiveFilter: Boolean
        get() = searchQuery.isNotBlank() ||
                filterType != null ||
                filterMonth != null

    val formAmountLong: Long
        get() = formAmount.filter { it.isDigit() }.toLongOrNull() ?: 0L
}

/* ============================================================
   VIEWMODEL
   ============================================================ */
class TxViewModel(
    private val txRepository: TxRepository,
    private val firebaseAuth: FirebaseAuth,
    private val currencyPreference: CurrencyPreference   // ⭐ BARU
) : ViewModel() {

    private val _state = MutableStateFlow(TxState())
    val state: StateFlow<TxState> = _state.asStateFlow()

    init {
        observeTransactions()
        observeCurrency()   // ⭐ BARU
    }

    /* ============================================================
       ⭐ OBSERVE CURRENCY (BARU)
       ============================================================ */
    private fun observeCurrency() {
        viewModelScope.launch {
            currencyPreference.currencyCode.collect { code ->
                _state.update { it.copy(currencyCode = code) }
            }
        }
    }

    /* ============================================================
       OBSERVE TRANSACTIONS
       ============================================================ */
    private fun observeTransactions() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            txRepository.observeByUser(uid).collect { txs ->
                _state.update { current ->
                    val months = computeAvailableMonths(txs)
                    val safeMonth = current.filterMonth?.takeIf { months.contains(it) }
                    val merged = current.copy(
                        loading = false,
                        transactions = txs,
                        availableMonths = months,
                        filterMonth = safeMonth
                    )
                    merged.copy(filtered = applyFilter(txs, merged))
                }
            }
        }
    }

    private fun computeAvailableMonths(txs: List<Transaction>): List<String> {
        return txs
            .mapNotNull { tx ->
                if (tx.date.length >= 7) tx.date.substring(0, 7) else null
            }
            .distinct()
            .sortedDescending()
    }

    /* ============================================================
       FILTER ACTIONS
       ============================================================ */
    fun onSearchChange(q: String) {
        _state.update { s ->
            val new = s.copy(searchQuery = q)
            new.copy(filtered = applyFilter(new.transactions, new))
        }
    }

    fun onFilterTypeChange(type: TxType?) {
        _state.update { s ->
            val new = s.copy(filterType = type)
            new.copy(filtered = applyFilter(new.transactions, new))
        }
    }

    fun onMonthFilterChange(monthKey: String?) {
        _state.update { s ->
            val new = s.copy(filterMonth = monthKey)
            new.copy(filtered = applyFilter(new.transactions, new))
        }
    }

    fun toggleSort() {
        _state.update { s ->
            val new = s.copy(sortDesc = !s.sortDesc)
            new.copy(filtered = applyFilter(new.transactions, new))
        }
    }

    /* ============================================================
       APPLY FILTER + SORT
       ============================================================ */
    private fun applyFilter(txs: List<Transaction>, s: TxState): List<Transaction> {
        var result = txs.toList()

        if (s.searchQuery.isNotBlank()) {
            val q = s.searchQuery.lowercase().trim()
            result = result.filter {
                it.desc.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.note.lowercase().contains(q)
            }
        }

        s.filterType?.let { t -> result = result.filter { it.type == t } }
        s.filterMonth?.let { m -> result = result.filter { it.date.startsWith(m) } }

        val comparator = compareByDescending<Transaction> { it.date }
            .thenByDescending { it.createdAt?.seconds ?: 0L }

        result = if (s.sortDesc) result.sortedWith(comparator)
        else result.sortedWith(comparator.reversed())

        return result
    }

    /* ============================================================
       FORM
       ============================================================ */
    fun openAddForm(type: TxType) {
        _state.update {
            it.copy(
                formOpen = true,
                formEditingId = null,
                formType = type,
                formDesc = "",
                formAmount = "",
                formCategory = "",
                formDate = Formatting.todayISO(),
                formNote = "",
                formError = null,
                datePickerOpen = false
            )
        }
    }

    fun openEditForm(txId: String) {
        val tx = _state.value.transactions.firstOrNull { it.id == txId } ?: return
        _state.update {
            it.copy(
                formOpen = true,
                formEditingId = txId,
                formType = tx.type,
                formDesc = tx.desc,
                formAmount = tx.amount.toString(),
                formCategory = tx.category,
                formDate = tx.date,
                formNote = tx.note,
                formError = null,
                datePickerOpen = false
            )
        }
    }

    fun closeForm() {
        _state.update { it.copy(formOpen = false, formError = null, datePickerOpen = false) }
    }

    fun onFormTypeChange(t: TxType) =
        _state.update { it.copy(formType = t, formCategory = "") }

    fun onFormDescChange(v: String) =
        _state.update { it.copy(formDesc = v, formError = null) }

    fun onFormAmountChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(15)
        _state.update { it.copy(formAmount = digits, formError = null) }
    }

    fun onFormCategoryChange(v: String) =
        _state.update { it.copy(formCategory = v, formError = null) }

    fun onFormNoteChange(v: String) =
        _state.update { it.copy(formNote = v, formError = null) }

    /* ============================================================
       DATE
       ============================================================ */
    fun openDatePicker() = _state.update { it.copy(datePickerOpen = true) }
    fun closeDatePicker() = _state.update { it.copy(datePickerOpen = false) }

    fun setFormDateToday() {
        _state.update { it.copy(formDate = Formatting.todayISO(), formError = null) }
    }

    fun setFormDateYesterday() {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val iso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        _state.update { it.copy(formDate = iso, formError = null) }
    }

    fun setFormDateDaysAgo(days: Int) {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -days)
        val iso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        _state.update { it.copy(formDate = iso, formError = null) }
    }

    fun setFormDateFromMillis(millis: Long) {
        val iso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date(millis))
        _state.update {
            it.copy(formDate = iso, datePickerOpen = false, formError = null)
        }
    }

    /* ============================================================
       SAVE
       ============================================================ */
    fun saveForm() {
        val s = _state.value
        if (s.formDesc.isBlank()) {
            _state.update { it.copy(formError = "Deskripsi wajib diisi.") }
            return
        }
        if (s.formAmountLong <= 0L) {
            _state.update { it.copy(formError = "Nominal harus lebih dari 0.") }
            return
        }
        if (s.formCategory.isBlank()) {
            _state.update { it.copy(formError = "Pilih kategori.") }
            return
        }
        if (s.formDate.isBlank()) {
            _state.update { it.copy(formError = "Pilih tanggal.") }
            return
        }

        val uid = firebaseAuth.currentUser?.uid ?: return

        _state.update { it.copy(formSaving = true, formError = null) }
        viewModelScope.launch {
            try {
                val tx = Transaction(
                    id = s.formEditingId.orEmpty(),
                    userId = uid,
                    type = s.formType,
                    desc = s.formDesc.trim(),
                    amount = s.formAmountLong,
                    category = s.formCategory,
                    date = s.formDate,
                    note = s.formNote.trim()
                )
                if (s.formEditingId == null) txRepository.add(tx)
                else txRepository.update(s.formEditingId, tx)
                _state.update { it.copy(formSaving = false, formOpen = false) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        formSaving = false,
                        formError = e.message ?: "Gagal menyimpan."
                    )
                }
            }
        }
    }

    /* ============================================================
       DELETE
       ============================================================ */
    fun requestDelete(txId: String) {
        val tx = _state.value.transactions.firstOrNull { it.id == txId } ?: return
        _state.update {
            it.copy(deleteConfirmId = txId, deleteConfirmTitle = tx.desc)
        }
    }

    fun cancelDelete() =
        _state.update { it.copy(deleteConfirmId = null, deleteConfirmTitle = "") }

    fun confirmDelete() {
        val id = _state.value.deleteConfirmId ?: return
        _state.update { it.copy(deleteConfirmId = null) }
        viewModelScope.launch {
            try { txRepository.delete(id) } catch (_: Throwable) { }
        }
    }
}