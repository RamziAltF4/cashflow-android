package com.ramzi.cashflow.presentation.dashboard.tx

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.Categories
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.presentation.dashboard.tx.components.CategoryIconShape
import com.ramzi.cashflow.presentation.theme.LocalAccent
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val idLocale = Locale("id", "ID")

private fun formatDigitsToRupiah(digits: String): String {
    if (digits.isBlank()) return ""
    val n = digits.toLongOrNull() ?: return digits
    return NumberFormat.getNumberInstance(idLocale).format(n)
}

private fun isoToDisplayDate(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) ?: return iso
        SimpleDateFormat("EEEE, d MMMM yyyy", idLocale).format(d)
    } catch (_: Throwable) { iso }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxFormScreen(viewModel: TxViewModel) {
    val state by viewModel.state.collectAsState()
    val accent = LocalAccent.current

    if (!state.formOpen) return

    val isIncome = state.formType == TxType.INCOME
    val isEdit = state.formEditingId != null
    val typeColor by animateColorAsState(
        targetValue = if (isIncome) Color(0xFF10B981) else Color(0xFFF43F5E),
        animationSpec = tween(durationMillis = 250),
        label = "typeColor"
    )

    // ---------- DATE PICKER DIALOG ----------
    if (state.datePickerOpen) {
        val initialMillis = remember {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(state.formDate)?.time
                    ?: System.currentTimeMillis()
            } catch (_: Throwable) { System.currentTimeMillis() }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.closeDatePicker() },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        viewModel.setFormDateFromMillis(millis)
                    } else {
                        viewModel.closeDatePicker()
                    }
                }) {
                    Text("Pilih Tanggal", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDatePicker() }) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // TOP BAR
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEdit) "Edit Transaksi" else "Tambah Transaksi",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isEdit) "Perbarui informasi transaksi tercatat" else "Catat arus kas masuk atau keluar",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeForm() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            // CONTENT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // TYPE SWITCHER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypeSegmentTab(
                        modifier = Modifier.weight(1f),
                        label = "Pemasukan",
                        icon = Icons.Filled.ArrowUpward,
                        active = isIncome,
                        activeColor = Color(0xFF10B981),
                        onClick = { viewModel.onFormTypeChange(TxType.INCOME) }
                    )
                    TypeSegmentTab(
                        modifier = Modifier.weight(1f),
                        label = "Pengeluaran",
                        icon = Icons.Filled.ArrowDownward,
                        active = !isIncome,
                        activeColor = Color(0xFFF43F5E),
                        onClick = { viewModel.onFormTypeChange(TxType.EXPENSE) }
                    )
                }

                // HERO AMOUNT CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, typeColor.copy(alpha = 0.25f)),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NOMINAL TRANSAKSI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.6.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Rp",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = typeColor.copy(alpha = 0.65f)
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = state.formAmount,
                                onValueChange = viewModel::onFormAmountChange,
                                textStyle = TextStyle(
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    color = typeColor,
                                    letterSpacing = (-1).sp,
                                    textAlign = TextAlign.Start
                                ),
                                cursorBrush = SolidColor(typeColor),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                visualTransformation = RupiahInputTransformation(),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (state.formAmount.isBlank()) {
                                            Text(
                                                text = "0",
                                                fontSize = 34.sp,
                                                fontWeight = FontWeight.Black,
                                                color = typeColor.copy(alpha = 0.25f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.widthIn(min = 100.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // QUICK PRESET CHIPS
                        val currentVal = state.formAmount.toLongOrNull() ?: 0L
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                        ) {
                            PresetChip("+10rb") {
                                viewModel.onFormAmountChange((currentVal + 10000L).toString())
                            }
                            PresetChip("+50rb") {
                                viewModel.onFormAmountChange((currentVal + 50000L).toString())
                            }
                            PresetChip("+100rb") {
                                viewModel.onFormAmountChange((currentVal + 100000L).toString())
                            }
                            PresetChip("+500rb") {
                                viewModel.onFormAmountChange((currentVal + 50000L).toString())
                            }
                            PresetChip("+1jt") {
                                viewModel.onFormAmountChange((currentVal + 1000000L).toString())
                            }
                            if (currentVal > 0) {
                                PresetChip("Reset", isReset = true) {
                                    viewModel.onFormAmountChange("")
                                }
                            }
                        }
                    }
                }

                // DESKRIPSI
                Column {
                    Text(
                        text = "DESKRIPSI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.formDesc,
                        onValueChange = viewModel::onFormDescChange,
                        placeholder = {
                            Text(
                                text = if (isIncome) "Contoh: Gaji Bulanan / Penjualan Produk"
                                else "Contoh: Belanja Bulanan / Makan Siang",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = typeColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // KATEGORI — 4 COLUMN GRID DENGAN ICON SHAPE
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KATEGORI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = state.formCategory.ifBlank { "Pilih satu" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    val categories = Categories.forType(state.formType)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categories.chunked(4).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { cat ->
                                    CategoryTile(
                                        modifier = Modifier.weight(1f),
                                        name = cat,
                                        active = state.formCategory == cat,
                                        accentColor = typeColor,
                                        onClick = { viewModel.onFormCategoryChange(cat) }
                                    )
                                }
                                repeat(4 - rowItems.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // TANGGAL TRANSAKSI
                Column {
                    Text(
                        text = "TANGGAL TRANSAKSI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatePillButton(
                            modifier = Modifier.weight(1f),
                            label = "Hari ini",
                            onClick = { viewModel.setFormDateToday() }
                        )
                        DatePillButton(
                            modifier = Modifier.weight(1f),
                            label = "Kemarin",
                            onClick = { viewModel.setFormDateYesterday() }
                        )
                        DatePillButton(
                            modifier = Modifier.weight(1f),
                            label = "7 hari lalu",
                            onClick = { viewModel.setFormDateDaysAgo(7) }
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        onClick = { viewModel.openDatePicker() },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(typeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Waktu Pencatatan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = isoToDisplayDate(state.formDate).ifBlank { "Pilih tanggal" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // CATATAN (OPSIONAL)
                Column {
                    Text(
                        text = "CATATAN TAMBAHAN (OPSIONAL)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.formNote,
                        onValueChange = viewModel::onFormNoteChange,
                        placeholder = {
                            Text("Tuliskan detail atau nomor struk (opsional)...", fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = typeColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ERROR NOTIFICATION
                if (state.formError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = state.formError!!,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
            }

            // BOTTOM ACTION BAR
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEdit) {
                        OutlinedButton(
                            onClick = {
                                val editId = state.formEditingId
                                if (editId != null) {
                                    viewModel.closeForm()
                                    viewModel.requestDelete(editId)
                                }
                            },
                            enabled = !state.formSaving,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Hapus", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.closeForm() },
                            enabled = !state.formSaving,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text("Batal", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.saveForm() },
                        enabled = !state.formSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = typeColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                    ) {
                        if (state.formSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isEdit) "Simpan Perubahan" else "Simpan Transaksi",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Type Segment Tab
// ============================================================
@Composable
private fun TypeSegmentTab(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) activeColor
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// Quick Preset Chip
// ============================================================
@Composable
private fun PresetChip(
    label: String,
    isReset: Boolean = false,
    onClick: () -> Unit
) {
    val accent = LocalAccent.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isReset) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        else accent.soft.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isReset) MaterialTheme.colorScheme.error else accent.text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// ============================================================
// Date Pill Button
// ============================================================
@Composable
private fun DatePillButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ============================================================
// Category Tile — dengan icon shape (Canvas) & selection border
// ============================================================
@Composable
private fun CategoryTile(
    modifier: Modifier = Modifier,
    name: String,
    active: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val iconColor = if (active) accentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (active) accentColor.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (active) BorderStroke(1.5.dp, accentColor)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CategoryIconShape(
                category = name,
                color = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = if (active) FontWeight.Black else FontWeight.Medium,
                color = if (active) accentColor else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                lineHeight = 11.sp
            )
        }
    }
}

// ============================================================
// Rupiah input transformation
// ============================================================
private class RupiahInputTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = formatDigitsToRupiah(original)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (original.isEmpty()) return 0
                val digitsBefore = offset.coerceIn(0, original.length)
                var count = 0
                var i = 0
                while (i < formatted.length && count < digitsBefore) {
                    if (formatted[i].isDigit()) count++
                    i++
                }
                return i
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (formatted.isEmpty()) return 0
                var count = 0
                for (i in 0 until offset.coerceAtMost(formatted.length)) {
                    if (formatted[i].isDigit()) count++
                }
                return count
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}