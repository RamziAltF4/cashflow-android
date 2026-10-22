package com.ramzi.cashflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.R
import com.ramzi.cashflow.data.parser.BcaNotificationParser
import com.ramzi.cashflow.data.repository.DetectedTxRepositoryImpl
import com.ramzi.cashflow.data.repository.MerchantAliasRepositoryImpl
import com.ramzi.cashflow.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service yang mendengarkan notifikasi MyBCA.
 *
 * Dijalankan sebagai FOREGROUND SERVICE supaya tidak dibunuh oleh sistem.
 * Akan muncul notifikasi senyap "Auto-Import aktif" selama service hidup.
 */
class BcaNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val detectedRepo by lazy { DetectedTxRepositoryImpl(firestore) }
    private val aliasRepo by lazy { MerchantAliasRepositoryImpl(firestore) }

    /* ============================================================
       LIFECYCLE
       ============================================================ */
    override fun onCreate() {
        super.onCreate()
        setInstance(this)

        // Mulai foreground service supaya tidak di-kill sistem
        startForegroundSafely()

        Log.d(TAG, "Listener created")
    }

    override fun onDestroy() {
        super.onDestroy()
        setInstance(null)
        Log.d(TAG, "Listener destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListener connected")

        // Pastikan foreground notification tetap hidup
        startForegroundSafely()

        // Auto-replay notif yang masih ada saat listener connect
        replayExistingNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListener disconnected")

        // Coba rebind otomatis (best effort)
        try {
            requestRebind(
                android.content.ComponentName(this, BcaNotificationListener::class.java)
            )
            Log.d(TAG, "requestRebind() called")
        } catch (e: Exception) {
            Log.w(TAG, "requestRebind failed", e)
        }
    }

    /* ============================================================
       FOREGROUND SERVICE
       ============================================================ */
    private fun startForegroundSafely() {
        try {
            val notification = buildForegroundNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+: bisa specify type
                startForeground(
                    FOREGROUND_NOTIF_ID,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                )
            } else {
                startForeground(FOREGROUND_NOTIF_ID, notification)
            }
            Log.d(TAG, "Foreground started")
        } catch (e: Exception) {
            Log.w(TAG, "startForeground failed", e)
        }
    }

    private fun buildForegroundNotification(): Notification {
        // Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BCA Auto-Import",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menjaga fitur auto-import tetap aktif"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        // Tap notifikasi → buka app
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto-Import aktif")
            .setContentText("Mendengarkan notifikasi MyBCA")
            .setSmallIcon(R.drawable.ic_notification) // ⚠️ pastikan drawable ini ada
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /* ============================================================
       NOTIF HANDLER
       ============================================================ */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        try {
            handleNotification(sbn)
        } catch (e: Exception) {
            Log.w(TAG, "onNotificationPosted error", e)
        }
    }

    /* ============================================================
       REPLAY EXISTING NOTIFICATIONS
       ============================================================ */
    private fun replayExistingNotifications() {
        val active = try {
            activeNotifications
        } catch (e: Exception) {
            Log.w(TAG, "activeNotifications failed", e)
            null
        }

        if (active == null || active.isEmpty()) {
            Log.d(TAG, "No active notifications")
            return
        }

        Log.d(TAG, "Replaying ${active.size} active notification(s)")

        val bcaNotifs = active.filter {
            it.packageName == BcaNotificationParser.PACKAGE_NAME
        }
        Log.d(TAG, "MyBCA notifications: ${bcaNotifs.size}")

        bcaNotifs.forEach { sbn ->
            try {
                handleNotification(sbn)
            } catch (e: Exception) {
                Log.w(TAG, "replay error", e)
            }
        }
    }

    /* ============================================================
       HANDLE NOTIFICATION
       ============================================================ */
    private fun handleNotification(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        if (!BcaNotificationParser.isBcaFinanceNotification(pkg, title)) return
        if (body.isNullOrBlank()) return

        val userId = auth.currentUser?.uid ?: run {
            Log.w(TAG, "No logged-in user, skip")
            return
        }

        Log.d(TAG, "BCA notif detected: $body")

        scope.launch {
            try {
                val detected = BcaNotificationParser.parse(
                    body = body,
                    userId = userId,
                    notifTimestamp = sbn.postTime
                ) ?: run {
                    Log.d(TAG, "Parser returned null for: $body")
                    return@launch
                }

                val aliased = BcaNotificationParser.applyAlias(
                    userId = userId,
                    merchant = detected.merchant,
                    aliasRepo = aliasRepo
                )

                val finalTx = if (aliased != detected.merchant) {
                    detected.copy(merchant = aliased)
                } else detected

                val saved = detectedRepo.addIfNotDuplicate(finalTx)
                Log.d(
                    TAG,
                    "Detected tx saved: $saved | ${finalTx.amount} ${finalTx.type} ${finalTx.merchant}"
                )
            } catch (e: Exception) {
                Log.w(TAG, "handleNotification processing error", e)
            }
        }
    }

    /* ============================================================
       COMPANION
       ============================================================ */
    companion object {
        private const val TAG = "BcaNotifListener"
        private const val CHANNEL_ID = "bca_auto_import"
        private const val FOREGROUND_NOTIF_ID = 1001

        @Volatile
        private var instance: BcaNotificationListener? = null

        fun requestReplay(context: Context): Boolean {
            Log.d(TAG, "requestReplay called, instance = $instance")

            val inst = instance
            return if (inst != null) {
                inst.replayExistingNotifications()
                true
            } else {
                // Coba paksa sistem rebind service
                try {
                    NotificationListenerService.requestRebind(
                        android.content.ComponentName(
                            context,
                            BcaNotificationListener::class.java
                        )
                    )
                    Log.d(TAG, "requestReplay: instance null, called requestRebind()")
                } catch (e: Exception) {
                    Log.w(TAG, "requestReplay: requestRebind failed", e)
                }
                false
            }
        }

        fun setInstance(listener: BcaNotificationListener?) {
            instance = listener
        }
    }
}