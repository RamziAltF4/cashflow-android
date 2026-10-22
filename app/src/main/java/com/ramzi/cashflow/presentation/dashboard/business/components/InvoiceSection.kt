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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ramzi.cashflow.domain.model.Invoice
import com.ramzi.cashflow.domain.model.InvoiceStatus
import com.ramzi.cashflow.util.Formatting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceSection(
    invoices: List<Invoice>,
    filteredInvoices: List<Invoice>,
    selectedIds: Set<String>,
    isBatchUpdating: Boolean,
    searchQuery: String,
    statusFilter: InvoiceStatus?,
    totalPendingAmount: Long,
    totalPaidAmount: Long,
    onSearchChange: (String) -> Unit,
    onFilterStatusChange: (InvoiceStatus?) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Invoice) -> Unit,
    onViewDetail: (Invoice) -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onUpdateStatus: (String, InvoiceStatus) -> Unit,
    onDelete: (String) -> Unit,
    onBatchUpdate: (InvoiceStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp)
    ) {
        // ============ HEADER ============
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Manajemen Invoice",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${invoices.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6366F1)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Lacak pembayaran & update batch status invoice",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6366F1))
                    .clickable { onAdd() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Buat",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // ============ SUMMARY METRICS ============
        if (invoices.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InvoiceMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Piutang (Pending)",
                    amount = totalPendingAmount,
                    color = Color(0xFFF59E0B)
                )
                InvoiceMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Tertagih (Lunas)",
                    amount = totalPaidAmount,
                    color = Color(0xFF10B981)
                )
            }
        }

        // ============ SEARCH & STATUS FILTERS ============
        if (invoices.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari no. invoice / nama klien...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusFilterChip(
                    label = "Semua",
                    active = statusFilter == null,
                    onClick = { onFilterStatusChange(null) }
                )
                StatusFilterChip(
                    label = "Pending",
                    active = statusFilter == InvoiceStatus.PENDING,
                    onClick = { onFilterStatusChange(InvoiceStatus.PENDING) }
                )
                StatusFilterChip(
                    label = "Lunas",
                    active = statusFilter == InvoiceStatus.PAID,
                    onClick = { onFilterStatusChange(InvoiceStatus.PAID) }
                )
                StatusFilterChip(
                    label = "Batal",
                    active = statusFilter == InvoiceStatus.CANCELLED,
                    onClick = { onFilterStatusChange(InvoiceStatus.CANCELLED) }
                )
            }
        }

        // ============ BATCH ACTION TOOLBAR ============
        if (invoices.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedIds.isEmpty()) "Pilih item untuk Batch Update"
                    else "${selectedIds.size} dipilih",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                if (selectedIds.isNotEmpty()) {
                    TextButton(onClick = onClearSelection) {
                        Text("Batal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = onSelectAll) {
                        Text("Pilih Semua", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (selectedIds.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onBatchUpdate(InvoiceStatus.PAID) },
                        enabled = !isBatchUpdating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isBatchUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text("Tandai Lunas", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { onBatchUpdate(InvoiceStatus.PENDING) },
                        enabled = !isBatchUpdating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pending", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { onBatchUpdate(InvoiceStatus.CANCELLED) },
                        enabled = !isBatchUpdating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batalkan", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ============ LIST ============
        if (filteredInvoices.isEmpty()) {
            EmptyInvoiceState(hasFilter = searchQuery.isNotBlank() || statusFilter != null)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredInvoices.forEach { inv ->
                    InvoiceRowItem(
                        invoice = inv,
                        isSelected = selectedIds.contains(inv.id),
                        onToggleSelect = { onToggleSelect(inv.id) },
                        onViewDetail = { onViewDetail(inv) },
                        onEdit = { onEdit(inv) },
                        onUpdateStatus = { onUpdateStatus(inv.id, it) },
                        onDelete = { onDelete(inv.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Long,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = Formatting.formatRupiah(amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) Color(0xFF6366F1)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InvoiceRowItem(
    invoice: Invoice,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onUpdateStatus: (InvoiceStatus) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.PAID -> Color(0xFF10B981)
        InvoiceStatus.PENDING -> Color(0xFFF59E0B)
        InvoiceStatus.CANCELLED -> Color(0xFFEF4444)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                if (isSelected) Color(0xFF6366F1).copy(alpha = 0.05f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onViewDetail() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable { onToggleSelect() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = invoice.invoiceNumber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = invoice.status.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = invoice.clientName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = Formatting.formatRupiah(invoice.amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Edit Button
            Surface(
                onClick = onEdit,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                )
            }

            // Quick Mark as Paid if not paid
            if (invoice.status != InvoiceStatus.PAID) {
                Surface(
                    onClick = { onUpdateStatus(InvoiceStatus.PAID) },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                ) {
                    Text(
                        "Lunas",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            // Delete Button
            Surface(
                onClick = onDelete,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

@Composable
fun InvoiceDetailDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onUpdateStatus: (InvoiceStatus) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.PAID -> Color(0xFF10B981)
        InvoiceStatus.PENDING -> Color(0xFFF59E0B)
        InvoiceStatus.CANCELLED -> Color(0xFFEF4444)
    }

    val dateFormatted = try {
        if (invoice.date > 0) {
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(Date(invoice.date))
        } else "-"
    } catch (_: Exception) {
        "-"
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = invoice.invoiceNumber,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rincian Tagihan Resmi",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = invoice.status.label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Detail Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailRow(label = "Klien / Pembeli", value = invoice.clientName)
                        DetailRow(label = "Nominal Tagihan", value = Formatting.formatRupiah(invoice.amount))
                        DetailRow(label = "Tanggal Terbit", value = dateFormatted)
                        DetailRow(label = "Status Pembayaran", value = invoice.status.label)
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Quick Status Changer
                Text(
                    text = "Ubah Status Tagihan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdateStatus(InvoiceStatus.PAID) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lunas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onUpdateStatus(InvoiceStatus.PENDING) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onUpdateStatus(InvoiceStatus.CANCELLED) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Hapus", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InvoiceDeleteConfirmDialog(
    invoice: Invoice,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Hapus Invoice?",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Apakah Anda yakin ingin menghapus invoice ${invoice.invoiceNumber} untuk ${invoice.clientName}?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Nominal: ${Formatting.formatRupiah(invoice.amount)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Tindakan ini permanen dan dokumen tagihan akan dihapus dari server.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Hapus Permanen", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun EmptyInvoiceState(hasFilter: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (hasFilter) "Tidak Ada Invoice yang Cocok" else "Belum Ada Invoice",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (hasFilter) "Coba sesuaikan kata kunci pencarian atau filter status."
                else "Klik tombol \"Buat\" di atas untuk mencatat invoice pertama",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
