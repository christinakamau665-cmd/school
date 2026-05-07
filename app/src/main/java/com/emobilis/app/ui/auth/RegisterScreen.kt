package com.emobilis.app.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.emobilis.app.AppConstants
import com.emobilis.app.data.model.Student
import com.emobilis.app.ui.theme.EmobilisPrimary
import com.emobilis.app.viewmodel.AuthState
import com.emobilis.app.viewmodel.AuthViewModel
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val context        = LocalContext.current
    var fullName       by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedLab    by remember { mutableStateOf("") }
    var selectedPC     by remember { mutableStateOf("") }
    var courseExpanded by remember { mutableStateOf(false) }
    var labExpanded    by remember { mutableStateOf(false) }
    var pcExpanded     by remember { mutableStateOf(false) }

    // Photo state
    var photoUri       by remember { mutableStateOf<Uri?>(null) }
    var uploadedUrl    by remember { mutableStateOf("") }
    var isUploading    by remember { mutableStateOf(false) }

    val state by vm.authState.collectAsState()

    val richPurple = Color(0xFF6D1B6D)

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoUri = it
            isUploading = true
            val ref = FirebaseStorage.getInstance().reference.child("photos/${System.currentTimeMillis()}.jpg")
            ref.putFile(it)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { url -> uploadedUrl = url.toString(); isUploading = false }
                .addOnFailureListener { isUploading = false }
        }
    }

    LaunchedEffect(state) { if (state is AuthState.Success) onRegistered() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Registration") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EmobilisPrimary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Photo upload ──────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Profile Photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Box(
                            modifier = Modifier.size(110.dp).clip(CircleShape)
                                .background(Color(0xFFEDE7F6))
                                .border(2.dp, richPurple, CircleShape)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUri != null) {
                                AsyncImage(model = photoUri, contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.AddAPhoto, "Add Photo", tint = richPurple, modifier = Modifier.size(40.dp))
                            }
                            if (isUploading) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f), CircleShape), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                        Text(if (uploadedUrl.isNotEmpty()) "✅ Photo uploaded!" else "Tap circle to add your photo",
                            fontSize = 12.sp, color = if (uploadedUrl.isNotEmpty()) Color(0xFF2E7D32) else Color.Gray)
                    }
                }
            }

            // ── Personal info ─────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, null) })
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, null) })
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, null) })
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, null) })
                    }
                }
            }

            // ── Course & Lab ──────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Course & Lab Assignment", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        ExposedDropdownMenuBox(expanded = courseExpanded, onExpandedChange = { courseExpanded = it }) {
                            OutlinedTextField(value = selectedCourse, onValueChange = {}, readOnly = true,
                                label = { Text("Select Course") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(courseExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = courseExpanded, onDismissRequest = { courseExpanded = false }) {
                                AppConstants.COURSES.forEach { c ->
                                    DropdownMenuItem(text = { Text(c) }, onClick = { selectedCourse = c; courseExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(expanded = labExpanded, onExpandedChange = { labExpanded = it }) {
                            OutlinedTextField(value = selectedLab, onValueChange = {}, readOnly = true,
                                label = { Text("Select Laboratory") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(labExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = labExpanded, onDismissRequest = { labExpanded = false }) {
                                AppConstants.LABORATORIES.forEach { l ->
                                    DropdownMenuItem(text = { Text(l) }, onClick = { selectedLab = l; labExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(expanded = pcExpanded, onExpandedChange = { pcExpanded = it }) {
                            OutlinedTextField(value = selectedPC, onValueChange = {}, readOnly = true,
                                label = { Text("Select Computer Number") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(pcExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor())
                            ExposedDropdownMenu(expanded = pcExpanded, onDismissRequest = { pcExpanded = false }) {
                                AppConstants.COMPUTER_NUMBERS.forEach { pc ->
                                    DropdownMenuItem(text = { Text(pc) }, onClick = { selectedPC = pc; pcExpanded = false })
                                }
                            }
                        }
                    }
                }
            }

            // ── Register button ───────────────────────────────────────────────
            item {
                if (state is AuthState.Error)
                    Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error)

                Button(
                    onClick = {
                        vm.register(
                            Student(fullName = fullName, email = email, phone = phone,
                                course = selectedCourse, laboratory = selectedLab,
                                computerNumber = selectedPC, photoUrl = uploadedUrl),
                            password
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = state !is AuthState.Loading && !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D1B6D))
                ) {
                    if (state is AuthState.Loading)
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else { Icon(Icons.Default.HowToReg, null); Spacer(Modifier.width(8.dp)); Text("Complete Registration", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
