package com.ramzi.cashflow.domain.model
import com.google.firebase.Timestamp

/**
 * Model broadcast dari Firestore `broadcasts/{id}` (broadcast.js).
 *
 * Fields:
 *   title, message, type, target, createdAt
 */
data class Broadcast(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: BroadcastType = BroadcastType.INFO,
    val target: BroadcastTarget = BroadcastTarget.ALL,
    val createdAt: Timestamp? = null
)

enum class BroadcastType(val id: String) {
    INFO("info"),
    SUCCESS("success"),
    WARNING("warning"),
    PROMO("promo");

    companion object {
        fun fromId(id: String?): BroadcastType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: INFO
    }
}

enum class BroadcastTarget(val id: String) {
    ALL("all"),
    FREE("free"),
    PRO("pro"),
    BUSINESS("business");

    companion object {
        fun fromId(id: String?): BroadcastTarget =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALL
    }

    /** Apakah broadcast ini relevan untuk plan user. */
    fun matches(plan: Plan): Boolean =
        this == ALL || id.equals(plan.id, ignoreCase = true)
}