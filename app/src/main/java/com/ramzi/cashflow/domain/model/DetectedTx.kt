package com.ramzi.cashflow.domain.model

import com.google.firebase.Timestamp

/**
 * Transaksi yang terdeteksi dari notifikasi bank, tapi belum dikonfirmasi user.
 * Disimpan di Firestore: detected_txs/{id}
 *
 * Setelah user konfirmasi (Simpan/Ubah) atau auto-save jam 00:00,
 * item ini dihapus dari `detected_txs` dan dipindah ke `transactions`.
 */
data class DetectedTx(
    val id: String = "",
    val userId: String = "",

    // Hasil parsing notif
    val bank: String = "BCA",             // "BCA" | "Mandiri" | dst
    val type: TxType = TxType.EXPENSE,
    val amount: Long = 0L,
    val merchant: String = "",            // contoh: "NNA AGUS" / "home uus"
    val categoryFromBank: String = "",    // contoh: "Belanja Bulanan"
    val categoryMapped: String = "",      // contoh: "Belanja" (hasil mapping)
    val rawText: String = "",             // teks notif asli, untuk audit

    // Waktu
    val notifTimestamp: Long = 0L,        // waktu notif diterima (ms)
    val date: String = "",                // "YYYY-MM-DD" untuk cutoff jam 00:00

    // State
    val status: DetectedStatus = DetectedStatus.PENDING,
    val createdAt: Timestamp? = null,

    // Anti-duplikat
    val fingerprint: String = ""          // hash(amount + type + merchant + date)
)

enum class DetectedStatus(val id: String) {
    PENDING("pending"),        // menunggu review user
    SAVED("saved"),            // sudah user simpan
    DISMISSED("dismissed"),    // user abaikan
    AUTO_SAVED("auto_saved");  // auto-save jam 00:00

    companion object {
        fun fromId(id: String?): DetectedStatus =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PENDING
    }
}