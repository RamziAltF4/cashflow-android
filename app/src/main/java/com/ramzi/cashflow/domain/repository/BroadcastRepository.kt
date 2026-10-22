package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.Broadcast
import kotlinx.coroutines.flow.Flow

/**
 * Padanan broadcast.js — baca collection `broadcasts`.
 * Di Android, dismiss disimpan lokal di DataStore, bukan di Firestore.
 */
interface BroadcastRepository {

    /**
     * Realtime stream broadcast terbaru (limit 20).
     * Padanan onSnapshot(query(collection('broadcasts'), orderBy('createdAt','desc'), limit(20)))
     */
    fun observeLatest(): Flow<List<Broadcast>>
}