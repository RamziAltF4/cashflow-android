package com.ramzi.cashflow.domain.model

/**
 * Padanan dokumen Firestore `requests/{id}` di free.js:
 *   {
 *     userId, currentPlan, requestedPlan, amount, billing,
 *     proof, note, date, status
 *   }
 */
data class UpgradeRequest(
    val id: String = "",
    val userId: String = "",
    val currentPlan: Plan = Plan.FREE,
    val requestedPlan: Plan = Plan.PRO,
    val amount: Long = 0L,
    val billing: String = "monthly",
    val proof: String = "",
    val note: String = "",
    val date: Long = 0L,
    val status: RequestStatus = RequestStatus.PENDING
)

enum class RequestStatus(val id: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun fromId(id: String?): RequestStatus =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PENDING
    }
}

/**
 * Padanan koleksi `pass_requests/{id}` (request reset password).
 */
data class PassResetRequest(
    val id: String = "",
    val userId: String = "",
    val note: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val date: Long = 0L
)