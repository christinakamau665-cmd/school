package com.emobilis.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*


import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emobilis.app.util.BiometricHelper
import com.emobilis.app.viewmodel.AuthState
import com.emobilis.app.viewmodel.AuthViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val context        = LocalContext.current
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var showPassword   by remember { mutableStateOf(false) }
    var bioError       by remember { mutableStateOf("") }
    val state          by vm.authState.collectAsState()

    val deepMaroon     = Color(0xFF3B0A2A)
    val richPurple     = Color(0xFF6D1B6D)
    val midPink        = Color(0xFFB5326F)
    val gold           = Color(0xFFFFCC00)
    val isBioAvailable = remember { BiometricHelper.isAvailable(context) }
    val isReturning    = remember { vm.isAlreadyLoggedIn() }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onLoginSuccess((state as AuthState.Success).role)
            vm.resetState()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(deepMaroon, richPurple, midPink)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0xFF9575CD).copy(alpha = 0.28f), 170f, Offset(50f, 220f))
            drawCircle(Color(0xFFFF80AB).copy(alpha = 0.22f), 130f, Offset(size.width - 40f, size.height * 0.6f))
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            // Gold sun
            Box(modifier = Modifier.size(88.dp).background(gold.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(62.dp).background(Brush.radialGradient(listOf(Color(0xFFFFEE58), gold, Color(0xFFFF8F00))), CircleShape))
                Canvas(Modifier.size(88.dp)) {
                    val cx = size.width / 2f; val cy = size.height / 2f
                    for (i in 0 until 12) {
                        val angle = Math.toRadians((i * 30.0)).toFloat()
                        drawLine(gold, Offset(cx + 34f * cos(angle), cy + 34f * sin(angle)),
                            Offset(cx + 44f * cos(angle), cy + 44f * sin(angle)), 4f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(modifier = Modifier.width(240.dp).height(1.5.dp).background(Color.White.copy(0.35f)))
            Spacer(Modifier.height(6.dp))
            Text("EMOBILIS", style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 7.sp, fontStyle = FontStyle.Italic))
            Box(modifier = Modifier.width(240.dp).height(1.5.dp).background(Color.White.copy(0.35f)))
            Spacer(Modifier.height(6.dp))
            Text("AWARD WINNING SCHOOL", style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(0.8f), letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold))
            Text("School Management System", style = MaterialTheme.typography.bodySmall.copy(color = gold.copy(0.9f)))
            Spacer(Modifier.height(28.dp))

            // Login Card
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(14.dp)) {
                Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Sign In", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = richPurple))
                    Text("Access your student or staff portal", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280)))

                    OutlinedTextField(value = email, onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = richPurple) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = richPurple, focusedLabelColor = richPurple))

                    OutlinedTextField(value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = richPurple) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = richPurple)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = richPurple, focusedLabelColor = richPurple))

                    // Error banner
                    AnimatedVisibility(visible = state is AuthState.Error || bioError.isNotEmpty()) {
                        val msg = if (state is AuthState.Error) (state as AuthState.Error).message else bioError
                        Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp), tint = Color(0xFFC62828))
                                Spacer(Modifier.width(6.dp))
                                Text(msg, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC62828)))
                            }
                        }
                    }

                    // Sign In button
                    Button(onClick = { bioError = ""; if (email.isNotBlank() && password.isNotBlank()) vm.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = state !is AuthState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = richPurple)) {
                        if (state is AuthState.Loading)
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                        else { Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Sign In", fontWeight = FontWeight.Bold) }
                    }

                    // Fingerprint button — only shown when device has biometric AND user has logged in before
                    if (isBioAvailable && isReturning) {
                        HorizontalDivider()
                        OutlinedButton(
                            onClick = {
                                bioError = ""
                                val activity = context as? FragmentActivity ?: run { bioError = "Biometric not available here"; return@OutlinedButton }
                                BiometricHelper.authenticate(activity,
                                    onSuccess  = { vm.loginWithBiometric() },
                                    onError    = { msg -> bioError = msg },
                                    onFallback = { /* user chose PIN — form stays visible */ })
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, richPurple)) {
                            Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(22.dp), tint = richPurple)
                            Spacer(Modifier.width(10.dp))
                            Text("Sign In with Fingerprint", fontWeight = FontWeight.SemiBold, color = richPurple)
                        }
                        Text("Quick access with biometrics", style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF6B7280), textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) { Text("Forgot Password?", color = richPurple) }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("New student? ", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280)))
                        Text("Register Here", style = MaterialTheme.typography.bodyMedium.copy(color = richPurple, fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable(onClick = onNavigateToRegister))
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("🔒 Firebase Auth + Biometric Security", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(0.5f), textAlign = TextAlign.Center))
            Spacer(Modifier.height(28.dp))
        }
    }
}
