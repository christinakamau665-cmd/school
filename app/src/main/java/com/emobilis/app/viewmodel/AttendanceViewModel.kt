package com.emobilis.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emobilis.app.data.model.Attendance
import com.emobilis.app.data.model.ComputerAlert
import com.emobilis.app.data.model.Student
import com.emobilis.app.data.repository.AttendanceRepository
import com.emobilis.app.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceViewModel(private val context: Context) : ViewModel() {

    private val repo = AttendanceRepository()

    private val _attendanceStatus = MutableStateFlow("")
    val attendanceStatus: StateFlow<String> = _attendanceStatus.asStateFlow()

    private val _attendanceList = MutableStateFlow<List<Attendance>>(emptyList())
    val attendanceList: StateFlow<List<Attendance>> = _attendanceList.asStateFlow()

    private val _alerts = MutableStateFlow<List<ComputerAlert>>(emptyList())
    val alerts: StateFlow<List<ComputerAlert>> = _alerts.asStateFlow()

    private val _isCheckingLocation = MutableStateFlow(false)
    val isCheckingLocation: StateFlow<Boolean> = _isCheckingLocation.asStateFlow()

    fun signAttendance(student: Student) {
        viewModelScope.launch {
            _attendanceStatus.value = "📍 Verifying your location..."
            _isCheckingLocation.value = true

            val atSchool = LocationHelper.isStudentAtSchool(context)
            _isCheckingLocation.value = false

            if (!atSchool) {
                _attendanceStatus.value = "❌ You must be physically at EMOBILIS to sign attendance!"
                return@launch
            }

            val now     = Date()
            val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val shortFmt= SimpleDateFormat("HH:mm", Locale.getDefault())

            val attendance = Attendance(
                studentUid     = student.uid,
                studentName    = student.fullName,
                date           = dateFmt.format(now),
                time           = timeFmt.format(now),
                status         = "present",
                signedAtSchool = true
            )
            val result = repo.signAttendance(attendance)
            _attendanceStatus.value = if (result.isSuccess)
                "✅ Attendance signed at ${shortFmt.format(now)}!"
            else
                "❌ Failed: ${result.exceptionOrNull()?.message}"

            loadAttendance(student.uid)
        }
    }

    fun loadAttendance(uid: String) {
        viewModelScope.launch {
            _attendanceList.value = repo.getStudentAttendance(uid)
        }
    }

    fun sendComputerAlert(student: Student, issue: String) {
        viewModelScope.launch {
            val alert = ComputerAlert(
                studentName    = student.fullName,
                studentUid     = student.uid,
                computerNumber = student.computerNumber,
                laboratory     = student.laboratory,
                issue          = issue,
                timestamp      = System.currentTimeMillis()
            )
            repo.sendComputerAlert(alert)
        }
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _alerts.value = repo.getUnresolvedAlerts()
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AttendanceViewModel(context.applicationContext) as T
            }
    }
}
