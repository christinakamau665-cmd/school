package com.emobilis.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emobilis.app.data.model.Message
import com.emobilis.app.data.model.Student
import com.emobilis.app.ui.theme.EmobilisAccent
import com.emobilis.app.ui.theme.EmobilisPrimary
import com.emobilis.app.viewmodel.AttendanceViewModel
import com.emobilis.app.viewmodel.AuthViewModel
import com.emobilis.app.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPortalScreen(onLogout: () -> Unit, vm: AuthViewModel = viewModel()) {
    val student    by vm.currentStudent.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Home", "Attendance", "Messages", "Alerts", "Profile")

    LaunchedEffect(Unit) { vm.loadCurrentStudent() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Portal${student?.fullName?.let { " · $it" } ?: ""}", maxLines = 1) },
                actions = {
                    IconButton(onClick = { vm.signOut(); onLogout() }) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmobilisPrimary, titleContentColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, title ->
                    NavigationBarItem(
                        selected = selectedTab == i,
                        onClick  = { selectedTab = i },
                        icon = {
                            Icon(when (i) {
                                0 -> Icons.Default.Home; 1 -> Icons.Default.CheckCircle
                                2 -> Icons.Default.Message; 3 -> Icons.Default.Warning
                                else -> Icons.Default.Person
                            }, contentDescription = title)
                        },
                        label = { Text(title, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> StudentHomeTab(student)
                1 -> AttendanceTab(student)
                2 -> MessageTab(student)
                3 -> ComputerAlertTab(student)
                4 -> ProfileTab(student) { vm.signOut(); onLogout() }
            }
        }
    }
}

// ── Home Tab ──────────────────────────────────────────────────────────────────
@Composable
fun StudentHomeTab(student: Student?) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EmobilisPrimary)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Welcome back!", color = Color.White, fontSize = 13.sp)
                    Text(student?.fullName ?: "Loading...", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Reg: ${student?.registrationNumber ?: "-"}", color = Color.White.copy(0.8f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("Course",    student?.course ?: "-",          Modifier.weight(1f))
                InfoCard("Lab",       student?.laboratory ?: "-",      Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("Computer",  student?.computerNumber ?: "-",  Modifier.weight(1f))
                InfoCard("Fees Bal.", "KSh ${student?.feesBalance ?: 0.0}", Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(22.dp), tint = EmobilisAccent)
                    Spacer(Modifier.width(10.dp))
                    Text("Attendance is GPS-verified. You must be at EMOBILIS campus to sign.",
                        fontSize = 12.sp, color = Color(0xFF5D4037))
                }
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

// ── Attendance Tab ────────────────────────────────────────────────────────────
@Composable
fun AttendanceTab(student: Student?) {
    val context = LocalContext.current
    val vm: AttendanceViewModel = viewModel(factory = AttendanceViewModel.factory(context))
    val status        by vm.attendanceStatus.collectAsState()
    val list          by vm.attendanceList.collectAsState()
    val isChecking    by vm.isCheckingLocation.collectAsState()

    LaunchedEffect(student) { student?.let { vm.loadAttendance(it.uid) } }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Attendance", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        item {
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(40.dp), tint = EmobilisPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text("Attendance is signed automatically when your GPS confirms you are at EMOBILIS campus.",
                        textAlign = TextAlign.Center, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { student?.let { vm.signAttendance(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChecking
                    ) {
                        if (isChecking) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        else Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isChecking) "Checking location..." else "Sign Today's Attendance")
                    }
                    if (status.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(status, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        item { Text("Attendance History", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
        if (list.isEmpty()) {
            item { Text("No attendance records yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(list) { att ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(att.date, fontWeight = FontWeight.Medium)
                    Text(att.time.take(8), fontSize = 13.sp)
                    Text(if (att.signedAtSchool) "✅ Present" else "❌ Absent")
                }
            }
        }
    }
}

// ── Messages Tab ──────────────────────────────────────────────────────────────
@Composable
fun MessageTab(student: Student?) {
    val vm: MessageViewModel = viewModel()
    var messageText by remember { mutableStateOf("") }
    var absenceMode by remember { mutableStateOf(false) }
    var sent        by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Messages & Notifications", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Message Type", fontWeight = FontWeight.SemiBold)
                Text("Toggle to send an absence notification instead of a general message", fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = absenceMode, onCheckedChange = { absenceMode = it; sent = false })
                    Spacer(Modifier.width(8.dp))
                    Text(if (absenceMode) "📵 Absence Notice" else "💬 General Message / Suggestion")
                }
            }
        }

        OutlinedTextField(
            value = messageText, onValueChange = { messageText = it; sent = false },
            label = { Text(if (absenceMode) "Reason for absence..." else "Your message or suggestion...") },
            modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5
        )

        Button(
            onClick = {
                student?.let {
                    vm.sendMessage(Message(
                        senderUid = it.uid, senderName = it.fullName, receiverUid = "SCHOOL",
                        content = messageText, timestamp = System.currentTimeMillis(),
                        type = if (absenceMode) "absence" else "general"
                    ))
                    sent = true; messageText = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = messageText.isNotBlank()
        ) {
            Icon(if (absenceMode) Icons.Default.Sms else Icons.Default.Send, null)
            Spacer(Modifier.width(8.dp))
            Text(if (absenceMode) "Send Absence Notification" else "Send Message / Suggestion")
        }

        if (sent) Text("✅ Message sent successfully!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
    }
}

// ── Computer Alert Tab ────────────────────────────────────────────────────────
@Composable
fun ComputerAlertTab(student: Student?) {
    val context = LocalContext.current
    val vm: AttendanceViewModel = viewModel(factory = AttendanceViewModel.factory(context))
    var issue by remember { mutableStateOf("") }
    var sent  by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Report Computer Issue", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
            Column(Modifier.padding(16.dp)) {
                Icon(Icons.Default.Warning, null, modifier = Modifier.size(32.dp), tint = EmobilisAccent)
                Spacer(Modifier.height(8.dp))
                Text("Alert auto-includes:", fontWeight = FontWeight.SemiBold)
                Text("• Computer: ${student?.computerNumber ?: "-"}")
                Text("• Lab: ${student?.laboratory ?: "-"}")
                Text("• Student: ${student?.fullName ?: "-"}")
                Text("• Reg No: ${student?.registrationNumber ?: "-"}")
            }
        }

        OutlinedTextField(
            value = issue, onValueChange = { issue = it; sent = false },
            label = { Text("Describe the computer issue...") },
            modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5
        )

        Button(
            onClick = { student?.let { vm.sendComputerAlert(it, issue); sent = true; issue = "" } },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            enabled = issue.isNotBlank()
        ) {
            Icon(Icons.Default.Warning, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("🚨 Send Alert to Lab Technician", color = Color.White)
        }

        if (sent) Text("✅ Alert sent to Lab Technician!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
    }
}

// ── Profile Tab ───────────────────────────────────────────────────────────────
@Composable
fun ProfileTab(student: Student?, onLogout: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("My Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        item {
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EmobilisPrimary)) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(student?.fullName ?: "-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(student?.registrationNumber ?: "", color = Color.White.copy(0.8f))
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileRow("Email",        student?.email ?: "-")
                    ProfileRow("Phone",        student?.phone ?: "-")
                    ProfileRow("Course",       student?.course ?: "-")
                    ProfileRow("Laboratory",   student?.laboratory ?: "-")
                    ProfileRow("Computer",     student?.computerNumber ?: "-")
                    ProfileRow("Fees Balance", "KSh ${student?.feesBalance ?: 0.0}")
                }
            }
        }
        item {
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Logout, null); Spacer(Modifier.width(8.dp)); Text("Sign Out")
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
    HorizontalDivider()
}
