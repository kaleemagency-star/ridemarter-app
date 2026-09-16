package com.smartdrivo.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartdrivo.app.ui.theme.BrandGreenPrimary
import com.smartdrivo.app.ui.theme.BrandGreenVariant
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.viewmodel.ThemeMode
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        // Animate scale from 0.5f to 1f with spring
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Wait 2500ms total
        delay(2500L)
        onNavigateToOnboarding()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo placeholder (green circle with "SD" in white bold 32sp)
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(BrandGreenVariant, BrandGreenPrimary, Color(0xFF007A33))
                        )
                    )
                    .border(2.dp, Color(0x66FFFFFF), CircleShape)
                    .testTag("splash_logo"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SD",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name "SmartDrivo" in white, bold, 28sp
            Text(
                text = "SmartDrivo",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Slogan "Smart Rides. Smart Earnings." in green (#00C853), 14sp, italic
            Text(
                text = "Smart Rides. Smart Earnings.",
                color = BrandGreenPrimary,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag("splash_slogan")
            )
        }
    }
}

@Preview(name = "Splash Dark", showBackground = true)
@Composable
fun SplashScreenDarkPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        SplashScreen(onNavigateToOnboarding = {})
    }
}

@Preview(name = "Splash Light", showBackground = true)
@Composable
fun SplashScreenLightPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        SplashScreen(onNavigateToOnboarding = {})
    }
}
