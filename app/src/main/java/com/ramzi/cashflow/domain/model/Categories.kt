package com.ramzi.cashflow.domain.model

/**
 * Daftar kategori per tipe transaksi.
 * Padanan CATEGORIES di free.js/pro.js/business.js:
 *
 *   income:  ['Gaji', 'Bonus', 'Investasi', 'Penjualan', 'Hadiah', 'Lainnya']
 *   expense: ['Makanan', 'Transportasi', 'Belanja', 'Tagihan',
 *             'Hiburan', 'Kesehatan', 'Pendidikan', 'Lainnya']
 */
object Categories {

    val INCOME: List<String> = listOf(
        "Gaji",
        "Bonus",
        "Investasi",
        "Penjualan",
        "Hadiah",
        "Lainnya"
    )

    val EXPENSE: List<String> = listOf(
        "Makanan",
        "Transportasi",
        "Belanja",
        "Tagihan",
        "Hiburan",
        "Kesehatan",
        "Pendidikan",
        "Lainnya"
    )

    /** Semua kategori (income + expense), tanpa duplikat. */
    val ALL: List<String> = (INCOME + EXPENSE).distinct()

    /** Kategori untuk tipe transaksi tertentu. */
    fun forType(type: TxType): List<String> = when (type) {
        TxType.INCOME -> INCOME
        TxType.EXPENSE -> EXPENSE
    }

    /** Cek apakah kategori valid untuk tipe. */
    fun isValid(type: TxType, category: String): Boolean =
        forType(type).contains(category)
}