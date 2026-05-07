package com.emobilis.app

object AppConstants {
    // ⚠️ UPDATE THESE with actual Emobilis campus GPS coordinates before deployment
    const val SCHOOL_LATITUDE = -1.26482
    const val SCHOOL_LONGITUDE = 36.79185
    const val SCHOOL_RADIUS_METERS = 20000.0

    val COURSES = listOf(
        "Cyber Security",
        "Full Stack Development",
        "Software Development",
        "MIT (Management Information Technology)",
        "Data Science",
        "Cloud Computing",
        "Networking & IT Support",
        "Other IT Course"
    )

    val LABORATORIES = listOf(
        "Opera Lab",
        "Safari Lab",
        "Google Lab",
        "Microsoft Lab",
        "Innovation Lab"
    )

    val COMPUTER_NUMBERS = (1..40).map { "PC-$it" }

    val ROLES = listOf("student", "lecturer", "lab_technician", "admin")
}