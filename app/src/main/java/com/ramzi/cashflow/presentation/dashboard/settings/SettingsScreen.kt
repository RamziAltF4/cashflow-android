package com.ramzi.cashflow.presentation.dashboard.settings


import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.provider.Settings

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ramzi.cashflow.service.BcaNotificationListener

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.data.local.LockTimeoutPreference
import com.ramzi.cashflow.domain.model.AccentColor
import com.ramzi.cashflow.domain.model.AccentColors
import com.ramzi.cashflow.presentation.dashboard.settings.components.TwoFASetupSheet
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.util.CurrencyFormatter
import com.ramzi.cashflow.util.NotificationAccessHelper

/**
 * Helper: cek apakah query match dengan title/subtitle/tags.
 * Case-insensitive + trim.
 */
private fun matchesQuery(query: String, vararg keywords: String?): Boolean {
    if (query.isBlank()) return true
    val q = query.trim().lowercase()
    return keywords.any { kw ->
        kw != null && kw.lowercase().contains(q)
    }
}

/**
 * Buka halaman akses notifikasi.
 * - API 30+ : langsung ke detail halaman aplikasi ini (paling "langsung")
 * - Di bawahnya: fallback ke daftar Notification Access
 */
private fun openNotificationAccessSettings(context: android.content.Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val component = ComponentName(context, BcaNotificationListener::class.java)
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    component.flattenToString()
                )
            }
            context.startActivity(intent)
        } else {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    } catch (e: Exception) {
        // Fallback kalau device/ROM tidak support intent di atas
        try {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        } catch (_: Exception) {
            NotificationAccessHelper.openSettings(context)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToPlans: () -> Unit = {},
    onNavigateToImports: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(LocalContext.current)
    )
) {
    val state by viewModel.state.collectAsState()
    val accent = LocalAccent.current
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var showEditNameDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var notifAccessGranted by remember {
        mutableStateOf(NotificationAccessHelper.isEnabled(context))
    }
    var showNotifDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notifAccessGranted = NotificationAccessHelper.isEnabled(context)
    }

    // Auto-refresh status saat balik dari Settings HP
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notifAccessGranted = NotificationAccessHelper.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isSearching = searchQuery.isNotBlank()

    /* ═══ DIALOGS ═══ */
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetProfileField()
                showEditNameDialog = false
            },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Edit Profile", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = state.editName,
                    onValueChange = viewModel::onEditNameChange,
                    label = { Text("Display Name", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProfile()
                        showEditNameDialog = false
                    },
                    enabled = !state.savingProfile,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent.base)
                ) {
                    Text(if (state.savingProfile) "Saving..." else "Save",
                        fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.resetProfileField()
                    showEditNameDialog = false
                }) { Text("Cancel", fontSize = 13.sp) }
            }
        )
    }

    if (state.showSetPinSheet) {
        var pin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.dismissSetPinSheet() },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    if (state.user?.pinEnabled == true) "Change PIN" else "Set Security PIN",
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text("Enter a 6-digit PIN to secure your account.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) pin = input
                        },
                        label = { Text("6-Digit PIN", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.pinError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(state.pinError!!, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.savePin(pin) },
                    enabled = pin.length == 6 && !state.pinSaving,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent.base)
                ) {
                    Text(if (state.pinSaving) "Saving..." else "Save",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSetPinSheet() }) {
                    Text("Cancel", fontSize = 13.sp)
                }
            }
        )
    }

    /* ═══ 2FA — PAKAI TwoFASetupSheet (dengan QR code) ═══ */
    TwoFASetupSheet(
        isOpen = state.show2FASheet,
        userEmail = state.user?.email.orEmpty(),
        onDismiss = { viewModel.dismiss2FASheet() },
        onVerified = { secret -> viewModel.verify2FA(secret) },
        saving = state.twoFASaving,
        error = state.twoFAError
    )

    if (state.showLockTimeoutSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissLockTimeoutSheet() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("Auto-Lock Timeout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Automatically lock app when idle for the specified duration",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(Modifier.height(14.dp))

                LockTimeoutPreference.ALL_OPTIONS.forEach { (ms, label) ->
                    val isSelected = ms == state.lockTimeoutMs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setLockTimeout(ms) }
                            .background(if (isSelected) accent.base.copy(alpha = 0.10f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) accent.base else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(Icons.Filled.Check, null, tint = accent.base,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    /* ═══ CURRENCY BOTTOM SHEET ═══ */
    if (state.showCurrencySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissCurrencySheet() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("Display Currency", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("All amounts will be converted from IDR to this currency",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(Modifier.height(14.dp))

                CurrencyFormatter.supportedCurrencies.forEach { opt ->
                    val isSelected = opt.code.equals(state.currencyCode, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setCurrency(opt.code) }
                            .background(
                                if (isSelected) accent.base.copy(alpha = 0.10f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            opt.symbol,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) accent.base
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(40.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                opt.code,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) accent.base
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                opt.label,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check, null,
                                tint = accent.base,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (state.showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutConfirm() },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Log Out?", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            text = { Text("Are you sure you want to log out of Cashflow on this device?",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissLogoutConfirm()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Log Out", fontWeight = FontWeight.Medium, fontSize = 13.sp) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutConfirm() }) {
                    Text("Cancel", fontSize = 13.sp)
                }
            }
        )
    }

    if (state.showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearDataConfirm() },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Clear All Transactions?", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            text = { Text("All income and expense records will be permanently deleted. This action cannot be undone.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllTransactions {} },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Clear All", fontWeight = FontWeight.Medium, fontSize = 13.sp) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearDataConfirm() }) {
                    Text("Cancel", fontSize = 13.sp)
                }
            }
        )
    }

    if (state.showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteAccountConfirm() },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Account Permanently?", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            text = { Text("Your account, financial data, and credentials will be permanently erased from Cashflow servers.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissDeleteAccountConfirm() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Delete Account", fontWeight = FontWeight.Medium, fontSize = 13.sp) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteAccountConfirm() }) {
                    Text("Cancel", fontSize = 13.sp)
                }
            }
        )
    }

    /* ═══ NOTIF DIALOG ═══ */
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF06B6D4)
                )
            },
            title = { Text("Aktifkan BCA Auto-Import", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            text = {
                Text(
                    "Untuk mencatat transaksi BCA secara otomatis, aplikasi perlu " +
                    "membaca notifikasi dari aplikasi BCA.\n\n" +
                    "Kamu akan diarahkan ke Pengaturan Android untuk mengizinkan " +
                    "akses notifikasi. Setelah diizinkan, semua berjalan otomatis " +
                    "di dalam aplikasi.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotifDialog = false
                        openNotificationAccessSettings(context)
                    }
                ) {
                    Text(
                        text = "Izinkan",
                        color = Color(0xFF06B6D4)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("Nanti", color = Color(0xFF6B7280))
                }
            }
        )
    }

    /* ═══ LOADING ═══ */
    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = accent.base, strokeWidth = 2.dp)
        }
        return
    }

    /* ═══════════════════════════════════════════════════════════
       PRE-COMPUTE: filter visibility setiap section
       ═══════════════════════════════════════════════════════════ */
    val q = searchQuery

    // Display & Formatting
    val showThemeRow = matchesQuery(q, "theme", "appearance", "dark", "light", "system", "auto")
    val showCurrencyRow = matchesQuery(q, "currency", "format", "language", "idr", "mata uang", "bahasa")
    val showDisplaySection = showThemeRow || showCurrencyRow

    // Account / Security
    val showPinRow = matchesQuery(q, "security pin", "pin", "lock", "6 digit", "device")
    val showBiometricRow = matchesQuery(q, "biometric", "fingerprint", "face", "sidik jari")
    val showAutoLockRow = matchesQuery(q, "auto lock", "timeout", "idle", "timer")
    val show2FARow = matchesQuery(q, "two factor", "2fa", "auth", "otp", "verifikasi")
    val showAccountSection = showPinRow || showBiometricRow || showAutoLockRow || show2FARow

    // Appearance
    val showAccentRow = matchesQuery(q, "accent", "color", "warna", "tema", "palette")
    val showAppearanceSection = showAccentRow

    // Smart Features
    val showBcaRow = matchesQuery(q, "bca", "auto import", "notification", "notifikasi", "import")
    val showDetectedRow = matchesQuery(q, "detected", "transactions", "captured", "import", "deteksi")
    val showSmartSection = showBcaRow || showDetectedRow

    // Usage / Quota
    val showQuotaRow = matchesQuery(q, "quota", "monthly", "limit", "usage", "batas", "transaksi")
    val showUsageSection = showQuotaRow

    // Business
    val showEnterpriseRow = matchesQuery(q, "enterprise", "business", "status", "unlimited")
    val showWorkspaceRow = matchesQuery(q, "workspace", "multi", "company", "bookkeeping")
    val showInvoicingRow = matchesQuery(q, "invoice", "billing", "subscription", "tagihan")
    val showBusinessSection = state.isBusiness &&
            (showEnterpriseRow || showWorkspaceRow || showInvoicingRow)

    // Danger Zone
    val showClearRow = matchesQuery(q, "clear", "erase", "delete transaction", "hapus", "bersihkan")
    val showDeleteRow = matchesQuery(q, "delete account", "remove", "hapus akun")
    val showLogoutRow = matchesQuery(q, "logout", "log out", "keluar", "sign out")
    val showDangerSection = showClearRow || showDeleteRow || showLogoutRow

    val noResults = isSearching &&
            !showDisplaySection &&
            !showAccountSection &&
            !showAppearanceSection &&
            !showSmartSection &&
            !showUsageSection &&
            !showBusinessSection &&
            !showDangerSection

    /* ═══════════════════════════════════════════════════════════
       MAIN CONTENT
       ═══════════════════════════════════════════════════════════ */
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── 1. HEADER (hilang saat search) ──
        if (!isSearching) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 10.dp)
                ) {
                    Text(
                        "Settings",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.6).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Security, data, and preferences",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            item {
                Spacer(Modifier.height(12.dp))
            }
        }

        // ── 2. SEARCH BAR ──
        item {
            SettingsSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" }
            )
        }

        // ── 3. DISPLAY & FORMATTING ──
        if (showDisplaySection) {
            item {
                SettingsSectionTitle("Display & Formatting")
                SettingsCard {
                    if (showThemeRow) {
                        ThemeRowInline(
                            selected = state.themeMode,
                            onSelect = viewModel::setThemeMode
                        )
                    }
                    if (showThemeRow && showCurrencyRow) {
                        SettingsCardDivider()
                    }
                    if (showCurrencyRow) {
                        SettingsCardRow(
                            icon = Icons.Filled.Language,
                            iconTint = Color(0xFF3B82F6),
                            iconBg = Color(0xFFDBEAFE),
                            title = "Currency",
                            subtitle = "Display currency for all amounts",
                            valueBadge = state.currencyLabel,
                            valueBadgeColor = Color(0xFF3B82F6),
                            onClick = { viewModel.showCurrencySheet() }
                        )
                    }
                }
            }
        }

        // ── 4. ACCOUNT / SECURITY ──
        if (showAccountSection) {
            item {
                SettingsSectionTitle("Account")
                SettingsCard {
                    var first = true

                    if (showPinRow) {
                        SettingsCardRow(
                            icon = Icons.Filled.Security,
                            iconTint = if (state.user?.pinEnabled == true) Color(0xFF10B981) else Color(0xFF6B7280),
                            iconBg = if (state.user?.pinEnabled == true) Color(0xFFD1FAE5) else Color(0xFFE5E7EB),
                            title = "Security PIN",
                            subtitle = if (state.user?.pinEnabled == true) "6-digit device lock" else "Not set",
                            valueBadge = if (state.user?.pinEnabled == true) "Enabled" else "Off",
                            valueBadgeColor = if (state.user?.pinEnabled == true) Color(0xFF10B981) else Color(0xFF6B7280),
                            onClick = { viewModel.showSetPinSheet() }
                        )
                        first = false
                    }
                    if (showBiometricRow) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.Filled.Fingerprint,
                            iconTint = if (state.biometricEnabled) Color(0xFF10B981) else Color(0xFF6B7280),
                            iconBg = if (state.biometricEnabled) Color(0xFFD1FAE5) else Color(0xFFE5E7EB),
                            title = "Biometric Lock",
                            subtitle = when {
                                state.biometricToggling -> "Updating..."
                                state.biometricEnabled -> "Fingerprint / Face"
                                !state.biometricAvailable -> "Unavailable on device"
                                else -> "Fast unlock with biometric"
                            },
                            valueBadge = if (state.biometricEnabled) "Enabled" else "Off",
                            valueBadgeColor = if (state.biometricEnabled) Color(0xFF10B981) else Color(0xFF6B7280),
                            onClick = {
                                if (activity != null) viewModel.toggleBiometric(!state.biometricEnabled, activity)
                            }
                        )
                        first = false
                    }
                    if (showAutoLockRow && state.biometricEnabled) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.Filled.Timer,
                            iconTint = Color(0xFF8B5CF6),
                            iconBg = Color(0xFFEDE9FE),
                            title = "Auto-Lock Timeout",
                            subtitle = "Lock when app is idle",
                            valueBadge = LockTimeoutPreference.ALL_OPTIONS
                                .firstOrNull { it.first == state.lockTimeoutMs }?.second ?: "30s",
                            valueBadgeColor = Color(0xFF8B5CF6),
                            onClick = { viewModel.showLockTimeoutSheet() }
                        )
                        first = false
                    }
                    if (show2FARow) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.Filled.Lock,
                            iconTint = if (state.user?.twoFAEnabled == true) Color(0xFF10B981)
                            else if (state.canUse2FA) Color(0xFFF59E0B) else Color(0xFF6B7280),
                            iconBg = if (state.user?.twoFAEnabled == true) Color(0xFFD1FAE5)
                            else if (state.canUse2FA) Color(0xFFFEF3C7) else Color(0xFFE5E7EB),
                            title = "Two-Factor Auth",
                            subtitle = when {
                                state.user?.twoFAEnabled == true -> "Active"
                                state.canUse2FA -> "Recommended"
                                else -> "Pro & Business exclusive"
                            },
                            valueBadge = when {
                                state.user?.twoFAEnabled == true -> "On"
                                state.canUse2FA -> "Enable"
                                else -> null
                            },
                            valueBadgeColor = when {
                                state.user?.twoFAEnabled == true -> Color(0xFF10B981)
                                state.canUse2FA -> Color(0xFFF59E0B)
                                else -> Color(0xFF6B7280)
                            },
                            onClick = {
                                if (state.canUse2FA && state.user?.twoFAEnabled != true) viewModel.show2FASheet()
                                else if (!state.canUse2FA) onNavigateToPlans()
                            }
                        )
                    }
                }
            }
        }

        // ── 5. APPEARANCE ──
        if (showAppearanceSection) {
            item {
                SettingsSectionTitle("Appearance")
                SettingsCard {
                    if (state.isBusiness || state.canChangeAccent) {
                        AccentPaletteInline(
                            selectedId = state.accentId,
                            canChange = state.canChangeAccent || state.isBusiness,
                            isBusiness = state.isBusiness,
                            onSelect = viewModel::setAccent,
                            onUpgradeClick = onNavigateToPlans
                        )
                    } else {
                        AccentColorLockedRow(onUpgradeClick = onNavigateToPlans)
                    }
                }
            }
        }

        // ── 6. SMART FEATURES ──
        if (showSmartSection) {
            item {
                SettingsSectionTitle("Smart Features")
                SettingsCard {
                    var first = true
                    if (showBcaRow) {
                        SettingsCardRow(
                            icon = Icons.Filled.Notifications,
                            iconTint = if (notifAccessGranted) Color(0xFF10B981) else Color(0xFF6B7280),
                            iconBg = if (notifAccessGranted) Color(0xFFD1FAE5) else Color(0xFFE5E7EB),
                            title = "BCA Auto-Import",
                            subtitle = if (notifAccessGranted) "Listening to bank notifications"
                            else "Notification access required",
                            valueBadge = if (notifAccessGranted) "Active" else "Enable",
                            valueBadgeColor = if (notifAccessGranted) Color(0xFF10B981) else Color(0xFF6B7280),
                            onClick = {
                                if (notifAccessGranted) {
                                    // Sudah aktif → langsung ke Settings (buat lihat / matikan)
                                    openNotificationAccessSettings(context)
                                } else {
                                    // Belum aktif → dialog penjelasan dulu
                                    showNotifDialog = true
                                }
                            }
                        )
                        first = false
                    }
                    if (showDetectedRow) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.Outlined.Inbox,
                            iconTint = Color(0xFF06B6D4),
                            iconBg = Color(0xFFCFFAFE),
                            title = "Detected Transactions",
                            subtitle = if (notifAccessGranted) "Review captured records"
                            else "Aktifkan BCA Auto-Import dulu",
                            onClick = onNavigateToImports
                        )
                    }
                }
            }
        }

        // ── 7. USAGE ──
        if (showUsageSection) {
            item {
                SettingsSectionTitle("Usage")
                SettingsCard {
                    SettingsCardRow(
                        icon = Icons.Filled.PieChart,
                        iconTint = Color(0xFFF59E0B),
                        iconBg = Color(0xFFFEF3C7),
                        title = "Monthly Quota",
                        subtitle = if (state.isBusiness) "Unlimited transactions"
                        else "Based on your plan",
                        valueBadge = if (state.isBusiness) "∞"
                        else "${state.txCount}/${state.planConfig?.limit ?: 10}",
                        valueBadgeColor = Color(0xFFF59E0B),
                        onClick = if (!state.isBusiness) onNavigateToPlans else null
                    )
                }
            }
        }

        // ── 8. BUSINESS ──
        if (showBusinessSection) {
            item {
                SettingsSectionTitle("Business")
                SettingsCard {
                    var first = true
                    if (showEnterpriseRow) {
                        SettingsCardRow(
                            icon = Icons.Filled.BusinessCenter,
                            iconTint = Color(0xFF8B5CF6),
                            iconBg = Color(0xFFEDE9FE),
                            title = "Enterprise Status",
                            subtitle = "Unlimited transactions & analytics",
                            valueBadge = "Active",
                            valueBadgeColor = Color(0xFF10B981)
                        )
                        first = false
                    }
                    if (showWorkspaceRow) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.Filled.Domain,
                            iconTint = Color(0xFF8B5CF6),
                            iconBg = Color(0xFFEDE9FE),
                            title = "Multi-Workspace",
                            subtitle = "Isolated company bookkeeping",
                            valueBadge = "Active",
                            valueBadgeColor = Color(0xFF10B981)
                        )
                        first = false
                    }
                    if (showInvoicingRow) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            iconTint = Color(0xFF06B6D4),
                            iconBg = Color(0xFFCFFAFE),
                            title = "Invoicing",
                            subtitle = "Automated billing cycles",
                            valueBadge = "Ready",
                            valueBadgeColor = Color(0xFF06B6D4)
                        )
                    }
                }
            }
        }

        // ── 9. DANGER ZONE ──
        if (showDangerSection) {
            item {
                SettingsSectionTitle("Danger Zone")
                SettingsCard {
                    var first = true
                    if (showClearRow) {
                        SettingsCardRow(
                            icon = Icons.Filled.DeleteSweep,
                            iconTint = Color(0xFFEF4444),
                            iconBg = Color(0xFFFEE2E2),
                            title = "Clear All Transactions",
                            subtitle = "Permanently erase records",
                            onClick = { viewModel.showClearDataConfirm() }
                        )
                        first = false
                    }
                    if (showDeleteRow) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.Filled.PersonRemove,
                            iconTint = Color(0xFFEF4444),
                            iconBg = Color(0xFFFEE2E2),
                            title = "Delete Account",
                            subtitle = "Remove profile & all data",
                            onClick = { viewModel.showDeleteAccountConfirm() }
                        )
                        first = false
                    }
                    if (showLogoutRow) {
                        if (!first) SettingsCardDivider()
                        SettingsCardRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            iconTint = Color(0xFFEF4444),
                            iconBg = Color(0xFFFEE2E2),
                            title = "Log Out",
                            subtitle = "End session on this device",
                            onClick = { viewModel.showLogoutConfirm() }
                        )
                    }
                }
            }
        }

        // ── 10. NO RESULTS ──
        if (noResults) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No results for \"$searchQuery\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Try different keywords",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // ── 11. FOOTER (hilang saat search) ──
        if (!isSearching) {
            item {
                val year = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Cashflow", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        letterSpacing = 1.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("Version 1.0.0 · © $year", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   SEARCH BAR (with clear button)
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Search, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        "Search Settings",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(LocalAccent.current.base),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                        .clickable { onClear() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Close, "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   SECTION TITLE
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 8.dp)
    )
}

/* ═══════════════════════════════════════════════════════════════
   CARD
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface),
        content = content
    )
}

@Composable
private fun SettingsCardDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    )
}

/* ═══════════════════════════════════════════════════════════════
   CARD ROW
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun SettingsCardRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    valueBadge: String? = null,
    valueBadgeColor: Color = Color(0xFF6B7280),
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (destructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.2).sp
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }

        if (valueBadge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(valueBadgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    valueBadge,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = valueBadgeColor
                )
            }
        }

        if (onClick != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   THEME ROW — Auto → system, Lt → light, Dk → dark
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ThemeRowInline(
    selected: String,
    onSelect: (String) -> Unit
) {
    val accent = LocalAccent.current

    val segments = listOf(
        "system" to "System",
        "light"  to "Light",
        "dark"   to "Dark"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Info, null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Theme",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.2).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                when (selected.lowercase()) {
                    "light" -> "Light"
                    "dark"  -> "Dark"
                    else    -> "System"
                },
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            segments.forEach { (value, label) ->
                val isSelected = selected.equals(value, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) accent.base else Color.Transparent)
                        .clickable { onSelect(value) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   ACCENT PALETTE
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun AccentPaletteInline(
    selectedId: String,
    canChange: Boolean,
    isBusiness: Boolean,
    onSelect: (String) -> Unit,
    onUpgradeClick: () -> Unit
) {
    val accent = LocalAccent.current

    val displayAccents: List<AccentColor> = remember(isBusiness) {
        if (isBusiness) AccentColors.ALL_ORDERED
        else AccentColors.ALL_ORDERED.filter { !it.businessOnly }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEDE9FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Info, null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Accent Color",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (isBusiness) "${displayAccents.size} colors available"
                    else "Upgrade to unlock more",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (!canChange && !isBusiness) {
                Text(
                    "Upgrade",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent.base,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onUpgradeClick() }
                        .background(accent.base.copy(alpha = 0.12f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        val perRow = 6
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            displayAccents.chunked(perRow).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { color ->
                        val isSelected = color.id == selectedId
                        val locked = !canChange && !isBusiness
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color.base)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (!locked) onSelect(color.id) else onUpgradeClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    repeat(perRow - rowItems.size) {
                        Spacer(Modifier.size(34.dp))
                    }
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   ACCENT COLOR LOCKED — Free plan
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun AccentColorLockedRow(
    onUpgradeClick: () -> Unit
) {
    val accent = LocalAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUpgradeClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEDE9FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Lock, null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Accent Color",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.2).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Pro & Business exclusive",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(accent.base.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                "Upgrade",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent.base
            )
        }
    }
}