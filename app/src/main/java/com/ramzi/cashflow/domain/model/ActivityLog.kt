package com.ramzi.cashflow.domain.model


import com.google.firebase.Timestamp

/**
 * Padanan dokumen Firestore `activity_logs/{id}` (business-extras.js).
 *
 *   { userId, action, detail, date, createdAt }
 *
 * Contoh `action`:
 *   "create_workspace", "create_recurring", "delete-user",
 *   "reset-password", "update-plan", "broadcast", dll.
 */
data class ActivityLog(
    val id: String = "",
    val userId: String = "",
    val action: String = "",
    val detail: String = "",
    val date: Long = 0L,
    val createdAt: Timestamp? = null
) {
    /** Label rapi untuk UI: "create_recurring" → "Create recurring" */
    val actionLabel: String
        get() = action.replace("_", " ")
            .replace("-", " ")
            .replaceFirstChar { it.uppercase() }
}
