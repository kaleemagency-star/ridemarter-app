package com.ridemarter.app.viewmodel

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.Context.POWER_SERVICE
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ridemarter.app.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    constructor() : this(Application())

    companion object {
        val SPEED_KEY = stringPreferencesKey("speed_ms")
        val DEFAULT_ACTION_KEY = stringPreferencesKey("default_action")
        val RAPIDO_ENABLED_KEY = booleanPreferencesKey("rapido_enabled")
        val UBER_ENABLED_KEY = booleanPreferencesKey("uber_enabled")
        val OLA_ENABLED_KEY = booleanPreferencesKey("ola_enabled")
        val NOTIFICATION_KEY = booleanPreferencesKey("notifications_enabled")
    }

    private val _selectedSpeed = MutableStateFlow("10ms")
    val selectedSpeed: StateFlow<String> = _selectedSpeed.asStateFlow()

    private val _defaultAction = MutableStateFlow("Ignore")
    val defaultAction: StateFlow<String> = _defaultAction.asStateFlow()

    private val _rapidoEnabled = MutableStateFlow(true)
    val rapidoEnabled: StateFlow<Boolean> = _rapidoEnabled.asStateFlow()

    private val _uberEnabled = MutableStateFlow(true)
    val uberEnabled: StateFlow<Boolean> = _uberEnabled.asStateFlow()

    private val _olaEnabled = MutableStateFlow(true)
    val olaEnabled: StateFlow<Boolean> = _olaEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _isOverlayEnabled = MutableStateFlow(false)
    val isOverlayEnabled: StateFlow<Boolean> = _isOverlayEnabled.asStateFlow()

    private val _isBatteryOptIgnored = MutableStateFlow(false)
    val isBatteryOptIgnored: StateFlow<Boolean> = _isBatteryOptIgnored.asStateFlow()

    private val _appVersion = MutableStateFlow("1.0.0")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val db: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    init {
        loadAllSettings()
        checkPermissions()
        _appVersion.value = getAppVersion(getApplication())
    }

    fun loadAllSettings() {
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                val prefs = app.dataStore.data.first()
                _selectedSpeed.value = prefs[SPEED_KEY] ?: "10ms"
                _defaultAction.value = prefs[DEFAULT_ACTION_KEY] ?: "Ignore"
                _rapidoEnabled.value = prefs[RAPIDO_ENABLED_KEY] ?: true
                _uberEnabled.value = prefs[UBER_ENABLED_KEY] ?: true
                _olaEnabled.value = prefs[OLA_ENABLED_KEY] ?: true
                _notificationsEnabled.value = prefs[NOTIFICATION_KEY] ?: true
            } catch (e: Exception) {
                Log.w("SettingsVM", "loadAllSettings error: ${e.message}")
            }
        }
    }

    fun saveSpeed(speed: String) {
        _selectedSpeed.value = speed
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.dataStore.edit { prefs ->
                    prefs[SPEED_KEY] = speed
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "saveSpeed error: ${e.message}")
            }
        }
    }

    fun saveDefaultAction(action: String) {
        _defaultAction.value = action
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.dataStore.edit { prefs ->
                    prefs[DEFAULT_ACTION_KEY] = action
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "saveDefaultAction error: ${e.message}")
            }
        }
    }

    fun setRapidoEnabled(enabled: Boolean) {
        _rapidoEnabled.value = enabled
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.dataStore.edit { prefs ->
                    prefs[RAPIDO_ENABLED_KEY] = enabled
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "setRapidoEnabled error: ${e.message}")
            }
        }
    }

    fun setUberEnabled(enabled: Boolean) {
        _uberEnabled.value = enabled
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.dataStore.edit { prefs ->
                    prefs[UBER_ENABLED_KEY] = enabled
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "setUberEnabled error: ${e.message}")
            }
        }
    }

    fun setOlaEnabled(enabled: Boolean) {
        _olaEnabled.value = enabled
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.dataStore.edit { prefs ->
                    prefs[OLA_ENABLED_KEY] = enabled
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "setOlaEnabled error: ${e.message}")
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.dataStore.edit { prefs ->
                    prefs[NOTIFICATION_KEY] = enabled
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "setNotificationsEnabled error: ${e.message}")
            }
        }
    }

    fun checkPermissions(context: Context? = null) {
        val ctx = context ?: getApplication()
        try {
            val am = ctx.getSystemService(ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabled = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            _isAccessibilityEnabled.value = enabled?.any {
                it.resolveInfo?.serviceInfo?.packageName == ctx.packageName
            } ?: false
        } catch (e: Exception) {
            _isAccessibilityEnabled.value = false
        }

        try {
            _isOverlayEnabled.value = Settings.canDrawOverlays(ctx)
        } catch (e: Exception) {
            _isOverlayEnabled.value = false
        }

        try {
            val pm = ctx.getSystemService(POWER_SERVICE) as? PowerManager
            _isBatteryOptIgnored.value = pm?.isIgnoringBatteryOptimizations(ctx.packageName) ?: false
        } catch (e: Exception) {
            _isBatteryOptIgnored.value = false
        }
    }

    fun getAppVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
