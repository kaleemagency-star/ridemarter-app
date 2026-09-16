package com.ridemarter.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ridemarter.app.model.DEFAULT_PLANS
import com.ridemarter.app.model.PaymentConfig
import com.ridemarter.app.model.PaymentRecord
import com.ridemarter.app.model.PlanItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class PaymentNavigationEvent {
    object NavigateToPending : PaymentNavigationEvent()
    object NavigateToDashboard : PaymentNavigationEvent()
    data class ShowToast(val message: String) : PaymentNavigationEvent()
    data class ShowDialog(val title: String, val message: String) : PaymentNavigationEvent()
}

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth?
        get() = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val firestore: FirebaseFirestore?
        get() = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    // Plans list
    private val _plans = MutableStateFlow<List<PlanItem>>(DEFAULT_PLANS)
    val plans: StateFlow<List<PlanItem>> = _plans.asStateFlow()

    private val _isLoadingPlans = MutableStateFlow(false)
    val isLoadingPlans: StateFlow<Boolean> = _isLoadingPlans.asStateFlow()

    // Selected plan
    private val _selectedPlan = MutableStateFlow<PlanItem>(DEFAULT_PLANS[1]) // Default 7 Days Popular
    val selectedPlan: StateFlow<PlanItem> = _selectedPlan.asStateFlow()

    // Payment Config
    private val _paymentConfig = MutableStateFlow(PaymentConfig())
    val paymentConfig: StateFlow<PaymentConfig> = _paymentConfig.asStateFlow()

    // Timer (in seconds, starts at 300 = 5 minutes)
    private val _remainingSeconds = MutableStateFlow(300)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isSessionExpired = MutableStateFlow(false)
    val isSessionExpired: StateFlow<Boolean> = _isSessionExpired.asStateFlow()

    private var timerJob: Job? = null

    // Transaction ID & Amount inputs
    private val _transactionId = MutableStateFlow("")
    val transactionId: StateFlow<String> = _transactionId.asStateFlow()

    private val _transactionIdError = MutableStateFlow<String?>(null)
    val transactionIdError: StateFlow<String?> = _transactionIdError.asStateFlow()

    private val _confirmedAmount = MutableStateFlow("129")
    val confirmedAmount: StateFlow<String> = _confirmedAmount.asStateFlow()

    // Submission state
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // Existing payment record
    private val _existingPayment = MutableStateFlow<PaymentRecord?>(null)
    val existingPayment: StateFlow<PaymentRecord?> = _existingPayment.asStateFlow()

    private val _isCheckingStatus = MutableStateFlow(false)
    val isCheckingStatus: StateFlow<Boolean> = _isCheckingStatus.asStateFlow()

    // Events
    private val _events = MutableSharedFlow<PaymentNavigationEvent>()
    val events: SharedFlow<PaymentNavigationEvent> = _events.asSharedFlow()

    private var autoPollJob: Job? = null

    init {
        startCountdownTimer()
        loadPaymentConfigAndPlans()
        checkExistingPayment()
    }

    fun selectPlan(plan: PlanItem) {
        _selectedPlan.value = plan
        _confirmedAmount.value = plan.price.toString()
    }

    fun onTransactionIdChange(value: String) {
        if (value.length <= 30) {
            _transactionId.value = value.trim()
            if (_transactionIdError.value != null && value.trim().length >= 8) {
                _transactionIdError.value = null
            }
        }
    }

    fun onAmountChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _confirmedAmount.value = digitsOnly
    }

    fun startCountdownTimer() {
        timerJob?.cancel()
        _remainingSeconds.value = 300
        _isSessionExpired.value = false

        timerJob = viewModelScope.launch {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0) {
                _isSessionExpired.value = true
            }
        }
    }

    fun refreshSession() {
        startCountdownTimer()
    }

    private fun loadPaymentConfigAndPlans() {
        viewModelScope.launch {
            _isLoadingPlans.value = true
            try {
                val db = firestore
                if (db != null) {
                    // Fetch payment config
                    val payDoc = db.collection("config").document("payment").get().await()
                    if (payDoc.exists()) {
                        val upiId = payDoc.getString("upiId") ?: "gpay-11189725657@okaxis"
                        val upiName = payDoc.getString("upiName") ?: "RideMarter Admin"
                        val qrImageUrl = payDoc.getString("qrImageUrl") ?: ""
                        _paymentConfig.value = PaymentConfig(upiId, upiName, qrImageUrl)
                    }

                    // Fetch plans
                    val planDoc = db.collection("config").document("plans").get().await()
                    if (planDoc.exists()) {
                        val plansMap = planDoc.get("items") as? List<Map<String, Any>>
                        if (plansMap != null && plansMap.isNotEmpty()) {
                            val parsedPlans = plansMap.mapIndexed { index, map ->
                                PlanItem(
                                    id = map["id"] as? String ?: "plan_$index",
                                    name = map["name"] as? String ?: "Plan",
                                    durationText = map["durationText"] as? String ?: "Days",
                                    durationDays = (map["durationDays"] as? Number)?.toInt() ?: 7,
                                    price = (map["price"] as? Number)?.toInt() ?: 129,
                                    isPopular = map["isPopular"] as? Boolean ?: (index == 1),
                                    isBestValue = map["isBestValue"] as? Boolean ?: (index == 3)
                                )
                            }
                            _plans.value = parsedPlans
                            _selectedPlan.value = parsedPlans.find { it.isPopular } ?: parsedPlans.first()
                            _confirmedAmount.value = _selectedPlan.value.price.toString()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("PaymentViewModel", "Using default payment/plan configs: ${e.message}")
            } finally {
                _isLoadingPlans.value = false
            }
        }
    }

    fun submitPayment() {
        val txId = _transactionId.value.trim()
        if (txId.length < 8) {
            _transactionIdError.value = "Please enter a valid Transaction ID (min 8 characters)"
            return
        }

        val amountVal = _confirmedAmount.value.toIntOrNull() ?: _selectedPlan.value.price
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = currentFirebaseUser?.uid

        if (uid.isNullOrBlank()) {
            _transactionIdError.value = "Driver authentication required. Please sign in again."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                // Fetch driver details from user document
                var name = currentFirebaseUser.displayName ?: "Driver Partner"
                var mobile = ""
                var userId = "SD" + uid.take(8).uppercase()

                val db = firestore
                if (db != null) {
                    try {
                        val userDoc = db.collection("users").document(uid).get().await()
                        if (userDoc.exists()) {
                            name = userDoc.getString("name") ?: name
                            mobile = userDoc.getString("mobile") ?: mobile
                            userId = userDoc.getString("userId") ?: userId
                        }
                    } catch (e: Exception) {
                        Log.w("PaymentViewModel", "Failed to fetch user doc: ${e.message}")
                    }
                }

                val planSelected = "${_selectedPlan.value.name} (${_selectedPlan.value.durationText})"
                val durationDays = _selectedPlan.value.durationDays
                val screenshotUrl = ""

                // 3. Create document in payments/{automaticDocumentId} with exact required fields:
                // uid, name, mobile, planSelected, amount, durationDays, transactionId, submittedAt (server timestamp), status ("pending"), screenshotUrl
                val paymentMap = hashMapOf<String, Any?>(
                    "uid" to uid,
                    "name" to name,
                    "mobile" to mobile,
                    "planSelected" to planSelected,
                    "amount" to amountVal,
                    "durationDays" to durationDays,
                    "transactionId" to txId,
                    "submittedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "status" to "pending",
                    "screenshotUrl" to screenshotUrl
                )

                var docId = ""
                if (db != null) {
                    val docRef = db.collection("payments").add(paymentMap).await()
                    docId = docRef.id

                    // Update user's paymentStatus in users/{uid}
                    try {
                        db.collection("users").document(uid).update(
                            mapOf(
                                "paymentStatus" to "pending",
                                "planName" to planSelected
                            )
                        ).await()
                    } catch (e: Exception) {
                        Log.w("PaymentViewModel", "Could not update user paymentStatus: ${e.message}")
                    }
                }

                val paymentRecord = PaymentRecord(
                    id = docId,
                    uid = uid,
                    userId = userId,
                    name = name,
                    mobile = mobile,
                    planSelected = planSelected,
                    durationDays = durationDays,
                    amount = amountVal,
                    transactionId = txId,
                    status = "pending",
                    screenshotUrl = screenshotUrl,
                    submittedAt = Timestamp.now()
                )
                _existingPayment.value = paymentRecord
                _events.emit(PaymentNavigationEvent.NavigateToPending)
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error submitting payment: ${e.message}", e)
                _events.emit(PaymentNavigationEvent.ShowToast("Payment submission error: ${e.localizedMessage ?: e.message}"))
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun checkExistingPayment() {
        val uid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        } ?: return

        viewModelScope.launch {
            _isCheckingStatus.value = true
            try {
                val db = firestore
                if (db != null) {
                    val querySnap = db.collection("payments")
                        .whereEqualTo("uid", uid)
                        .get()
                        .await()

                    if (!querySnap.isEmpty) {
                        val latestDoc = querySnap.documents.maxByOrNull {
                            it.getTimestamp("submittedAt")?.seconds ?: 0L
                        }
                        if (latestDoc != null) {
                            val record = PaymentRecord(
                                id = latestDoc.id,
                                uid = latestDoc.getString("uid") ?: uid,
                                userId = latestDoc.getString("userId") ?: ("SD" + uid.take(8).uppercase()),
                                name = latestDoc.getString("name") ?: "",
                                mobile = latestDoc.getString("mobile") ?: "",
                                planSelected = latestDoc.getString("planSelected") ?: "",
                                durationDays = (latestDoc.get("durationDays") as? Number)?.toInt() ?: 0,
                                amount = (latestDoc.get("amount") as? Number)?.toInt() ?: 0,
                                transactionId = latestDoc.getString("transactionId") ?: "",
                                status = latestDoc.getString("status") ?: "pending",
                                screenshotUrl = latestDoc.getString("screenshotUrl") ?: "",
                                submittedAt = latestDoc.getTimestamp("submittedAt") ?: Timestamp.now(),
                                approvedAt = latestDoc.getTimestamp("approvedAt"),
                                adminNote = latestDoc.getString("adminNote") ?: ""
                            )
                            _existingPayment.value = record
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("PaymentViewModel", "Error checking existing payment: ${e.message}")
            } finally {
                _isCheckingStatus.value = false
            }
        }
    }

    fun checkPaymentStatusManual() {
        val uid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        }

        if (uid.isNullOrBlank()) {
            viewModelScope.launch {
                _events.emit(PaymentNavigationEvent.ShowToast("Driver session not found. Please log in."))
            }
            return
        }

        viewModelScope.launch {
            _isCheckingStatus.value = true
            try {
                val db = firestore
                if (db != null) {
                    // Check user document first for planStatus or paymentStatus
                    val userDoc = db.collection("users").document(uid).get().await()
                    val userPlanStatus = userDoc.getString("planStatus") ?: "none"
                    val userPayStatus = userDoc.getString("paymentStatus") ?: "none"

                    if (userPlanStatus.equals("active", ignoreCase = true) || userPayStatus.equals("approved", ignoreCase = true)) {
                        _events.emit(PaymentNavigationEvent.NavigateToDashboard)
                        return@launch
                    }

                    // Check payments collection
                    val querySnap = db.collection("payments")
                        .whereEqualTo("uid", uid)
                        .get()
                        .await()

                    if (!querySnap.isEmpty) {
                        val latestDoc = querySnap.documents.maxByOrNull {
                            it.getTimestamp("submittedAt")?.seconds ?: 0L
                        }
                        val status = latestDoc?.getString("status") ?: "pending"
                        val adminNote = latestDoc?.getString("adminNote") ?: ""

                        when (status.lowercase()) {
                            "approved" -> {
                                _events.emit(PaymentNavigationEvent.NavigateToDashboard)
                            }
                            "rejected" -> {
                                _events.emit(
                                    PaymentNavigationEvent.ShowDialog(
                                        title = "Payment Rejected",
                                        message = if (adminNote.isNotBlank()) "Admin note: $adminNote. Please verify details and resubmit."
                                        else "Your payment could not be verified. Please enter correct transaction details or contact support."
                                    )
                                )
                            }
                            else -> {
                                _events.emit(
                                    PaymentNavigationEvent.ShowDialog(
                                        title = "Payment Under Review",
                                        message = "Your payment is currently being verified by admin. This usually takes 10 to 60 minutes. Please check back shortly."
                                    )
                                )
                            }
                        }
                    } else {
                        _events.emit(PaymentNavigationEvent.ShowToast("No payment record found. Please submit your payment details."))
                    }
                } else {
                    _events.emit(PaymentNavigationEvent.ShowToast("Unable to connect to database."))
                }
            } catch (e: Exception) {
                _events.emit(PaymentNavigationEvent.ShowToast("Unable to check status right now: ${e.message}"))
            } finally {
                _isCheckingStatus.value = false
            }
        }
    }

    fun startAutoPolling(onApproved: () -> Unit) {
        autoPollJob?.cancel()
        val uid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        } ?: return

        autoPollJob = viewModelScope.launch {
            while (isActive) {
                delay(20_000) // Poll every 20 seconds
                try {
                    val db = firestore
                    if (db != null) {
                        // Check user doc
                        val userDoc = db.collection("users").document(uid).get().await()
                        val planStatus = userDoc.getString("planStatus") ?: "none"
                        val paymentStatus = userDoc.getString("paymentStatus") ?: "none"

                        if (planStatus.equals("active", ignoreCase = true) || paymentStatus.equals("approved", ignoreCase = true)) {
                            onApproved()
                            break
                        }

                        // Check payments collection
                        val querySnap = db.collection("payments")
                            .whereEqualTo("uid", uid)
                            .get()
                            .await()

                        val latestDoc = querySnap.documents.maxByOrNull {
                            it.getTimestamp("submittedAt")?.seconds ?: 0L
                        }
                        if (latestDoc != null) {
                            val status = latestDoc.getString("status") ?: "pending"
                            if (status.equals("approved", ignoreCase = true)) {
                                onApproved()
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("PaymentViewModel", "Auto poll error: ${e.message}")
                }
            }
        }
    }

    fun stopAutoPolling() {
        autoPollJob?.cancel()
        autoPollJob = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        autoPollJob?.cancel()
    }
}
