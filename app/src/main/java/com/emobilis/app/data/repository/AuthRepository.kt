package com.emobilis.app.data.repository

import com.emobilis.app.data.model.Staff
import com.emobilis.app.data.model.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db:   FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun registerStudent(email: String, password: String, student: Student): Result<String> =
        runCatching {
            val cred  = auth.createUserWithEmailAndPassword(email, password).await()
            val uid   = cred.user!!.uid
            val regNo = "EMO-${System.currentTimeMillis().toString().takeLast(6)}"
            db.collection("students").document(uid)
                .set(student.copy(uid = uid, registrationNumber = regNo)).await()
            uid
        }

    suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        val cred = auth.signInWithEmailAndPassword(email, password).await()
        val uid  = cred.user!!.uid
        val studentSnap = db.collection("students").document(uid).get().await()
        if (studentSnap.exists()) return@runCatching "student"
        val staffSnap = db.collection("staff").document(uid).get().await()
        if (staffSnap.exists()) return@runCatching staffSnap.getString("role") ?: "lecturer"
        throw Exception("Account not found. Please contact administration.")
    }

    suspend fun getStudentProfile(uid: String): Student? = runCatching {
        db.collection("students").document(uid).get().await().toObject(Student::class.java)
    }.getOrNull()

    suspend fun getRoleForUid(uid: String): String = runCatching {
        val staffSnap = db.collection("staff").document(uid).get().await()
        if (staffSnap.exists()) staffSnap.getString("role") ?: "lecturer" else "student"
    }.getOrDefault("student")

    /** Lecturer adds another lecturer (creates Firebase Auth + Firestore record) */
    suspend fun addLecturer(staff: Staff): Result<String> = runCatching {
        val tempPassword = "Emobilis@${System.currentTimeMillis().toString().takeLast(4)}"
        val cred = auth.createUserWithEmailAndPassword(staff.email,
            staff.password.ifBlank { tempPassword }).await()
        val uid = cred.user!!.uid
        db.collection("staff").document(uid).set(staff.copy(uid = uid, password = "")).await()
        uid
    }

    /** Lecturer/admin deletes a student from Firestore (Auth deletion needs Admin SDK server-side) */
    suspend fun deleteStudent(uid: String): Result<Unit> = runCatching {
        db.collection("students").document(uid).delete().await()
        // Also remove their attendance records
        val attSnap = db.collection("attendance").whereEqualTo("studentUid", uid).get().await()
        attSnap.documents.forEach { it.reference.delete() }
    }

    /** Get all students for lecturer view */
    suspend fun getAllStudents(): List<Student> = runCatching {
        db.collection("students").get().await().toObjects(Student::class.java)
    }.getOrDefault(emptyList())

    fun getCurrentUid(): String? = auth.currentUser?.uid
    fun isLoggedIn(): Boolean    = auth.currentUser != null
    fun signOut()                = auth.signOut()
}
