package com.ramzi.cashflow.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.MerchantAlias
import com.ramzi.cashflow.domain.repository.MerchantAliasRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MerchantAliasRepositoryImpl(
    private val firestore: FirebaseFirestore
) : MerchantAliasRepository {

    private val col = firestore.collection("merchant_aliases")

    private fun mapDoc(id: String, data: Map<String, Any?>): MerchantAlias {
        return MerchantAlias(
            id = id,
            userId = data["userId"] as? String ?: "",
            merchantRaw = data["merchantRaw"] as? String ?: "",
            displayName = data["displayName"] as? String ?: "",
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp
        )
    }

    override fun observeAll(userId: String): Flow<List<MerchantAlias>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = try {
            col.whereEqualTo("userId", userId)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.w("MerchantAliasRepo", "Snapshot error: ${err.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snap?.documents
                        ?.map { mapDoc(it.id, it.data ?: emptyMap()) }
                        ?: emptyList()
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.w("MerchantAliasRepo", "Failed to attach listener", e)
            trySend(emptyList())
            null
        }
        awaitClose { listener?.remove() }
    }

    override suspend fun getAll(userId: String): List<MerchantAlias> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snap = col.whereEqualTo("userId", userId).get().await()
            snap.documents.map { mapDoc(it.id, it.data ?: emptyMap()) }
        } catch (e: Exception) {
            Log.w("MerchantAliasRepo", "getAll failed", e)
            emptyList()
        }
    }

    override suspend fun save(alias: MerchantAlias) {
        try {
            val payload = mapOf(
                "userId" to alias.userId,
                "merchantRaw" to alias.merchantRaw.lowercase().trim(),
                "displayName" to alias.displayName.trim(),
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            if (alias.id.isBlank()) {
                col.add(payload).await()
            } else {
                col.document(alias.id).set(payload).await()
            }
        } catch (e: Exception) {
            Log.w("MerchantAliasRepo", "save failed", e)
        }
    }

    override suspend fun delete(id: String) {
        try {
            col.document(id).delete().await()
        } catch (e: Exception) {
            Log.w("MerchantAliasRepo", "delete failed", e)
        }
    }

    override suspend fun find(userId: String, merchantRaw: String): MerchantAlias? {
        if (userId.isBlank() || merchantRaw.isBlank()) return null
        return try {
            val snap = col.whereEqualTo("userId", userId)
                .whereEqualTo("merchantRaw", merchantRaw.lowercase().trim())
                .limit(1)
                .get()
                .await()
            snap.documents.firstOrNull()?.let { mapDoc(it.id, it.data ?: emptyMap()) }
        } catch (e: Exception) {
            Log.w("MerchantAliasRepo", "find failed", e)
            null
        }
    }
}