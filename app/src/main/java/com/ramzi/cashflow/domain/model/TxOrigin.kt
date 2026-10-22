package com.ramzi.cashflow.domain.model

/**
 * Menandai dari mana transaksi berasal.
 * - MANUAL      : user input sendiri lewat form
 * - IMPORT      : dari notifikasi bank, user konfirmasi lewat inbox
 * - AUTO_IMPORT : dari notifikasi bank, auto-save tanpa review user
 */
enum class TxOrigin(val id: String) {
    MANUAL("manual"),
    IMPORT("import"),
    AUTO_IMPORT("auto_import");

    companion object {
        fun fromId(id: String?): TxOrigin =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MANUAL
    }
}