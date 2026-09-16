package com.ridemarter.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ridemarter.app.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    constructor() : this(Application())

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _planDaysRemaining = MutableStateFlow(0)
    val planDaysRemaining: StateFlow<Int> = _planDaysRemaining.asStateFlow()

    private val _planExpiryFormatted = MutableStateFlow("")
    val planExpiryFormatted: StateFlow<String> = _planExpiryFormatted.asStateFlow()

    private val _isPlanExpired = MutableStateFlow(false)
    val isPlanExpired: StateFlow<Boolean> = _isPlanExpired.asStateFlow()

    private val _isPlanExpiringSoon = MutableStateFlow(false)
    val isPlanExpiringSoon: StateFlow<Boolean> = _isPlanExpiringSoon.asStateFlow()

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

    private var userListener: ListenerRegistration? = null

    init {
        loadUserProfile()
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        userListener = null
    }

    fun loadUserProfile() {
        val uid = try {
            auth?.currentUser?.uid
        } catch (e: Exception) {
            null
        }

        if (uid == null || db == null) {
            _userData.value = getDemoUserData()
            calculatePlanStatus()
            return
        }

        _isLoading.value = true

        try {
            userListener = db.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        Log.w("ProfileVM", "Snapshot listener error or missing: ${error?.message}")
                        if (_userData.value == null) {
                            _userData.value = getDemoUserData()
                            calculatePlanStatus()
                        }
                        return@addSnapshotListener
                    }

                    val user = try {
                        snapshot.toObject(UserData::class.java)
                    } catch (e: Exception) {
                        null
                    }

                    _userData.value = user ?: getDemoUserData()
                    calculatePlanStatus()
                }
        } catch (e: Exception) {
            Log.w("ProfileVM", "loadUserProfile error: ${e.message}")
            _isLoading.value = false
            _userData.value = getDemoUserData()
            calculatePlanStatus()
        }
    }

    fun updateMobile(newMobile: String, onResult: (Boolean) -> Unit = {}) {
        val clean = newMobile.filter { it.isDigit() }.take(10)
        if (clean.length != 10) {
            onResult(false)
            return
        }

        _userData.value = _userData.value?.copy(mobile = clean)

        val uid = try {
            auth?.currentUser?.uid
        } catch (e: Exception) {
            null
        }

        if (uid != null && db != null) {
            db.collection("users").document(uid).update("mobile", clean)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        } else {
            onResult(true)
        }
    }

    private fun getDemoUserData(): UserData {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }
        return UserData(
            uid = "demo_uid",
            userId = "SD12345678",
            name = "Ravi Kumar",
            email = "driver@ridemarter.com",
            mobile = "9876543210",
            vehicleType = "AUTO",
            approved = true,
            status = "approved",
            planStatus = "active",
            planName = "Popular (7 Days)",
            planExpiry = Timestamp(cal.time),
            loginType = "google",
            profilePhotoUrl = ""
        )
    }

    fun calculatePlanStatus() {
        val expiry = _userData.value?.planExpiry
        if (expiry == null) {
            _planDaysRemaining.value = 0
            _isPlanExpired.value = true
            _isPlanExpiringSoon.value = false
            _planExpiryFormatted.value = "No Active Plan"
            return
        }

        val now = Date()
        val expiryDate = expiry.toDate()
        val diffMs = expiryDate.time - now.time
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

        _planDaysRemaining.value = maxOf(0, diffDays)
        _isPlanExpired.value = diffDays < 0
        _isPlanExpiringSoon.value = diffDays in 0..2

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        _planExpiryFormatted.value = sdf.format(expiryDate)
    }

    fun signOut(onSignedOut: () -> Unit) {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w("ProfileVM", "signOut error: ${e.message}")
        }
        onSignedOut()
    }
}
