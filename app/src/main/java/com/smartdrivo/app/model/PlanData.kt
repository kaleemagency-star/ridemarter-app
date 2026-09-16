package com.smartdrivo.app.model

import com.google.firebase.Timestamp

data class PlanItem(
    val id: String = "",
    val name: String = "",
    val durationText: String = "",
    val durationDays: Int = 0,
    val price: Int = 0,
    val isPopular: Boolean = false,
    val isBestValue: Boolean = false,
    val features: List<String> = listOf(
        "Auto Accept on Rapido, Uber, Ola",
        "Unlimited Area Groups",
        "Full Order History",
        "Priority Support"
    )
)

data class PaymentConfig(
    val upiId: String = "gpay-11189725657@okaxis",
    val upiName: String = "SmartDrivo Admin",
    val qrImageUrl: String = ""
)

data class PaymentRecord(
    val uid: String = "",
    val userId: String = "",
    val name: String = "",
    val mobile: String = "",
    val planSelected: String = "",
    val durationDays: Int = 0,
    val amount: Int = 0,
    val transactionId: String = "",
    val status: String = "pending",
    val submittedAt: Timestamp? = null,
    val approvedAt: Timestamp? = null,
    val adminNote: String = ""
)

val DEFAULT_PLANS = listOf(
    PlanItem(
        id = "plan_3days",
        name = "Starter",
        durationText = "3 Days",
        durationDays = 3,
        price = 59
    ),
    PlanItem(
        id = "plan_7days",
        name = "Popular",
        durationText = "7 Days",
        durationDays = 7,
        price = 129,
        isPopular = true
    ),
    PlanItem(
        id = "plan_15days",
        name = "Value",
        durationText = "15 Days",
        durationDays = 15,
        price = 199
    ),
    PlanItem(
        id = "plan_1month",
        name = "Best Deal",
        durationText = "1 Month",
        durationDays = 30,
        price = 329,
        isBestValue = true
    )
)
