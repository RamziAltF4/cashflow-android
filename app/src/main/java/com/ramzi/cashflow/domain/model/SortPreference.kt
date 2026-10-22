package com.ramzi.cashflow.domain.model

/**
 * Padanan `_sortDir` di free.js.
 * Nilai default: DESC (Terbaru), persist di localStorage key `cashflow_sort_dir`.
 */
enum class SortDir(val id: String) {
    DESC("desc"),   // Terbaru
    ASC("asc");     // Terlama

    companion object {
        fun fromId(id: String?): SortDir =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DESC
    }
}

enum class BillingMode(val id: String) {
    MONTHLY("monthly"),
    YEARLY("yearly");

    companion object {
        fun fromId(id: String?): BillingMode =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MONTHLY
    }
}

enum class ThemeMode(val id: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromId(id: String?): ThemeMode =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SYSTEM
    }
}