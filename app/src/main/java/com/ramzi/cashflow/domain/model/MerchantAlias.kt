package com.ramzi.cashflow.domain.model

import com.google.firebase.Timestamp

/**
 * Alias merchant / nama pengirim.
 * Contoh: "Nna Agus Ne Hi" -> "Mama"
 * Disimpan di Firestore: merchant_aliases/{id}
 */
data class MerchantAlias(
    val id: String = "",
    val userId: String = "",
    val merchantRaw: String = "",   // lowercase, hasil parser
    val displayName: String = "",   // nama yang user mau
    val createdAt: Timestamp? = null
)