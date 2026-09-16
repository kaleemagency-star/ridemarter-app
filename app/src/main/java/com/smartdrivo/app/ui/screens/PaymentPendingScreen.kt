package com.smartdrivo.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartdrivo.app.ui.theme.*
import com.smartdrivo.app.viewmodel.PaymentNavigationEvent
import com.smartdrivo.app.viewmodel.PaymentViewModel
import com.smartdrivo.app.viewmodel.ThemeMode
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PaymentPendingScreen(
    onPaymentApproved: () -> Unit,
    viewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val existingPayment by viewModel.existingPayment.collectAsStateWithLifecycle()
    val isCheckingStatus by viewModel.isCheckingStatus.collectAsStateWithLifecycle()

    var showApprovedDialog by remember { mutableStateOf(false) }

    // Pulsing animation for the success/pending icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Start auto polling every 30 seconds
    DisposableEffect(Unit) {
        viewModel.startAutoPolling(
            onApproved = {
                showApprovedDialog = true
            }
        )
        onDispose {
            viewModel.stopAutoPolling()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PaymentNavigationEvent.NavigateToDashboard -> {
                    showApprovedDialog = true
                }
                is PaymentNavigationEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is PaymentNavigationEvent.ShowDialog -> {
                    Toast.makeText(context, "${event.title}: ${event.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    if (showApprovedDialog) {
        AlertDialog(
            onDismissRequest = {
                showApprovedDialog = false
                onPaymentApproved()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Approved",
                    tint = BrandGreenPrimary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Payment Approved!", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Your payment has been successfully verified and your SmartDrivo subscription plan is now active! Tap below to open your driver dashboard.",
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApprovedDialog = false
                        onPaymentApproved()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Go to Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ANIMATED ICON
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulseScale)
                    .background(BrandGreenPrimary.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, BrandGreenPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = "Payment Pending",
                    tint = BrandGreenPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TITLE & SUBTITLE
            Text(
                text = "Payment Submitted!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Awaiting admin verification",
                fontSize = 14.sp,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // STATUS TIMELINE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_timeline_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                border = BorderStroke(1.dp, Color(0xFF262626))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Activation Progress",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 1: Payment Details Submitted (Completed - Green)
                    TimelineStepRow(
                        stepNumber = 1,
                        title = "Payment Details Submitted",
                        isCompleted = true,
                        isCurrent = false,
                        showLine = true
                    )

                    // Step 2: Admin Verifying Payment (In Progress - Orange)
                    TimelineStepRow(
                        stepNumber = 2,
                        title = "Admin Verifying Payment",
                        isCompleted = false,
                        isCurrent = true,
                        showLine = true
                    )

                    // Step 3: Plan Getting Activated (Upcoming - Gray)
                    TimelineStepRow(
                        stepNumber = 3,
                        title = "Plan Getting Activated",
                        isCompleted = false,
                        isCurrent = false,
                        showLine = true
                    )

                    // Step 4: Access Granted (Upcoming - Gray)
                    TimelineStepRow(
                        stepNumber = 4,
                        title = "Access Granted",
                        isCompleted = false,
                        isCurrent = false,
                        showLine = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TRANSACTION DETAILS CARD
            val payment = existingPayment
            val planName = payment?.planSelected?.ifBlank { "Popular (7 Days)" } ?: "Popular (7 Days)"
            val amount = if ((payment?.amount ?: 0) > 0) payment!!.amount else 129
            val txId = payment?.transactionId?.ifBlank { "Pending verification" } ?: "Pending verification"
            val formattedDate = payment?.submittedAt?.toDate()?.let {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
            } ?: "Just now"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_details_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                border = BorderStroke(1.dp, Color(0xFF262626))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transaction Details",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF6D00).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PENDING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9100)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DetailRowItem(label = "Plan", value = planName)
                    DetailRowItem(label = "Amount", value = "₹$amount")
                    DetailRowItem(label = "Transaction ID", value = txId)
                    DetailRowItem(label = "Submitted At", value = formattedDate)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MANUAL CHECK STATUS BUTTON
            OutlinedButton(
                onClick = { viewModel.checkPaymentStatusManual() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("check_status_button"),
                border = BorderStroke(1.dp, BrandGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCheckingStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BrandGreenPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checking...", color = BrandGreenPrimary)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = BrandGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check Status",
                        color = BrandGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CONTACT ADMIN WHATSAPP BUTTON
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://api.whatsapp.com/send?phone=919876543210&text=Hi%20Admin,%20I%20have%20submitted%20my%20payment%20(TX:%20$txId).%20Please%20approve%20my%20SmartDrivo%20account.")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("contact_admin_button"),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = "WhatsApp",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contact Admin on WhatsApp",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Status will update automatically once verified (auto-checks every 30s).",
                fontSize = 11.sp,
                color = DarkTextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TimelineStepRow(
    stepNumber: Int,
    title: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    showLine: Boolean
) {
    val stepColor = when {
        isCompleted -> BrandGreenPrimary
        isCurrent -> Color(0xFFFF9100)
        else -> Color(0xFF444444)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(stepColor.copy(alpha = 0.2f))
                    .border(1.5.dp, stepColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = BrandGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = stepColor
                    )
                }
            }

            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(if (isCompleted) BrandGreenPrimary else Color(0xFF333333))
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isCurrent || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCurrent || isCompleted) Color.White else DarkTextMuted
            )
            if (isCurrent) {
                Text(
                    text = "Verification in progress (usually 10-60 mins)",
                    fontSize = 11.sp,
                    color = Color(0xFFFFB74D)
                )
            }
        }
    }
}

@Composable
fun DetailRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = DarkTextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Preview(name = "PaymentPendingScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PaymentPendingScreenPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        PaymentPendingScreen(
            onPaymentApproved = {}
        )
    }
}
