package com.smartdrivo.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.smartdrivo.app.MainActivity
import com.example.R
import com.smartdrivo.app.dataStore
import com.smartdrivo.app.model.OrderAlertData
import com.smartdrivo.app.ui.overlay.OrderAlertPopupContent
import com.smartdrivo.app.util.MyLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAlertOverlayService : Service() {

    companion object {
        const val TAG = "OrderAlertOverlayService"

        const val ACTION_START_SERVICE = "com.smartdrivo.app.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.smartdrivo.app.STOP_SERVICE"
        const val ACTION_SHOW_ALERT = "show_alert"
        const val ACTION_HIDE_ALERT = "hide_alert"

        const val EXTRA_PLATFORM = "platform"
        const val EXTRA_FARE = "fare"
        const val EXTRA_PICKUP_DIST = "pickup_dist"
        const val EXTRA_DROP_DIST = "drop_dist"
        const val EXTRA_DROP_AREA = "drop_area"
        const val EXTRA_DROP_ADDRESS = "drop_address"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_ACTION = "action"

        const val CHANNEL_ID = "smartdrivo_overlay_channel"
        const val NOTIFICATION_ID = 1001

        val PREF_LAST_ORDER_PLATFORM = stringPreferencesKey("last_order_platform")
        val PREF_LAST_ORDER_FARE = stringPreferencesKey("last_order_fare")
        val PREF_LAST_ORDER_ACTION = stringPreferencesKey("last_order_action")
        val PREF_LAST_ORDER_TIME = stringPreferencesKey("last_order_time")

        var instance: OrderAlertOverlayService? = null

        fun startService(context: Context) {
            val intent = Intent(context, OrderAlertOverlayService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, OrderAlertOverlayService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
        }

        fun showAlert(context: Context, data: OrderAlertData) {
            val intent = Intent(context, OrderAlertOverlayService::class.java).apply {
                action = ACTION_SHOW_ALERT
                putExtra(EXTRA_PLATFORM, data.platform)
                putExtra(EXTRA_FARE, data.fare)
                putExtra(EXTRA_PICKUP_DIST, data.pickupDistance)
                putExtra(EXTRA_DROP_DIST, data.dropDistance)
                putExtra(EXTRA_DROP_AREA, data.dropArea)
                putExtra(EXTRA_DROP_ADDRESS, data.dropAddress)
                putExtra(EXTRA_SPEED, data.speed)
                putExtra(EXTRA_ACTION, data.action)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun hideAlert(context: Context) {
            val intent = Intent(context, OrderAlertOverlayService::class.java).apply {
                action = ACTION_HIDE_ALERT
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var lifecycleOwner: MyLifecycleOwner? = null
    private val handler = Handler(Looper.getMainLooper())
    private var autoDismissRunnable: Runnable? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val auth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val db by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "OrderAlertOverlayService created and running in foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_ALERT -> {
                val data = OrderAlertData(
                    platform = intent.getStringExtra(EXTRA_PLATFORM) ?: "",
                    fare = intent.getStringExtra(EXTRA_FARE) ?: "0",
                    pickupDistance = intent.getStringExtra(EXTRA_PICKUP_DIST) ?: "0 km",
                    dropDistance = intent.getStringExtra(EXTRA_DROP_DIST) ?: "0 km",
                    dropArea = intent.getStringExtra(EXTRA_DROP_AREA) ?: "Unknown Destination",
                    dropAddress = intent.getStringExtra(EXTRA_DROP_ADDRESS) ?: "",
                    action = intent.getStringExtra(EXTRA_ACTION) ?: "Accepted",
                    speed = intent.getStringExtra(EXTRA_SPEED) ?: "10ms",
                    timestamp = System.currentTimeMillis()
                )
                showOverlay(data)
            }
            ACTION_HIDE_ALERT -> {
                hideOverlay()
            }
            ACTION_STOP_SERVICE -> {
                hideOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_START_SERVICE -> {
                Log.d(TAG, "Overlay service started")
            }
        }
        return START_STICKY
    }

    private fun showOverlay(data: OrderAlertData) {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW permission not granted. Cannot show overlay.")
            return
        }

        // Remove any existing overlay view first
        hideOverlay()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 140 // Positioned below system status bar
        }

        val owner = MyLifecycleOwner()
        owner.start()
        lifecycleOwner = owner

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                OrderAlertPopupContent(
                    data = data,
                    onDismiss = { hideOverlay() },
                    onAccept = { handleOrderAction(data, "Accepted") },
                    onReject = { handleOrderAction(data, "Rejected") },
                    onIgnore = { handleOrderAction(data, "Ignored") }
                )
            }
        }

        overlayView = composeView

        try {
            windowManager?.addView(composeView, params)
            Log.d(TAG, "Overlay popup successfully attached to WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view: ${e.message}", e)
        }

        // Schedule auto-dismiss in 10 seconds if user takes no action
        autoDismissRunnable = Runnable {
            Log.d(TAG, "Auto-dismiss timer expired after 10s")
            hideOverlay()
        }
        handler.postDelayed(autoDismissRunnable!!, 10000L)
    }

    private fun hideOverlay() {
        autoDismissRunnable?.let {
            handler.removeCallbacks(it)
            autoDismissRunnable = null
        }

        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
                Log.d(TAG, "Overlay view removed from WindowManager")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view: ${e.message}", e)
            }
            overlayView = null
        }

        lifecycleOwner?.stop()
        lifecycleOwner = null
    }

    private fun handleOrderAction(data: OrderAlertData, actionResult: String) {
        Log.d(TAG, "Driver selected action: $actionResult for ${data.platform} fare=${data.fare}")
        hideOverlay()

        // 1. Log to Firestore history
        logOrderToFirestore(data, actionResult)

        // 2. Save to local DataStore
        saveOrderToDataStore(data, actionResult)
    }

    private fun logOrderToFirestore(data: OrderAlertData, actionResult: String) {
        val uid = auth?.currentUser?.uid ?: "anonymous_driver"
        val database = db ?: return

        serviceScope.launch {
            try {
                val now = Date()
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                val orderRecord = hashMapOf(
                    "platform" to data.platform,
                    "fare" to data.fare,
                    "pickupDistance" to data.pickupDistance,
                    "dropDistance" to data.dropDistance,
                    "dropArea" to data.dropArea,
                    "dropAddress" to data.dropAddress,
                    "action" to actionResult,
                    "speed" to data.speed,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "date" to dateFormat.format(now),
                    "time" to timeFormat.format(now)
                )

                database.collection("users")
                    .document(uid)
                    .collection("history")
                    .add(orderRecord)
                    .addOnSuccessListener {
                        Log.d(TAG, "Order logged successfully to Firestore history: ${it.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed to log order to Firestore: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception logging order to Firestore: ${e.message}", e)
            }
        }
    }

    private fun saveOrderToDataStore(data: OrderAlertData, actionResult: String) {
        serviceScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[PREF_LAST_ORDER_PLATFORM] = data.platform
                    preferences[PREF_LAST_ORDER_FARE] = data.fare
                    preferences[PREF_LAST_ORDER_ACTION] = actionResult
                    preferences[PREF_LAST_ORDER_TIME] = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                }
                Log.d(TAG, "Order recorded in local DataStore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record order in DataStore: ${e.message}", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SmartDrivo Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating ride alert popups over navigation and driving apps"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SmartDrivo Running")
            .setContentText("Overlay service active and monitoring ride requests")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        hideOverlay()
        serviceScope.cancel()
        instance = null
        Log.d(TAG, "OrderAlertOverlayService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
