package com.emobilis.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emobilis.app.data.model.Staff
import com.emobilis.app.data.model.Student
import com.emobilis.app.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentStudent = MutableStateFlow<Student?>(null)
    val currentStudent: StateFlow<Student?> = _currentStudent.asStateFlow()

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    val allStudents: StateFlow<List<Student>> = _allStudents.asStateFlow()

    private val _operationMessage = MutableStateFlow("")
    val operationMessage: StateFlow<String> = _operationMessage.asStateFlow()

    init { if (repo.isLoggedIn()) loadCurrentStudent() }

    fun register(student: Student, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.registerStudent(email = student.email, password = password, student = student)
            _authState.value = if (result.isSuccess) AuthState.Success("student")
            else AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.signIn(email, password)
            if (result.isSuccess) {
                val role = result.getOrDefault("student")
                _authState.value = AuthState.Success(role)
                if (role == "student") loadCurrentStudent()
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed. Check your credentials.")
            }
        }
    }

    /** Called after successful biometric verification — re-uses existing Firebase session */
    fun loginWithBiometric() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                _authState.value = AuthState.Error("Session expired. Please sign in with email.")
                return@launch
            }
            _authState.value = AuthState.Loading
            val student = repo.getStudentProfile(uid)
            if (student != null) {
                _currentStudent.value = student
                _authState.value = AuthState.Success("student")
            } else {
                val role = repo.getRoleForUid(uid)
                _authState.value = AuthState.Success(role)
            }
        }
    }

    /** Lecturer: add a new lecturer account */
    fun addLecturer(staff: Staff) {
        viewModelScope.launch {
            _operationMessage.value = "Adding lecturer..."
            val result = repo.addLecturer(staff)
            _operationMessage.value = if (result.isSuccess)
                "✅ Lecturer ${staff.fullName} added successfully!"
            else "❌ Failed: ${result.exceptionOrNull()?.message}"
        }
    }

    /** Lecturer: delete a student */
    fun deleteStudent(uid: String, name: String) {
        viewModelScope.launch {
            _operationMessage.value = "Deleting student..."
            val result = repo.deleteStudent(uid)
            _operationMessage.value = if (result.isSuccess)
                "✅ Student $name removed."
            else "❌ Failed: ${result.exceptionOrNull()?.message}"
            if (result.isSuccess) loadAllStudents()
        }
    }

    fun loadAllStudents() {
        viewModelScope.launch { _allStudents.value = repo.getAllStudents() }
    }

    fun loadCurrentStudent() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            _currentStudent.value = repo.getStudentProfile(uid)
        }
    }

    fun isAlreadyLoggedIn(): Boolean = repo.isLoggedIn()
    fun signOut() { repo.signOut(); _authState.value = AuthState.Idle; _currentStudent.value = null }
    fun resetState() { _authState.value = AuthState.Idle }
    fun clearMessage() { _operationMessage.value = "" }
}
