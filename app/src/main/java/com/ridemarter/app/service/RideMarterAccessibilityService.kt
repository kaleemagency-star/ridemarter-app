package com.ridemarter.app.service

/*
 * Accessibility Service Configuration (app/src/main/res/xml/accessibility_service_config.xml):
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <accessibility-service
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged"
 *     android:accessibilityFeedbackType="feedbackGeneric"
 *     android:accessibilityFlags="flagReportViewIds|flagRetrieveInteractiveWindows|flagIncludeNotImportantViews"
 *     android:canRetrieveWindowContent="true"
 *     android:notificationTimeout="10"
 *     android:description="@string/accessibility_service_description"
 *     android:packageNames="com.rapido.driver,com.rapido.passenger,com.ubercab.driver,com.olacabs.driver,com.olacabs.partner" />
 */

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ridemarter.app.dataStore
import com.ridemarter.app.model.AreaGroup
import com.ridemarter.app.model.OrderAlertData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RideMarterAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "RideMarterAccessService"

        var isRunning = false
            private set

        // Supported Driver & Passenger Package Identifiers
        const val PKG_RAPIDO_DRIVER = "com.rapido.driver"
        const val PKG_RAPIDO_PASSENGER = "com.rapido.passenger"
        const val PKG_UBER_DRIVER = "com.ubercab.driver"
        const val PKG_OLA_DRIVER = "com.olacabs.driver"
        const val PKG_OLA_PARTNER = "com.olacabs.partner"

        val SUPPORTED_PACKAGES = setOf(
            PKG_RAPIDO_DRIVER,
            PKG_RAPIDO_PASSENGER,
            PKG_UBER_DRIVER,
            PKG_OLA_DRIVER,
            PKG_OLA_PARTNER
        )

        // Accept Button texts in various apps and localizations
        private val ACCEPT_BUTTON_TEXTS = listOf(
            "ACCEPT", "Accept", "Accept Ride", "Accept Trip",
            "CONFIRM", "Confirm", "Confirm Booking",
            "GO", "Accept Order", "Tap to Accept"
        )

        // Reject Button texts
        private val REJECT_BUTTON_TEXTS = listOf(
            "REJECT", "Reject", "DECLINE", "Decline",
            "CANCEL", "Cancel", "PASS", "Pass", "Skip"
        )

        // Known View IDs for fast lookup
        private val ACCEPT_VIEW_IDS = listOf(
            "com.rapido.driver:id/btn_accept",
            "com.rapido.driver:id/accept_button",
            "com.rapido.passenger:id/btn_accept",
            "com.ubercab.driver:id/ub__accept_button",
            "com.ubercab.driver:id/accept_button",
            "com.olacabs.driver:id/btn_accept_ride",
            "com.olacabs.driver:id/accept_button"
        )

        private val REJECT_VIEW_IDS = listOf(
            "com.rapido.driver:id/btn_reject",
            "com.rapido.driver:id/btn_decline",
            "com.ubercab.driver:id/ub__reject_button",
            "com.olacabs.driver:id/btn_reject_ride"
        )

        // DataStore Preference Keys
        val PREF_SPEED_SETTING = stringPreferencesKey("speed_setting_ms")
        val PREF_SPEED_MS = stringPreferencesKey("speed_ms")
        val PREF_PLAN_STATUS = stringPreferencesKey("plan_status")
        val PREF_IS_PLAN_EXPIRED = booleanPreferencesKey("is_plan_expired")
        val PREF_GLOBAL_MIN_PICKUP = floatPreferencesKey("global_min_pickup")
        val PREF_GLOBAL_MAX_DROP = floatPreferencesKey("global_max_drop")
        val PREF_GOTO_KEYWORDS = stringPreferencesKey("goto_area_keywords")
        val PREF_NOGO_KEYWORDS = stringPreferencesKey("nogo_area_keywords")
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastProcessedOrderId = ""
    private var lastProcessedTime = 0L
    private val DUPLICATE_DEBOUNCE_MS = 4000L

    // Cached Settings
    private var cachedSpeedMs = 10L
    private var cachedSpeedText = "10ms"
    private var isPlanExpired = false
    private var globalMinPickupKm = 0.5f
    private var gotoGroups = mutableListOf<AreaGroup>()
    private var noGoGroups = mutableListOf<AreaGroup>()

    private val auth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.i(TAG, "RideMarter Accessibility Service connected successfully")

        // Configure accessibility service parameters programmatically
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            packageNames = SUPPORTED_PACKAGES.toTypedArray()
            notificationTimeout = 10
        }

        // Initialize and listen to preferences and user groups
        refreshPreferencesAndGroups()
    }

    private fun refreshPreferencesAndGroups() {
        serviceScope.launch {
            try {
                // 1. Read DataStore preferences
                val prefs = dataStore.data.first()
                val speedVal = prefs[PREF_SPEED_SETTING] ?: prefs[PREF_SPEED_MS] ?: "10ms"
                cachedSpeedText = speedVal
                cachedSpeedMs = when (speedVal.trim().lowercase()) {
                    "1ms" -> 1L
                    "50ms" -> 50L
                    else -> 10L
                }

                val planStatus = prefs[PREF_PLAN_STATUS] ?: "active"
                val planExpiredFlag = prefs[PREF_IS_PLAN_EXPIRED] ?: false
                isPlanExpired = planExpiredFlag || planStatus.equals("expired", ignoreCase = true)

                globalMinPickupKm = prefs[PREF_GLOBAL_MIN_PICKUP] ?: 0.5f

                // Load initial sample area groups
                if (gotoGroups.isEmpty()) {
                    gotoGroups.addAll(getDefaultGotoGroups())
                }
                if (noGoGroups.isEmpty()) {
                    noGoGroups.addAll(getDefaultNoGoGroups())
                }

                // 2. Sync from Firestore if available
                syncFromFirestore()
            } catch (e: Exception) {
                Log.w(TAG, "Error loading preferences in service: ${e.message}")
            }
        }
    }

    private fun syncFromFirestore() {
        val uid = auth?.currentUser?.uid ?: return
        val db = firestore ?: return

        // Fetch User Plan Status
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val status = doc.getString("planStatus") ?: "active"
                    val expiry = doc.getTimestamp("planExpiry")
                    val now = System.currentTimeMillis()
                    val isExpiredByTime = expiry != null && expiry.toDate().time < now
                    isPlanExpired = status.equals("expired", ignoreCase = true) || isExpiredByTime
                    Log.d(TAG, "Firestore Plan Status: $status, isPlanExpired=$isPlanExpired")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to check plan status: ${e.message}")
            }

        // Fetch GOTO Area Groups
        db.collection("users").document(uid).collection("areaGroups")
            .whereEqualTo("type", "GOTO")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val groups = snapshot.documents.mapNotNull { doc ->
                        AreaGroup(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            isEnabled = doc.getBoolean("isEnabled") ?: (doc.getBoolean("enabled") ?: true),
                            minPickupKm = (doc.getDouble("minPickupKm") ?: 0.5).toFloat(),
                            maxDropKm = (doc.getDouble("maxDropKm") ?: 6.5).toFloat(),
                            keywords = (doc.get("keywords") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            type = "GOTO"
                        )
                    }
                    if (groups.isNotEmpty()) {
                        gotoGroups.clear()
                        gotoGroups.addAll(groups)
                    }
                }
            }

        // Fetch NOGO Area Groups
        db.collection("users").document(uid).collection("areaGroups")
            .whereEqualTo("type", "NOGO")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val groups = snapshot.documents.mapNotNull { doc ->
                        AreaGroup(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            isEnabled = doc.getBoolean("isEnabled") ?: (doc.getBoolean("enabled") ?: true),
                            minPickupKm = (doc.getDouble("minPickupKm") ?: 0.5).toFloat(),
                            maxDropKm = (doc.getDouble("maxDropKm") ?: 30.0).toFloat(),
                            keywords = (doc.get("keywords") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            type = "NOGO",
                            noGoAction = doc.getString("noGoAction") ?: "REJECT"
                        )
                    }
                    if (groups.isNotEmpty()) {
                        noGoGroups.clear()
                        noGoGroups.addAll(groups)
                    }
                }
            }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val packageName = event.packageName?.toString() ?: return
        if (packageName !in SUPPORTED_PACKAGES) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val rootNode = rootInActiveWindow ?: return
        processOrderEvent(rootNode, packageName)
    }

    private fun processOrderEvent(rootNode: AccessibilityNodeInfo, packageName: String) {
        val platformName = when {
            packageName.contains("rapido") -> "Rapido"
            packageName.contains("uber") -> "Uber"
            packageName.contains("ola") -> "Ola"
            else -> "Ride Alert"
        }

        // Extract all visible text nodes from the screen
        val allTexts = mutableListOf<String>()
        collectAllTextNodes(rootNode, allTexts)

        if (allTexts.isEmpty()) return

        // 1. Check if this is an incoming ride order dialog/popup
        val acceptButton = findAcceptButton(rootNode)
        val hasRideKeywords = allTexts.any { text ->
            val lower = text.lowercase()
            lower.contains("fare") || lower.contains("pickup") || lower.contains("drop") ||
                    lower.contains("trip") || lower.contains("km") || lower.contains("₹") || lower.contains("rs")
        }

        if (acceptButton == null && !hasRideKeywords) {
            return
        }

        // 2. Parse Order Information from Nodes
        val fare = extractFare(allTexts)
        val pickupDistance = extractPickupDistance(allTexts)
        val dropDistance = extractDropDistance(allTexts)
        val dropArea = extractDropArea(allTexts)
        val dropAddress = extractDropAddress(allTexts, dropArea)

        // Deduplicate events to prevent spamming
        val orderSignature = "${platformName}_${fare}_${pickupDistance}_${dropDistance}_${dropArea}"
        val currentTime = System.currentTimeMillis()
        if (orderSignature == lastProcessedOrderId && (currentTime - lastProcessedTime) < DUPLICATE_DEBOUNCE_MS) {
            return
        }
        lastProcessedOrderId = orderSignature
        lastProcessedTime = currentTime

        Log.d(TAG, "Detected Ride Request: Platform=$platformName, Fare=$fare, Pickup=$pickupDistance, Drop=$dropDistance, Area=$dropArea")

        // 3. Plan Expiry Check
        if (isPlanExpired) {
            Log.w(TAG, "User subscription plan is EXPIRED. Auto-accept disabled.")
            val alertData = OrderAlertData(
                platform = platformName,
                fare = fare,
                pickupDistance = pickupDistance,
                dropDistance = dropDistance,
                dropArea = dropArea,
                dropAddress = dropAddress,
                action = "Ignored",
                speed = cachedSpeedText,
                timestamp = currentTime
            )
            recordAndNotifyOrder(alertData, "Ignored (Plan Expired)")
            return
        }

        // 4. Decision Logic: Check NO GO and GO TO rules
        val decision = evaluateOrderDecision(dropArea, dropAddress, pickupDistance, dropDistance)
        Log.i(TAG, "Order Decision: $decision (Target Speed: ${cachedSpeedMs}ms)")

        val alertData = OrderAlertData(
            platform = platformName,
            fare = fare,
            pickupDistance = pickupDistance,
            dropDistance = dropDistance,
            dropArea = dropArea,
            dropAddress = dropAddress,
            action = decision,
            speed = cachedSpeedText,
            timestamp = currentTime
        )

        when (decision) {
            "Accepted" -> {
                // Fast Auto-Accept at target response time (1ms, 10ms, 50ms)
                mainHandler.postDelayed({
                    val clicked = acceptButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
                    Log.i(TAG, "AUTO-ACCEPT executed (Clicked=$clicked) at ${cachedSpeedMs}ms")
                    recordAndNotifyOrder(alertData, "Accepted")
                }, cachedSpeedMs)
            }
            "Rejected" -> {
                // Auto-Reject
                val rejectButton = findRejectButton(rootNode)
                mainHandler.postDelayed({
                    val clicked = rejectButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
                    Log.i(TAG, "AUTO-REJECT executed (Clicked=$clicked)")
                    recordAndNotifyOrder(alertData, "Rejected")
                }, cachedSpeedMs)
            }
            else -> {
                // Auto-Ignore
                Log.i(TAG, "AUTO-IGNORE executed for order outside selected groups/range")
                recordAndNotifyOrder(alertData, "Ignored")
            }
        }
    }

    /**
     * Evaluates whether to ACCEPT, REJECT, or IGNORE the order.
     */
    private fun evaluateOrderDecision(
        dropArea: String,
        dropAddress: String,
        pickupDistStr: String,
        dropDistStr: String
    ): String {
        val pickupKm = parseKm(pickupDistStr)
        val dropKm = parseKm(dropDistStr)
        val combinedDest = "$dropArea $dropAddress".lowercase()

        // 1. Check NO GO Groups
        for (group in noGoGroups.filter { it.isEnabled }) {
            for (keyword in group.keywords) {
                if (keyword.isNotBlank() && combinedDest.contains(keyword.lowercase())) {
                    Log.d(TAG, "Matched NO GO group '${group.name}' on keyword '$keyword'")
                    return if (group.noGoAction.equals("IGNORE", ignoreCase = true)) "Ignored" else "Rejected"
                }
            }
        }

        // 2. Check GO TO Groups
        val activeGotoGroups = gotoGroups.filter { it.isEnabled }
        if (activeGotoGroups.isEmpty()) {
            // Default rule if no active groups: accept if within global bounds
            return if (pickupKm <= 5.0f && dropKm <= 15.0f) "Accepted" else "Ignored"
        }

        for (group in activeGotoGroups) {
            val matchesKeyword = group.keywords.isEmpty() || group.keywords.any { kw ->
                kw.isNotBlank() && combinedDest.contains(kw.lowercase())
            }

            if (matchesKeyword) {
                val minPickup = if (group.minPickupKm > 0f) group.minPickupKm else globalMinPickupKm
                val maxDrop = if (group.maxDropKm > 0f) group.maxDropKm else 25.0f

                val isPickupValid = pickupKm >= minPickup
                val isDropValid = dropKm <= maxDrop

                if (isPickupValid && isDropValid) {
                    Log.d(TAG, "Matched GO TO group '${group.name}' within range: pickup=$pickupKm, drop=$dropKm")
                    return "Accepted"
                } else {
                    Log.d(TAG, "Group '${group.name}' matched keyword but outside range: pickup=$pickupKm (min $minPickup), drop=$dropKm (max $maxDrop)")
                }
            }
        }

        // Unmatched or outside range: AUTO-IGNORE
        return "Ignored"
    }

    private fun parseKm(distStr: String): Float {
        return try {
            val num = Regex("([0-9]+(?:\\.[0-9]+)?)").find(distStr)?.groupValues?.get(1)
            num?.toFloatOrNull() ?: 1.0f
        } catch (e: Exception) {
            1.0f
        }
    }

    private fun recordAndNotifyOrder(alertData: OrderAlertData, actionResult: String) {
        // 1. Send OrderAlertData to OrderAlertOverlayService to show side popup
        try {
            if (Settings.canDrawOverlays(this)) {
                OrderAlertOverlayService.showAlert(this, alertData.copy(action = actionResult))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not start overlay service: ${e.message}")
        }

        // 2. Save order to Firebase Firestore history
        val uid = auth?.currentUser?.uid ?: "anonymous_driver"
        val db = firestore
        if (db != null) {
            serviceScope.launch {
                try {
                    val now = Date()
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                    val orderRecord = hashMapOf(
                        "platform" to alertData.platform,
                        "fare" to alertData.fare,
                        "pickupDistance" to alertData.pickupDistance,
                        "dropDistance" to alertData.dropDistance,
                        "dropArea" to alertData.dropArea,
                        "dropAddress" to alertData.dropAddress,
                        "action" to actionResult,
                        "speed" to alertData.speed,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "date" to dateFormat.format(now),
                        "time" to timeFormat.format(now)
                    )

                    db.collection("users").document(uid).collection("history").add(orderRecord)
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore history log failed: ${e.message}")
                }
            }
        }

        // 3. Save to local DataStore
        serviceScope.launch {
            try {
                dataStore.edit { prefs ->
                    prefs[OrderAlertOverlayService.PREF_LAST_ORDER_PLATFORM] = alertData.platform
                    prefs[OrderAlertOverlayService.PREF_LAST_ORDER_FARE] = alertData.fare
                    prefs[OrderAlertOverlayService.PREF_LAST_ORDER_ACTION] = actionResult
                    prefs[OrderAlertOverlayService.PREF_LAST_ORDER_TIME] =
                        SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                }
            } catch (e: Exception) {
                Log.w(TAG, "DataStore write failed: ${e.message}")
            }
        }
    }

    // Node Traversal & Text Extraction Utilities

    private fun collectAllTextNodes(node: AccessibilityNodeInfo?, result: MutableList<String>) {
        node ?: return
        node.text?.toString()?.trim()?.let {
            if (it.isNotBlank()) result.add(it)
        }
        node.contentDescription?.toString()?.trim()?.let {
            if (it.isNotBlank()) result.add(it)
        }
        for (i in 0 until node.childCount) {
            collectAllTextNodes(node.getChild(i), result)
        }
    }

    private fun findAcceptButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Fast ID lookup first
        for (id in ACCEPT_VIEW_IDS) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            val btn = nodes?.firstOrNull { it.isClickable || it.isEnabled }
            if (btn != null) return btn
        }

        // Text matching lookup
        for (text in ACCEPT_BUTTON_TEXTS) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            val btn = nodes?.firstOrNull { it.isClickable }
                ?: nodes?.firstOrNull()?.let { findClickableParent(it) }
            if (btn != null) return btn
        }
        return null
    }

    private fun findRejectButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (id in REJECT_VIEW_IDS) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            val btn = nodes?.firstOrNull { it.isClickable || it.isEnabled }
            if (btn != null) return btn
        }

        for (text in REJECT_BUTTON_TEXTS) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            val btn = nodes?.firstOrNull { it.isClickable }
                ?: nodes?.firstOrNull()?.let { findClickableParent(it) }
            if (btn != null) return btn
        }
        return null
    }

    private fun findClickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) return current
            current = current.parent
        }
        return null
    }

    private fun extractFare(texts: List<String>): String {
        for (text in texts) {
            val match = Regex("(?:₹|Rs\\.?|rs\\.?|INR)\\s*([0-9]+(?:\\.[0-9]+)?)").find(text)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        for (text in texts) {
            if (text.startsWith("₹") || text.startsWith("Rs")) {
                val clean = text.filter { it.isDigit() || it == '.' }
                if (clean.isNotBlank()) return clean
            }
        }
        return "85"
    }

    private fun extractPickupDistance(texts: List<String>): String {
        for (i in texts.indices) {
            val t = texts[i].lowercase()
            if (t.contains("pickup") || t.contains("away") || t.contains("from you")) {
                val distMatch = Regex("([0-9]+(?:\\.[0-9]+)?\\s*(?:km|m|mins|min))").find(texts[i])
                if (distMatch != null) return distMatch.groupValues[1]
                if (i + 1 < texts.size) {
                    val nextMatch = Regex("([0-9]+(?:\\.[0-9]+)?\\s*(?:km|m))").find(texts[i + 1])
                    if (nextMatch != null) return nextMatch.groupValues[1]
                }
            }
        }
        val generalMatch = texts.mapNotNull { Regex("([0-9]+(?:\\.[0-9]+)?\\s*km)").find(it) }.firstOrNull()
        return generalMatch?.groupValues?.get(1) ?: "1.2 km"
    }

    private fun extractDropDistance(texts: List<String>): String {
        var foundPickup = false
        for (i in texts.indices) {
            val t = texts[i].lowercase()
            if (t.contains("drop") || t.contains("trip") || t.contains("distance")) {
                val distMatch = Regex("([0-9]+(?:\\.[0-9]+)?\\s*(?:km|m))").find(texts[i])
                if (distMatch != null) return distMatch.groupValues[1]
                if (i + 1 < texts.size) {
                    val nextMatch = Regex("([0-9]+(?:\\.[0-9]+)?\\s*(?:km|m))").find(texts[i + 1])
                    if (nextMatch != null) return nextMatch.groupValues[1]
                }
            }
        }
        val kmMatches = texts.mapNotNull { Regex("([0-9]+(?:\\.[0-9]+)?\\s*km)").find(it)?.groupValues?.get(1) }
        return if (kmMatches.size >= 2) kmMatches[1] else "5.8 km"
    }

    private fun extractDropArea(texts: List<String>): String {
        for (i in texts.indices) {
            val t = texts[i].lowercase()
            if (t.contains("drop") || t.contains("to") || t.contains("destination")) {
                if (i + 1 < texts.size && texts[i + 1].length in 3..40) {
                    return texts[i + 1]
                }
            }
        }
        for (text in texts) {
            if (text.contains("Road") || text.contains("Layout") || text.contains("Nagar") ||
                text.contains("Block") || text.contains("Sector") || text.contains("Stage") ||
                text.contains("Cross") || text.contains("Circle")
            ) {
                return text.take(35)
            }
        }
        return "Central City"
    }

    private fun extractDropAddress(texts: List<String>, dropArea: String): String {
        for (text in texts) {
            if (text != dropArea && text.length > 15 &&
                (text.contains(",") || text.contains("Near") || text.contains("Opp") || text.contains("Behind"))
            ) {
                return text.take(65)
            }
        }
        return "Near Metro Station, Main Commercial Road"
    }

    private fun getDefaultGotoGroups(): List<AreaGroup> {
        return listOf(
            AreaGroup(
                id = "goto_default_1",
                name = "City Center",
                isEnabled = true,
                minPickupKm = 0.5f,
                maxDropKm = 6.5f,
                keywords = listOf("MG Road", "Brigade Road", "Koramangala", "Indiranagar", "HSR Layout"),
                type = "GOTO"
            )
        )
    }

    private fun getDefaultNoGoGroups(): List<AreaGroup> {
        return listOf(
            AreaGroup(
                id = "nogo_default_1",
                name = "Far Suburbs & Congestion",
                isEnabled = true,
                minPickupKm = 0.5f,
                maxDropKm = 30.0f,
                keywords = listOf("Whitefield", "Silk Board", "KR Puram", "Electronic City"),
                type = "NOGO",
                noGoAction = "REJECT"
            )
        )
    }

    override fun onInterrupt() {
        isRunning = false
        Log.w(TAG, "RideMarter Accessibility Service interrupted")
    }

    override fun onDestroy() {
        isRunning = false
        serviceScope.cancel()
        super.onDestroy()
        Log.i(TAG, "RideMarter Accessibility Service destroyed")
    }
}
