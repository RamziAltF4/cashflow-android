package com.ramzi.cashflow.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.ramzi.cashflow.service.BcaNotificationListener

/**
 * Helper untuk cek & buka Settings Notification Access.
 */
object NotificationAccessHelper {

    /**
     * Cek apakah app kita sudah diberi izin Notification Access.
     */
    fun isEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        val pkg = context.packageName
        val component = ComponentName(context, BcaNotificationListener::class.java)
        val flatId = component.flattenToString()

        return flat.contains(pkg) || flat.contains(flatId)
    }

    /**
     * Buka halaman Settings → Notification Access.
     * User harus aktifkan manual.
     */
    fun openSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback ke Settings utama
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (_: Exception) { }
        }
    }
}