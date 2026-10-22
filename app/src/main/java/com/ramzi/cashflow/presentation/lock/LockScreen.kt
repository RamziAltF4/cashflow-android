package com.ramzi.cashflow.presentation.lock

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.ramzi.cashflow.util.AppLockManager
import com.ramzi.cashflow.util.BiometricAuthHelper
import kotlinx.coroutines.launch

private const val TAG = "LOCK_SCREEN"

@Composable
fun LockScreen() {
    val activity = LocalActivity.current as? FragmentActivity
    val scope = rememberCoroutineScope()

    var authenticating by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Tap tombol di bawah untuk membuka") }

    Log.d(TAG, "LockScreen composed, activity=$activity")

    fun triggerAuth() {
        Log.d(TAG, "triggerAuth() called, activity=$activity, authenticating=$authenticating")

        if (activity == null) {
            Log.e(TAG, "activity NULL — tidak bisa prompt")
            statusText = "Error: Activity tidak valid"
            return
        }
        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG, "activity finishing/destroyed — skip prompt")
            statusText = "Activity tidak valid"
            return
        }
        if (authenticating) {
            Log.d(TAG, "skip — sedang authenticating")
            return
        }

        scope.launch {
            authenticating = true
            statusText = "Memverifikasi..."

            try {
                val ok = BiometricAuthHelper.authenticate(
                    activity = activity,
                    title = "Cashflow Terkunci",
                    subtitle = "Verifikasi untuk membuka"
                )
                Log.d(TAG, "authenticate result = $ok")

                if (ok) {
                    AppLockManager.unlock()
                } else {
                    statusText = "Verifikasi gagal. Coba lagi."
                }
            } catch (e: Throwable) {
                Log.e(TAG, "authenticate error: ${e.message}", e)
                statusText = "Error: ${e.message}"
            } finally {
                authenticating = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F0A1F), Color(0xFF1A1B3A)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Cashflow Terkunci",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = statusText,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { triggerAuth() },
                enabled = !authenticating,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0F0A1F)
                ),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (authenticating) "Memverifikasi..." else "Buka dengan Biometrik",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}