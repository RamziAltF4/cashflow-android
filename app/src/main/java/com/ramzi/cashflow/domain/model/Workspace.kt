package com.ramzi.cashflow.domain.model


import com.google.firebase.Timestamp

/**
 * Padanan dokumen Firestore `workspaces/{id}` (business-extras.js).
 *
 *   { ownerId, name, date, createdAt }
 */
data class Workspace(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val date: Long = 0L,
    val createdAt: Timestamp? = null
) {
    val initial: String
        get() = name.trim().firstOrNull()?.uppercase() ?: "W"

    companion object {
        const val DEFAULT_ID = "default"
        val DEFAULT = Workspace(
            id = DEFAULT_ID,
            name = "Default",
            ownerId = ""
        )
    }
}
