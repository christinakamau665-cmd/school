package com.emobilis.app.data.model

data class ComputerAlert(
    val id: String = "",
    val studentName: String = "",
    val studentUid: String = "",
    val computerNumber: String = "",
    val laboratory: String = "",
    val issue: String = "",
    val timestamp: Long = 0L,
    val resolved: Boolean = false
)
