package com.ramzi.cashflow.presentation.dashboard.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.domain.model.BillingMode
import com.ramzi.cashflow.presentation.dashboard.plans.components.PlanCard
import com.ramzi.cashflow.presentation.dashboard.plans.components.UpgradeRequestDialog
import com.ramzi.cashflow.presentation.theme.LocalAccent

@Composable
fun PlansScreen(
    viewModel: PlansViewModel = viewModel(factory = PlansViewModelFactory)
) {
    val state by viewModel.state.collectAsState()
    val accent = LocalAccent.current

    // Refresh rekening tiap kali screen dibuka lagi
    LaunchedEffect(Unit) {
        viewModel.refreshBankConfig()
    }

    // ============================================================
    // UPGRADE REQUEST DIALOG
    // ============================================================
    UpgradeRequestDialog(
        isOpen = state.showUpgradeDialog,
        plan = state.selectedPlan,
        billingMode = state.billingMode,
        bankConfig = state.bankConfig,             // ← INI YANG PENTING
        isSubmitting = state.submitting,
        errorMessage = state.submitError,
        onDismiss = { viewModel.closeUpgradeDialog() },
        onSubmit = { proofBase64, note ->
            viewModel.submitUpgradeRequest(proofBase64, note)
        }
    )

    // ============================================================
    // SUCCESS DIALOG
    // ============================================================
    if (state.submitSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Request Terkirim!", fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "Request upgrade kamu sudah dikirim ke admin. " +
                    "Tunggu verifikasi maksimal 1×24 jam.\n\n" +
                    "Setelah disetujui, paket kamu akan otomatis berubah.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("Mengerti", fontWeight = FontWeight.Black)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = 20.dp, bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Upgrade Paket",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Pilih paket yang sesuai kebutuhanmu",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Banner pending
        if (state.hasPendingRequest) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFFBEB))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Menunggu Verifikasi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF92400E)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Request upgrade kamu sedang diproses admin.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // Billing toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BillingChip(
                    modifier = Modifier.weight(1f),
                    label = "Bulanan",
                    active = state.billingMode == BillingMode.MONTHLY,
                    onClick = { viewModel.setBillingMode(BillingMode.MONTHLY) }
                )
                BillingChip(
                    modifier = Modifier.weight(1f),
                    label = "Tahunan • hemat",
                    active = state.billingMode == BillingMode.YEARLY,
                    onClick = { viewModel.setBillingMode(BillingMode.YEARLY) }
                )
            }
        }

        // Plan cards
        items(state.plans, key = { it.plan.id }) { plan ->
            PlanCard(
                plan = plan,
                billingMode = state.billingMode,
                isCurrent = plan.plan == state.currentPlan,
                onUpgrade = { viewModel.openUpgradeDialog(plan) }
            )
        }

        // Footer
        item {
            Text(
                text = "Pembayaran via transfer bank & lampirkan bukti. Admin akan verifikasi dalam 1×24 jam.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun BillingChip(
    modifier: Modifier = Modifier,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val accent = LocalAccent.current
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = if (active) accent.base else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}