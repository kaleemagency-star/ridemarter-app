package com.ridemarter.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import coil.compose.AsyncImage
import com.ridemarter.app.model.PlanItem
import com.ridemarter.app.ui.theme.*
import com.ridemarter.app.viewmodel.PaymentNavigationEvent
import com.ridemarter.app.viewmodel.PaymentViewModel
import com.ridemarter.app.viewmodel.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onPaymentSubmitted: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val selectedPlan by viewModel.selectedPlan.collectAsStateWithLifecycle()
    val paymentConfig by viewModel.paymentConfig.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val isSessionExpired by viewModel.isSessionExpired.collectAsStateWithLifecycle()
    val transactionId by viewModel.transactionId.collectAsStateWithLifecycle()
    val transactionIdError by viewModel.transactionIdError.collectAsStateWithLifecycle()
    val confirmedAmount by viewModel.confirmedAmount.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val isCheckingStatus by viewModel.isCheckingStatus.collectAsStateWithLifecycle()

    var statusDialogInfo by remember { mutableStateOf<Pair<String, String>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PaymentNavigationEvent.NavigateToPending -> {
                    onPaymentSubmitted()
                }
                is PaymentNavigationEvent.NavigateToDashboard -> {
                    onNavigateToDashboard()
                }
                is PaymentNavigationEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is PaymentNavigationEvent.ShowDialog -> {
                    statusDialogInfo = Pair(event.title, event.message)
                }
            }
        }
    }

    // Session Expired Dialog
    if (isSessionExpired) {
        AlertDialog(
            onDismissRequest = { /* Must refresh */ },
            title = {
                Text(
                    text = "Session Expired",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Your payment session has expired. Tap Refresh to get a new session and updated payment details.",
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.refreshSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh Session", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Status Review Dialog
    statusDialogInfo?.let { (title, message) ->
        AlertDialog(
            onDismissRequest = { statusDialogInfo = null },
            title = {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(message, color = DarkTextSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { statusDialogInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = DarkSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Choose Your Plan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pay via UPI to activate access",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- PLAN SELECTION TITLE ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select a Plan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 4 PLAN CARDS VERTICAL LIST
            plans.forEach { plan ->
                PlanCardItem(
                    plan = plan,
                    isSelected = plan.id == selectedPlan.id,
                    onClick = { viewModel.selectPlan(plan) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SELECTED PLAN SUMMARY BOX
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_plan_summary_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, Color(0xFF2E2E2E))
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
                            text = "You Selected:",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${selectedPlan.name} (${selectedPlan.durationText})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${selectedPlan.price}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Proceed to payment",
                            tint = BrandGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- COMPLETE PAYMENT SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Complete Payment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SUB INFO CARD (orange border, dark background)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161208)),
                border = BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Payment instruction",
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Make payment to the UPI ID or scan QR code below. After payment enter your Transaction ID and submit. Admin will verify and activate your plan within 1 hour.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFFFFD180)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // UPI ID DISPLAY BOX
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upi_id_display_box"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UPI ID",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )

                        // Verified UPI ID Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = BrandGreenPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = BrandGreenPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified UPI ID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = paymentConfig.upiId,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("RideMarter UPI ID", paymentConfig.upiId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied UPI ID!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy UPI ID",
                                tint = BrandGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QR CODE SECTION
            Text(
                text = "Or Scan QR Code",
                fontSize = 14.sp,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Large QR code box (200dp x 200dp)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (paymentConfig.qrImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = paymentConfig.qrImageUrl,
                        contentDescription = "Payment QR Code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                } else {
                    // Stylized vector QR placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code Placeholder",
                            tint = BrandGreenPrimary,
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "RideMarter QR Code",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Scan with any UPI App",
                            fontSize = 10.sp,
                            color = DarkTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Open in GPay / PhonePe / Paytm",
                fontSize = 12.sp,
                color = DarkTextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // COUNTDOWN TIMER
            PaymentTimerWidget(
                remainingSeconds = remainingSeconds,
                totalSeconds = 300
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TRANSACTION ID INPUT
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter Transaction ID",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { viewModel.onTransactionIdChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_id_input"),
                    placeholder = { Text("e.g. 425812345678", color = DarkTextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Receipt",
                            tint = BrandGreenPrimary
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "${transactionId.length}/30",
                            fontSize = 11.sp,
                            color = DarkTextMuted,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    isError = transactionIdError != null,
                    supportingText = {
                        if (transactionIdError != null) {
                            Text(transactionIdError!!, color = Color(0xFFF44336), fontSize = 11.sp)
                        } else {
                            Text("Find Transaction ID in your UPI app payment history", color = DarkTextMuted, fontSize = 11.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF121212),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedBorderColor = BrandGreenPrimary,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AMOUNT CONFIRMATION ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141414), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount Paid:",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(
                        value = confirmedAmount,
                        onValueChange = { viewModel.onAmountChange(it) },
                        modifier = Modifier
                            .width(90.dp)
                            .height(48.dp)
                            .testTag("amount_confirm_field"),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandGreenPrimary,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SUBMIT PAYMENT BUTTON
            val isSubmitEnabled = transactionId.trim().length >= 8 && !isSubmitting && !isSessionExpired

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submitPayment()
                },
                enabled = isSubmitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_payment_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreenPrimary,
                    disabledContainerColor = Color(0xFF263238)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting Payment...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Submit",
                        tint = if (isSubmitEnabled) Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Payment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubmitEnabled) Color.White else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ALREADY PAID SECTION
            Text(
                text = "Already submitted payment?",
                fontSize = 12.sp,
                color = DarkTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = { viewModel.checkPaymentStatusManual() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("check_payment_status_button"),
                border = BorderStroke(1.dp, BrandGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCheckingStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = BrandGreenPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checking...", color = BrandGreenPrimary)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Check Status",
                        tint = BrandGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check Payment Status",
                        color = BrandGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CONTACT SUPPORT ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Need help? Contact us: ",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )

                // WhatsApp button
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=919876543210&text=Hi%20RideMarter%20Support,%20I%20need%20help%20with%20my%20payment")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp Support",
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Telegram button
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://t.me/ridemarter_support")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Telegram not installed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Telegram Support",
                        tint = TelegramBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlanCardItem(
    plan: PlanItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF0D2B0D) else Color(0xFF1A1A1A)
    val borderColor = if (isSelected) BrandGreenPrimary else Color(0xFF2B2B2B)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("plan_card_${plan.id}")
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection Radio Button on Left
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = BrandGreenPrimary,
                        unselectedColor = Color.Gray
                    ),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Plan Name and Duration Badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plan.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF6D00), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = plan.durationText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${plan.price}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandGreenPrimary
                        )
                        Text(
                            text = " /plan",
                            fontSize = 12.sp,
                            color = DarkTextMuted,
                            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                        )
                    }
                }

                // Ribbons top right
                if (plan.isPopular) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF6D00), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MOST POPULAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                } else if (plan.isBestValue) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFB300), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "BEST VALUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFF2E2E2E), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Features list with small gray checkmarks
            plan.features.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = DarkTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feature,
                        fontSize = 12.sp,
                        color = DarkTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentTimerWidget(
    remainingSeconds: Int,
    totalSeconds: Int
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    // Green > 120s, Orange 60s..120s, Red < 60s
    val timerColor = when {
        remainingSeconds > 120 -> BrandGreenPrimary
        remainingSeconds in 60..120 -> Color(0xFFFF9100)
        else -> Color(0xFFF44336)
    }

    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Payment Session Expires In",
            fontSize = 12.sp,
            color = DarkTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = timerColor,
                trackColor = Color(0xFF222222),
                strokeWidth = 6.dp
            )
            Text(
                text = timeFormatted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = timerColor
            )
        }
    }
}

@Preview(name = "PaymentScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PaymentScreenPreview() {
    RideMarterTheme(themeMode = ThemeMode.DARK) {
        PaymentScreen(
            onPaymentSubmitted = {},
            onNavigateToDashboard = {}
        )
    }
}
