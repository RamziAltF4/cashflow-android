package com.ramzi.cashflow.presentation.dashboard.imports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.local.CurrencyPreference
import com.ramzi.cashflow.domain.model.DetectedStatus
import com.ramzi.cashflow.domain.model.DetectedTx
import com.ramzi.cashflow.domain.model.MerchantAlias
import com.ramzi.cashflow.domain.model.TxOrigin
import com.ramzi.cashflow.domain.repository.DetectedTxRepository
import com.ramzi.cashflow.domain.repository.MerchantAliasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ImportsViewModel(
    private val detectedRepo: DetectedTxRepository,
    private val aliasRepo: MerchantAliasRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val currencyPreference: CurrencyPreference
) : ViewModel() {

    private val _state = MutableStateFlow(ImportsState())
    val state: StateFlow<ImportsState> = _state.asStateFlow()

    private val uid: String?
        get() = firebaseAuth.currentUser?.uid

    init {
        observePending()
        observeCurrency()
    }

    /* ============================================================
       ⭐ OBSERVE CURRENCY
       ============================================================ */
    private fun observeCurrency() {
        viewModelScope.launch {
            currencyPreference.currencyCode.collect { code ->
                _state.update { it.copy(currencyCode = code) }
            }
        }
    }

    /* ============================================================
       ⭐ DEDUPE — buang item dengan signature sama
       ============================================================ */
    private fun dedupeBySignature(list: List<DetectedTx>): List<DetectedTx> {
        return list
            .groupBy { "${it.date}|${it.amount}|${it.merchant}|${it.type.id}" }
            .mapNotNull { (_, items) ->
                items.maxByOrNull { it.id } ?: items.firstOrNull()
            }
            .sortedByDescending { it.notifTimestamp }
    }

    /* ============================================================
       OBSERVE PENDING
       ============================================================ */
    private fun observePending() {
        val userId = uid ?: run {
            _state.update { it.copy(loading = false, error = "Sesi tidak valid.") }
            return
        }
        viewModelScope.launch {
            detectedRepo.observePending(userId).collect { list ->
                val deduped = dedupeBySignature(list)
                _state.update {
                    it.copy(
                        loading = false,
                        items = deduped,
                        error = null
                    )
                }
            }
        }
    }

    fun refresh() {
        val userId = uid ?: return
        viewModelScope.launch {
            try {
                val list = detectedRepo.getPending(userId)
                _state.update { it.copy(items = dedupeBySignature(list)) }
            } catch (e: Exception) {
                Log.w("ImportsVM", "refresh failed", e)
            }
        }
    }

    fun showToast(msg: String) {
        _state.update { it.copy(toastMessage = msg) }
    }

    /* ============================================================
       SIMPAN 1 ITEM
       ============================================================ */
    fun requestSave(id: String) {
        val item = _state.value.items.find { it.id == id } ?: return
        _state.update {
            it.copy(
                confirmSaveId = id,
                confirmSaveTitle = item.merchant.ifBlank {
                    "${item.type.name} ${item.amount}"
                }
            )
        }
    }

    fun cancelSave() {
        _state.update { it.copy(confirmSaveId = null, confirmSaveTitle = "") }
    }

    fun confirmSave() {
        val id = _state.value.confirmSaveId ?: return
        val item = _state.value.items.find { it.id == id } ?: return
        val userId = uid ?: return

        _state.update { it.copy(confirmSaveId = null, toastMessage = "Menyimpan...") }

        viewModelScope.launch {
            try {
                val txCol = firestore.collection("transactions")
                txCol.add(
                    mapOf(
                        "userId" to userId,
                        "type" to item.type.id,
                        "desc" to item.merchant.ifBlank { "Import BCA" },
                        "amount" to item.amount,
                        "category" to item.categoryMapped,
                        "date" to item.date,
                        "note" to "Dari notifikasi BCA",
                        "origin" to TxOrigin.IMPORT.id,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()

                detectedRepo.updateStatus(id, DetectedStatus.SAVED.id)
                _state.update { it.copy(toastMessage = "Transaksi disimpan.") }
            } catch (e: Exception) {
                Log.w("ImportsVM", "save failed", e)
                _state.update { it.copy(toastMessage = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    /* ============================================================
       ABAIKAN 1 ITEM
       ============================================================ */
    fun requestDismiss(id: String) {
        val item = _state.value.items.find { it.id == id } ?: return
        _state.update {
            it.copy(
                confirmDismissId = id,
                confirmDismissTitle = item.merchant.ifBlank {
                    "${item.type.name} ${item.amount}"
                }
            )
        }
    }

    fun cancelDismiss() {
        _state.update { it.copy(confirmDismissId = null, confirmDismissTitle = "") }
    }

    fun confirmDismiss() {
        val id = _state.value.confirmDismissId ?: return
        _state.update { it.copy(confirmDismissId = null) }

        viewModelScope.launch {
            detectedRepo.updateStatus(id, DetectedStatus.DISMISSED.id)
            _state.update { it.copy(toastMessage = "Diabaikan.") }
        }
    }

    /* ============================================================
       BULK — SIMPAN SEMUA
       ============================================================ */
    fun requestSaveAll() {
        if (_state.value.items.isEmpty()) return
        _state.update { it.copy(confirmSaveAll = true) }
    }

    fun cancelSaveAll() {
        _state.update { it.copy(confirmSaveAll = false) }
    }

    fun confirmSaveAll() {
        val userId = uid ?: return
        val items = _state.value.items.toList()
        if (items.isEmpty()) return
        _state.update { it.copy(confirmSaveAll = false, toastMessage = "Menyimpan semua...") }

        viewModelScope.launch {
            try {
                val txCol = firestore.collection("transactions")
                val detCol = firestore.collection("detected_txs")

                items.chunked(250).forEach { chunk ->
                    val batch = firestore.batch()
                    for (item in chunk) {
                        val newTxRef = txCol.document()
                        batch.set(
                            newTxRef,
                            mapOf(
                                "userId" to userId,
                                "type" to item.type.id,
                                "desc" to item.merchant.ifBlank { "Import BCA" },
                                "amount" to item.amount,
                                "category" to item.categoryMapped,
                                "date" to item.date,
                                "note" to "Dari notifikasi BCA",
                                "origin" to TxOrigin.IMPORT.id,
                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                        )

                        val detRef = detCol.document(item.id)
                        batch.update(detRef, "status", DetectedStatus.SAVED.id)
                    }
                    batch.commit().await()
                }

                _state.update {
                    it.copy(toastMessage = "${items.size} transaksi berhasil disimpan sekaligus.")
                }
            } catch (e: Exception) {
                Log.w("ImportsVM", "saveAll batch failed", e)
                _state.update {
                    it.copy(toastMessage = "Gagal simpan batch: ${e.message}")
                }
            }
        }
    }

    /* ============================================================
       BULK — ABAIKAN SEMUA
       ============================================================ */
    fun requestDismissAll() {
        if (_state.value.items.isEmpty()) return
        _state.update { it.copy(confirmDismissAll = true) }
    }

    fun cancelDismissAll() {
        _state.update { it.copy(confirmDismissAll = false) }
    }

    fun confirmDismissAll() {
        val items = _state.value.items.toList()
        if (items.isEmpty()) return
        _state.update { it.copy(confirmDismissAll = false) }

        viewModelScope.launch {
            try {
                val detCol = firestore.collection("detected_txs")
                items.chunked(500).forEach { chunk ->
                    val batch = firestore.batch()
                    for (item in chunk) {
                        val ref = detCol.document(item.id)
                        batch.update(ref, "status", DetectedStatus.DISMISSED.id)
                    }
                    batch.commit().await()
                }
                _state.update { it.copy(toastMessage = "${items.size} item berhasil diabaikan.") }
            } catch (e: Exception) {
                Log.w("ImportsVM", "dismissAll batch failed", e)
                _state.update { it.copy(toastMessage = "Gagal mengabaikan: ${e.message}") }
            }
        }
    }

    /* ============================================================
       ALIAS
       ============================================================ */
    fun openAliasModal(originalName: String) {
        _state.update {
            it.copy(
                aliasModalOpen = true,
                aliasOriginal = originalName,
                aliasDisplayInput = originalName,
                aliasSaving = false
            )
        }
    }

    fun closeAliasModal() {
        if (_state.value.aliasSaving) return
        _state.update { it.copy(aliasModalOpen = false) }
    }

    fun setAliasDisplayInput(v: String) {
        _state.update { it.copy(aliasDisplayInput = v) }
    }

    fun saveAlias() {
        val userId = uid ?: return
        val s = _state.value
        if (s.aliasOriginal.isBlank()) return
        val display = s.aliasDisplayInput.trim()
        if (display.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(aliasSaving = true) }
            try {
                aliasRepo.save(
                    MerchantAlias(
                        userId = userId,
                        merchantRaw = s.aliasOriginal,
                        displayName = display
                    )
                )
                _state.update {
                    it.copy(
                        aliasSaving = false,
                        aliasModalOpen = false,
                        toastMessage = "Alias disimpan: \"$display\""
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        aliasSaving = false,
                        toastMessage = "Gagal simpan alias: ${e.message}"
                    )
                }
            }
        }
    }

    /* ============================================================
       TOAST
       ============================================================ */
    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }
}