package com.ridemarter.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ridemarter.app.ui.theme.BrandGreenPrimary
import com.ridemarter.app.ui.theme.BrandOrangeSecondary
import com.ridemarter.app.ui.theme.DarkBackground
import com.ridemarter.app.ui.theme.DarkCardBorder
import com.ridemarter.app.ui.theme.DarkSurface
import com.ridemarter.app.ui.theme.DarkSurfaceVariant
import com.ridemarter.app.ui.theme.DarkTextMuted
import com.ridemarter.app.ui.theme.DarkTextSecondary
import com.ridemarter.app.ui.theme.RideMarterTheme
import com.ridemarter.app.ui.theme.WhatsAppGreen
import com.ridemarter.app.viewmodel.AuthState
import com.ridemarter.app.viewmodel.AuthViewModel
import com.ridemarter.app.viewmodel.ThemeMode
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalScreen(
    authViewModel: AuthViewModel,
    onNavigateToPayment: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUserData by authViewModel.currentUserData.collectAsStateWithLifecycle()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var isChecking by remember { mutableStateOf(false) }

    val userId = currentUserData?.userId ?: authViewModel.generateUserId()
    val currentUid = currentUserData?.uid ?: authViewModel.getCurrentUser()?.uid

    // Smooth rotating animation for the Hourglass/Clock
    val infiniteTransition = rememberInfiniteTransition(label = "hourglass_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Realtime Approval Listener via Firestore
    DisposableEffect(currentUid) {
        if (!currentUid.isNullOrBlank()) {
            authViewModel.startApprovalStatusListener(currentUid) { user ->
                if (user.approved) {
                    Toast.makeText(context, "Account Approved!", Toast.LENGTH_SHORT).show()
                    onNavigateToPayment()
                }
            }
        }
        onDispose {
            authViewModel.stopApprovalStatusListener()
        }
    }

    // Auto-check status every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            authViewModel.checkUserStatus { data ->
                if (data != null && data.approved) {
                    onNavigateToPayment()
                }
            }
        }
    }

    // React to UI State
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthState.UserApproved, is AuthState.UserActive -> {
                onNavigateToPayment()
            }
            else -> Unit
        }
    }

    val isRejected = currentUserData?.status == "rejected" || currentUserData?.approvalStatus == "rejected"

    Scaffold(
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.statusBars,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Bar row with Back Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .testTag("pending_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Account Status",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("pending_header_title")
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Large animated status illustration (Clock / Hourglass / Error if rejected)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (isRejected) Color(0xFFEF5350).copy(alpha = 0.15f) else BrandOrangeSecondary.copy(alpha = 0.15f))
                        .border(2.dp, if (isRejected) Color(0xFFEF5350).copy(alpha = 0.5f) else BrandOrangeSecondary.copy(alpha = 0.5f), CircleShape)
                        .testTag("pending_animation_container"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRejected) Icons.Default.Error else Icons.Default.HourglassTop,
                        contentDescription = if (isRejected) "Application Rejected" else "Pending Approval",
                        tint = if (isRejected) Color(0xFFEF5350) else BrandOrangeSecondary,
                        modifier = Modifier
                            .size(54.dp)
                            .then(if (!isRejected) Modifier.rotate(rotationAngle) else Modifier)
                            .testTag("pending_hourglass_icon")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title: "Under Review" or "Application Rejected"
                Text(
                    text = if (isRejected) "Application Not Approved" else "Account Under Review",
                    color = if (isRejected) Color(0xFFEF5350) else Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("pending_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = if (isRejected) {
                        val reason = currentUserData?.rejectionReason?.ifBlank { "Please contact admin for verification." }
                        "Reason: $reason"
                    } else {
                        "Your application has been received and is pending admin approval"
                    },
                    color = if (isRejected) Color(0xFFFFCDD2) else DarkTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("pending_subtitle")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info card (dark surface, rounded 16dp, padding 16dp) with 4 Steps
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_steps_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        StepRow(
                            icon = Icons.Default.CheckCircle,
                            iconColor = BrandGreenPrimary,
                            title = "Step 1: Account Created",
                            isCompleted = true
                        )
                        StepRow(
                            icon = Icons.Default.Schedule,
                            iconColor = BrandOrangeSecondary,
                            title = "Step 2: Waiting Admin Approval",
                            isCurrent = true
                        )
                        StepRow(
                            icon = Icons.Default.Lock,
                            iconColor = DarkTextMuted,
                            title = "Step 3: Choose Plan",
                            isPending = true
                        )
                        StepRow(
                            icon = Icons.Default.PlayArrow,
                            iconColor = DarkTextMuted,
                            title = "Step 4: Start RideMarter",
                            isPending = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User ID display box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, Color(0xFF2E2E2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_user_id_box")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "YOUR USER ID",
                            color = DarkTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userId,
                                color = BrandGreenPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(userId))
                                    Toast.makeText(context, "User ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262626))
                                    .testTag("copy_id_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy User ID",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Share this ID when contacting admin for approval",
                            color = DarkTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info text
                Text(
                    text = "Admin will review and approve your account within 24 hours. You will get notified once approved.",
                    color = DarkTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("pending_info_text")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // WhatsApp button: "Contact Admin on WhatsApp"
                Button(
                    onClick = {
                        openWhatsAppAdmin(context, userId)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("whatsapp_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contact Admin on WhatsApp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Refresh Status button: "Check Approval Status"
                OutlinedButton(
                    onClick = {
                        isChecking = true
                        authViewModel.checkUserStatus { data ->
                            isChecking = false
                            if (data != null && data.approved) {
                                Toast.makeText(context, "Account Approved!", Toast.LENGTH_SHORT).show()
                                onNavigateToPayment()
                            } else {
                                Toast.makeText(context, "Account still pending admin approval", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BrandGreenPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandGreenPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("check_status_button")
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = BrandGreenPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = BrandGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check Approval Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StepRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    isCompleted: Boolean = false,
    isCurrent: Boolean = false,
    isPending: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = when {
                isCompleted -> Color.White
                isCurrent -> BrandOrangeSecondary
                else -> DarkTextMuted
            },
            fontSize = 13.sp,
            fontWeight = if (isCurrent || isCompleted) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun openWhatsAppAdmin(context: Context, userId: String) {
    try {
        val message = "Hello Admin, please approve my RideMarter driver account. My User ID is: $userId"
        val encodedMessage = Uri.encode(message)
        val url = "https://wa.me/?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Opening WhatsApp for approval request...", Toast.LENGTH_SHORT).show()
    }
}

@Preview(name = "PendingApproval Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PendingApprovalScreenPreviewDark() {
    RideMarterTheme(themeMode = ThemeMode.DARK) {
        PendingApprovalScreen(
            authViewModel = AuthViewModel(),
            onNavigateToPayment = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "PendingApproval Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PendingApprovalScreenPreviewLight() {
    RideMarterTheme(themeMode = ThemeMode.LIGHT) {
        PendingApprovalScreen(
            authViewModel = AuthViewModel(),
            onNavigateToPayment = {},
            onNavigateBack = {}
        )
    }
}
