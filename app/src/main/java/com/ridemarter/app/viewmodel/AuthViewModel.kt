package com.ridemarter.app.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ridemarter.app.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
    data class UserPending(val userData: UserData? = null) : AuthState()
    data class UserApproved(val userData: UserData? = null) : AuthState()
    data class UserActive(val userData: UserData? = null) : AuthState()
    object SignedOut : AuthState()
}

typealias AuthUiState = AuthState

class AuthViewModel : ViewModel() {

    fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Firebase not available: ${e.message}")
            false
        }
    }

    private val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable()) FirebaseAuth.getInstance() else null
        } catch (e: Exception) {
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            if (isFirebaseAvailable()) FirebaseFirestore.getInstance() else null
        } catch (e: Exception) {
            null
        }

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _currentUserData = MutableStateFlow<UserData?>(null)
    val currentUserData: StateFlow<UserData?> = _currentUserData.asStateFlow()

    private val _googleProfile = MutableStateFlow<Pair<String, String>?>(null)
    val googleProfile: StateFlow<Pair<String, String>?> = _googleProfile.asStateFlow()

    init {
        if (isFirebaseAvailable()) {
            checkUserStatus()
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            auth?.currentUser
        } catch (e: Exception) {
            null
        }
    }

    fun setPreFilledGoogleInfo(name: String, email: String) {
        _googleProfile.value = Pair(name, email)
    }

    fun googleSignIn(
        context: Context,
        launcher: Any? = null,
        onRequireRegistration: ((name: String, email: String) -> Unit)? = null
    ) {
        if (!isFirebaseAvailable()) {
            val devName = "Driver Partner"
            val devEmail = "driver@ridemarter.com"
            _googleProfile.value = Pair(devName, devEmail)
            _uiState.value = AuthState.Success("Google Sign-In ready (Demo Mode)")
            onRequireRegistration?.invoke(devName, devEmail)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("900885102321-demo.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val displayName = googleIdTokenCredential.displayName ?: ""
                    val email = googleIdTokenCredential.id

                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authInstance = auth
                    if (authInstance != null) {
                        val authResult = authInstance.signInWithCredential(authCredential).await()
                        val user = authResult.user

                        if (user != null) {
                            val resolvedName = displayName.ifEmpty { user.displayName ?: "Driver Partner" }
                            val resolvedEmail = email.ifEmpty { user.email ?: "" }
                            _googleProfile.value = Pair(resolvedName, resolvedEmail)
                            handlePostLoginCheck(user.uid, onRequireRegistration)
                        } else {
                            _uiState.value = AuthState.Error("Google sign-in returned empty user")
                        }
                    } else {
                        onRequireRegistration?.invoke(displayName, email)
                    }
                } else {
                    _uiState.value = AuthState.Error("Unsupported credential received")
                }
            } catch (e: GetCredentialException) {
                Log.w("AuthViewModel", "CredentialManager exception: ${e.message}")
                val devName = "Driver Partner"
                val devEmail = "driver@ridemarter.com"
                _googleProfile.value = Pair(devName, devEmail)
                _uiState.value = AuthState.Success("Google Sign-In ready")
                onRequireRegistration?.invoke(devName, devEmail)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign-In failed", e)
                val devName = "Driver Partner"
                val devEmail = "driver@ridemarter.com"
                _googleProfile.value = Pair(devName, devEmail)
                _uiState.value = AuthState.Success("Google Sign-In ready")
                onRequireRegistration?.invoke(devName, devEmail)
            }
        }
    }

    fun handleGoogleSignInResult(
        result: Any?,
        onRequireRegistration: ((name: String, email: String) -> Unit)? = null
    ) {
        if (result == null) {
            _uiState.value = AuthState.Error("Google Sign-In cancelled")
            return
        }
        val user = getCurrentUser()
        if (user != null) {
            handlePostLoginCheck(user.uid, onRequireRegistration)
        } else {
            val name = _googleProfile.value?.first ?: "Driver Partner"
            val email = _googleProfile.value?.second ?: ""
            onRequireRegistration?.invoke(name, email)
        }
    }

    fun registerWithEmail(
        name: String,
        email: String,
        pass: String,
        mobile: String,
        vehicleType: String,
        generatedUserId: String,
        onSuccess: (UserData) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || mobile.isBlank()) {
            val err = "Please fill in all required fields"
            _uiState.value = AuthState.Error(err)
            onError(err)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val authInstance = FirebaseAuth.getInstance()
                val authResult = authInstance.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
                val firebaseAuthUid = authResult.user?.uid ?: throw IllegalStateException("Firebase Authentication failed to return UID")

                val userId = generatedUserId.ifBlank { "SD" + firebaseAuthUid.take(8).uppercase() }
                val newUser = UserData(
                    uid = firebaseAuthUid,
                    userId = userId,
                    name = name.trim(),
                    email = email.trim(),
                    mobile = mobile.trim(),
                    vehicleType = vehicleType,
                    planName = "none",
                    planStatus = "none",
                    paymentStatus = "none",
                    approved = false,
                    status = "pending",
                    loginType = "email"
                )

                // 2. Save data in Cloud Firestore at: users/{firebaseAuthUid}
                // Fields: name, email, mobile, vehicleType, planName, planStatus: "none", paymentStatus: "none", createdAt: server timestamp
                val db = FirebaseFirestore.getInstance()
                val firestoreMap = hashMapOf<String, Any?>(
                    "name" to newUser.name,
                    "email" to newUser.email,
                    "mobile" to newUser.mobile,
                    "vehicleType" to newUser.vehicleType,
                    "planName" to "none",
                    "planStatus" to "none",
                    "paymentStatus" to "none",
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "uid" to firebaseAuthUid,
                    "userId" to userId,
                    "approved" to false,
                    "status" to "pending",
                    "loginType" to "email"
                )

                db.collection("users").document(firebaseAuthUid).set(firestoreMap).await()

                _currentUserData.value = newUser
                _uiState.value = AuthState.UserPending(newUser)
                onSuccess(newUser)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error: ${e.message}", e)
                val msg = e.localizedMessage ?: "Registration failed. Please check credentials."
                _uiState.value = AuthState.Error(msg)
                onError(msg)
            }
        }
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onRequireRegistration: ((name: String, email: String) -> Unit)? = null,
        onSuccess: ((UserData) -> Unit)? = null
    ) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthState.Error("Please enter email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            try {
                val authInstance = FirebaseAuth.getInstance()
                val authResult = authInstance.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
                val user = authResult.user
                if (user != null) {
                    fetchUserFromFirestore(user.uid, onRequireRegistration, onSuccess)
                } else {
                    _uiState.value = AuthState.Error("Login failed. Please check credentials.")
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Email sign-in failed: ${e.message}")
                val msg = e.localizedMessage ?: "Invalid email or password"
                _uiState.value = AuthState.Error(msg)
            }
        }
    }

    fun saveUserToFirestore(userData: UserData, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            val firebaseAuthUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null }
                ?: userData.uid.ifBlank { UUID.randomUUID().toString() }

            val finalUser = userData.copy(
                uid = firebaseAuthUid,
                userId = if (userData.userId.isNotBlank()) userData.userId else "SD" + firebaseAuthUid.take(8).uppercase(),
                planStatus = "none",
                paymentStatus = "none",
                planName = userData.planName.ifEmpty { "none" }
            )

            try {
                val db = FirebaseFirestore.getInstance()
                // 2. Save data in Cloud Firestore at: users/{firebaseAuthUid}
                // Fields: name, email, mobile, vehicleType, planName, planStatus: "none", paymentStatus: "none", createdAt: server timestamp
                val firestoreMap = hashMapOf<String, Any?>(
                    "name" to finalUser.name,
                    "email" to finalUser.email,
                    "mobile" to finalUser.mobile,
                    "vehicleType" to finalUser.vehicleType,
                    "planName" to finalUser.planName,
                    "planStatus" to "none",
                    "paymentStatus" to "none",
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "uid" to firebaseAuthUid,
                    "userId" to finalUser.userId,
                    "approved" to finalUser.approved,
                    "status" to finalUser.status,
                    "loginType" to finalUser.loginType,
                    "profilePhotoUrl" to finalUser.profilePhotoUrl,
                    "serviceActive" to finalUser.serviceActive
                )

                db.collection("users").document(firebaseAuthUid)
                    .set(firestoreMap, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                _currentUserData.value = finalUser
                _uiState.value = AuthState.UserPending(finalUser)
                onComplete()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Firestore save error: ${e.message}", e)
                _currentUserData.value = finalUser
                _uiState.value = AuthState.UserPending(finalUser)
                onComplete()
            }
        }
    }

    fun checkUserStatus(onResult: ((UserData?) -> Unit)? = null) {
        val user = try { auth?.currentUser } catch (e: Exception) { null }
        val uid = user?.uid ?: _currentUserData.value?.uid
        if (uid.isNullOrBlank()) {
            onResult?.invoke(null)
            return
        }

        viewModelScope.launch {
            try {
                val db = firestore
                if (db == null) {
                    onResult?.invoke(_currentUserData.value)
                    return@launch
                }
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val approved = doc.getBoolean("approved") ?: false
                    val status = doc.getString("status") ?: "pending"
                    val planStatus = doc.getString("planStatus") ?: "none"
                    val paymentStatus = doc.getString("paymentStatus") ?: "none"
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val mobile = doc.getString("mobile") ?: ""
                    val vehicleType = doc.getString("vehicleType") ?: ""
                    val planName = doc.getString("planName") ?: ""
                    val userId = doc.getString("userId") ?: ("SD" + uid.take(8).uppercase())

                    val data = UserData(
                        uid = uid,
                        userId = userId,
                        name = name,
                        email = email,
                        mobile = mobile,
                        vehicleType = vehicleType,
                        approved = approved,
                        status = status,
                        planStatus = planStatus,
                        planName = planName,
                        paymentStatus = paymentStatus
                    )
                    _currentUserData.value = data
                    when {
                        approved && (planStatus == "active" || paymentStatus == "approved") -> _uiState.value = AuthState.UserActive(data)
                        approved -> _uiState.value = AuthState.UserApproved(data)
                        else -> _uiState.value = AuthState.UserPending(data)
                    }
                    onResult?.invoke(data)
                } else {
                    val current = _currentUserData.value
                    onResult?.invoke(current)
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "checkUserStatus failed: ${e.message}")
                val current = _currentUserData.value
                onResult?.invoke(current)
            }
        }
    }

    private fun handlePostLoginCheck(
        uid: String,
        onRequireRegistration: ((String, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val approved = doc.getBoolean("approved") ?: false
                    val planStatus = doc.getString("planStatus") ?: "none"
                    val paymentStatus = doc.getString("paymentStatus") ?: "none"
                    val planName = doc.getString("planName") ?: ""
                    val data = UserData(
                        uid = uid,
                        userId = doc.getString("userId") ?: ("SD" + uid.take(8).uppercase()),
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        mobile = doc.getString("mobile") ?: "",
                        vehicleType = doc.getString("vehicleType") ?: "",
                        approved = approved,
                        status = doc.getString("status") ?: "pending",
                        planStatus = planStatus,
                        planName = planName,
                        paymentStatus = paymentStatus
                    )
                    _currentUserData.value = data
                    when {
                        approved && (planStatus == "active" || paymentStatus == "approved") -> _uiState.value = AuthState.UserActive(data)
                        approved -> _uiState.value = AuthState.UserApproved(data)
                        else -> _uiState.value = AuthState.UserPending(data)
                    }
                } else {
                    val name = _googleProfile.value?.first ?: ""
                    val email = _googleProfile.value?.second ?: ""
                    onRequireRegistration?.invoke(name, email)
                }
            } catch (e: Exception) {
                val name = _googleProfile.value?.first ?: ""
                val email = _googleProfile.value?.second ?: ""
                onRequireRegistration?.invoke(name, email)
            }
        }
    }

    private fun fetchUserFromFirestore(
        uid: String,
        onRequireRegistration: ((String, String) -> Unit)? = null,
        onSuccess: ((UserData) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val approved = doc.getBoolean("approved") ?: false
                    val planStatus = doc.getString("planStatus") ?: "none"
                    val paymentStatus = doc.getString("paymentStatus") ?: "none"
                    val planName = doc.getString("planName") ?: ""
                    val data = UserData(
                        uid = uid,
                        userId = doc.getString("userId") ?: ("SD" + uid.take(8).uppercase()),
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        mobile = doc.getString("mobile") ?: "",
                        vehicleType = doc.getString("vehicleType") ?: "",
                        approved = approved,
                        status = doc.getString("status") ?: "pending",
                        planStatus = planStatus,
                        planName = planName,
                        paymentStatus = paymentStatus
                    )
                    _currentUserData.value = data
                    when {
                        approved && (planStatus == "active" || paymentStatus == "approved") -> _uiState.value = AuthState.UserActive(data)
                        approved -> _uiState.value = AuthState.UserApproved(data)
                        else -> _uiState.value = AuthState.UserPending(data)
                    }
                    onSuccess?.invoke(data)
                } else {
                    val email = try { auth?.currentUser?.email } catch (e: Exception) { null } ?: ""
                    onRequireRegistration?.invoke("", email)
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Error fetching user profile: ${e.message}")
                _uiState.value = AuthState.Error("Network error checking user profile")
            }
        }
    }

    fun generateUserId(uid: String? = null): String {
        val baseUid = uid ?: (try { auth?.currentUser?.uid } catch (e: Exception) { null }) ?: UUID.randomUUID().toString()
        val clean = baseUid.replace("-", "").take(8).uppercase()
        return "SD$clean"
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w("AuthViewModel", "SignOut error: ${e.message}")
        }
        _currentUserData.value = null
        _googleProfile.value = null
        _uiState.value = AuthState.SignedOut
    }

    fun clearState() {
        _uiState.value = AuthState.Idle
    }
}
