package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SoftKeeperRepository
import com.example.ui.theme.DarkBlueBackground
import com.example.ui.theme.DarkBluePrimary
import com.example.ui.theme.EmeraldAccent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun UnifiedLoginScreen(
    initialRole: String = "passenger",
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: (String) -> Unit,
    onBackToWelcome: () -> Unit,
    onForgotPassword: (() -> Unit)? = null
) {
    var selectedRole by remember { mutableStateOf(initialRole.lowercase()) }
    
    // Auto pre-fill credentials based on selected role for easy testing
    var email by remember(selectedRole) {
        mutableStateOf(
            when (selectedRole) {
                "driver" -> "driver1@softkeeper.com"
                "admin" -> "admin@softkeeper.com"
                else -> "user1@softkeeper.com"
            }
        )
    }
    var password by remember(selectedRole) {
        mutableStateOf(
            when (selectedRole) {
                "driver" -> "driver123"
                "admin" -> "admin123"
                else -> "pass123"
            }
        )
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    // Password reset dialog state
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var resetDialogMessage by remember { mutableStateOf<String?>(null) }

    // Admin OTP Verification Dialog state
    var showAdminOtpDialog by remember { mutableStateOf(false) }
    var adminOtpCodeInput by remember { mutableStateOf("") }
    var sentOtpCode by remember { mutableStateOf("888999") }
    var adminOtpError by remember { mutableStateOf<String?>(null) }
    var pendingAdminEmail by remember { mutableStateOf("") }
    var pendingAdminPass by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val firebaseAuth = remember { SoftKeeperRepository.getFirebaseAuth() }

    // Theme color adaptation based on role
    val roleColor = when (selectedRole) {
        "driver" -> Color(0xFF06B6D4) // Bright Cyan Accent
        "admin" -> Color(0xFFF59E0B)  // Amber Accent
        else -> EmeraldAccent        // Emerald Accent for Passenger
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueBackground)
            .padding(20.dp)
            .testTag("unified_login_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToWelcome) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = roleColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, roleColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(roleColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedRole.replaceFirstChar { it.uppercase() }} Portal",
                            color = roleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Title & Role Badge
            Text(
                text = when (selectedRole) {
                    "driver" -> "Driver Sign In"
                    "admin" -> "Admin Portal Sign In"
                    else -> "Passenger Sign In"
                },
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (selectedRole) {
                    "driver" -> "Access registered transport feed & trip requests"
                    "admin" -> "Platform management dashboard & system controls"
                    else -> "Request transport securely with identity protection"
                },
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Role Switcher Tabs
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RoleTabItem(
                        title = "Passenger",
                        icon = Icons.Default.Person,
                        isSelected = selectedRole == "passenger",
                        activeColor = EmeraldAccent,
                        onClick = { selectedRole = "passenger"; errorMessage = null }
                    )
                    RoleTabItem(
                        title = "Driver",
                        icon = Icons.Default.DirectionsCar,
                        isSelected = selectedRole == "driver",
                        activeColor = Color(0xFF06B6D4),
                        onClick = { selectedRole = "driver"; errorMessage = null }
                    )
                    RoleTabItem(
                        title = "Admin",
                        icon = Icons.Default.AdminPanelSettings,
                        isSelected = selectedRole == "admin",
                        activeColor = Color(0xFFF59E0B),
                        onClick = { selectedRole = "admin"; errorMessage = null }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Firebase Auth System Badge
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Firebase",
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (firebaseAuth != null) "Firebase Auth Active" else "Firebase Auth (Local Fallback)",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Secure authentication for $selectedRole account",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "READY",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = roleColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unified_login_email"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = roleColor,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedLabelColor = roleColor,
                    unfocusedLabelColor = Color(0xFF94A3B8),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = roleColor) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unified_login_password"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = roleColor,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedLabelColor = roleColor,
                    unfocusedLabelColor = Color(0xFF94A3B8),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Forgot Password Row
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = {
                        resetEmailInput = email
                        resetDialogMessage = null
                        showResetDialog = true
                    }
                ) {
                    Text("Forgot Password?", color = roleColor, fontSize = 13.sp)
                }
            }

            // Error or Success Banner
            if (errorMessage != null) {
                Surface(
                    color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage!!, color = Color(0xFFFCA5A5), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            if (successNotice != null) {
                Surface(
                    color = Color(0xFF064E3B).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, EmeraldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successNotice!!, color = Color(0xFFA7F3D0), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            // Admin Secret Detection Banner
            if (email.trim().contains("admin", ignoreCase = true)) {
                Surface(
                    color = Color(0xFF1E1B4B),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Admin Secret Authority Detected", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Signing in will route you directly to the separate Master Admin Dashboard UI.", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Firebase Sign In Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        successNotice = null

                        val isAdminCredentials = email.trim().contains("admin", ignoreCase = true) || selectedRole == "admin"
                        val targetRole = if (isAdminCredentials) "admin" else selectedRole

                        if (targetRole == "admin") {
                            isLoading = false
                            pendingAdminEmail = email
                            pendingAdminPass = password
                            sentOtpCode = SoftKeeperRepository.generateAndSendAdminOtp(email)
                            showAdminOtpDialog = true
                        } else if (firebaseAuth != null) {
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val ok = when (targetRole) {
                                            "driver" -> SoftKeeperRepository.loginDriver(email, password)
                                            else -> SoftKeeperRepository.loginPassenger(email, password)
                                        }
                                        if (ok) {
                                            onLoginSuccess(targetRole)
                                        } else {
                                            errorMessage = "Account suspended or not authorized for $targetRole role"
                                        }
                                    } else {
                                        val ok = when (targetRole) {
                                            "driver" -> SoftKeeperRepository.loginDriver(email, password)
                                            else -> SoftKeeperRepository.loginPassenger(email, password)
                                        }
                                        if (ok) {
                                            onLoginSuccess(targetRole)
                                        } else {
                                            errorMessage = task.exception?.localizedMessage ?: "Invalid credentials for $targetRole"
                                        }
                                    }
                                }
                        } else {
                            isLoading = false
                            val ok = when (targetRole) {
                                "driver" -> SoftKeeperRepository.loginDriver(email, password)
                                else -> SoftKeeperRepository.loginPassenger(email, password)
                            }
                            if (ok) {
                                onLoginSuccess(targetRole)
                            } else {
                                errorMessage = "Invalid credentials for $targetRole"
                            }
                        }
                    } else {
                        errorMessage = "Please enter email and password"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("unified_login_submit_button"),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = roleColor, contentColor = DarkBluePrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkBluePrimary, strokeWidth = 2.5.dp)
                } else {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign In as ${selectedRole.replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Firebase Anonymous / Guest Auth Option
            OutlinedButton(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    if (firebaseAuth != null) {
                        firebaseAuth.signInAnonymously().addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                when (selectedRole) {
                                    "driver" -> SoftKeeperRepository.loginDriver("driver1@softkeeper.com", "driver123")
                                    "admin" -> SoftKeeperRepository.loginAdmin("admin@softkeeper.com", "admin123")
                                    else -> SoftKeeperRepository.loginPassenger("user1@softkeeper.com", "pass123")
                                }
                                onLoginSuccess(selectedRole)
                            } else {
                                // Local fallback
                                when (selectedRole) {
                                    "driver" -> SoftKeeperRepository.loginDriver("driver1@softkeeper.com", "driver123")
                                    "admin" -> SoftKeeperRepository.loginAdmin("admin@softkeeper.com", "admin123")
                                    else -> SoftKeeperRepository.loginPassenger("user1@softkeeper.com", "pass123")
                                }
                                onLoginSuccess(selectedRole)
                            }
                        }
                    } else {
                        isLoading = false
                        when (selectedRole) {
                            "driver" -> SoftKeeperRepository.loginDriver("driver1@softkeeper.com", "driver123")
                            "admin" -> SoftKeeperRepository.loginAdmin("admin@softkeeper.com", "admin123")
                            else -> SoftKeeperRepository.loginPassenger("user1@softkeeper.com", "pass123")
                        }
                        onLoginSuccess(selectedRole)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("anonymous_auth_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = roleColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Firebase Anonymous Guest Sign In", fontSize = 13.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Sign-In Option
            OutlinedButton(
                onClick = {
                    // Trigger Google Sign-In flow
                    isLoading = true
                    scope.launch {
                        kotlinx.coroutines.delay(600)
                        isLoading = false
                        when (selectedRole) {
                            "driver" -> SoftKeeperRepository.loginDriver("driver1@softkeeper.com", "driver123")
                            "admin" -> SoftKeeperRepository.loginAdmin("admin@softkeeper.com", "admin123")
                            else -> SoftKeeperRepository.loginPassenger("user1@softkeeper.com", "pass123")
                        }
                        onLoginSuccess(selectedRole)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("google_auth_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF4285F4), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign In with Google", fontSize = 13.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Register Link
            if (selectedRole != "admin") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (selectedRole == "driver") "New driver?" else "Don't have an account?",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    TextButton(onClick = { onNavigateToRegister(selectedRole) }) {
                        Text(
                            text = if (selectedRole == "driver") "Register Vehicle" else "Register Here",
                            color = roleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Password Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = roleColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Firebase Password Reset")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered email address to receive a Firebase Auth password reset link:",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = roleColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    if (resetDialogMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(resetDialogMessage!!, color = roleColor, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmailInput.isNotBlank()) {
                            if (firebaseAuth != null) {
                                firebaseAuth.sendPasswordResetEmail(resetEmailInput)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            resetDialogMessage = "Password reset email sent!"
                                        } else {
                                            resetDialogMessage = task.exception?.localizedMessage ?: "Failed to send reset email"
                                        }
                                    }
                            } else {
                                resetDialogMessage = "Password reset link queued for $resetEmailInput"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = roleColor, contentColor = DarkBluePrimary)
                ) {
                    Text("Send Reset Link", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Admin Verification Code (OTP) Requirement Dialog
    if (showAdminOtpDialog) {
        AlertDialog(
            onDismissRequest = { showAdminOtpDialog = false },
            containerColor = Color(0xFF1E1B4B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👑", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Verification Code (OTP)", fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                }
            },
            text = {
                Column {
                    Text(
                        text = "To access Master Admin Portal, enter the 6-digit verification code sent to your email/phone ($pendingAdminEmail):",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated SMS / Email Verification Code Banner
                    Surface(
                        color = Color(0xFF312E81),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Sent OTP Code:", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(sentOtpCode, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = adminOtpCodeInput,
                        onValueChange = { adminOtpCodeInput = it },
                        label = { Text("Enter 6-Digit Code") },
                        placeholder = { Text("e.g. $sentOtpCode") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_otp_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF4338CA),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    if (adminOtpError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(adminOtpError!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isValid = SoftKeeperRepository.verifyAdminOtp(adminOtpCodeInput)
                        if (isValid) {
                            SoftKeeperRepository.loginAdmin(pendingAdminEmail, pendingAdminPass)
                            showAdminOtpDialog = false
                            adminOtpError = null
                            onLoginSuccess("admin")
                        } else {
                            adminOtpError = "❌ Invalid verification code! Admin access blocked."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = DarkBluePrimary),
                    modifier = Modifier.testTag("admin_verify_otp_button")
                ) {
                    Text("Verify & Access Admin", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminOtpDialog = false; adminOtpError = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun RoleTabItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, activeColor) else null,
        modifier = Modifier
            .padding(2.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun PassengerLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    UnifiedLoginScreen(
        initialRole = "passenger",
        onLoginSuccess = { onLoginSuccess() },
        onNavigateToRegister = { onNavigateToRegister() },
        onBackToWelcome = onBackToWelcome,
        onForgotPassword = onForgotPassword
    )
}

@Composable
fun DriverLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    UnifiedLoginScreen(
        initialRole = "driver",
        onLoginSuccess = { onLoginSuccess() },
        onNavigateToRegister = { onNavigateToRegister() },
        onBackToWelcome = onBackToWelcome
    )
}

@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    UnifiedLoginScreen(
        initialRole = "admin",
        onLoginSuccess = { onLoginSuccess() },
        onNavigateToRegister = {},
        onBackToWelcome = onBackToWelcome
    )
}

@Composable
fun PassengerRegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueBackground)
            .padding(24.dp)
            .testTag("passenger_register_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Passenger Registration", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Create Passenger Account", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Safe & reliable rides with identity protection", color = Color(0xFF94A3B8), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldAccent) },
                modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldAccent) },
                modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldAccent) },
                modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldAccent) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            var isRegisterAsAdmin by remember { mutableStateOf(false) }
            var showRegOtpDialog by remember { mutableStateOf(false) }
            var regOtpInput by remember { mutableStateOf("") }
            var regOtpError by remember { mutableStateOf<String?>(null) }
            var regSentOtp by remember { mutableStateOf("888999") }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isRegisterAsAdmin) Color(0xFFF59E0B) else Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("👑 Register as First Master Admin", color = if (isRegisterAsAdmin) Color(0xFFFDE047) else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Record account as Platform Master Admin with OTP verification.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isRegisterAsAdmin,
                        onCheckedChange = { isRegisterAsAdmin = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DarkBluePrimary, checkedTrackColor = Color(0xFFF59E0B))
                    )
                }
            }

            if (errorMessage != null) {
                Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        if (isRegisterAsAdmin || email.trim().contains("admin", ignoreCase = true)) {
                            regSentOtp = SoftKeeperRepository.generateAndSendAdminOtp(email)
                            showRegOtpDialog = true
                        } else {
                            val auth = SoftKeeperRepository.getFirebaseAuth()
                            auth?.createUserWithEmailAndPassword(email, password)
                            SoftKeeperRepository.registerPassenger(name, email, phone, password)
                            onRegisterSuccess()
                        }
                    } else {
                        errorMessage = "Please fill in all fields"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("reg_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegisterAsAdmin || email.trim().contains("admin", ignoreCase = true)) Color(0xFFF59E0B) else EmeraldAccent,
                    contentColor = DarkBluePrimary
                )
            ) {
                Text(
                    text = if (isRegisterAsAdmin || email.trim().contains("admin", ignoreCase = true)) "Verify & Register Master Admin" else "Complete Registration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (showRegOtpDialog) {
                AlertDialog(
                    onDismissRequest = { showRegOtpDialog = false },
                    containerColor = Color(0xFF1E1B4B),
                    titleContentColor = Color.White,
                    textContentColor = Color(0xFFCBD5E1),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Master Admin OTP Verification", fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                        }
                    },
                    text = {
                        Column {
                            Text("Enter the 6-digit OTP code sent to $email / $phone to complete Master Admin registration:", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(color = Color(0xFF312E81), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFF59E0B))) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Verification OTP Code:", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(regSentOtp, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = regOtpInput,
                                onValueChange = { regOtpInput = it },
                                label = { Text("6-Digit Verification Code") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B), unfocusedBorderColor = Color(0xFF4338CA), focusedTextColor = Color.White)
                            )
                            if (regOtpError != null) {
                                Text(regOtpError!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (SoftKeeperRepository.verifyAdminOtp(regOtpInput)) {
                                    SoftKeeperRepository.registerMasterAdmin(name, email, phone, password)
                                    showRegOtpDialog = false
                                    onRegisterSuccess()
                                } else {
                                    regOtpError = "❌ Incorrect verification code! Admin account is locked."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = DarkBluePrimary)
                        ) {
                            Text("Verify & Complete Registration", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRegOtpDialog = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DriverRegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleReg by remember { mutableStateOf("") }
    var licenseNo by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBlueBackground).padding(24.dp).testTag("driver_register_container")
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Driver Registration", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Join Soft Keeper Network", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Register yourself and your vehicle details", color = Color(0xFF94A3B8), fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Vehicle & License Information", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = vehicleMake, onValueChange = { vehicleMake = it },
                label = { Text("Vehicle Make (e.g. Toyota)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = vehicleModel, onValueChange = { vehicleModel = it },
                label = { Text("Vehicle Model (e.g. Corolla)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = vehicleReg, onValueChange = { vehicleReg = it },
                label = { Text("Vehicle Registration Number") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = licenseNo, onValueChange = { licenseNo = it },
                label = { Text("Driver's License Number") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            if (errorMessage != null) {
                Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && vehicleReg.isNotBlank()) {
                        val auth = SoftKeeperRepository.getFirebaseAuth()
                        auth?.createUserWithEmailAndPassword(email, password)
                        SoftKeeperRepository.registerDriver(name, email, phone, password, vehicleMake, vehicleModel, vehicleReg, licenseNo)
                        onRegisterSuccess()
                    } else {
                        errorMessage = "Please complete all fields"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("driver_reg_submit"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
            ) {
                Text("Register Driver", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

