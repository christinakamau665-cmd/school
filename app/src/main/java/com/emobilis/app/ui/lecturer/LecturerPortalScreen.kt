package com.emobilis.app.ui.lecturer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.emobilis.app.data.model.Attendance
import com.emobilis.app.data.model.Message
import com.emobilis.app.data.model.Staff
import com.emobilis.app.data.model.Student
import com.emobilis.app.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

val LecturerGreen = Color(0xFF1B5E20)
val LecturerPurple = Color(0xFF6D1B6D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerPortalScreen(onLogout: () -> Unit, authVm: AuthViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Students", "Attendance", "Messages", "Manage")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lecturer Portal", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = { authVm.signOut(); onLogout() }) { Icon(Icons.Default.Logout, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LecturerPurple, titleContentColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, title ->
                    NavigationBarItem(selected = selectedTab == i, onClick = { selectedTab = i },
                        icon = {
                            Icon(when (i) {
                                0 -> Icons.Default.Dashboard; 1 -> Icons.Default.People
                                2 -> Icons.Default.HowToReg; 3 -> Icons.Default.Message
                                else -> Icons.Default.ManageAccounts
                            }, null)
                        },
                        label = { Text(title, fontSize = 9.sp) })
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> LecturerDashboard()
                1 -> StudentListTab(authVm)
                2 -> AttendanceViewTab()
                3 -> LecturerMessagesTab()
                4 -> ManageTab(authVm)
            }
        }
    }
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
@Composable
fun LecturerDashboard() {
    val db = FirebaseFirestore.getInstance()
    var studentCount  by remember { mutableIntStateOf(0) }
    var presentToday  by remember { mutableIntStateOf(0) }
    var pendingAlerts by remember { mutableIntStateOf(0) }
    var reminderSent  by remember { mutableStateOf(false) }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    LaunchedEffect(Unit) {
        db.collection("students").get().addOnSuccessListener { studentCount = it.size() }
        db.collection("attendance").whereEqualTo("date", today).get().addOnSuccessListener { presentToday = it.size() }
        db.collection("computer_alerts").whereEqualTo("resolved", false).get().addOnSuccessListener { pendingAlerts = it.size() }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LecturerPurple)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Welcome, Lecturer 👋", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("EMOBILIS School of Technology", color = Color.White.copy(0.8f))
                    Text(SimpleDateFormat("EEEE, MMM d yyyy", Locale.getDefault()).format(Date()), color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LStatCard("Total Students", "$studentCount", Icons.Default.People, LecturerPurple, Modifier.weight(1f))
                LStatCard("Present Today", "$presentToday", Icons.Default.CheckCircle, Color(0xFF2E7D32), Modifier.weight(1f))
                LStatCard("Alerts", "$pendingAlerts", Icons.Default.Warning, if (pendingAlerts > 0) Color.Red else Color.Gray, Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📢 Send Fees Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("This sends a payment reminder to ALL students via their message inbox.", fontSize = 12.sp, color = Color.Gray)
                    Button(
                        onClick = {
                            db.collection("students").get().addOnSuccessListener { snap ->
                                snap.documents.forEach { doc ->
                                    db.collection("messages").add(mapOf(
                                        "senderUid" to "SCHOOL", "senderName" to "Administration",
                                        "receiverUid" to doc.id,
                                        "content" to "📌 FEES REMINDER: Your next installment is due. Please pay promptly to avoid penalties. Contact admin for payment details.",
                                        "timestamp" to System.currentTimeMillis(), "type" to "fees_reminder"
                                    ))
                                }
                                reminderSent = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("💰 Send Fees Reminder to All Students", fontWeight = FontWeight.Bold)
                    }
                    if (reminderSent) {
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(10.dp)) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("✅ Fees reminder sent to all ${studentCount} students!", color = Color(0xFF2E7D32), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

// ── Students list with delete ─────────────────────────────────────────────────
@Composable
fun StudentListTab(authVm: AuthViewModel) {
    val db = FirebaseFirestore.getInstance()
    var students       by remember { mutableStateOf<List<Student>>(emptyList()) }
    var classMessage   by remember { mutableStateOf("") }
    var messageSent    by remember { mutableStateOf(false) }
    var deleteTarget   by remember { mutableStateOf<Student?>(null) }
    val opMsg          by authVm.operationMessage.collectAsState()

    LaunchedEffect(Unit) {
        db.collection("students").get().addOnSuccessListener { snap ->
            students = snap.documents.mapNotNull { doc ->
                doc.toObject(Student::class.java)?.copy(uid = doc.id)
            }
        }
    }

    LaunchedEffect(opMsg) { if (opMsg.contains("removed")) {
        db.collection("students").get().addOnSuccessListener { snap ->
            students = snap.documents.mapNotNull { it.toObject(Student::class.java)?.copy(uid = it.id) }
        }
    }}

    // Delete confirmation dialog
    deleteTarget?.let { student ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = Color.Red, modifier = Modifier.size(40.dp)) },
            title = { Text("Delete Student?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove ${student.fullName} (${student.registrationNumber}) from the system.") },
            confirmButton = {
                Button(onClick = { authVm.deleteStudent(student.uid, student.fullName); deleteTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Students (${students.size})", fontWeight = FontWeight.Bold, fontSize = 20.sp) }

        if (opMsg.isNotEmpty()) item {
            Surface(color = if (opMsg.startsWith("✅")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                Text(opMsg, modifier = Modifier.padding(12.dp), color = if (opMsg.startsWith("✅")) Color(0xFF2E7D32) else Color.Red)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📢 Broadcast Class Notice", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = classMessage, onValueChange = { classMessage = it; messageSent = false },
                        label = { Text("e.g. Class postponed to 3 PM...") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Button(onClick = {
                        students.forEach { s ->
                            db.collection("messages").add(mapOf("senderUid" to "LECTURER", "senderName" to "Lecturer",
                                "receiverUid" to s.uid, "content" to classMessage,
                                "timestamp" to System.currentTimeMillis(), "type" to "class_notice"))
                        }
                        messageSent = true; classMessage = ""
                    }, modifier = Modifier.fillMaxWidth(), enabled = classMessage.isNotBlank()) {
                        Text("Send to All ${students.size} Students")
                    }
                    if (messageSent) Text("✅ Class notice sent!", color = Color(0xFF2E7D32))
                }
            }
        }

        items(students) { s ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Student photo
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFEDE7F6)), contentAlignment = Alignment.Center) {
                        if (s.photoUrl.isNotEmpty()) {
                            AsyncImage(model = s.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Text(s.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = LecturerPurple)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.fullName, fontWeight = FontWeight.Bold)
                        Text("${s.course} • ${s.laboratory}", fontSize = 12.sp, color = Color.Gray)
                        Text("PC: ${s.computerNumber} • Reg: ${s.registrationNumber}", fontSize = 11.sp, color = Color.Gray)
                        Text(s.email, fontSize = 11.sp, color = LecturerPurple)
                    }
                    IconButton(onClick = { deleteTarget = s }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }
        }
    }
}

// ── Attendance — GPS verified, list who signed today ─────────────────────────
@Composable
fun AttendanceViewTab() {
    val db = FirebaseFirestore.getInstance()
    var attendanceList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedDate   by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }

    LaunchedEffect(selectedDate) {
        db.collection("attendance").whereEqualTo("date", selectedDate).get().addOnSuccessListener { snap ->
            attendanceList = snap.documents.mapNotNull { it.data }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("GPS-Verified Attendance", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Only students physically at Emobilis can sign in", fontSize = 12.sp, color = Color.Gray)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = LecturerPurple)
                    Spacer(Modifier.width(8.dp))
                    Text("Date: $selectedDate", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Surface(color = LecturerPurple, shape = RoundedCornerShape(20.dp)) {
                        Text("${attendanceList.size} Present", color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            // Quick date navigation
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Today", "Yesterday", "2 Days Ago").forEachIndexed { i, label ->
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    OutlinedButton(onClick = { selectedDate = dateStr }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (selectedDate == dateStr) LecturerPurple else Color.Gray)) {
                        Text(label, fontSize = 10.sp)
                    }
                }
            }
        }
        if (attendanceList.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, tint = LecturerPurple, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No attendance records for $selectedDate", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
        items(attendanceList) { record ->
            val signedAtSchool = record["signedAtSchool"] as? Boolean ?: false
            val lat = record["latitude"] as? Double ?: 0.0
            val lng = record["longitude"] as? Double ?: 0.0
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = if (signedAtSchool) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (signedAtSchool) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        null, tint = if (signedAtSchool) Color(0xFF2E7D32) else Color(0xFFE65100), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(record["studentName"] as? String ?: "Unknown", fontWeight = FontWeight.Bold)
                        Text("Signed at: ${record["time"] as? String ?: "-"}", fontSize = 12.sp)
                        if (lat != 0.0) Text("GPS: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Surface(shape = RoundedCornerShape(12.dp),
                        color = if (signedAtSchool) Color(0xFF2E7D32) else Color(0xFFE65100)) {
                        Text(if (signedAtSchool) "✅ At School" else "⚠ Off-site",
                            color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Messages from students ────────────────────────────────────────────────────
@Composable
fun LecturerMessagesTab() {
    val db = FirebaseFirestore.getInstance()
    var messages by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("messages").get().addOnSuccessListener { snap ->
            messages = snap.documents.mapNotNull { it.data }
                .filter { (it["type"] as? String)?.let { t -> t != "fees_reminder" } ?: true }
                .sortedByDescending { it["timestamp"] as? Long ?: 0L }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Student Messages (${messages.size})", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        if (messages.isEmpty()) item { Text("No messages yet.", color = Color.Gray) }
        items(messages) { msg ->
            val type = msg["type"] as? String ?: "general"
            val bgColor = when (type) {
                "absence" -> Color(0xFFFFF3E0)
                "class_notice" -> Color(0xFFE3F2FD)
                else -> Color.White
            }
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(msg["senderName"] as? String ?: "Unknown", fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(12.dp), color = when (type) {
                            "absence" -> Color(0xFFE65100); "class_notice" -> Color(0xFF1565C0); else -> Color(0xFF6D1B6D)
                        }) {
                            Text(when (type) { "absence" -> "🚫 Absence"; "class_notice" -> "📢 Class"; else -> "💬 Message" },
                                color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp)
                        }
                    }
                    Text(msg["content"] as? String ?: "", fontSize = 14.sp)
                    val ts = msg["timestamp"] as? Long
                    if (ts != null) Text(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(ts)),
                        fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

// ── Manage — Add lecturer, view all ──────────────────────────────────────────
@Composable
fun ManageTab(authVm: AuthViewModel) {
    var showAddLecturer by remember { mutableStateOf(false) }
    var lecturerName   by remember { mutableStateOf("") }
    var lecturerEmail  by remember { mutableStateOf("") }
    var lecturerPhone  by remember { mutableStateOf("") }
    var lecturerPass   by remember { mutableStateOf("") }
    var department     by remember { mutableStateOf("") }
    val opMsg          by authVm.operationMessage.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Staff Management", fontWeight = FontWeight.Bold, fontSize = 20.sp) }

        if (opMsg.isNotEmpty()) item {
            Surface(color = if (opMsg.startsWith("✅")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (opMsg.startsWith("✅")) Icons.Default.CheckCircle else Icons.Default.Error, null,
                        tint = if (opMsg.startsWith("✅")) Color(0xFF2E7D32) else Color.Red, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(opMsg, color = if (opMsg.startsWith("✅")) Color(0xFF2E7D32) else Color.Red)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, null, tint = LecturerPurple, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Lecturer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.weight(1f))
                        Switch(checked = showAddLecturer, onCheckedChange = { showAddLecturer = it; authVm.clearMessage() })
                    }
                    if (showAddLecturer) {
                        OutlinedTextField(value = lecturerName, onValueChange = { lecturerName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) })
                        OutlinedTextField(value = lecturerEmail, onValueChange = { lecturerEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Email, null) })
                        OutlinedTextField(value = lecturerPhone, onValueChange = { lecturerPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Phone, null) })
                        OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department / Course") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.School, null) })
                        OutlinedTextField(value = lecturerPass, onValueChange = { lecturerPass = it }, label = { Text("Temporary Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Lock, null) })
                        Button(
                            onClick = {
                                authVm.addLecturer(Staff(fullName = lecturerName, email = lecturerEmail,
                                    phone = lecturerPhone, role = "lecturer", department = department, password = lecturerPass))
                                lecturerName = ""; lecturerEmail = ""; lecturerPhone = ""; lecturerPass = ""; department = ""
                                showAddLecturer = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = lecturerName.isNotBlank() && lecturerEmail.isNotBlank() && lecturerPass.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = LecturerPurple)
                        ) {
                            Icon(Icons.Default.PersonAdd, null); Spacer(Modifier.width(8.dp)); Text("Add Lecturer Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = LecturerPurple)
                        Spacer(Modifier.width(8.dp))
                        Text("Notes for Admin", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("• Added lecturers can log in with their email and the temporary password", fontSize = 12.sp)
                    Text("• Deleting a student removes their Firestore data only (Firebase Auth cleanup requires Admin SDK)", fontSize = 12.sp)
                    Text("• All fees reminders are delivered via the student's in-app messages", fontSize = 12.sp)
                }
            }
        }
    }
}
