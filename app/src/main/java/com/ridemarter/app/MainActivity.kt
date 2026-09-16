package com.ridemarter.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.ridemarter.app.navigation.AppNavigation
import com.ridemarter.app.ui.theme.RideMarterTheme
import com.ridemarter.app.viewmodel.ThemeMode
import com.ridemarter.app.viewmodel.ThemeViewModel

open class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Safe Firebase initialization
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app == null) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey("AIzaSyBYQ7RCBmXwtp7-O7YqNRm0Nx2gm8K85bA")
                        .setApplicationId("1:440889531345:android:comridemarterapp4408")
                        .setProjectId("ridemarter")
                        .setStorageBucket("ridemarter.firebasestorage.app")
                        .setGcmSenderId("440889531345")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
            Log.d("RideMarter", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.w("RideMarter", "Firebase default init failed: ${e.message}, trying explicit options")
            try {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBYQ7RCBmXwtp7-O7YqNRm0Nx2gm8K85bA")
                    .setApplicationId("1:440889531345:android:comridemarterapp4408")
                    .setProjectId("ridemarter")
                    .setStorageBucket("ridemarter.firebasestorage.app")
                    .setGcmSenderId("440889531345")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("RideMarter", "Firebase initialized with explicit options")
            } catch (e2: Exception) {
                Log.e("RideMarter", "Firebase initialization failed: ${e2.message}")
            }
        }

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val currentThemeMode by themeViewModel.themeMode
                .collectAsStateWithLifecycle()

            RideMarterTheme(themeMode = currentThemeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        isDarkTheme = currentThemeMode == ThemeMode.DARK,
                        onToggleTheme = { themeViewModel.toggleTheme() }
                    )
                }
            }
        }
    }
}
