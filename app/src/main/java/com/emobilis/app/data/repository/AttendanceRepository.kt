package com.emobilis.app.data.repository

import com.emobilis.app.data.model.Attendance
import com.emobilis.app.data.model.ComputerAlert
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AttendanceRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun signAttendance(attendance: Attendance): Result<Unit> = runCatching {
        // Prevent duplicate sign-in for same day
        val existing = db.collection("attendance")
            .whereEqualTo("studentUid", attendance.studentUid)
            .whereEqualTo("date", attendance.date)
            .get().await()
        if (existing.isEmpty) {
            db.collection("attendance").add(attendance).await()
        }
    }

    suspend fun getStudentAttendance(uid: String): List<Attendance> = runCatching {
        db.collection("attendance")
            .whereEqualTo("studentUid", uid)
            .get().await()
            .toObjects(Attendance::class.java)
            .sortedByDescending { it.date }
    }.getOrDefault(emptyList())

    /** Lecturer: get all attendance records for a specific date */
    suspend fun getAttendanceByDate(date: String): List<Attendance> = runCatching {
        db.collection("attendance")
            .whereEqualTo("date", date)
            .get().await()
            .toObjects(Attendance::class.java)
    }.getOrDefault(emptyList())

    suspend fun sendComputerAlert(alert: ComputerAlert): Result<Unit> = runCatching {
        db.collection("computer_alerts").add(alert).await()
    }

    suspend fun getUnresolvedAlerts(): List<ComputerAlert> = runCatching {
        db.collection("computer_alerts")
            .whereEqualTo("resolved", false)
            .get().await()
            .toObjects(ComputerAlert::class.java)
    }.getOrDefault(emptyList())
}
