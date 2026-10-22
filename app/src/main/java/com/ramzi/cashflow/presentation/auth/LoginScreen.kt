package com.ramzi.cashflow.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.data.local.ThemePreference
import com.ramzi.cashflow.domain.model.AccentColor
import com.ramzi.cashflow.domain.model.AuthError
import com.ramzi.cashflow.presentation.auth.components.AuthErrorBox
import com.ramzi.cashflow.presentation.auth.components.AuthPrimaryButton
import com.ramzi.cashflow.presentation.auth.components.AuthTabRow
import com.ramzi.cashflow.presentation.auth.components.AuthTextField
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.presentation.theme.ThemeToggleButton
import com.ramzi.cashflow.presentation.theme.ThemeViewModel
import com.ramzi.cashflow.presentation.theme.ThemeViewModelFactory

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToPinLogin: () -> Unit = {},
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

    LaunchedEffect(state.tab) {
        if (state.tab == AuthTab.REGISTER) {
            onGoToRegister()
            viewModel.setTab(AuthTab.LOGIN)
        }
    }

    ResetPasswordDialog(
        isOpen = state.resetDialogOpen,
        email = state.resetEmail,
        sending = state.resetSending,
        success = state.resetSuccess,
        error = state.resetError,
        onEmailChange = viewModel::onResetEmailChange,
        onDismiss = viewModel::closeResetPasswordDialog,
        onSend = viewModel::sendPasswordReset
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeroBackground(accent = accent, isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .statusBarsPadding()
        ) {
            // ===== HEADER =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, top = 32.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color.White.copy(alpha = 0.4f),
                                spotColor = Color.White.copy(alpha = 0.5f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.28f),
                                        Color.White.copy(alpha = 0.12f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "C",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    Text(
                        text = "Cashflow",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1.5).sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.7f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Kelola keuanganmu, dengan tenang.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.88f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.1.sp
                        )
                    }
                }

                ThemeToggleButton(
                    isDark = isDark,
                    onToggle = { themeVM.toggleLightDark(systemDark) }
                )
            }

            Spacer(Modifier.height(36.dp))

            // ===== FORM CARD =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = accent.glow,
                        spotColor = accent.glow
                    )
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 26.dp, vertical = 30.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(accent.base, accent.hover)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "→",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Masuk",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "Selamat datang kembali",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        AuthTabRow(
                            selected = 0,
                            onSelect = { idx -> if (idx == 1) onGoToRegister() }
                        )

                        Spacer(Modifier.height(24.dp))

                        AuthTextField(
                            value = state.loginEmail,
                            onValueChange = viewModel::onLoginEmailChange,
                            placeholder = "Email",
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )

                        Spacer(Modifier.height(14.dp))

                        AuthTextField(
                            value = state.loginPassword,
                            onValueChange = viewModel::onLoginPasswordChange,
                            placeholder = "Password",
                            isPassword = true,
                            passwordVisible = state.loginPasswordVisible,
                            onToggleVisible = viewModel::toggleLoginPasswordVisible,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Lupa password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent.base,
                                modifier = Modifier
                                    .clickable { viewModel.openResetPasswordDialog() }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = state.loginError != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            if (state.loginError != null) {
                                Column {
                                    Spacer(Modifier.height(8.dp))
                                    AuthErrorBox(errorMessage(state.loginError!!))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        AuthPrimaryButton(
                            text = "Masuk",
                            loading = state.loginLoading,
                            onClick = { viewModel.onLogin(onLoginSuccess) }
                        )

                        Spacer(Modifier.height(24.dp))

                        RowDivider()

                        Spacer(Modifier.height(20.dp))

                        PinButton(accent = accent, onClick = onGoToPinLogin)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ===== FOOTER =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
    text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Cashflow. All rights reserved.",
    fontSize = 10.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
)
            }
        }
    }
}

/* ============================================================
 * HERO BACKGROUND
 * ============================================================ */
@Composable
private fun HeroBackground(accent: AccentColor, isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")

    val orbShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbShift"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.base,
                        accent.hover,
                        if (isDark) Color(0xFF0F0A1F) else accent.hover.copy(alpha = 0.92f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    translationX = 40f * orbShift
                    translationY = -30f * orbShift
                }
                .blur(60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    translationX = -50f * orbShift
                    translationY = 40f * orbShift
                }
                .blur(50.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    translationY = 60f * orbShift
                }
                .blur(70.dp)
                .clip(CircleShape)
                .background(accent.glow)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.06f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, Color.Transparent),
                        center = Offset(0.5f, 0.3f),
                        radius = 600f
                    )
                )
        )
    }
}

@Composable
private fun RowDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )
                    )
                )
        )
        Text(
            text = "ATAU",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun PinButton(accent: AccentColor, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        color = accent.soft.copy(alpha = 0.6f),
        border = BorderStroke(
            width = 1.dp,
            color = accent.base.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(accent.base, accent.hover))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "••",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Masuk dengan PIN",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accent.text,
                letterSpacing = 0.2.sp
            )
        }
    }
}

internal fun errorMessage(e: AuthError): String = when (e) {
    AuthError.INVALID_CREDENTIALS -> "Email atau password salah."
    AuthError.TOO_MANY_REQUESTS -> "Terlalu banyak percobaan. Coba lagi nanti."
    AuthError.USER_DISABLED -> "Akun ini sedang dinonaktifkan."
    AuthError.EMAIL_ALREADY_IN_USE -> "Email sudah terdaftar. Silakan masuk."
    AuthError.INVALID_EMAIL -> "Format email tidak valid."
    AuthError.WEAK_PASSWORD -> "Password minimal 8 karakter."
    AuthError.ACCOUNT_NOT_FOUND -> "Akun tidak ditemukan."
    AuthError.PIN_NOT_SET -> "PIN belum diaktifkan untuk akun ini."
    AuthError.PIN_TOO_MANY_ATTEMPTS -> "Terlalu banyak percobaan PIN. Coba lagi nanti."
    AuthError.FIREBASE_NOT_READY -> "Layanan belum siap. Coba lagi."
    AuthError.INVALID_SERVER_RESPONSE -> "Server mengembalikan respons tidak valid."
    AuthError.CUSTOM_TOKEN_MISSING -> "Custom token tidak diterima dari server."
    AuthError.NETWORK -> "Koneksi bermasalah. Cek internet Anda."
    AuthError.UNKNOWN -> "Terjadi kesalahan. Coba lagi."
}