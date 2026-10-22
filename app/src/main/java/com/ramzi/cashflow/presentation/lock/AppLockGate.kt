package com.ramzi.cashflow.presentation.lock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ramzi.cashflow.util.AppLockManager

@Composable
fun AppLockGate(
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    val locked by AppLockManager.locked.collectAsState()

    if (locked) {
        LockScreen()
    } else {
        content()
    }
}