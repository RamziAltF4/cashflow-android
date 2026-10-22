package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.Workspace
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {

    /** Observe semua workspace milik user. */
    fun observeAll(ownerId: String): Flow<List<Workspace>>

    /** One-shot get. */
    suspend fun getAll(ownerId: String): List<Workspace>

    /** Buat workspace baru. */
    suspend fun create(ownerId: String, name: String)

    /** Hapus workspace. */
    suspend fun delete(id: String)
}
