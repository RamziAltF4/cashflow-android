package com.ramzi.cashflow.presentation.dashboard.imports

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.domain.model.DetectedTx
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.service.BcaNotificationListener
import com.ramzi.cashflow.util.Formatting
import com.ramzi.cashflow.util.NotificationAccessHelper
import kotlinx.coroutines.delay

/* ═══════════════════════════════════════════════════════════
   PALETTE — accent per section
   ═══════════════════════════════════════════════════════════ */
private object ImportsPalette {
    val Indigo = Color(0xFF6366F1)
    val IndigoDark = Color(0xFF4338CA)
    val Violet = Color(0xFF8B5CF6)
    val Emerald = Color(0xFF059669)
    val EmeraldLight = Color(0xFF10B981)
    val EmeraldBg = Color(0xFFECFDF5)
    val Rose = Color(0xFFE11D48)
    val RoseLight = Color(0xFFF43F5E)
    val RoseBg = Color(0xFFFFF1F2)
    val Amber = Color(0xFFF59E0B)
    val AmberBg = Color(0xFFFEF3C7)
    val Slate = Color(0xFF64748B)
    val Cyan = Color(0xFF06B6D4)
    val CyanBg = Color(0xFFCFFAFE)
}

@Composable
fun ImportsScreen(
    onBack: () -> Unit = {},
    viewModel: ImportsViewModel = viewModel(factory = ImportsViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current

    // ── Cek izin Notification Access ──
    var notifEnabled by remember {
        mutableStateOf(NotificationAccessHelper.isEnabled(context))
    }

    // Auto-refresh saat balik dari Settings HP
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notifEnabled = NotificationAccessHelper.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!notifEnabled) {
        // ── GATE: belum aktif ──
        BcaAutoImportNotEnabledScreen(
            onBack = onBack,
            onEnableClick = { openNotificationAccessSettings(context) }
        )
        return
    }

    // ── SUDAH AKTIF: tampilkan seperti biasa ──
    ImportsContent(onBack = onBack, viewModel = viewModel)
}

/* ═══════════════════════════════════════════════════════════
   CONTENT ASLI — dipisah supaya gate di atas bersih
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun ImportsContent(
    onBack: () -> Unit,
    viewModel: ImportsViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var scanTrigger by remember { mutableIntStateOf(0) }

    /* ═══ SNACKBAR ═══ */
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearToast()
        }
    }

    /* ═══ AUTO-REFRESH SETELAH SCAN ═══ */
    LaunchedEffect(scanTrigger) {
        if (scanTrigger > 0) {
            delay(2000)
            viewModel.refresh()
        }
    }

    val doScan: () -> Unit = {
        val ok = BcaNotificationListener.requestReplay(context)
        if (ok) scanTrigger++
        else viewModel.showToast("Listener belum aktif — transfer kecil dulu di MyBCA")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            /* ═══ HEADER ═══ */
            item {
                ImportsHeader(
                    pendingCount = state.pendingCount,
                    onBack = onBack,
                    onScan = doScan
                )
            }

            /* ═══ STATS ROW ═══ */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Menunggu",
                        value = "${state.pendingCount}",
                        accent = ImportsPalette.Amber,
                        accentBg = ImportsPalette.AmberBg,
                        icon = Icons.Filled.Inbox
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Total Hari Ini",
                        value = "${state.items.size}",
                        accent = ImportsPalette.Indigo,
                        accentBg = Color(0xFFDBEAFE),
                        icon = Icons.Filled.Check
                    )
                }
            }

            /* ═══ SCAN CTA ═══ */
            item {
                ScanCTA(hasItems = state.hasItems, onScan = doScan)
            }

            /* ═══ BULK ACTIONS ═══ */
            if (state.hasItems) {
                item {
                    Spacer(Modifier.height(20.dp))
                    BulkActionRow(
                        onSaveAll = { viewModel.requestSaveAll() },
                        onDismissAll = { viewModel.requestDismissAll() }
                    )
                }
            }

            /* ═══ SECTION LABEL ═══ */
            if (state.hasItems) {
                item {
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = "Terdeteksi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        letterSpacing = (-0.1).sp,
                        modifier = Modifier.padding(horizontal = 22.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            /* ═══ LIST / EMPTY ═══ */
            if (state.loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ImportsPalette.Indigo,
                            strokeWidth = 2.dp
                        )
                    }
                }
            } else if (!state.hasItems) {
                item { EmptyImports(onScan = doScan) }
            } else {
                items(state.items, key = { it.id }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        ImportItemCard(
                            item = item,
                            onSave = { viewModel.requestSave(item.id) },
                            onDismiss = { viewModel.requestDismiss(item.id) },
                            onAlias = { viewModel.openAliasModal(item.merchant) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(12.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface
            )
        }

        /* ═══ DIALOGS ═══ */
        ConfirmDialog(
            visible = state.confirmSaveId != null,
            title = "Simpan Transaksi?",
            message = "\"${state.confirmSaveTitle}\" akan disimpan ke daftar transaksi.",
            okText = "Simpan",
            danger = false,
            onOk = { viewModel.confirmSave() },
            onCancel = { viewModel.cancelSave() }
        )

        ConfirmDialog(
            visible = state.confirmDismissId != null,
            title = "Abaikan Transaksi?",
            message = "\"${state.confirmDismissTitle}\" tidak akan disimpan.",
            okText = "Abaikan",
            danger = true,
            onOk = { viewModel.confirmDismiss() },
            onCancel = { viewModel.cancelDismiss() }
        )

        ConfirmDialog(
            visible = state.confirmSaveAll,
            title = "Simpan Semua?",
            message = "${state.pendingCount} transaksi akan disimpan sekaligus.",
            okText = "Simpan Semua",
            danger = false,
            onOk = { viewModel.confirmSaveAll() },
            onCancel = { viewModel.cancelSaveAll() }
        )

        ConfirmDialog(
            visible = state.confirmDismissAll,
            title = "Abaikan Semua?",
            message = "${state.pendingCount} transaksi akan diabaikan.",
            okText = "Abaikan Semua",
            danger = true,
            onOk = { viewModel.confirmDismissAll() },
            onCancel = { viewModel.cancelDismissAll() }
        )

        if (state.aliasModalOpen) {
            AliasDialog(
                original = state.aliasOriginal,
                input = state.aliasDisplayInput,
                isSaving = state.aliasSaving,
                onInputChange = { viewModel.setAliasDisplayInput(it) },
                onSave = { viewModel.saveAlias() },
                onDismiss = { viewModel.closeAliasModal() }
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════
   GATE SCREEN — BELUM AKTIF
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun BcaAutoImportNotEnabledScreen(
    onBack: () -> Unit,
    onEnableClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Konten tengah ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ImportsPalette.Indigo.copy(alpha = 0.15f),
                                    ImportsPalette.Violet.copy(alpha = 0.15f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(
                                    Float.POSITIVE_INFINITY,
                                    Float.POSITIVE_INFINITY
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ImportsPalette.Indigo,
                                        ImportsPalette.IndigoDark
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "BCA Auto-Import Belum Aktif",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Aktifkan akses notifikasi dulu supaya Cashflow bisa " +
                            "membaca notifikasi MyBCA dan mencatat transaksi " +
                            "secara otomatis.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(28.dp))

                // Info card kecil
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFD1FAE5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Aman — notifikasi hanya dibaca di perangkat kamu, " +
                                "tidak dikirim ke mana-mana.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Tombol utama
                Button(
                    onClick = onEnableClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImportsPalette.Indigo,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Aktifkan Sekarang",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Kamu akan diarahkan ke Pengaturan Android",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════
   Buka Settings Notification Access
   ═══════════════════════════════════════════════════════════ */
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
        try {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        } catch (_: Exception) {
            NotificationAccessHelper.openSettings(context)
        }
    }
}

/* ═══════════════════════════════════════════════════════════
   HEADER — clean, tall, with big title
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun ImportsHeader(
    pendingCount: Int,
    onBack: () -> Unit,
    onScan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        ImportsPalette.Indigo,
                        ImportsPalette.IndigoDark,
                        Color(0xFF312E81)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 22.dp)
    ) {
        // Decorative circle
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onScan() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Scan notifikasi",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Title
            Text(
                text = "Terdeteksi",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.6).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Dari notifikasi MyBCA",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(18.dp))

            if (pendingCount > 0) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$pendingCount menunggu konfirmasi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Semua sudah ditinjau",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════
   STAT TILE — icon container + value
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    accentBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.3).sp
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════
   SCAN CTA
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun ScanCTA(
    hasItems: Boolean,
    onScan: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ImportsPalette.Indigo.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = ImportsPalette.Indigo.copy(alpha = 0.20f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onScan() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(ImportsPalette.Indigo, ImportsPalette.IndigoDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Scan Notifikasi",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ImportsPalette.IndigoDark,
                letterSpacing = (-0.1).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (hasItems)
                    "Cek notif MyBCA terbaru yang belum masuk"
                else "Baca notif MyBCA yang masih ada di panel",
                fontSize = 11.5.sp,
                color = ImportsPalette.Indigo.copy(alpha = 0.75f)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = ImportsPalette.IndigoDark,
            modifier = Modifier.size(18.dp)
        )
    }
}

/* ═══════════════════════════════════════════════════════════
   BULK ACTION ROW
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun BulkActionRow(
    onSaveAll: () -> Unit,
    onDismissAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BulkButton(
            modifier = Modifier.weight(1f),
            label = "Simpan Semua",
            icon = Icons.Filled.Check,
            accent = ImportsPalette.Emerald,
            accentBg = ImportsPalette.EmeraldBg,
            onClick = onSaveAll
        )
        BulkButton(
            modifier = Modifier.weight(1f),
            label = "Abaikan Semua",
            icon = Icons.Filled.Close,
            accent = ImportsPalette.Slate,
            accentBg = Color(0xFFF1F5F9),
            onClick = onDismissAll
        )
    }
}

/* ═══════════════════════════════════════════════════════════
   BULK BUTTON
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun BulkButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    accentBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accentBg)
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent
        )
    }
}

/* ═══════════════════════════════════════════════════════════
   ITEM CARD — premium minimalist
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun ImportItemCard(
    item: DetectedTx,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onAlias: () -> Unit
) {
    val isIncome = item.type == TxType.INCOME
    val accent = if (isIncome) ImportsPalette.Emerald else ImportsPalette.Rose
    val accentBg = if (isIncome) ImportsPalette.EmeraldBg else ImportsPalette.RoseBg
    val directionIcon = if (isIncome) Icons.Filled.SouthWest else Icons.Filled.NorthEast

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        // TOP ROW: icon + amount + date
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Direction icon container
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = directionIcon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isIncome) "Pemasukan" else "Pengeluaran",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = (if (isIncome) "+ " else "− ") + Formatting.formatRupiah(item.amount),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    letterSpacing = (-0.4).sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.date,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // MERCHANT ROW
        if (item.merchant.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.merchant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (isIncome) {
                    Surface(
                        onClick = onAlias,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Alias",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY BADGE
        if (item.categoryMapped.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.categoryMapped,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ACTION BUTTONS
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImportsPalette.Emerald,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Simpan", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Abaikan", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════
   EMPTY STATE
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun EmptyImports(onScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ImportsPalette.Indigo.copy(alpha = 0.12f),
                            ImportsPalette.Violet.copy(alpha = 0.12f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Inbox,
                contentDescription = null,
                tint = ImportsPalette.Indigo,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Belum ada transaksi",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.2).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Lakukan transfer di MyBCA,\nlalu tap tombol di bawah untuk scan.",
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ImportsPalette.Indigo)
                .clickable { onScan() }
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Scan Notifikasi",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════
   CONFIRM DIALOG
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    okText: String,
    danger: Boolean,
    onOk: () -> Unit,
    onCancel: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = (-0.2).sp
            )
        },
        text = {
            Text(
                message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onOk,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (danger) MaterialTheme.colorScheme.error
                    else ImportsPalette.Emerald,
                    contentColor = Color.White
                )
            ) {
                Text(okText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    "Batal",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/* ═══════════════════════════════════════════════════════════
   ALIAS DIALOG
   ═══════════════════════════════════════════════════════════ */
@Composable
private fun AliasDialog(
    original: String,
    input: String,
    isSaving: Boolean,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ImportsPalette.Indigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = ImportsPalette.Indigo,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Simpan Alias",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Beri nama yang lebih mudah diingat untuk pengirim ini.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(18.dp))

                OutlinedTextField(
                    value = original,
                    onValueChange = { },
                    label = { Text("Nama dari notifikasi", fontSize = 11.sp) },
                    enabled = false,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    label = { Text("Nama pilihanmu", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Mama, Bude, Kantor", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            "Batal",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onSave,
                        enabled = !isSaving && input.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImportsPalette.Indigo
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Simpan", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}