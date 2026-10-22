package com.ramzi.cashflow.presentation.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramzi.cashflow.data.local.ThemePreference
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.presentation.theme.ThemeToggleButton
import com.ramzi.cashflow.presentation.theme.ThemeViewModel
import com.ramzi.cashflow.presentation.theme.ThemeViewModelFactory

@Composable
fun PinLoginScreen(
    onLoginSuccess: () -> Unit,
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

    LaunchedEffect(state.pinDigits) {
        if (state.pinDigits.length == 6 && !state.pinLoading) {
            viewModel.onPinSubmit(onLoginSuccess)
        }
    }

    val onDigitClick: (String) -> Unit = { digit ->
        if (state.pinDigits.length < 6 && !state.pinLoading) {
            viewModel.onPinInput(state.pinDigits + digit)
        }
    }

    val onBackspace: () -> Unit = {
        if (state.pinDigits.isNotEmpty() && !state.pinLoading) {
            viewModel.onPinInput(state.pinDigits.dropLast(1))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            ThemeToggleButton(
                isDark = isDark,
                onToggle = { themeVM.toggleLightDark(systemDark) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(80.dp))

            // HEADER & ICON
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(accent.base.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = accent.base,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Verifikasi PIN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Masukkan 6 digit kode keamanan akun kamu",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // PIN DOTS INDICATOR
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { index ->
                        val isFilled = index < state.pinDigits.length
                        val dotColor by animateColorAsState(
                            targetValue = if (isFilled) accent.base else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            animationSpec = tween(durationMillis = 150),
                            label = "dotColor"
                        )

                        Box(
                            modifier = Modifier
                                .size(if (isFilled) 18.dp else 14.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                // ERROR / LOADING INDICATOR
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.pinLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = accent.base
                        )
                    } else if (state.pinError != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PIN salah. Silakan coba lagi.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ON-SCREEN FINTECH NUMERIC KEYPAD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                keypadRows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowKeys.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(Modifier.weight(1f))
                            } else if (key == "DEL") {
                                Surface(
                                    onClick = onBackspace,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Hapus",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    onClick = { onDigitClick(key) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    shadowElevation = 1.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Masuk menggunakan Email & Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}