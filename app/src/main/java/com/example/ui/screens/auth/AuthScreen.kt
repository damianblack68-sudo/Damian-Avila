package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.credentials.CredentialManager
import com.example.auth.FirebaseAuthHelper
import com.example.R
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

@Composable
fun AuthScreen(
    onLoginSuccess: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isSigningInWithGoogle by remember { mutableStateOf(false) }

    // Intentar inicio de sesión automático silencioso si ya hay una cuenta vinculada
    LaunchedEffect(Unit) {
        FirebaseAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { profile -> onLoginSuccess(profile) },
            onUnauthenticated = { /* Continúa en pantalla de login */ },
            scope = coroutineScope
        )
    }

    var isRegisterMode by remember { mutableStateOf(false) }

    var emailInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var customGoogleEmailInput by remember { mutableStateOf("") }

    val adminEmail = "damianblack68@gmail.com"

    fun processLogin(email: String, username: String, name: String) {
        val cleanEmail = email.trim().lowercase()
        val isAdmin = cleanEmail == adminEmail

        val profile = if (isAdmin) {
            UserProfile(
                email = adminEmail,
                username = "damian_avila_admin",
                displayName = "Damian Ávila (Creador & Admin)",
                role = UserRole.ADMIN_CREATOR,
                subscription = SubscriptionTier.EVOLUTION_1000,
                isVerified = true,
                qrCodeId = "ONX-DAMIAN-ADMIN-PASSPORT"
            )
        } else {
            val uName = if (username.isNotBlank()) username.removePrefix("@").trim() else cleanEmail.substringBefore("@")
            val dName = if (name.isNotBlank()) name.trim() else uName.capitalize()
            UserProfile(
                email = cleanEmail,
                username = uName,
                displayName = dName,
                role = UserRole.STANDARD_USER,
                subscription = SubscriptionTier.FREE,
                isVerified = false,
                qrCodeId = "ONX-$uName"
            )
        }

        val welcomeMsg = if (isAdmin) {
            "¡Bienvenido Creador Damian Ávila! Acceso Supremo y Servidor Admin activado."
        } else {
            "¡Bienvenido a Onyx Open, @${profile.username}!"
        }
        Toast.makeText(context, welcomeMsg, Toast.LENGTH_LONG).show()
        onLoginSuccess(profile)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0E))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, OnyxRedPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo & Header
                Image(
                    painter = painterResource(id = R.drawable.ic_onyx_logo),
                    contentDescription = "Onyx Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ONYX",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = OnyxRedPrimary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OPEN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (isRegisterMode) "Crea tu cuenta única con QR personal" else "Inicia sesión para acceder a tu IA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                // Primary Google Sign-In via Jetpack CredentialManager & Firebase Auth
                Button(
                    onClick = {
                        if (!isSigningInWithGoogle && context is Activity) {
                            isSigningInWithGoogle = true
                            FirebaseAuthHelper.signInWithGoogle(
                                activity = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = { profile ->
                                    isSigningInWithGoogle = false
                                    onLoginSuccess(profile)
                                },
                                onAuthError = { err ->
                                    isSigningInWithGoogle = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isSigningInWithGoogle = false
                                }
                            )
                        }
                    },
                    enabled = !isSigningInWithGoogle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF161922),
                        contentColor = OnyxWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, OnyxRedPrimary.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSigningInWithGoogle) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnyxRedPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificando credencial...", fontSize = 12.sp, color = OnyxWhite)
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Iniciar sesión con Google", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnyxWhite)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast Social Login Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Google Button (Fast trigger)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (!isSigningInWithGoogle && context is Activity) {
                                    isSigningInWithGoogle = true
                                    FirebaseAuthHelper.signInWithGoogle(
                                        activity = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = { profile ->
                                            isSigningInWithGoogle = false
                                            onLoginSuccess(profile)
                                        },
                                        onAuthError = { err ->
                                            isSigningInWithGoogle = false
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        },
                                        scope = coroutineScope,
                                        onAuthCancelled = {
                                            isSigningInWithGoogle = false
                                        }
                                    )
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Instagram Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                processLogin(
                                    email = "usuario_instagram@ig.com",
                                    username = "user_instagram",
                                    name = "Usuario Instagram"
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_instagram),
                                contentDescription = "Instagram",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Facebook Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                processLogin(
                                    email = "usuario_facebook@fb.com",
                                    username = "user_facebook",
                                    name = "Usuario Facebook"
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_facebook),
                                contentDescription = "Facebook",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Text(
                        text = "  o con tu correo  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input fields
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("ej: damianblack68@gmail.com") },
                    trailingIcon = {
                        if (emailInput.trim().equals(adminEmail, ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = OnyxRedPrimary
                            ) {
                                Text("ADMIN", color = OnyxWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                AnimatedVisibility(visible = isRegisterMode) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Nombre de usuario único (@usuario)") },
                            placeholder = { Text("ej: damian_pro") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = displayNameInput,
                            onValueChange = { displayNameInput = it },
                            label = { Text("Nombre visible / Apodo") },
                            placeholder = { Text("ej: Damian Ávila") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Contraseña") },
                    placeholder = { Text("••••••••") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit button
                Button(
                    onClick = {
                        val email = emailInput.trim()
                        if (email.isNotBlank()) {
                            processLogin(email, usernameInput, displayNameInput)
                        } else {
                            Toast.makeText(context, "Por favor ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isRegisterMode) "Crear Cuenta & Generar Pase QR" else "Iniciar Sesión",
                        color = OnyxWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle register/login
                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión aquí" else "¿Nuevo en Onyx Open? Regístrate gratis con tu @usuario",
                        color = OnyxRedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quick Admin shortcut hint
                if (!emailInput.contains("@")) {
                    Text(
                        text = "Tip para el creador: Escribe damianblack68@gmail.com para activar el modo Dueño.",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Real-time Google Account Chooser
        if (showGoogleAccountPicker) {
            AlertDialog(
                onDismissRequest = { showGoogleAccountPicker = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Acceder con Google en tiempo real", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Selecciona una cuenta de Google activa en tu dispositivo o ingresa una nueva:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        // Option 1: Damian Admin Account
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showGoogleAccountPicker = false
                                    processLogin("damianblack68@gmail.com", "damian_avila_admin", "Damian Ávila")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = CircleShape, color = OnyxRedPrimary, modifier = Modifier.size(32.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("D", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Damian Ávila", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = OnyxRedPrimary) {
                                            Text("CREADOR", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                    Text("damianblack68@gmail.com", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Option 2: Custom Real Google Account
                        OutlinedTextField(
                            value = customGoogleEmailInput,
                            onValueChange = { customGoogleEmailInput = it },
                            label = { Text("O escribe tu cuenta Google real") },
                            placeholder = { Text("tu_cuenta@gmail.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                val clean = customGoogleEmailInput.trim()
                                if (clean.contains("@gmail.com", ignoreCase = true) || clean.contains("@", ignoreCase = true)) {
                                    showGoogleAccountPicker = false
                                    val uName = clean.substringBefore("@")
                                    processLogin(clean, uName, uName.capitalize())
                                } else {
                                    Toast.makeText(context, "Por favor ingresa una cuenta válida de Google (@gmail.com)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Entrar con esta cuenta Google", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showGoogleAccountPicker = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

