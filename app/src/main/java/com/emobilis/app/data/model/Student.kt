package com.emobilis.app.data.model

data class Student(
    val uid:                String = "",
    val fullName:           String = "",
    val email:              String = "",
    val phone:              String = "",
    val course:             String = "",
    val computerNumber:     String = "",
    val laboratory:         String = "",
    val role:               String = "student",
    val registrationNumber: String = "",
    val feesBalance:        Double = 0.0,
    val photoUrl:           String = ""   // Firebase Storage URL
)
