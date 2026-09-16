package com.smartdrivo.app.model

import com.google.firebase.Timestamp

// Used in Dashboard recent orders list
data class OrderRecord(
    val id: String = "",
    val appName: String = "",
    val fare: Int = 0,
    val pickup: String = "",
    val drop: String = "",
    val distance: String = "",
    val timeAgo: String = "",
    val status: String = ""
)

// Used in History screen full order details
data class OrderHistory(
    val docId: String = "",
    val platform: String = "",
    val vehicleType: String = "",
    val fare: String = "",
    val pickupDistance: String = "",
    val dropDistance: String = "",
    val dropArea: String = "",
    val action: String = "",
    val speed: String = "",
    val timestamp: Timestamp? = null,
    val date: String = "",
    val time: String = ""
)

data class HistoryStats(
    val total: Int = 0,
    val accepted: Int = 0,
    val rejected: Int = 0,
    val ignored: Int = 0
)

enum class DateFilter {
    TODAY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, ALL_TIME
}
