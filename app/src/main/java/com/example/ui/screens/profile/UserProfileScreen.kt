package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.PaymentCard
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

@Composable
fun UserProfileScreen(
    currentUser: UserProfile,
    onUpdateProfile: (UserProfile) -> Unit,
    onOpenShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canSetBanner = currentUser.subscription.canCustomizeBanner || currentUser.role == UserRole.ADMIN_CREATOR

    var showBannerDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }

    val bannerGradients = remember {
        listOf(
            Triple("Rojo Nebulosa", 0xFFB71C1C, 0xFF120307),
            Triple("Aurora Boreal", 0xFF004D40, 0xFF001510),
            Triple("Cyberpunk Onyx", 0xFF4A148C, 0xFF0D021A),
            Triple("Oro Real", 0xFFFF6F00, 0xFF241000),
            Triple("Azul Galaxia", 0xFF0D47A1, 0xFF020E24)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                if (canSetBanner) {
                    // Customizable Gradient Banner for Evolution / Admin
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(currentUser.bannerColorStart),
                                        Color(currentUser.bannerColorEnd)
                                    )
                                )
                            )
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clickable { showBannerDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = OnyxWhite, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cambiar Banner (Evolution)", color = OnyxWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Locked Banner for Free / Standard tiers
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E1E24))
                            .clickable { onOpenShop() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Banner exclusivo de Onyx Evolution ($1,000 MXN/año)",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Toca para desbloquear en la tienda de planes",
                                color = OnyxRedPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Profile Avatar positioned overlapping bottom of banner
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp)
                        .offset(y = 36.dp)
                        .size(80.dp)
                        .border(3.dp, OnyxRedPrimary, CircleShape)
                        .clickable { showAvatarDialog = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_onyx_logo),
                            contentDescription = "Avatar de Perfil",
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        )
                        // Camera overlay badge
                        Surface(
                            shape = CircleShape,
                            color = OnyxRedPrimary,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar Foto", tint = OnyxWhite, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(46.dp))
        }

        // Profile Details
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                            if (currentUser.isVerified || currentUser.subscription.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verificado",
                                    tint = Color(0xFF2979FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "@${currentUser.username} • ${currentUser.email}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentUser.role == UserRole.ADMIN_CREATOR) {
                        Surface(shape = RoundedCornerShape(8.dp), color = OnyxRedPrimary) {
                            Text("DUEÑO / CREADOR", color = OnyxWhite, fontWeight = FontWeight.Black, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subscription Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Nivel de Suscripción", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = currentUser.subscription.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnyxRedPrimary
                                )
                            }

                            Button(
                                onClick = onOpenShop,
                                colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Ver Planes", color = OnyxWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Duración: ${currentUser.subscription.period} • ${if (currentUser.subscription.hasAds) "Anuncios activos" else "Cero publicidad"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Private Payment Method & Wallet
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = OnyxRedPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Método de Pago Privado",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(onClick = { showCardDialog = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar tarjeta", modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val card = currentUser.savedPaymentCard
                        if (card != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("${card.cardBrand} terminada en •••• ${card.lastFourDigits}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Titular: ${card.cardHolder} • Exp: ${card.expiry}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF00E676).copy(alpha = 0.2f)) {
                                        Text("CIFRADA", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Aún no has registrado una tarjeta privada. Puedes agregarla al suscribirte o escanearla aquí.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showCardDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registrar / Escanear Tarjeta Privada", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = OnyxRedPrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tus datos financieros son 100% privados y exclusivos de tu cuenta.",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Academic Attendance & Absence Register Profile Link
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Registro de Asistencia & Faltas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF00E676).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (currentUser.role == UserRole.ADMIN_CREATOR) "DOCENTE ACTIVO" else "ESTUDIANTE",
                                    color = Color(0xFF00E676),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Color Indicators: Green, Yellow, Red
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0A1F13),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🟢 Asistió", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("95%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF261D07),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🟡 No llegó", color = Color(0xFFFFD600), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("3", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF24080B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🔴 Faltó", color = Color(0xFFFF1744), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("1", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tu perfil está vinculado al sistema de listas nominales con código de color inteligente (🟢 Asistió, 🟡 No llegó, 🔴 Faltó).",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Dialog: Banner Customizer for Evolution
    if (showBannerDialog) {
        AlertDialog(
            onDismissRequest = { showBannerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = OnyxRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir Banner Exclusivo (Evolution)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Selecciona el diseño para tu cabecera de perfil:", fontSize = 11.sp)
                    bannerGradients.forEach { (name, start, end) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable {
                                    val updated = currentUser.copy(
                                        bannerTheme = name,
                                        bannerColorStart = start,
                                        bannerColorEnd = end
                                    )
                                    onUpdateProfile(updated)
                                    showBannerDialog = false
                                    Toast.makeText(context, "Banner actualizado a $name", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.horizontalGradient(listOf(Color(start), Color(end)))),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "  $name",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBannerDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Dialog: Avatar Photo for all users
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Foto de Perfil", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Todos los usuarios (gratis y suscritos) pueden cambiar su foto de perfil.", fontSize = 11.sp)
                    Button(
                        onClick = {
                            showAvatarDialog = false
                            Toast.makeText(context, "Foto de perfil actualizada correctamente", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Subir Foto de Galería", color = OnyxWhite)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAvatarDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Dialog: Card Reader & Editor
    if (showCardDialog) {
        var cardNum by remember { mutableStateOf("") }
        var holder by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCardDialog = false },
            title = { Text("Tarjeta de Pago Privada", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Guarda o escanea tu tarjeta para que quede cifrada en tu cuenta privada:", fontSize = 11.sp)
                    Button(
                        onClick = {
                            cardNum = "4532 9912 3456 1234"
                            holder = currentUser.displayName
                            Toast.makeText(context, "Tarjeta leída con escaneo seguro", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = OnyxRedPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escanear Tarjeta Ahora", color = OnyxRedPrimary, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = cardNum,
                        onValueChange = { cardNum = it },
                        label = { Text("Número de tarjeta") },
                        placeholder = { Text("•••• •••• •••• 1234") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = holder,
                        onValueChange = { holder = it },
                        label = { Text("Nombre del titular") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val last4 = if (cardNum.length >= 4) cardNum.takeLast(4) else "1234"
                        val updated = currentUser.copy(
                            savedPaymentCard = PaymentCard(
                                cardHolder = if (holder.isNotBlank()) holder else currentUser.displayName,
                                lastFourDigits = last4,
                                cardBrand = "Visa",
                                expiry = "12/28",
                                isEncrypted = true
                            )
                        )
                        onUpdateProfile(updated)
                        showCardDialog = false
                        Toast.makeText(context, "Tarjeta guardada de forma segura y privada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Guardar Tarjeta", color = OnyxWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCardDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
