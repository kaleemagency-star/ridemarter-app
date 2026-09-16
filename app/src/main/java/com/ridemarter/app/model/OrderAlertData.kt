package com.ridemarter.app.model

data class OrderAlertData(
    val platform: String = "",
    val fare: String = "",
    val pickupDistance: String = "",
    val dropDistance: String = "",
    val dropArea: String = "",
    val dropAddress: String = "",
    val action: String = "Accepted",
    val speed: String = "10ms",
    val timestamp: Long = System.currentTimeMillis()
)
