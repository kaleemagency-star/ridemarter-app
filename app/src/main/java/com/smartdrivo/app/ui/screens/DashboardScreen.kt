package com.smartdrivo.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import com.smartdrivo.app.model.OrderRecord
import com.smartdrivo.app.model.UserData
import com.smartdrivo.app.ui.theme.*
import com.smartdrivo.app.viewmodel.DashboardEvent
import com.smartdrivo.app.viewmodel.DashboardViewModel
import com.smartdrivo.app.viewmodel.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(
    onNavigateToPayment: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current

    val currentUser by viewModel.currentUserData.collectAsStateWithLifecycle()
    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()
    val selectedSpeed by viewModel.selectedSpeed.collectAsStateWithLifecycle()
    val isRapidoDetected by viewModel.isRapidoDetected.collectAsStateWithLifecycle()
    val isUberDetected by viewModel.isUberDetected.collectAsStateWithLifecycle()
    val isOlaDetected by viewModel.isOlaDetected.collectAsStateWithLifecycle()
    val acceptedCount by viewModel.acceptedCount.collectAsStateWithLifecycle()
    val rejectedCount by viewModel.rejectedCount.collectAsStateWithLifecycle()
    val ignoredCount by viewModel.ignoredCount.collectAsStateWithLifecycle()
    val recentOrders by viewModel.recentOrders.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf("dashboard") }

    LaunchedEffect(currentTab) {
        selectedTab = currentTab
    }

    var permissionDialogState by remember { mutableStateOf<DashboardEvent?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Pulsing dot for blinking green indicator when active
    val infiniteTransition = rememberInfiniteTransition(label = "blinking_indicator")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DashboardEvent.NeedAccessibilityPermission,
                is DashboardEvent.NeedOverlayPermission,
                is DashboardEvent.NeedBatteryOptimization -> {
                    permissionDialogState = event
                }
                is DashboardEvent.ShowMessage -> {
                    Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Dialogs for permissions
    permissionDialogState?.let { event ->
        when (event) {
            is DashboardEvent.NeedAccessibilityPermission -> {
                AlertDialog(
                    onDismissRequest = { permissionDialogState = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = "Accessibility",
                            tint = BrandGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text("Accessibility Service Required", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "SmartDrivo requires Accessibility permissions to detect ride notifications and auto-accept incoming rides on Rapido, Uber, and Ola.",
                            color = DarkTextSecondary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                permissionDialogState = null
                                try {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open settings directly", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                        ) {
                            Text("Open Settings", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            permissionDialogState = null
                            viewModel.forceToggleOnForDemo()
                        }) {
                            Text("Simulate / Demo", color = DarkTextMuted)
                        }
                    },
                    containerColor = DarkSurfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            is DashboardEvent.NeedOverlayPermission -> {
                AlertDialog(
                    onDismissRequest = { permissionDialogState = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Overlay",
                            tint = BrandGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text("Display Over Other Apps", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "Please grant 'Display over other apps' permission to allow SmartDrivo to show the floating auto-accept widget.",
                            color = DarkTextSecondary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                permissionDialogState = null
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open overlay settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                        ) {
                            Text("Enable Overlay", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            permissionDialogState = null
                            viewModel.forceToggleOnForDemo()
                        }) {
                            Text("Simulate / Demo", color = DarkTextMuted)
                        }
                    },
                    containerColor = DarkSurfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            is DashboardEvent.NeedBatteryOptimization -> {
                AlertDialog(
                    onDismissRequest = { permissionDialogState = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text("Disable Battery Optimization", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "To keep auto-accepting orders smoothly when your screen is locked or in the background, please disable battery optimization for SmartDrivo.",
                            color = DarkTextSecondary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                permissionDialogState = null
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Settings not supported", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                        ) {
                            Text("Allow in Background", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            permissionDialogState = null
                            viewModel.forceToggleOnForDemo()
                        }) {
                            Text("Simulate / Demo", color = DarkTextMuted)
                        }
                    },
                    containerColor = DarkSurfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            else -> {}
        }
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("• Welcome to SmartDrivo! Fast order accept service is ready.", color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Server latency optimized for your area (response 10ms).", color = DarkTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Auto-accept active for Rapido, Uber, and Ola partner apps.", color = DarkTextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                ) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = DarkSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            DashboardTopBar(
                isServiceActive = isServiceActive,
                dotAlpha = dotAlpha,
                onNotificationClick = { showNotificationDialog = true },
                onProfileClick = {
                    selectedTab = "profile"
                    viewModel.setTab("profile")
                }
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                currentTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    viewModel.setTab(it)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "dashboard" -> {
                    DashboardMainContent(
                        currentUser = currentUser,
                        isServiceActive = isServiceActive,
                        selectedSpeed = selectedSpeed,
                        isRapidoDetected = isRapidoDetected,
                        isUberDetected = isUberDetected,
                        isOlaDetected = isOlaDetected,
                        acceptedCount = acceptedCount,
                        rejectedCount = rejectedCount,
                        ignoredCount = ignoredCount,
                        recentOrders = recentOrders,
                        onToggleService = { viewModel.toggleService() },
                        onSpeedSelected = { viewModel.setSpeedSetting(it) },
                        onRenewClick = onNavigateToPayment,
                        onViewAllHistory = {
                            selectedTab = "history"
                            viewModel.setTab("history")
                        }
                    )
                }
                "history" -> {
                    HistoryTabView(
                        onNavigateToDashboard = {
                            selectedTab = "dashboard"
                            viewModel.setTab("dashboard")
                        }
                    )
                }
                "areas" -> {
                    AreasTabView()
                }
                "settings" -> {
                    SettingsTabView()
                }
                "profile" -> {
                    ProfileTabView(
                        currentUser = currentUser,
                        onLogout = onLogout,
                        onRenew = onNavigateToPayment
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardTopBar(
    isServiceActive: Boolean,
    dotAlpha: Float,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: "SmartDrivo" green bold 18sp + green dot (blinking when active)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SmartDrivo",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandGreenPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isServiceActive) BrandGreenPrimary.copy(alpha = dotAlpha)
                        else Color.Gray
                    )
            )
        }

        // Right: Bell icon (notifications) + Avatar icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("notification_bell_button")
            ) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = Color(0xFFFF6D00)) {
                            Text("3", fontSize = 10.sp, color = Color.White)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F1F))
                    .testTag("avatar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = BrandGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardMainContent(
    currentUser: UserData?,
    isServiceActive: Boolean,
    selectedSpeed: String,
    isRapidoDetected: Boolean,
    isUberDetected: Boolean,
    isOlaDetected: Boolean,
    acceptedCount: Int,
    rejectedCount: Int,
    ignoredCount: Int,
    recentOrders: List<OrderRecord>,
    onToggleService: () -> Unit,
    onSpeedSelected: (String) -> Unit,
    onRenewClick: () -> Unit,
    onViewAllHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // PLAN STATUS BANNER
        item {
            PlanStatusBanner(
                userData = currentUser,
                onRenewClick = onRenewClick
            )
        }

        // MAIN TOGGLE CARD
        item {
            MainToggleCard(
                isServiceActive = isServiceActive,
                isRapidoDetected = isRapidoDetected,
                isUberDetected = isUberDetected,
                isOlaDetected = isOlaDetected,
                onToggle = onToggleService
            )
        }

        // TODAY'S ACTIVITY STATS
        item {
            TodayStatsSection(
                acceptedCount = acceptedCount,
                rejectedCount = rejectedCount,
                ignoredCount = ignoredCount
            )
        }

        // SPEED SETTING QUICK CARD
        item {
            SpeedSettingCard(
                selectedSpeed = selectedSpeed,
                onSpeedSelected = onSpeedSelected
            )
        }

        // RECENT ORDERS PREVIEW
        item {
            RecentOrdersSection(
                orders = recentOrders,
                onViewAllHistory = onViewAllHistory
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PlanStatusBanner(
    userData: UserData?,
    onRenewClick: () -> Unit
) {
    val expiry = userData?.planExpiry?.toDate()?.time ?: (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5))
    val diffMillis = expiry - System.currentTimeMillis()
    val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    val planName = userData?.planName?.ifBlank { "Popular (7 Days)" } ?: "Popular (7 Days)"

    when {
        diffMillis <= 0 -> {
            // Expired
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_status_expired_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF330B0B)),
                border = BorderStroke(1.dp, Color(0xFFF44336)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plan Expired. Tap to Renew.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onRenewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Renew", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        diffHours < 48 -> {
            // Expiring soon (< 2 days)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_status_expiring_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1C07)),
                border = BorderStroke(1.dp, Color(0xFFFF6D00)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plan expiring in ${diffHours}h! Renew Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onRenewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Renew", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        else -> {
            // Active
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_status_active_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2410)),
                border = BorderStroke(1.dp, BrandGreenPrimary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BrandGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Plan Active: $planName — $diffDays days remaining",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MainToggleCard(
    isServiceActive: Boolean,
    isRapidoDetected: Boolean,
    isUberDetected: Boolean,
    isOlaDetected: Boolean,
    onToggle: () -> Unit
) {
    val backgroundBrush = if (isServiceActive) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF00380E), Color(0xFF000000))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF141414))
        )
    }

    val borderColor = if (isServiceActive) BrandGreenPrimary else Color(0xFF333333)
    val borderWidth = if (isServiceActive) 2.dp else 1.dp

    // Infinite pulsing ring when service is ON
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("main_service_toggle_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SmartDrivo Status",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // GIANT CUSTOM TOGGLE SWITCH (80dp wide 44dp tall)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(10.dp)
                ) {
                    // Pulsing ring if ON
                    if (isServiceActive) {
                        Box(
                            modifier = Modifier
                                .size(96.dp, 58.dp)
                                .scale(ringScale)
                                .border(2.dp, BrandGreenPrimary.copy(alpha = ringAlpha), RoundedCornerShape(29.dp))
                        )
                    }

                    // Toggle track & thumb
                    Box(
                        modifier = Modifier
                            .size(84.dp, 44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isServiceActive) BrandGreenPrimary else Color(0xFF333333))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onToggle() }
                            .padding(4.dp)
                            .testTag("giant_service_toggle_switch"),
                        contentAlignment = if (isServiceActive) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isServiceActive) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "Active",
                                    tint = BrandGreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status label
                if (isServiceActive) {
                    Text(
                        text = "Auto Accept ACTIVE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenPrimary
                    )
                } else {
                    Text(
                        text = "Tap to Start Auto Accept",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Service Status row (when ON)
                AnimatedVisibility(visible = isServiceActive) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFF1E3A24), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PartnerAppStatusItem(name = "Rapido", isDetected = isRapidoDetected)
                            PartnerAppStatusItem(name = "Uber", isDetected = isUberDetected)
                            PartnerAppStatusItem(name = "Ola", isDetected = isOlaDetected)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerAppStatusItem(name: String, isDetected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isDetected) BrandGreenPrimary else Color.Gray)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            color = if (isDetected) Color.White else DarkTextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TodayStatsSection(
    acceptedCount: Int,
    rejectedCount: Int,
    ignoredCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Today's Activity",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Accepted (Green)
            StatCountCard(
                modifier = Modifier.weight(1f),
                count = acceptedCount,
                label = "Accepted",
                countColor = BrandGreenPrimary,
                testTag = "accepted_stat_card"
            )

            // Card 2: Rejected (Red)
            StatCountCard(
                modifier = Modifier.weight(1f),
                count = rejectedCount,
                label = "Rejected",
                countColor = Color(0xFFF44336),
                testTag = "rejected_stat_card"
            )

            // Card 3: Ignored (Orange)
            StatCountCard(
                modifier = Modifier.weight(1f),
                count = ignoredCount,
                label = "Ignored",
                countColor = Color(0xFFFF9100),
                testTag = "ignored_stat_card"
            )
        }
    }
}

@Composable
fun StatCountCard(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    countColor: Color,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(1.dp, Color(0xFF2B2B2B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = countColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = DarkTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SpeedSettingCard(
    selectedSpeed: String,
    onSpeedSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("response_speed_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(1.dp, Color(0xFF282828))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Response Speed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Auto-click delay",
                    fontSize = 11.sp,
                    color = DarkTextMuted
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1ms", "10ms", "50ms").forEach { speed ->
                    val isSelected = speed == selectedSpeed
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) BrandGreenPrimary else Color(0xFF222222))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) BrandGreenPrimary else Color(0xFF333333),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onSpeedSelected(speed) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("speed_pill_$speed"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = speed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else DarkTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentOrdersSection(
    orders: List<OrderRecord>,
    onViewAllHistory: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Orders",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "View All History",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandGreenPrimary,
                modifier = Modifier
                    .clickable { onViewAllHistory() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (orders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders received yet today",
                        fontSize = 13.sp,
                        color = DarkTextMuted
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                orders.take(4).forEach { order ->
                    OrderItemRow(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(order: OrderRecord) {
    val appColor = when (order.appName.lowercase()) {
        "rapido" -> Color(0xFFFF5722)
        "uber" -> Color(0xFFFFFFFF)
        "ola" -> Color(0xFF00B140)
        else -> BrandGreenPrimary
    }

    val statusColor = when (order.status) {
        "ACCEPTED" -> BrandGreenPrimary
        "REJECTED" -> Color(0xFFF44336)
        else -> Color(0xFFFF9100)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_item_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(1.dp, Color(0xFF282828))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App badge circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222))
                    .border(1.dp, appColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = order.appName.take(1).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Pickup & Drop
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.appName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${order.timeAgo}",
                        fontSize = 11.sp,
                        color = DarkTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${order.pickup} → ${order.drop}",
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fare and status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${order.fare}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0D0D0D),
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("dashboard_bottom_nav")
    ) {
        val navItems = listOf(
            Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
            Triple("history", "History", Icons.Default.ReceiptLong),
            Triple("areas", "Areas", Icons.Default.NearMe),
            Triple("settings", "Settings", Icons.Default.Tune),
            Triple("profile", "Profile", Icons.Default.Person)
        )

        navItems.forEach { (tabId, label, icon) ->
            val isSelected = currentTab == tabId
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tabId) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandGreenPrimary,
                    selectedTextColor = BrandGreenPrimary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = BrandGreenPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

// -------------------------------------------------------------
// EMBEDDED TAB VIEWS FOR COMPREHENSIVE SINGLE-ACTIVITY EXPERIENCE
// -------------------------------------------------------------

@Composable
fun HistoryTabView(
    onNavigateToDashboard: () -> Unit = {}
) {
    HistoryScreen(
        onNavigateToDashboard = onNavigateToDashboard
    )
}

@Composable
fun AreasTabView() {
    AreaGroupsScreen()
}

@Composable
fun SettingsTabView() {
    SettingsScreen()
}

@Composable
fun ProfileTabView(
    currentUser: UserData?,
    onLogout: () -> Unit,
    onRenew: () -> Unit
) {
    ProfileScreen(
        onNavigateToPayment = onRenew,
        onLogout = onLogout
    )
}

@Preview(name = "DashboardScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun DashboardScreenPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        DashboardScreen(
            onNavigateToPayment = {},
            onLogout = {}
        )
    }
}
