package com.ridemarter.app.model

import com.google.firebase.Timestamp

data class UserData(
    val uid: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val mobileVerified: Boolean = false,
    val vehicleType: String = "AUTO",
    val approved: Boolean = false,
    val status: String = "pending",
    val planStatus: String = "none",
    val planName: String = "",
    val planExpiry: Timestamp? = null,
    val planActivatedAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val loginType: String = "google",
    val profilePhotoUrl: String = "",
    val serviceActive: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "userId" to userId,
        "name" to name,
        "email" to email,
        "mobile" to mobile,
        "mobileVerified" to mobileVerified,
        "vehicleType" to vehicleType,
        "approved" to approved,
        "status" to status,
        "planStatus" to planStatus,
        "planName" to planName,
        "planExpiry" to planExpiry,
        "planActivatedAt" to planActivatedAt,
        "createdAt" to (createdAt ?: Timestamp.now()),
        "loginType" to loginType,
        "profilePhotoUrl" to profilePhotoUrl,
        "serviceActive" to serviceActive
    )
}
