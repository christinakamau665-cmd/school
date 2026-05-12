package com.emobilis.app.data.model
data class Attendance(
    val id: String = "",
    val studentUid: String = "",
    val studentName: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "present",
    val signedAtSchool: Boolean = false
)
