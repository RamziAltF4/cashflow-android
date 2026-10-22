package com.ramzi.cashflow.util

import com.ramzi.cashflow.domain.model.TxType

object CategoryMapper {

    val INCOME_CATEGORIES = listOf(
        "Gaji", "Bonus", "Investasi", "Penjualan", "Hadiah", "Lainnya"
    )

    val EXPENSE_CATEGORIES = listOf(
        "Makanan", "Transportasi", "Belanja", "Tagihan",
        "Hiburan", "Kesehatan", "Pendidikan", "Lainnya"
    )

    private val categoryMap: Map<String, String> = mapOf(
        // Expense
        "makanan" to "Makanan",
        "makanan & minuman" to "Makanan",
        "makanan dan minuman" to "Makanan",
        "minuman" to "Makanan",
        "restoran" to "Makanan",
        "restaurant" to "Makanan",
        "cafe" to "Makanan",
        "kafe" to "Makanan",
        "food" to "Makanan",
        "beverage" to "Makanan",

        "transportasi" to "Transportasi",
        "transport" to "Transportasi",
        "bensin" to "Transportasi",
        "bbm" to "Transportasi",
        "parkir" to "Transportasi",
        "tol" to "Transportasi",
        "gojek" to "Transportasi",
        "grab" to "Transportasi",
        "taksi" to "Transportasi",
        "taxi" to "Transportasi",
        "ojek" to "Transportasi",
        "krl" to "Transportasi",
        "mrt" to "Transportasi",
        "transjakarta" to "Transportasi",

        "belanja" to "Belanja",
        "belanja bulanan" to "Belanja",
        "belanja online" to "Belanja",
        "belanja harian" to "Belanja",
        "shopping" to "Belanja",
        "retail" to "Belanja",
        "supermarket" to "Belanja",
        "minimarket" to "Belanja",

        "tagihan" to "Tagihan",
        "tagihan & utilitas" to "Tagihan",
        "utilitas" to "Tagihan",
        "listrik" to "Tagihan",
        "air" to "Tagihan",
        "internet" to "Tagihan",
        "telepon" to "Tagihan",
        "pulsa" to "Tagihan",
        "pln" to "Tagihan",
        "pdam" to "Tagihan",
        "telkom" to "Tagihan",
        "indihome" to "Tagihan",

        "hiburan" to "Hiburan",
        "entertainment" to "Hiburan",
        "game" to "Hiburan",
        "streaming" to "Hiburan",
        "bioskop" to "Hiburan",
        "movie" to "Hiburan",
        "netflix" to "Hiburan",
        "spotify" to "Hiburan",
        "disney" to "Hiburan",
        "youtube" to "Hiburan",

        "kesehatan" to "Kesehatan",
        "health" to "Kesehatan",
        "obat" to "Kesehatan",
        "apotek" to "Kesehatan",
        "apotik" to "Kesehatan",
        "rumah sakit" to "Kesehatan",
        "klinik" to "Kesehatan",
        "dokter" to "Kesehatan",

        "pendidikan" to "Pendidikan",
        "education" to "Pendidikan",
        "sekolah" to "Pendidikan",
        "kuliah" to "Pendidikan",
        "kursus" to "Pendidikan",
        "buku" to "Pendidikan",

        // Income
        "gaji" to "Gaji",
        "salary" to "Gaji",
        "payroll" to "Gaji",
        "upah" to "Gaji",
        "bonus" to "Bonus",
        "thr" to "Bonus",
        "insentif" to "Bonus",
        "investasi" to "Investasi",
        "investment" to "Investasi",
        "dividen" to "Investasi",
        "bunga" to "Investasi",
        "penjualan" to "Penjualan",
        "sales" to "Penjualan",
        "jualan" to "Penjualan",
        "omzet" to "Penjualan",
        "hadiah" to "Hadiah",
        "gift" to "Hadiah",
        "reward" to "Hadiah",
        "cashback" to "Hadiah"
    )

    fun mapCategory(bankCategory: String): String? {
        if (bankCategory.isBlank()) return null
        return categoryMap[bankCategory.trim().lowercase()]
    }

    fun detectFromMerchant(merchant: String): String? {
        if (merchant.isBlank()) return null
        val m = merchant.lowercase()

        for ((key, category) in categoryMap) {
            if (m.contains(key)) return category
        }

        return when {
            m.contains("gojek") || m.contains("grab") -> "Transportasi"
            m.contains("shopee") || m.contains("tokopedia") ||
                    m.contains("lazada") || m.contains("blibli") -> "Belanja"
            m.contains("indomaret") || m.contains("alfamart") ||
                    m.contains("superindo") -> "Makanan"
            m.contains("pln") || m.contains("pdam") -> "Tagihan"
            m.contains("netflix") || m.contains("spotify") -> "Hiburan"
            else -> null
        }
    }

    fun resolve(
        type: TxType,
        bankCategory: String,
        merchant: String
    ): String {
        mapCategory(bankCategory)?.let { return it }
        detectFromMerchant(merchant)?.let { return it }
        return "Lainnya"
    }
}