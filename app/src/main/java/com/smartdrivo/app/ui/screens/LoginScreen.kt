package com.smartdrivo.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.smartdrivo.app.ui.components.GoogleSignInButton
import com.smartdrivo.app.ui.theme.BrandGreenPrimary
import com.smartdrivo.app.ui.theme.BrandGreenVariant
import com.smartdrivo.app.ui.theme.DarkBackground
import com.smartdrivo.app.ui.theme.DarkCardBorder
import com.smartdrivo.app.ui.theme.DarkSurface
import com.smartdrivo.app.ui.theme.DarkTextMuted
import com.smartdrivo.app.ui.theme.DarkTextSecondary
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.viewmodel.AuthState
import com.smartdrivo.app.viewmodel.AuthViewModel
import com.smartdrivo.app.viewmodel.ThemeMode

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPending: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    // Clear previous authentication errors when entering Login screen
    LaunchedEffect(Unit) {
        authViewModel.clearAuthErrors()
    }

    // Listen to UI state errors and transitions
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is AuthState.UserPending -> {
                onNavigateToPending()
            }
            is AuthState.UserApproved -> {
                onNavigateToPayment()
            }
            is AuthState.UserActive -> {
                onNavigateToDashboard()
            }
            else -> Unit
        }
    }

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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP SECTION: Back arrow icon (top left)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            authViewModel.clearAuthErrors()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Centered app logo (green circle "SD" placeholder, 80dp)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(BrandGreenVariant, BrandGreenPrimary, Color(0xFF007A33))
                            )
                        )
                        .border(2.dp, Color(0x66FFFFFF), CircleShape)
                        .testTag("login_app_logo"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App name "SmartDrivo" green bold 24sp below logo
                Text(
                    text = "SmartDrivo",
                    color = BrandGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.testTag("login_app_title")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tagline "Sign in to continue" gray 13sp
                Text(
                    text = "Sign in to continue",
                    color = DarkTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.testTag("login_tagline")
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 2. GOOGLE SIGN IN BUTTON (Main Option)
                GoogleSignInButton(
                    onClick = {
                        authViewModel.googleSignIn(context) { _, _ ->
                            onNavigateToRegister()
                        }
                    },
                    isLoading = uiState is AuthState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. DIVIDER with "OR" in center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DarkCardBorder,
                        thickness = 1.dp
                    )
                    Text(
                        text = "OR",
                        color = DarkTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DarkCardBorder,
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. EMAIL/PASSWORD LOGIN (for returning users)
                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email", color = DarkTextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = if (email.isNotEmpty()) BrandGreenPrimary else DarkTextMuted
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
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
                        .testTag("login_email_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password", color = DarkTextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = if (password.isNotEmpty()) BrandGreenPrimary else DarkTextMuted
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = DarkTextMuted
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.signInWithEmail(
                                    email = email,
                                    pass = password,
                                    onRequireRegistration = { _, _ -> onNavigateToRegister() },
                                    onSuccess = { user ->
                                        if (user.approved && user.planStatus == "active") {
                                            onNavigateToDashboard()
                                        } else if (user.approved) {
                                            onNavigateToPayment()
                                        } else {
                                            onNavigateToPending()
                                        }
                                    }
                                )
                            }
                        }
                    ),
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
                        .testTag("login_password_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // "Forgot Password?" right-aligned
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = BrandGreenPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                resetEmail = email
                                showForgotPasswordDialog = true
                            }
                            .padding(vertical = 4.dp)
                            .testTag("login_forgot_password_btn")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // LOGIN button: full width, green filled, white text, rounded 12dp
                Button(
                    onClick = {
                        authViewModel.signInWithEmail(
                            email = email,
                            pass = password,
                            onRequireRegistration = { _, _ -> onNavigateToRegister() },
                            onSuccess = { user ->
                                if (user.approved && user.planStatus == "active") {
                                    onNavigateToDashboard()
                                } else if (user.approved) {
                                    onNavigateToPayment()
                                } else {
                                    onNavigateToPending()
                                }
                            }
                        )
                    },
                    enabled = uiState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank(),
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
                        .testTag("login_submit_button")
                ) {
                    if (uiState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "LOGIN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 5. BOTTOM TEXT: "New here? Get Started"
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "New here? ",
                        color = DarkTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Get Started",
                        color = BrandGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                authViewModel.clearAuthErrors()
                                onNavigateToRegister()
                            }
                            .padding(4.dp)
                            .testTag("login_go_to_register_link")
                    )
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Reset Password",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered email address and we will send you a password reset link.",
                        color = DarkTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = { Text("driver@email.com", color = DarkTextMuted, fontSize = 13.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreenPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            try {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(resetEmail.trim())
                                Toast.makeText(context, "Password reset link sent to $resetEmail", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Reset link sent if email is registered", Toast.LENGTH_SHORT).show()
                            }
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Send Link", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("forgot_password_dialog")
        )
    }
}

@Preview(name = "LoginScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun LoginScreenPreviewDark() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        LoginScreen(
            authViewModel = AuthViewModel(),
            onNavigateBack = {},
            onNavigateToRegister = {},
            onNavigateToPending = {},
            onNavigateToPayment = {},
            onNavigateToDashboard = {}
        )
    }
}

@Preview(name = "LoginScreen Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun LoginScreenPreviewLight() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        LoginScreen(
            authViewModel = AuthViewModel(),
            onNavigateBack = {},
            onNavigateToRegister = {},
            onNavigateToPending = {},
            onNavigateToPayment = {},
            onNavigateToDashboard = {}
        )
    }
}
