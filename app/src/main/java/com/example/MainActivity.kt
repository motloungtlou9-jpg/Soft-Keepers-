package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.*
import com.example.ui.screens.driver.DriverHomeScreen
import com.example.ui.screens.passenger.PassengerHomeScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.theme.SoftKeeperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoftKeeperTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SoftKeeperNavHost()
                }
            }
        }
    }
}

@Composable
fun SoftKeeperNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onSelectPassenger = { navController.navigate("login/passenger") },
                onSelectDriver = { navController.navigate("login/driver") },
                onSelectAdmin = { navController.navigate("login/admin") }
            )
        }

        composable("login/{role}") { backStackEntry ->
            val roleParam = backStackEntry.arguments?.getString("role") ?: "passenger"
            UnifiedLoginScreen(
                initialRole = roleParam,
                onLoginSuccess = { selectedRole ->
                    when (selectedRole) {
                        "driver" -> navController.navigate("driver_home") { popUpTo("welcome") { inclusive = true } }
                        "admin" -> navController.navigate("admin_dashboard") { popUpTo("welcome") { inclusive = true } }
                        else -> navController.navigate("passenger_home") { popUpTo("welcome") { inclusive = true } }
                    }
                },
                onNavigateToRegister = { selectedRole ->
                    if (selectedRole == "driver") {
                        navController.navigate("driver_register")
                    } else {
                        navController.navigate("passenger_register")
                    }
                },
                onBackToWelcome = { navController.navigateUp() }
            )
        }

        composable("passenger_login") {
            UnifiedLoginScreen(
                initialRole = "passenger",
                onLoginSuccess = { role ->
                    when (role) {
                        "admin" -> navController.navigate("admin_dashboard") { popUpTo("welcome") { inclusive = true } }
                        "driver" -> navController.navigate("driver_home") { popUpTo("welcome") { inclusive = true } }
                        else -> navController.navigate("passenger_home") { popUpTo("welcome") { inclusive = true } }
                    }
                },
                onNavigateToRegister = { navController.navigate("passenger_register") },
                onBackToWelcome = { navController.navigateUp() }
            )
        }

        composable("passenger_register") {
            PassengerRegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("passenger_home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigateUp() }
            )
        }

        composable("driver_login") {
            UnifiedLoginScreen(
                initialRole = "driver",
                onLoginSuccess = { role ->
                    when (role) {
                        "admin" -> navController.navigate("admin_dashboard") { popUpTo("welcome") { inclusive = true } }
                        "driver" -> navController.navigate("driver_home") { popUpTo("welcome") { inclusive = true } }
                        else -> navController.navigate("passenger_home") { popUpTo("welcome") { inclusive = true } }
                    }
                },
                onNavigateToRegister = { navController.navigate("driver_register") },
                onBackToWelcome = { navController.navigateUp() }
            )
        }

        composable("driver_register") {
            DriverRegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("driver_home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigateUp() }
            )
        }

        composable("admin_login") {
            UnifiedLoginScreen(
                initialRole = "admin",
                onLoginSuccess = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToRegister = { },
                onBackToWelcome = { navController.navigateUp() }
            )
        }

        composable("passenger_home") {
            PassengerHomeScreen(
                onLogout = {
                    navController.navigate("welcome") {
                        popUpTo("passenger_home") { inclusive = true }
                    }
                }
            )
        }

        composable("driver_home") {
            DriverHomeScreen(
                onLogout = {
                    navController.navigate("welcome") {
                        popUpTo("driver_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_dashboard") {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate("welcome") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}
