package com.ramzi.cashflow.presentation.broadcast

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ramzi.cashflow.domain.model.Broadcast
import com.ramzi.cashflow.domain.model.Plan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class BroadcastViewModel(
    private val context: Context
) : ViewModel() {

    private val _currentBroadcast = MutableStateFlow<Broadcast?>(null)
    val currentBroadcast: StateFlow<Broadcast?> = _currentBroadcast.asStateFlow()

    private val _activeBroadcasts = MutableStateFlow<List<Broadcast>>(emptyList())

    private val seenIds: MutableSet<String> = loadSeenIds()

    @Volatile
    private var cachedPlan: Plan = Plan.FREE

    private var planListener: ListenerRegistration? = null

    // Cache raw broadcasts terakhir biar bisa re-filter tanpa nunggu Firestore
    @Volatile
    private var lastRawBroadcasts: List<Broadcast> = emptyList()

    // Flag: sudah pernah terima snapshot pertama?
    @Volatile
    private var hasInitialized: Boolean = false

    init {
        // Fallback awal dari prefs
        cachedPlan = readPlanFromPrefs()
        startPlanListener()

        viewModelScope.launch {
            BroadcastListener.observe()
                .catch { emit(emptyList()) }
                .collect { broadcasts ->
                    lastRawBroadcasts = broadcasts

                    if (!hasInitialized) {
                        // Load pertama: mark semua LAMA sebagai seen,
                        // sisakan HANYA yang paling baru (index 0, urut createdAt DESC)
                        if (broadcasts.isNotEmpty()) {
                            val newestId = broadcasts.first().id
                            broadcasts.forEach { b ->
                                if (b.id != newestId) seenIds.add(b.id)
                            }
                            persistSeenIds()
                        }
                        hasInitialized = true
                    }

                    applyFilter(broadcasts)
                }
        }
    }

    /* ═══════════════════════════════════════════════════════════
       PLAN LISTENER — auto update kalau admin ubah plan user
       ═══════════════════════════════════════════════════════════ */
    private fun startPlanListener() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            android.util.Log.d("BroadcastVM", "No UID, plan=${cachedPlan.id}")
            return
        }

        planListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("BroadcastVM", "Plan listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val raw = snapshot.getString("plan")
                    if (!raw.isNullOrBlank()) {
                        val newPlan = Plan.fromId(raw)
                        if (newPlan != cachedPlan) {
                            android.util.Log.d("BroadcastVM",
                                "Plan CHANGED: ${cachedPlan.id} → ${newPlan.id}")
                            cachedPlan = newPlan
                            applyFilter(lastRawBroadcasts)
                        }
                    }
                }
            }

        android.util.Log.d("BroadcastVM", "Plan listener started for uid=$uid")
    }

    /**
     * MANUAL REFRESH — dipanggil dari MainActivity saat app resume.
     */
    fun refreshPlan() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            cachedPlan = readPlanFromPrefs()
            applyFilter(lastRawBroadcasts)
            return
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val raw = doc.getString("plan")
                if (!raw.isNullOrBlank()) {
                    val newPlan = Plan.fromId(raw)
                    if (newPlan != cachedPlan) {
                        cachedPlan = newPlan
                        applyFilter(lastRawBroadcasts)
                    }
                }
            }
    }

    private fun readPlanFromPrefs(): Plan {
        return try {
            val p = context.getSharedPreferences("cashflow_prefs", Context.MODE_PRIVATE)
            val raw = p.getString("user_plan", null) ?: p.getString("plan", null)
            if (!raw.isNullOrBlank()) Plan.fromId(raw) else Plan.FREE
        } catch (_: Throwable) {
            Plan.FREE
        }
    }

    /* ═══════════════════════════════════════════════════════════
       FILTER + DISPLAY
       ═══════════════════════════════════════════════════════════ */
    private fun applyFilter(allBroadcasts: List<Broadcast>) {
        val filtered = allBroadcasts
            .filter { it.target.matches(cachedPlan) }
            .filter { it.id !in seenIds }

        android.util.Log.d("BroadcastVM",
            "Filter: total=${allBroadcasts.size} plan=${cachedPlan.id} " +
            "seen=${seenIds.size} shown=${filtered.size}")

        _activeBroadcasts.value = filtered
        refreshCurrent()
    }

    private fun refreshCurrent() {
        // Hanya ambil 1 yang paling baru (list sudah urut createdAt DESC)
        val next = _activeBroadcasts.value.firstOrNull { it.id !in seenIds }
        _currentBroadcast.value = next
    }

    fun dismissCurrent() {
        val current = _currentBroadcast.value ?: return
        seenIds.add(current.id)
        persistSeenIds()
        _activeBroadcasts.value = _activeBroadcasts.value.filter { it.id != current.id }
        refreshCurrent()
    }

    /* ═══════════════════════════════════════════════════════════
       PERSISTENCE
       ═══════════════════════════════════════════════════════════ */
    private fun prefs() = context.getSharedPreferences(
        "cashflow_broadcast_seen",
        Context.MODE_PRIVATE
    )

    private fun loadSeenIds(): MutableSet<String> =
        (prefs().getStringSet(KEY_SEEN_IDS, emptySet()) ?: emptySet()).toMutableSet()

    private fun persistSeenIds() {
        val trimmed = seenIds.toList().takeLast(100).toSet()
        seenIds.clear()
        seenIds.addAll(trimmed)
        prefs().edit().putStringSet(KEY_SEEN_IDS, trimmed).apply()
    }

    fun resetAllSeen() {
        seenIds.clear()
        persistSeenIds()
        applyFilter(lastRawBroadcasts)
    }

    override fun onCleared() {
        super.onCleared()
        planListener?.remove()
    }

    companion object {
        private const val KEY_SEEN_IDS = "seen_broadcast_ids"
    }
}

class BroadcastViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BroadcastViewModel(context.applicationContext) as T
    }
}