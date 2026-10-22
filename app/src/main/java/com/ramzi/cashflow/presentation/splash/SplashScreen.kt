package com.ramzi.cashflow.presentation.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.7f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(900)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) onAuthenticated() else onUnauthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        ) {
            // Glowing App Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0xFF6366F1),
                        ambientColor = Color(0xFF6366F1)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Cashflow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.8).sp,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Smart Financial & Business Intelligence",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = Color(0xFF6366F1)
            )
        }
    }
}