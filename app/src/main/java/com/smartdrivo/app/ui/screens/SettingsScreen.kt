package com.smartdrivo.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.viewmodel.SettingsViewModel
import com.smartdrivo.app.viewmodel.ThemeMode
import com.smartdrivo.app.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val selectedSpeed by viewModel.selectedSpeed.collectAsStateWithLifecycle()
    val defaultAction by viewModel.defaultAction.collectAsStateWithLifecycle()
    val rapidoEnabled by viewModel.rapidoEnabled.collectAsStateWithLifecycle()
    val uberEnabled by viewModel.uberEnabled.collectAsStateWithLifecycle()
    val olaEnabled by viewModel.olaEnabled.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val isOverlayEnabled by viewModel.isOverlayEnabled.collectAsStateWithLifecycle()
    val isBatteryOptIgnored by viewModel.isBatteryOptIgnored.collectAsStateWithLifecycle()
    val currentTheme by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1 — APPEARANCE CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_appearance_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Appearance",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Theme",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    Pair("Dark", ThemeMode.DARK),
                                    Pair("Light", ThemeMode.LIGHT),
                                    Pair("Auto", ThemeMode.SYSTEM)
                                ).forEach { (label, mode) ->
                                    val isSelected = currentTheme == mode
                                    if (isSelected) {
                                        Button(
                                            onClick = { themeViewModel.setTheme(mode) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF00C853),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { themeViewModel.setTheme(mode) },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFFAAAAAA)
                                            ),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF333333))
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(label, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2 — SPEED SETTING CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_speed_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Response Speed",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "How fast to click Accept button",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Faster = higher chance to get order",
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("1ms", "10ms", "50ms").forEach { speed ->
                                val isSelected = selectedSpeed == speed
                                if (isSelected) {
                                    Button(
                                        onClick = { viewModel.saveSpeed(speed) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00C853),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(speed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.saveSpeed(speed) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFAAAAAA)
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF333333))
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(speed, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF777777),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1ms is fastest but may drain battery faster",
                                color = Color(0xFF777777),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // SECTION 3 — DEFAULT ACTION CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_default_action_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Default Action",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "What to do with orders not matching any area rule",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Accept", "Reject", "Ignore").forEach { action ->
                                val isSelected = defaultAction == action
                                val activeColor = when (action) {
                                    "Accept" -> Color(0xFF00C853)
                                    "Reject" -> Color(0xFFD50000)
                                    else -> Color(0xFFFF6D00)
                                }
                                if (isSelected) {
                                    Button(
                                        onClick = { viewModel.saveDefaultAction(action) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = activeColor,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(action, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.saveDefaultAction(action) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFAAAAAA)
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF333333))
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(action, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4 — APP PLATFORMS CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_platforms_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Active Platforms",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enable or disable auto-accept for each app",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        PlatformToggleRow(
                            label = "Rapido",
                            badgeColor = Color(0xFFFF5722),
                            enabled = rapidoEnabled,
                            onToggle = { viewModel.setRapidoEnabled(!rapidoEnabled) }
                        )
                        HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
                        PlatformToggleRow(
                            label = "Uber",
                            badgeColor = Color(0xFF222222),
                            enabled = uberEnabled,
                            onToggle = { viewModel.setUberEnabled(!uberEnabled) }
                        )
                        HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
                        PlatformToggleRow(
                            label = "Ola",
                            badgeColor = Color(0xFF00B140),
                            enabled = olaEnabled,
                            onToggle = { viewModel.setOlaEnabled(!olaEnabled) }
                        )
                    }
                }
            }

            // SECTION 5 — PERMISSIONS CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_permissions_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Required Permissions",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "All 3 must be enabled for SmartDrivo to work",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        PermissionRow(
                            label = "Accessibility Service",
                            description = "Required to detect and click orders",
                            isGranted = isAccessibilityEnabled,
                            onFix = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionRow(
                            label = "Draw Over Other Apps",
                            description = "Required to show order popup alerts",
                            isGranted = isOverlayEnabled,
                            onFix = {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionRow(
                            label = "Battery Optimization Off",
                            description = "Prevents service from being killed",
                            isGranted = isBatteryOptIgnored,
                            onFix = {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                } catch (e: Exception) {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                    } catch (ex: Exception) {
                                        // Ignore
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // SECTION 6 — NOTIFICATIONS CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_notifications_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Order Notifications",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Show notification when order is accepted",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00C853),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF333333)
                            ),
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }
                }
            }

            // SECTION 7 — APP INFO CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_info_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "App Information",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("App Name", "SmartDrivo")
                        InfoRow("Version", "1.0.0 (Beta)")
                        InfoRow("Package", "com.smartdrivo.app")
                        InfoRow("Build", "Release")
                        InfoRow("Support", "Rapido, Uber, Ola")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PlatformToggleRow(
    label: String,
    badgeColor: Color,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(badgeColor)
                .border(1.dp, Color(0xFF444444), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.firstOrNull()?.uppercase() ?: "",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00C853),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
private fun PermissionRow(
    label: String,
    description: String,
    isGranted: Boolean,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Color(0xFFAAAAAA),
                fontSize = 11.sp
            )
        }
        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = Color(0xFF00C853),
                modifier = Modifier.size(28.dp)
            )
        } else {
            OutlinedButton(
                onClick = onFix,
                shape = RoundedCornerShape(20.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD50000))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD50000)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Fix",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(name = "SettingsScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SettingsScreenDarkPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        SettingsScreen()
    }
}

@Preview(name = "SettingsScreen Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun SettingsScreenLightPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        SettingsScreen()
    }
}
