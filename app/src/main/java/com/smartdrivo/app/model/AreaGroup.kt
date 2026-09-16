package com.smartdrivo.app.model

data class AreaGroup(
    val id: String = "",
    val name: String = "",
    val isEnabled: Boolean = true,
    val minPickupKm: Float = 0.5f,
    val maxDropKm: Float = 6.5f,
    val keywords: List<String> = emptyList(),
    val type: String = "GOTO",
    val noGoAction: String = "REJECT"
)
