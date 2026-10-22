package com.ramzi.cashflow.domain.model
/**
 * Enum Plan — sesuai `PLANS` di core.js:
 *   PLANS = { free, pro, business }
 *
 * PENTING: nilai di Firestore adalah `free`, `pro`, `business` (bukan `premium`).
 */
enum class Plan(val id: String) {
    FREE("free"),
    PRO("pro"),
    BUSINESS("business");

    companion object {
        fun fromId(id: String?): Plan =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FREE
    }
}

enum class AccountStatus(val id: String) {
    ACTIVE("active"),
    INACTIVE("inactive"),
    DISABLED("disabled"),
    SUSPENDED("suspended");

    companion object {
        fun fromId(id: String?): AccountStatus =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ACTIVE
    }
}