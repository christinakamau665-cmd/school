package com.emobilis.app.data.model

data class Staff(
    val uid:        String = "",
    val fullName:   String = "",
    val email:      String = "",
    val phone:      String = "",
    val role:       String = "lecturer",  // "lecturer" | "lab_technician"
    val department: String = "",
    val password:   String = ""           // plain only for initial creation; Firebase Auth handles real auth
)
