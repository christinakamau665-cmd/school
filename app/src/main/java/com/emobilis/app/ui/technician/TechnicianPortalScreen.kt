package com.emobilis.app.ui.technician

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emobilis.app.viewmodel.AttendanceViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

val TechnicianPurple = Color(0xFF4A148C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianPortalScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val vm: AttendanceViewModel = viewModel(factory = AttendanceViewModel.factory(context))
    val alerts by vm.alerts.collectAsState()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) { vm.loadAlerts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lab Technician Portal") },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechnicianPurple, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Computer Issue Alerts", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(
                    if (alerts.isEmpty()) "✅ All systems operational" else "⚠️ ${alerts.size} unresolved issue(s)",
                    color = if (alerts.isEmpty()) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
            if (alerts.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(56.dp), tint = Color(0xFF2E7D32))
                            Spacer(Modifier.height(8.dp))
                            Text("All systems operational!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("No pending computer alerts", fontSize = 13.sp, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
            items(alerts) { alert ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, modifier = Modifier.size(20.dp), tint = Color.Red)
                            Text("${alert.laboratory} — ${alert.computerNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text("Student: ${alert.studentName}")
                        Text("Issue: ${alert.issue}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Reported: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(alert.timestamp))}", fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {
                                db.collection("computer_alerts")
                                    .whereEqualTo("studentUid", alert.studentUid)
                                    .whereEqualTo("computerNumber", alert.computerNumber)
                                    .get()
                                    .addOnSuccessListener { snap ->
                                        snap.documents.firstOrNull()?.reference?.update("resolved", true)
                                        vm.loadAlerts()
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Build, null)
                            Spacer(Modifier.width(8.dp))
                            Text("✔ Mark as Resolved")
                        }
                    }
                }
            }
        }
    }
}
