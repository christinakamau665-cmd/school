package com.emobilis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.multidex.MultiDex
import android.content.Context
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emobilis.app.ui.auth.LoginScreen
import com.emobilis.app.ui.auth.RegisterScreen
import com.emobilis.app.ui.auth.SplashScreen
import com.emobilis.app.ui.lecturer.LecturerPortalScreen
import com.emobilis.app.ui.student.StudentPortalScreen
import com.emobilis.app.ui.technician.TechnicianPortalScreen
import com.emobilis.app.ui.theme.EmobilisTheme

class MainActivity : ComponentActivity() {

    // Required for MultiDex on Android 7
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmobilisTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = "technician_portal") {

                    composable("splash") {
                        SplashScreen {
                            nav.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }

                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { role ->
                                val dest = when (role) {
                                    "lecturer"       -> "lecturer_portal"
                                    "lab_technician" -> "technician_portal"
                                    else             -> "student_portal"
                                }
                                nav.navigate(dest) {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { nav.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegistered = {
                                nav.navigate("student_portal") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }

                    composable("student_portal") {
                        StudentPortalScreen(
                            onLogout = {
                                nav.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("lecturer_portal") {
                        LecturerPortalScreen(
                            onLogout = {
                                nav.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("technician_portal") {
                        TechnicianPortalScreen(
                            onLogout = {
                                nav.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
