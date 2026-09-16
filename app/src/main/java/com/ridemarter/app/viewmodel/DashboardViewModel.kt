package com.ridemarter.app.viewmodel

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ridemarter.app.dataStore
import com.ridemarter.app.model.OrderRecord
import com.ridemarter.app.model.UserData
import com.ridemarter.app.service.RideMarterAccessibilityService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

sealed class DashboardEvent {
    object NeedAccessibilityPermission : DashboardEvent()
    object NeedOverlayPermission : DashboardEvent()
    object NeedBatteryOptimization : DashboardEvent()
    data class ShowMessage(val text: String) : DashboardEvent()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth?
        get() = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val firestore: FirebaseFirestore?
        get() = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val speedKey = stringPreferencesKey("speed_setting_ms")

    // UI State
    private val _currentUserData = MutableStateFlow<UserData?>(null)
    val currentUserData: StateFlow<UserData?> = _currentUserData.asStateFlow()

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    // Apps installed/detected status
    private val _isRapidoDetected = MutableStateFlow(false)
    val isRapidoDetected: StateFlow<Boolean> = _isRapidoDetected.asStateFlow()

    private val _isUberDetected = MutableStateFlow(false)
    val isUberDetected: StateFlow<Boolean> = _isUberDetected.asStateFlow()

    private val _isOlaDetected = MutableStateFlow(false)
    val isOlaDetected: StateFlow<Boolean> = _isOlaDetected.asStateFlow()

    // Speed setting: "1ms", "10ms", "50ms"
    private val _selectedSpeed = MutableStateFlow("10ms")
    val selectedSpeed: StateFlow<String> = _selectedSpeed.asStateFlow()

    // Stats
    private val _acceptedCount = MutableStateFlow(18)
    val acceptedCount: StateFlow<Int> = _acceptedCount.asStateFlow()

    private val _rejectedCount = MutableStateFlow(4)
    val rejectedCount: StateFlow<Int> = _rejectedCount.asStateFlow()

    private val _ignoredCount = MutableStateFlow(2)
    val ignoredCount: StateFlow<Int> = _ignoredCount.asStateFlow()

    // Recent orders
    private val _recentOrders = MutableStateFlow<List<OrderRecord>>(emptyList())
    val recentOrders: StateFlow<List<OrderRecord>> = _recentOrders.asStateFlow()

    // Events (permissions, errors)
    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    // Current Bottom Nav Tab
    private val _currentTab = MutableStateFlow("dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private var userListenerRegistration: ListenerRegistration? = null

    init {
        loadSpeedSetting()
        fetchUserData()
        detectDriverApps()
        loadInitialOrders()
    }

    override fun onCleared() {
        super.onCleared()
        userListenerRegistration?.remove()
        userListenerRegistration = null
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    private fun loadSpeedSetting() {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().dataStore.data.first()
                val speed = prefs[speedKey] ?: "10ms"
                _selectedSpeed.value = speed
            } catch (e: Exception) {
                _selectedSpeed.value = "10ms"
            }
        }
    }

    fun setSpeedSetting(speed: String) {
        _selectedSpeed.value = speed
        viewModelScope.launch {
            try {
                getApplication<Application>().dataStore.edit { prefs ->
                    prefs[speedKey] = speed
                }
            } catch (e: Exception) {
                Log.w("DashboardViewModel", "Failed to save speed: ${e.message}")
            }
        }
    }

    fun fetchUserData() {
        val uid = try { auth?.currentUser?.uid } catch (e: Exception) { null }
        if (uid.isNullOrBlank()) {
            // Provide active test account data if in offline/guest state
            if (_currentUserData.value == null) {
                val now = System.currentTimeMillis()
                val expiryMillis = now + TimeUnit.DAYS.toMillis(6) // 6 days active
                _currentUserData.value = UserData(
                    uid = "test_driver_uid",
                    userId = "SD88492011",
                    name = "Driver Partner",
                    email = "driver@ridemarter.com",
                    mobile = "9876543210",
                    approved = true,
                    status = "approved",
                    planStatus = "active",
                    planName = "Popular (7 Days)",
                    planExpiry = Timestamp(Date(expiryMillis)),
                    planActivatedAt = Timestamp(Date(now - TimeUnit.DAYS.toMillis(1)))
                )
            }
            return
        }

        userListenerRegistration?.remove()
        val db = firestore
        if (db != null) {
            userListenerRegistration = db.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("DashboardViewModel", "Firestore snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val approved = snapshot.getBoolean("approved") ?: true
                        val status = snapshot.getString("status") ?: "approved"
                        val planStatus = snapshot.getString("planStatus") ?: "active"
                        val planName = snapshot.getString("planName") ?: "Popular (7 Days)"
                        val planExpiry = snapshot.getTimestamp("planExpiry")
                        val planActivatedAt = snapshot.getTimestamp("planActivatedAt")
                        val serviceActive = snapshot.getBoolean("serviceActive") ?: false

                        val data = UserData(
                            uid = uid,
                            userId = snapshot.getString("userId") ?: ("SD" + uid.take(8).uppercase()),
                            name = snapshot.getString("name") ?: (try { auth?.currentUser?.displayName } catch (e: Exception) { null } ?: "Driver Partner"),
                            email = snapshot.getString("email") ?: (try { auth?.currentUser?.email } catch (e: Exception) { null } ?: "driver@ridemarter.com"),
                            mobile = snapshot.getString("mobile") ?: "",
                            vehicleType = snapshot.getString("vehicleType") ?: "AUTO",
                            approved = approved,
                            status = status,
                            planStatus = planStatus,
                            planName = planName,
                            planExpiry = planExpiry,
                            planActivatedAt = planActivatedAt,
                            serviceActive = serviceActive
                        )
                        _currentUserData.value = data
                        _isServiceActive.value = serviceActive
                    }
                }
        }
    }

    /**
     * Check permissions and toggle the service state
     */
    fun toggleService() {
        if (_isServiceActive.value) {
            // Turning OFF
            _isServiceActive.value = false
            updateServiceInFirestore(false)
        } else {
            // Turning ON -> check permissions
            val context = getApplication<Application>()

            if (!isAccessibilityServiceEnabled(context)) {
                viewModelScope.launch {
                    _events.emit(DashboardEvent.NeedAccessibilityPermission)
                }
                return
            }

            if (!Settings.canDrawOverlays(context)) {
                viewModelScope.launch {
                    _events.emit(DashboardEvent.NeedOverlayPermission)
                }
                return
            }

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                viewModelScope.launch {
                    _events.emit(DashboardEvent.NeedBatteryOptimization)
                }
                return
            }

            // All permissions satisfied
            _isServiceActive.value = true
            updateServiceInFirestore(true)
        }
    }

    fun forceToggleOnForDemo() {
        _isServiceActive.value = true
        updateServiceInFirestore(true)
    }

    private fun updateServiceInFirestore(active: Boolean) {
        val uid = try { auth?.currentUser?.uid } catch (e: Exception) { null } ?: return
        viewModelScope.launch {
            try {
                firestore?.collection("users")?.document(uid)?.update("serviceActive", active)?.await()
            } catch (e: Exception) {
                Log.w("DashboardViewModel", "Could not update serviceActive in Firestore: ${e.message}")
            }
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val targetService = "${context.packageName}/${RideMarterAccessibilityService::class.java.canonicalName}"
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
    }

    private fun detectDriverApps() {
        val pm = getApplication<Application>().packageManager
        _isRapidoDetected.value = try {
            isAppInstalled(pm, "com.rapido.rider") ||
            isAppInstalled(pm, "com.rapido.captain")
        } catch (e: Exception) { true }

        _isUberDetected.value = try {
            isAppInstalled(pm, "com.ubercab.driver")
        } catch (e: Exception) { true }

        _isOlaDetected.value = try {
            isAppInstalled(pm, "com.olacabs.partner")
        } catch (e: Exception) { true }
    }

    private fun isAppInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun loadInitialOrders() {
        _recentOrders.value = listOf(
            OrderRecord(
                id = "ord_101",
                appName = "Rapido",
                fare = 84,
                pickup = "MG Road Metro Station",
                drop = "Indiranagar 100ft Rd",
                distance = "3.2 km",
                timeAgo = "2m ago",
                status = "ACCEPTED"
            ),
            OrderRecord(
                id = "ord_102",
                appName = "Uber",
                fare = 145,
                pickup = "Koramangala 4th Block",
                drop = "HSR Layout Sector 2",
                distance = "5.8 km",
                timeAgo = "8m ago",
                status = "ACCEPTED"
            ),
            OrderRecord(
                id = "ord_103",
                appName = "Ola",
                fare = 52,
                pickup = "Silk Board Flyover",
                drop = "BTM 2nd Stage",
                distance = "2.1 km",
                timeAgo = "18m ago",
                status = "REJECTED"
            ),
            OrderRecord(
                id = "ord_104",
                appName = "Rapido",
                fare = 35,
                pickup = "Whitefield Main Gate",
                drop = "ITPB Back Gate",
                distance = "1.4 km",
                timeAgo = "32m ago",
                status = "IGNORED"
            ),
            OrderRecord(
                id = "ord_105",
                appName = "Uber",
                fare = 210,
                pickup = "Hebbal Flyover",
                drop = "Manyata Tech Park Gate 5",
                distance = "6.4 km",
                timeAgo = "48m ago",
                status = "ACCEPTED"
            )
        )
    }
}
