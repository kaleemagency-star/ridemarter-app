package com.smartdrivo.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartdrivo.app.ui.components.CommunitySocialRow
import com.smartdrivo.app.ui.components.FeatureCard
import com.smartdrivo.app.ui.components.PlansHorizontalRow
import com.smartdrivo.app.ui.components.StatItem
import com.smartdrivo.app.ui.components.StatsBannerRow
import com.smartdrivo.app.ui.components.defaultFeatures
import com.smartdrivo.app.ui.theme.BrandDarkGreenGradEnd
import com.smartdrivo.app.ui.theme.BrandDarkGreenGradStart
import com.smartdrivo.app.ui.theme.BrandGreenPrimary
import com.smartdrivo.app.ui.theme.BrandGreenVariant
import com.smartdrivo.app.ui.theme.DarkBackground
import com.smartdrivo.app.ui.theme.DarkCardBorder
import com.smartdrivo.app.ui.theme.DarkSurface
import com.smartdrivo.app.ui.theme.DarkTextMuted
import com.smartdrivo.app.ui.theme.DarkTextSecondary
import com.smartdrivo.app.ui.theme.OlaGreen
import com.smartdrivo.app.ui.theme.RapidoOrange
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.ui.theme.UberBlack
import com.smartdrivo.app.viewmodel.ThemeMode

@Composable
fun OnboardingScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statsList = listOf(
        StatItem("50+", "Commands"),
        StatItem("1ms", "Speed"),
        StatItem("3 Apps", "Supported")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- TOP APP BAR ---
            TopAppBarSection(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )

            // --- SCROLLABLE MAIN CONTENT ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("onboarding_lazy_column"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = 120.dp // Room for sticky bottom section
                )
            ) {
                // HERO SECTION
                item {
                    HeroSection(modifier = Modifier.padding(16.dp))
                }

                // STATS BANNER
                item {
                    StatsBannerRow(
                        stats = statsList,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // FEATURES SECTION
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Why SmartDrivo?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("features_section_title")
                    )
                }

                items(defaultFeatures, key = { it.id }) { feature ->
                    FeatureCard(
                        feature = feature,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // SUPPORTED APPS SECTION
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    SupportedAppsSection(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // PLANS PREVIEW SECTION
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Simple Plans",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.testTag("plans_section_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Unlock full access with affordable plans",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PlansHorizontalRow()
                }

                // COMMUNITY SECTION
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Join Our Community",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.testTag("community_section_title")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CommunitySocialRow()
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // --- BOTTOM STICKY SECTION ---
        BottomStickySection(
            onLoginClick = onNavigateToLogin,
            onGetStartedClick = onNavigateToGetStarted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

// -------------------------------------------------------------
// TOP APP BAR COMPOSABLE
// -------------------------------------------------------------
@Composable
private fun TopAppBarSection(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkBackground,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Name & Beta Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SmartDrivo",
                    color = BrandGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.testTag("top_bar_app_name")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Green BETA badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrandGreenPrimary.copy(alpha = 0.2f))
                        .border(0.5.dp, BrandGreenPrimary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BETA",
                        color = BrandGreenPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dark/Light Toggle Button
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E1E))
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = BrandGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// HERO SECTION COMPOSABLE
// -------------------------------------------------------------
@Composable
private fun HeroSection(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF005511))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandDarkGreenGradStart, BrandDarkGreenGradEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // AI Powered pulsing badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x3300C853))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        // Pulsing green dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(BrandGreenVariant.copy(alpha = pulseAlpha))
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "AI Powered",
                            color = BrandGreenVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Drive Smarter",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Auto-accept rides on Rapido, Uber & Ola",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right side: car/bike graphic icon in green
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0x2200C853))
                        .border(1.dp, Color(0x5500C853), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Driver Vehicle",
                        tint = BrandGreenPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUPPORTED APPS SECTION COMPOSABLE
// -------------------------------------------------------------
@Composable
private fun SupportedAppsSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Works With",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.testTag("supported_apps_title")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rapido
            AppBadgeCard(
                name = "Rapido",
                symbol = "R",
                backgroundColor = RapidoOrange,
                borderColor = Color.Transparent
            )

            // Uber
            AppBadgeCard(
                name = "Uber",
                symbol = "U",
                backgroundColor = UberBlack,
                borderColor = Color(0x66FFFFFF)
            )

            // Ola
            AppBadgeCard(
                name = "Ola",
                symbol = "O",
                backgroundColor = OlaGreen,
                borderColor = Color.Transparent
            )
        }
    }
}

@Composable
private fun AppBadgeCard(
    name: String,
    symbol: String,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.testTag("app_badge_${name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// -------------------------------------------------------------
// BOTTOM STICKY SECTION COMPOSABLE
// -------------------------------------------------------------
@Composable
private fun BottomStickySection(
    onLoginClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left text: "Already a member?"
                Text(
                    text = "Already a member?",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Right side: Login and Get Started side-by-side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 1: "Login" - outlined green border, green text, rounded
                    OutlinedButton(
                        onClick = onLoginClick,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, BrandGreenPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandGreenPrimary
                        ),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("onboarding_login_button")
                    ) {
                        Text(
                            text = "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Button 2: "Get Started" - filled green background, white text, rounded
                    Button(
                        onClick = onGetStartedClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("onboarding_get_started_button")
                    ) {
                        Text(
                            text = "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Small text below buttons centered: "Admin approval required after registration" gray 10sp
            Text(
                text = "Admin approval required after registration",
                color = DarkTextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Onboarding Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun OnboardingScreenDarkPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        OnboardingScreen(
            isDarkTheme = true,
            onToggleTheme = {},
            onNavigateToLogin = {},
            onNavigateToGetStarted = {}
        )
    }
}

@Preview(name = "Onboarding Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun OnboardingScreenLightPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        OnboardingScreen(
            isDarkTheme = false,
            onToggleTheme = {},
            onNavigateToLogin = {},
            onNavigateToGetStarted = {}
        )
    }
}
