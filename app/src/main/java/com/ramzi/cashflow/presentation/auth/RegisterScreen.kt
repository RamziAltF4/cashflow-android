package com.ramzi.cashflow.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.data.local.ThemePreference
import com.ramzi.cashflow.presentation.auth.components.AuthErrorBox
import com.ramzi.cashflow.presentation.auth.components.AuthPrimaryButton
import com.ramzi.cashflow.presentation.auth.components.AuthTabRow
import com.ramzi.cashflow.presentation.auth.components.AuthTextField
import com.ramzi.cashflow.presentation.auth.components.PasswordRequirements
import com.ramzi.cashflow.presentation.auth.components.PasswordStrengthMeter
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.presentation.theme.ThemeToggleButton
import com.ramzi.cashflow.presentation.theme.ThemeViewModel
import com.ramzi.cashflow.presentation.theme.ThemeViewModelFactory

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory.create(LocalContext.current)
    )
) {
    val state by viewModel.state.collectAsState()
    val accent = LocalAccent.current

    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val themeVM: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(context))
    val themeMode by themeVM.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemePreference.MODE_DARK -> true
        ThemePreference.MODE_LIGHT -> false
        else -> systemDark
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.base,
                            accent.hover,
                            accent.hover.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 30.dp, end = 30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopStart)
                    .padding(top = 110.dp, start = 50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // Header: Back + Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 40.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "←",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Kembali",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.weight(1f))

                ThemeToggleButton(
                    isDark = isDark,
                    onToggle = { themeVM.toggleLightDark(systemDark) }
                )
            }

            // Hero Branding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, top = 4.dp, bottom = 28.dp)
            ) {
                Text(
                    text = "Buat Akun",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Gratis · Tanpa kartu kredit · 1 menit selesai",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    // Step Indicator
                    StepIndicator(step = state.regStep)

                    Spacer(Modifier.height(16.dp))

                    // Step Title
                    Text(
                        text = if (state.regStep == 1) "Data Diri" else "Buat Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state.regStep == 1)
                            "Isi nama dan email kamu"
                        else
                            "Buat password yang kuat untuk keamanan akun",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(20.dp))

                    // Tab Switcher
                    AuthTabRow(
                        selected = 1,
                        onSelect = { idx -> if (idx == 0) onBack() }
                    )

                    Spacer(Modifier.height(24.dp))

                    if (state.regStep == 1) {
                        // STEP 1: Nama + Email
                        AuthTextField(
                            value = state.regName,
                            onValueChange = viewModel::onRegNameChange,
                            placeholder = "Nama Lengkap",
                            imeAction = ImeAction.Next
                        )
                        Spacer(Modifier.height(14.dp))
                        AuthTextField(
                            value = state.regEmail,
                            onValueChange = viewModel::onRegEmailChange,
                            placeholder = "Email",
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )

                        if (state.regError != null) {
                            Spacer(Modifier.height(14.dp))
                            AuthErrorBox(errorMessage(state.regError!!))
                        }

                        Spacer(Modifier.height(24.dp))
                        AuthPrimaryButton(
                            text = "Lanjutkan",
                            enabled = state.registerStep1Valid,
                            onClick = viewModel::onRegisterStep1
                        )
                    } else {
                        // STEP 2: Password
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "EMAIL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = state.regEmail,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        AuthTextField(
                            value = state.regPassword,
                            onValueChange = viewModel::onRegPasswordChange,
                            placeholder = "Password",
                            isPassword = true,
                            passwordVisible = state.regPasswordVisible,
                            onToggleVisible = viewModel::toggleRegPasswordVisible,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )

                        if (state.regPassword.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            PasswordStrengthMeter(strength = state.passwordStrength)
                        }

                        Spacer(Modifier.height(14.dp))
                        PasswordRequirements(
                            minLength = state.passwordMinLength,
                            hasUpper = state.passwordHasUpper,
                            hasDigit = state.passwordHasDigit,
                            hasSymbol = state.passwordHasSymbol
                        )

                        Spacer(Modifier.height(16.dp))

                        AuthTextField(
                            value = state.regConfirm,
                            onValueChange = viewModel::onRegConfirmChange,
                            placeholder = "Konfirmasi Password",
                            isPassword = true,
                            passwordVisible = state.regConfirmVisible,
                            onToggleVisible = viewModel::toggleRegConfirmVisible,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )

                        AnimatedVisibility(
                            visible = state.regConfirm.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                val match = state.passwordsMatch
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (match) Color(0xFF10B981)
                                                else MaterialTheme.colorScheme.error
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (match) "✓" else "✕",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (match) "Password cocok"
                                        else "Password tidak cocok",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (match) Color(0xFF10B981)
                                        else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        if (state.regError != null) {
                            Spacer(Modifier.height(14.dp))
                            AuthErrorBox(errorMessage(state.regError!!))
                        }

                        Spacer(Modifier.height(20.dp))

                        AuthPrimaryButton(
                            text = "Buat Akun",
                            loading = state.regLoading,
                            enabled = state.passwordValid && state.passwordsMatch,
                            onClick = { viewModel.onRegisterStep2(onRegisterSuccess) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            if (state.regStep == 1) onBack()
                            else viewModel.onRegisterStep1Back()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (state.regStep == 1)
                                "Sudah punya akun? Masuk"
                            else
                                "← Ganti email",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent.base
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dengan mendaftar, kamu menyetujui Syarat & Ketentuan.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int) {
    val accent = LocalAccent.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(2) { i ->
            val active = i < step
            val current = i == step - 1
            Box(
                modifier = Modifier
                    .width(if (current) 32.dp else 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (active) accent.base
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
            if (i == 0) Spacer(Modifier.width(6.dp))
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Langkah $step dari 2",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}