package com.ramzi.cashflow

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.data.local.BiometricPreference
import com.ramzi.cashflow.presentation.broadcast.BroadcastBanner
import com.ramzi.cashflow.presentation.broadcast.BroadcastViewModel
import com.ramzi.cashflow.presentation.broadcast.BroadcastViewModelFactory
import com.ramzi.cashflow.presentation.lock.AppLockGate
import com.ramzi.cashflow.presentation.navigation.CashflowNavHost
import com.ramzi.cashflow.presentation.theme.ThemeViewModel
import com.ramzi.cashflow.presentation.theme.ThemeViewModelFactory
import com.ramzi.cashflow.service.AutoSaveWorker
import com.ramzi.cashflow.ui.theme.CashflowTheme
import com.ramzi.cashflow.util.AppLockManager

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ═══ MINTA IZIN POST_NOTIFICATIONS (Android 13+) ═══
        // Diperlukan agar foreground notification BcaNotificationListener muncul,
        // sehingga service tidak mudah di-kill sistem.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQ_POST_NOTIF
                )
            }
        }

        enableEdgeToEdge()

        AppLockManager.register(this)
        AppLockManager.lockOnLaunch(this)

        AutoSaveWorker.schedule(this)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(applicationContext)
            )
            val themeMode by themeViewModel.themeMode.collectAsState()
            val accentId by themeViewModel.accentId.collectAsState()

            val bioPref = BiometricPreference(applicationContext)
            val bioEnabled by bioPref.enabled.collectAsState(initial = false)

            val broadcastViewModel: BroadcastViewModel = viewModel(
                factory = BroadcastViewModelFactory(applicationContext)
            )
            val currentBroadcast by broadcastViewModel.currentBroadcast.collectAsState()

            // ═══ AUTO-REFRESH PLAN SAAT APP RESUME ═══
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        broadcastViewModel.refreshPlan()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            CashflowTheme(
                themeMode = themeMode,
                accentId = accentId
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppLockGate(enabled = bioEnabled) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CashflowNavHost()

                            BroadcastBanner(
                                broadcast = currentBroadcast,
                                onDismiss = { broadcastViewModel.dismissCurrent() },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val REQ_POST_NOTIF = 1001
    }
}