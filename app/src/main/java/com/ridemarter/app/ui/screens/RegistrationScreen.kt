package com.ridemarter.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Timestamp
import com.ridemarter.app.model.UserData
import com.ridemarter.app.ui.theme.BrandGreenPrimary
import com.ridemarter.app.ui.theme.DarkBackground
import com.ridemarter.app.ui.theme.DarkCardBorder
import com.ridemarter.app.ui.theme.DarkSurface
import com.ridemarter.app.ui.theme.DarkSurfaceVariant
import com.ridemarter.app.ui.theme.DarkTextMuted
import com.ridemarter.app.ui.theme.DarkTextSecondary
import com.ridemarter.app.ui.theme.RideMarterTheme
import com.ridemarter.app.viewmodel.AuthState
import com.ridemarter.app.viewmodel.AuthViewModel
import com.ridemarter.app.viewmodel.ThemeMode

@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPending: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val googleProfile by authViewModel.googleProfile.collectAsStateWithLifecycle()

    val isGoogleUser = googleProfile != null

    var name by remember { mutableStateOf(googleProfile?.first ?: "") }
    var email by remember { mutableStateOf(googleProfile?.second ?: "") }
    var mobile by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf<String?>(null) } // "AUTO", "BIKE", "CAR"
    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Auto-generated user ID
    val generatedUserId = remember {
        authViewModel.generateUserId()
    }

    // Sync google profile when it updates
    LaunchedEffect(googleProfile) {
        googleProfile?.let {
            if (name.isEmpty()) name = it.first
            if (email.isEmpty()) email = it.second
        }
    }

    // Listen to UI state transitions
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is AuthState.UserPending -> {
                onNavigateToPending()
            }
            else -> Unit
        }
    }

    val isNameValid = name.trim().length >= 3
    val isEmailValid = email.isNotBlank() && email.contains("@") && email.contains(".")
    val isMobileValid = mobile.length == 10 && mobile.all { it.isDigit() }
    val isVehicleSelected = selectedVehicle != null
    val canSubmit = isNameValid && isEmailValid && isMobileValid && isVehicleSelected && termsAccepted

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    .padding(horizontal = 24.dp, vertical = 16.dp)
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
                            .testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Driver Registration",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("register_title")
                        )
                        Text(
                            text = "Complete your driver profile",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // FIELD 1 — Full Name
                Text(
                    text = "Full Name",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (!isGoogleUser) name = it },
                    readOnly = isGoogleUser,
                    placeholder = { Text("Enter your full name", color = DarkTextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name Icon",
                            tint = if (name.isNotEmpty()) BrandGreenPrimary else DarkTextMuted
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreenPrimary,
                        unfocusedBorderColor = if (name.isNotEmpty() && !isNameValid) Color(0xFFE53935) else DarkCardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = BrandGreenPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_name_input")
                )
                if (name.isNotEmpty() && !isNameValid) {
                    Text(
                        text = "Name must be at least 3 characters",
                        color = Color(0xFFE53935),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FIELD 2 — Email Address
                Text(
                    text = "Email Address",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (!isGoogleUser) email = it },
                    readOnly = isGoogleUser,
                    placeholder = { Text("driver@email.com", color = DarkTextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = if (email.isNotEmpty()) BrandGreenPrimary else DarkTextMuted
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreenPrimary,
                        unfocusedBorderColor = if (email.isNotEmpty() && !isEmailValid) Color(0xFFE53935) else DarkCardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = BrandGreenPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_email_input")
                )
                if (email.isNotEmpty() && !isEmailValid) {
                    Text(
                        text = "Please enter a valid email address",
                        color = Color(0xFFE53935),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FIELD 3 — Mobile Number (simple input, NO OTP)
                Text(
                    text = "Mobile Number",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(10)
                        mobile = digits
                    },
                    placeholder = { Text("Enter 10-digit mobile number", color = DarkTextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon",
                            tint = if (mobile.isNotEmpty()) BrandGreenPrimary else DarkTextMuted
                        )
                    },
                    prefix = {
                        Text(
                            text = "+91 ",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreenPrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = BrandGreenPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_mobile_input")
                )
                Text(
                    text = "Required for admin to contact you",
                    color = DarkTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // FIELD 4 — Vehicle Type (single select cards)
                Text(
                    text = "Select Vehicle Type",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VehicleOptionCard(
                        title = "AUTO",
                        subtitle = "Rickshaw",
                        icon = Icons.Default.ElectricRickshaw,
                        isSelected = selectedVehicle == "AUTO",
                        onClick = { selectedVehicle = "AUTO" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vehicle_card_auto")
                    )
                    VehicleOptionCard(
                        title = "BIKE",
                        subtitle = "Two Wheeler",
                        icon = Icons.Default.TwoWheeler,
                        isSelected = selectedVehicle == "BIKE",
                        onClick = { selectedVehicle = "BIKE" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vehicle_card_bike")
                    )
                    VehicleOptionCard(
                        title = "CAR",
                        subtitle = "Cab / Taxi",
                        icon = Icons.Default.DirectionsCar,
                        isSelected = selectedVehicle == "CAR",
                        onClick = { selectedVehicle = "CAR" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vehicle_card_car")
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // FIELD 5 — User ID (read-only, auto-generated)
                Text(
                    text = "RideMarter User ID",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("register_user_id_box")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = generatedUserId,
                                color = BrandGreenPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Share with admin for support",
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedUserId))
                                Toast.makeText(context, "User ID copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy User ID",
                                tint = BrandGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // TERMS CHECKBOX
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { termsAccepted = !termsAccepted }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = BrandGreenPrimary,
                            uncheckedColor = DarkCardBorder,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.testTag("register_terms_checkbox")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    val annotatedText = buildAnnotatedString {
                        append("I agree to ")
                        withStyle(style = SpanStyle(color = BrandGreenPrimary, fontWeight = FontWeight.SemiBold)) {
                            append("Terms of Service")
                        }
                        append(" and ")
                        withStyle(style = SpanStyle(color = BrandGreenPrimary, fontWeight = FontWeight.SemiBold)) {
                            append("Privacy Policy")
                        }
                    }
                    Text(
                        text = annotatedText,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SUBMIT BUTTON "Create Account"
                Button(
                    onClick = {
                        val currentFirebaseUser = authViewModel.getCurrentUser()
                        val targetUid = currentFirebaseUser?.uid ?: ""
                        val photoUrl = currentFirebaseUser?.photoUrl?.toString() ?: ""

                        val newUser = UserData(
                            uid = targetUid,
                            userId = generatedUserId,
                            name = name.trim(),
                            email = email.trim(),
                            mobile = mobile.trim(),
                            vehicleType = selectedVehicle ?: "AUTO",
                            approved = false,
                            status = "pending",
                            planStatus = "none",
                            createdAt = Timestamp.now(),
                            loginType = if (isGoogleUser) "google" else "email",
                            profilePhotoUrl = photoUrl
                        )

                        authViewModel.saveUserToFirestore(newUser) {
                            onNavigateToPending()
                        }
                    },
                    enabled = canSubmit && uiState !is AuthState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreenPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF242424),
                        disabledContentColor = Color(0xFF666666)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_submit_button")
                ) {
                    if (uiState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Terms and Conditions Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Terms & Privacy Policy",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "RideMarter Driver Terms:\n\n" +
                                "1. RideMarter is an assistive automation and ride filtering utility designed for registered transport drivers.\n\n" +
                                "2. You agree to use the application safely and in accordance with all local driving laws and traffic regulations.\n\n" +
                                "3. Subscription plans provide automated assistant features during active validity periods.\n\n" +
                                "4. Account activation is subject to admin verification.",
                        color = DarkTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        termsAccepted = true
                        showTermsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                ) {
                    Text("Accept", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun VehicleOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandGreenPrimary.copy(alpha = 0.12f) else DarkSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BrandGreenPrimary else DarkCardBorder
        ),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) BrandGreenPrimary else DarkTextMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else DarkTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = if (isSelected) BrandGreenPrimary else DarkTextMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "RegistrationScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun RegistrationScreenPreviewDark() {
    RideMarterTheme(themeMode = ThemeMode.DARK) {
        RegistrationScreen(
            authViewModel = AuthViewModel(),
            onNavigateBack = {},
            onNavigateToPending = {}
        )
    }
}

@Preview(name = "RegistrationScreen Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun RegistrationScreenPreviewLight() {
    RideMarterTheme(themeMode = ThemeMode.LIGHT) {
        RegistrationScreen(
            authViewModel = AuthViewModel(),
            onNavigateBack = {},
            onNavigateToPending = {}
        )
    }
}
