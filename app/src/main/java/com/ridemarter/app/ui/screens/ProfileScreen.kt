package com.ridemarter.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ridemarter.app.ui.theme.BrandGreenPrimary
import com.ridemarter.app.ui.theme.DarkBackground
import com.ridemarter.app.ui.theme.DarkCardBorder
import com.ridemarter.app.ui.theme.DarkSurface
import com.ridemarter.app.ui.theme.DarkTextMuted
import com.ridemarter.app.ui.theme.DarkTextSecondary
import com.ridemarter.app.ui.theme.RideMarterTheme
import com.ridemarter.app.viewmodel.ProfileViewModel
import com.ridemarter.app.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToPayment: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val planDaysRemaining by viewModel.planDaysRemaining.collectAsStateWithLifecycle()
    val planExpiryFormatted by viewModel.planExpiryFormatted.collectAsStateWithLifecycle()
    val isPlanExpired by viewModel.isPlanExpired.collectAsStateWithLifecycle()
    val isPlanExpiringSoon by viewModel.isPlanExpiringSoon.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Mobile edit state
    var isEditingMobile by remember { mutableStateOf(false) }
    var editedMobile by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Driver Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("profile_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ITEM 1 — PROFILE HEADER CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_header_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Avatar circle 90dp
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(BrandGreenPrimary.copy(alpha = 0.15f))
                                .border(2.dp, BrandGreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userData?.name?.firstOrNull()?.uppercase() ?: "D",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = userData?.name?.ifBlank { "Driver Partner" } ?: "Driver Partner",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Driver ID: ${userData?.userId?.ifBlank { "SD102938" } ?: "SD102938"}",
                            fontSize = 13.sp,
                            color = DarkTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (userData?.approved == true) BrandGreenPrimary.copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                if (userData?.approved == true) BrandGreenPrimary else Color(0xFFFF9800)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (userData?.approved == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (userData?.approved == true) BrandGreenPrimary else Color(0xFFFF9800),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (userData?.approved == true) "Verified Partner" else "Pending Review",
                                    color = if (userData?.approved == true) BrandGreenPrimary else Color(0xFFFF9800),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ITEM 2 — PLAN & SUBSCRIPTION CARD (Read Only)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_plan_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Subscription Plan",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = userData?.planName?.ifBlank { "Popular (7 Days)" } ?: "Popular (7 Days)",
                                    color = BrandGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            // Days remaining color: green if >3 days, orange if 1-2 days, red if expired
                            val statusColor = when {
                                isPlanExpired -> Color(0xFFD50000)
                                isPlanExpiringSoon -> Color(0xFFFF9800)
                                else -> BrandGreenPrimary
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = statusColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (isPlanExpired) "EXPIRED" else "$planDaysRemaining DAYS LEFT",
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Valid till: $planExpiryFormatted",
                            color = DarkTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Status: ${if (isPlanExpired) "Expired" else if (userData?.planStatus == "active") "Active" else "Pending"}",
                            color = if (userData?.planStatus == "active" && !isPlanExpired) BrandGreenPrimary else Color(0xFFFF9800),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (isPlanExpiringSoon || isPlanExpired) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFF9800).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPlanExpired) "Your plan has expired. Renew to continue auto-accepting rides." else "Your plan expires in $planDaysRemaining days. Renew early to avoid interruption.",
                                    color = Color(0xFFFF9800),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToPayment,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("renew_plan_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Renew / Change Plan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ITEM 3 — DRIVER DETAILS CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_details_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Account Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Non-editable: Name
                        ProfileDetailRow("Name", userData?.name?.ifBlank { "Driver Partner" } ?: "Driver Partner")
                        HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

                        // Non-editable: Email
                        ProfileDetailRow("Email", userData?.email?.ifBlank { "driver@ridemarter.com" } ?: "driver@ridemarter.com")
                        HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

                        // Non-editable: User ID
                        ProfileDetailRow("User ID", userData?.userId?.ifBlank { "SD102938" } ?: "SD102938")
                        HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

                        // Non-editable: Vehicle Type
                        ProfileDetailRow("Vehicle Type", userData?.vehicleType?.ifBlank { "AUTO" } ?: "AUTO")
                        HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

                        // Non-editable: Login Type
                        ProfileDetailRow("Login Type", "Google")
                        HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)

                        // Editable: Mobile Number (No OTP, saves directly to Firestore)
                        if (isEditingMobile) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Edit Mobile Number (10 Digits)",
                                    color = DarkTextMuted,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = editedMobile,
                                        onValueChange = { input ->
                                            editedMobile = input.filter { it.isDigit() }.take(10)
                                        },
                                        placeholder = { Text("10 digits", color = DarkTextMuted, fontSize = 13.sp) },
                                        prefix = { Text("+91 ", color = Color.White, fontSize = 13.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandGreenPrimary,
                                            unfocusedBorderColor = DarkCardBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("profile_edit_mobile_input")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (editedMobile.length == 10) {
                                                viewModel.updateMobile(editedMobile) { success ->
                                                    if (success) {
                                                        Toast.makeText(context, "Mobile updated successfully", Toast.LENGTH_SHORT).show()
                                                        isEditingMobile = false
                                                    } else {
                                                        Toast.makeText(context, "Failed to update mobile", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(context, "Enter 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(BrandGreenPrimary)
                                            .testTag("profile_save_mobile_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { isEditingMobile = false },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(DarkCardBorder)
                                            .testTag("profile_cancel_mobile_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancel",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mobile Number",
                                    color = DarkTextSecondary,
                                    fontSize = 13.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "+91 ${userData?.mobile?.ifBlank { "9876543210" } ?: "9876543210"}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            editedMobile = userData?.mobile?.replace("+91", "")?.trim() ?: ""
                                            isEditingMobile = true
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(BrandGreenPrimary.copy(alpha = 0.15f))
                                            .testTag("profile_edit_mobile_icon")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Mobile Number",
                                            tint = BrandGreenPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ITEM 4 — LOGOUT BUTTON
            item {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD50000)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFD50000)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // LOGOUT CONFIRMATION DIALOG
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = DarkSurface,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of RideMarter? You will need to log in again.",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.signOut(onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD50000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = DarkTextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

@Preview(name = "ProfileScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ProfileScreenDarkPreview() {
    RideMarterTheme(themeMode = ThemeMode.DARK) {
        ProfileScreen()
    }
}

@Preview(name = "ProfileScreen Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ProfileScreenLightPreview() {
    RideMarterTheme(themeMode = ThemeMode.LIGHT) {
        ProfileScreen()
    }
}
