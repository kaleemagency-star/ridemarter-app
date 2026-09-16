package com.smartdrivo.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartdrivo.app.ui.screens.AreaGroupsScreen
import com.smartdrivo.app.ui.screens.DashboardScreen
import com.smartdrivo.app.ui.screens.HistoryScreen
import com.smartdrivo.app.ui.screens.LoginScreen
import com.smartdrivo.app.ui.screens.OnboardingScreen
import com.smartdrivo.app.ui.screens.PaymentPendingScreen
import com.smartdrivo.app.ui.screens.PaymentScreen
import com.smartdrivo.app.ui.screens.PendingApprovalScreen
import com.smartdrivo.app.ui.screens.ProfileScreen
import com.smartdrivo.app.ui.screens.SettingsScreen
import com.smartdrivo.app.ui.screens.PlaceholderScreen
import com.smartdrivo.app.ui.screens.RegistrationScreen
import com.smartdrivo.app.ui.screens.SplashScreen
import com.smartdrivo.app.viewmodel.AuthViewModel

object AppDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PENDING = "pending"
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val AREAS = "areas"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val PAYMENT = "payment"
    const val PAYMENT_PENDING = "payment_pending"
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH,
        modifier = modifier
    ) {
        composable(AppDestinations.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(AppDestinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onNavigateToLogin = {
                    navController.navigate(AppDestinations.LOGIN)
                },
                onNavigateToGetStarted = {
                    navController.navigate(AppDestinations.LOGIN)
                }
            )
        }

        composable(AppDestinations.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER)
                },
                onNavigateToPending = {
                    navController.navigate(AppDestinations.PENDING) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToPayment = {
                    navController.navigate(AppDestinations.PAYMENT)
                },
                onNavigateToDashboard = {
                    navController.navigate(AppDestinations.DASHBOARD)
                }
            )
        }

        composable(AppDestinations.REGISTER) {
            RegistrationScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPending = {
                    navController.navigate(AppDestinations.PENDING) {
                        popUpTo(AppDestinations.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.PENDING) {
            PendingApprovalScreen(
                authViewModel = authViewModel,
                onNavigateToPayment = {
                    navController.navigate(AppDestinations.PAYMENT)
                },
                onNavigateBack = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.PENDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToPayment = {
                    navController.navigate(AppDestinations.PAYMENT)
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.PAYMENT) {
            PaymentScreen(
                onPaymentSubmitted = {
                    navController.navigate(AppDestinations.PAYMENT_PENDING)
                },
                onNavigateToDashboard = {
                    navController.navigate(AppDestinations.DASHBOARD) {
                        popUpTo(AppDestinations.PAYMENT) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.PAYMENT_PENDING) {
            PaymentPendingScreen(
                onPaymentApproved = {
                    navController.navigate(AppDestinations.DASHBOARD) {
                        popUpTo(AppDestinations.PAYMENT_PENDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.HISTORY) {
            HistoryScreen(
                onNavigateToDashboard = {
                    navController.navigate(AppDestinations.DASHBOARD) {
                        popUpTo(AppDestinations.HISTORY) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.AREAS) {
            AreaGroupsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                onNavigateToPayment = {
                    navController.navigate(AppDestinations.PAYMENT)
                },
                onLogout = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
