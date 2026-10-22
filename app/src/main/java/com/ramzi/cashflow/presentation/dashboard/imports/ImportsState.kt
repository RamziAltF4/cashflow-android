package com.ramzi.cashflow.presentation.dashboard.imports

import com.ramzi.cashflow.domain.model.DetectedTx

data class ImportsState(
    val loading: Boolean = true,
    val error: String? = null,

    // Daftar transaksi terdeteksi (PENDING)
    val items: List<DetectedTx> = emptyList(),

    // Confirm dialog — simpan
    val confirmSaveId: String? = null,
    val confirmSaveTitle: String = "",

    // Confirm dialog — abaikan
    val confirmDismissId: String? = null,
    val confirmDismissTitle: String = "",

    // Confirm dialog — bulk
    val confirmSaveAll: Boolean = false,
    val confirmDismissAll: Boolean = false,

    // Alias dialog — simpan pengirim jadi label
    val aliasModalOpen: Boolean = false,
    val aliasOriginal: String = "",
    val aliasDisplayInput: String = "",
    val aliasSaving: Boolean = false,

    // Toast
    val toastMessage: String? = null,

    // ⭐ BARU — display currency
    val currencyCode: String = "IDR"
) {
    val pendingCount: Int get() = items.size

    val hasItems: Boolean get() = items.isNotEmpty()
}