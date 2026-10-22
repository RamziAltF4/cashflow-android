package com.ramzi.cashflow.presentation.dashboard.business.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ramzi.cashflow.domain.model.RecurringFrequency
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.presentation.dashboard.business.BusinessState

@Composable
fun RecurringModal(
    state: BusinessState,
    onDescChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TxType) -> Unit,
    onCategoryChange: (String) -> Unit,
    onFrequencyChange: (RecurringFrequency) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!state.recSaving) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (state.editingRecurringId == null)
                        "Tambah Transaksi Berulang"
                    else "Edit Transaksi Berulang",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(14.dp))

                // ============ TYPE TOGGLE ============
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    TypeChip(
                        modifier = Modifier.weight(1f),
                        label = "Pengeluaran",
                        active = state.recType == TxType.EXPENSE,
                        onClick = { onTypeChange(TxType.EXPENSE) }
                    )
                    TypeChip(
                        modifier = Modifier.weight(1f),
                        label = "Pemasukan",
                        active = state.recType == TxType.INCOME,
                        onClick = { onTypeChange(TxType.INCOME) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ============ DESC ============
                OutlinedTextField(
                    value = state.recDesc,
                    onValueChange = onDescChange,
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Netflix, Gaji, Listrik...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // ============ AMOUNT ============
                OutlinedTextField(
                    value = state.recAmountText,
                    onValueChange = onAmountChange,
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // ============ CATEGORY ============
                OutlinedTextField(
                    value = state.recCategory,
                    onValueChange = onCategoryChange,
                    label = { Text("Kategori") },
                    placeholder = { Text("Tagihan, Hiburan, dll") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                // ============ FREQUENCY ============
                Text(
                    text = "Frekuensi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecurringFrequency.entries.forEach { freq ->
                        FreqChip(
                            modifier = Modifier.weight(1f),
                            label = freq.label,
                            active = state.recFrequency == freq,
                            onClick = { onFrequencyChange(freq) }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ============ DATES ============
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.recStartDate,
                        onValueChange = onStartDateChange,
                        label = { Text("Mulai") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.recEndDate,
                        onValueChange = onEndDateChange,
                        label = { Text("Berhenti") },
                        placeholder = { Text("(opsional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ============ NOTE ============
                OutlinedTextField(
                    value = state.recNote,
                    onValueChange = onNoteChange,
                    label = { Text("Catatan (opsional)") },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // ============ ACTIVE SWITCH (edit only) ============
                if (state.editingRecurringId != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aktif",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nonaktifkan untuk pause tanpa hapus",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.recActive,
                            onCheckedChange = onActiveChange
                        )
                    }
                }

                // ============ ERROR ============
                state.recError?.let { err ->
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = err,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ============ BUTTONS ============
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !state.recSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSave,
                        enabled = !state.recSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.recSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Simpan", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/* ============================================================
   CHIPS
   ============================================================ */
@Composable
private fun TypeChip(
    modifier: Modifier = Modifier,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (active) MaterialTheme.colorScheme.surface
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FreqChip(
    modifier: Modifier = Modifier,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .border(
                width = 1.dp,
                color = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(9.dp)
            )
            .background(
                if (active) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.Medium,
            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}