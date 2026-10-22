package com.ramzi.cashflow.util

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ramzi.cashflow.data.local.BiometricPreference
import com.ramzi.cashflow.data.local.LockTimeoutPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object AppLockManager : DefaultLifecycleObserver {

    private const val TAG = "APPLOCK"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private var lastBackgroundTime = 0L
    private var isRegistered = false
    private var timeoutPref: LockTimeoutPreference? = null
    private var biometricPref: BiometricPreference? = null
    private var lockJob: Job? = null

    private var isFirstLaunch = true

    fun register(context: Context) {
        if (isRegistered) return
        isRegistered = true
        timeoutPref = LockTimeoutPreference(context.applicationContext)
        biometricPref = BiometricPreference(context.applicationContext)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "registered")
    }

    /**
     * Dipanggil di MainActivity.onCreate() untuk handle cold start.
     */
    fun lockOnLaunch(context: Context) {
        scope.launch {
            val bioEnabled = try {
                biometricPref?.enabled?.first() ?: false
            } catch (_: Throwable) {
                false
            }

            Log.d(TAG, "lockOnLaunch: bioEnabled=$bioEnabled")

            if (bioEnabled) {
                Log.d(TAG, "lockOnLaunch: → lock sekarang")
                _locked.value = true
                isFirstLaunch = false
            }
        }
    }

    fun lock() {
        Log.d(TAG, "lock() manual")
        lockJob?.cancel()
        _locked.value = true
    }

    fun unlock() {
        Log.d(TAG, "unlock()")
        lockJob?.cancel()
        _locked.value = false
        isFirstLaunch = false
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.d(TAG, "onPause")
        lastBackgroundTime = System.currentTimeMillis()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "═══ onStop ═══")
        lastBackgroundTime = System.currentTimeMillis()
        lockJob?.cancel()

        lockJob = scope.launch {
            val timeout = try {
                timeoutPref?.timeoutMs?.first() ?: LockTimeoutPreference.SEC_30
            } catch (_: Throwable) {
                LockTimeoutPreference.SEC_30
            }
            Log.d(TAG, "onStop: timeout = $timeout")

            if (timeout == LockTimeoutPreference.IMMEDIATELY) {
                Log.d(TAG, "onStop: IMMEDIATE → lock")
                _locked.value = true
            } else {
                delay(timeout.toLong())
                val elapsed = System.currentTimeMillis() - lastBackgroundTime
                Log.d(TAG, "onStop: delay selesai, elapsed = $elapsed")
                if (elapsed >= timeout) {
                    _locked.value = true
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "═══ onStart ═══ locked=${_locked.value}, firstLaunch=$isFirstLaunch")

        scope.launch {
            val timeout = try {
                timeoutPref?.timeoutMs?.first() ?: LockTimeoutPreference.SEC_30
            } catch (_: Throwable) {
                LockTimeoutPreference.SEC_30
            }
            Log.d(TAG, "onStart: timeout = $timeout")

            if (isFirstLaunch) {
                Log.d(TAG, "onStart: cold start → lock")
                _locked.value = true
                return@launch
            }

            if (timeout == LockTimeoutPreference.IMMEDIATELY) {
                Log.d(TAG, "onStart: IMMEDIATE → tetap lock")
                return@launch
            }

            val elapsed = System.currentTimeMillis() - lastBackgroundTime
            if (elapsed in 1 until timeout) {
                Log.d(TAG, "onStart: kembali cepat (elapsed=$elapsed) → unlock")
                lockJob?.cancel()
                _locked.value = false
            }
        }
    }
}