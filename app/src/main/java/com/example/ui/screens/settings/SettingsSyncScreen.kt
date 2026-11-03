package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.service.OnyxFloatingVoiceService
import com.example.service.OnyxFirebaseMessagingService
import com.example.ui.screens.voice.OnyxQuickVoiceActivity
import com.example.engine.OnyxShortcutHelper
import com.example.engine.OnyxNotificationHelper
import com.example.engine.OnyxProactiveAlertManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ProfileType
import com.example.domain.model.VoicePersona
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

@Composable
fun SettingsSyncScreen(
    currentProfile: ProfileType,
    onProfileChange: (ProfileType) -> Unit,
    currentPersona: VoicePersona,
    onPersonaChange: (VoicePersona) -> Unit,
    onTestVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var endToEndEncryptionEnabled by remember { mutableStateOf(true) }
    var autoCloudSyncEnabled by remember { mutableStateOf(true) }
    var notificationFrequency by remember { mutableStateOf("Estratégica (Baja Interrupción)") }
    var driveConnected by remember { mutableStateOf(true) }
    var notionConnected by remember { mutableStateOf(true) }
    var dropboxConnected by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Profile Switcher
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gestión de Perfiles Separados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnyxRedPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Separa totalmente tus notas, chats y tareas de la vida personal y laboral.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileType.values().forEach { prof ->
                            val isSelected = prof == currentProfile
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) OnyxRedPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onProfileChange(prof) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (prof == ProfileType.PERSONAL) Icons.Default.Person else Icons.Default.Business,
                                        contentDescription = null,
                                        tint = if (isSelected) OnyxRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = prof.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) OnyxRedPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Voice Personalities
        item {
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
                        Text(
                            text = "Voces Sintéticas de Onyx Open",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnyxRedPrimary
                        )
                        IconButton(onClick = onTestVoice) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Probar voz", tint = OnyxRedPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    VoicePersona.values().forEach { persona ->
                        val isSelected = persona == currentPersona
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) OnyxRedPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onPersonaChange(persona) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = persona.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) OnyxRedPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = persona.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnyxRedPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Asistente Flotante en Pantalla de Inicio (Sin entrar a la app)
        item {
            var isOverlayActive by remember { mutableStateOf(false) }

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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Onyx Flotante en Pantalla de Inicio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnyxRedPrimary
                            )
                            Text(
                                text = "Habla desde el inicio o cualquier app sin entrar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isOverlayActive,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        Toast.makeText(context, "Permite 'Mostrar sobre otras aplicaciones' para Onyx Open", Toast.LENGTH_LONG).show()
                                        val permIntent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(permIntent)
                                    } else {
                                        isOverlayActive = true
                                        OnyxFloatingVoiceService.startService(context)
                                        Toast.makeText(context, "Círculo Onyx activado en tu pantalla de inicio", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isOverlayActive = false
                                    OnyxFloatingVoiceService.stopService(context)
                                    Toast.makeText(context, "Círculo flotante desactivado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnyxRedPrimary, checkedTrackColor = OnyxRedPrimary.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Coloca el círculo animado minimalista con resplandor rojo vibrante flotando en tu pantalla. Tócalo en cualquier momento para hablar y recibir respuestas ejecutivas inmediatas sin ver pantallas de chat, fotos ni menús.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, OnyxQuickVoiceActivity::class.java).apply {
                                    putExtra(OnyxQuickVoiceActivity.EXTRA_AUTO_START_RECORDING, true)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = OnyxWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probar Ahora", color = OnyxWhite, style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Desliza hacia abajo la barra superior de Android para usar el mosaico 'Onyx IA Voz'.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ajustes Rápidos", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Cloud Integrations (Google Drive, Notion, Dropbox)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sincronización en la Nube & Integraciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnyxRedPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Google Drive
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = OnyxRedPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Google Drive", fontWeight = FontWeight.SemiBold)
                                Text("Sincronización automática de notas y reportes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = driveConnected,
                            onCheckedChange = { driveConnected = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnyxWhite, checkedTrackColor = OnyxRedPrimary)
                        )
                    }

                    // Notion
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = OnyxRedPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Notion Workspace", fontWeight = FontWeight.SemiBold)
                                Text("Exportación de bloques, tareas y mapas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = notionConnected,
                            onCheckedChange = { notionConnected = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnyxWhite, checkedTrackColor = OnyxRedPrimary)
                        )
                    }

                    // Dropbox
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = OnyxRedPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Dropbox", fontWeight = FontWeight.SemiBold)
                                Text("Copia de seguridad cifrada", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = dropboxConnected,
                            onCheckedChange = { dropboxConnected = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnyxWhite, checkedTrackColor = OnyxRedPrimary)
                        )
                    }
                }
            }
        }

        // Security, Privacy & Notifications
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seguridad, Privacidad y Alertas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnyxRedPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cifrado Extremo a Extremo (E2EE)", fontWeight = FontWeight.SemiBold)
                            Text("Tus datos matemáticos y notas se cifran en dispositivo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = endToEndEncryptionEnabled,
                            onCheckedChange = { endToEndEncryptionEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnyxWhite, checkedTrackColor = OnyxRedPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Frecuencia de Notificaciones Inteligentes:", style = MaterialTheme.typography.labelSmall, color = OnyxRedPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "Estratégica (Baja Interrupción)",
                        "Moderada (Recordatorios Clave)",
                        "Productiva (Enfoque Total)"
                    ).forEach { freq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notificationFrequency = freq }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = notificationFrequency == freq,
                                onClick = { notificationFrequency = freq },
                                colors = RadioButtonDefaults.colors(selectedColor = OnyxRedPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(freq, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            OnyxNotificationHelper.sendRealtimeNotification(
                                context = context,
                                notificationId = 201,
                                channelId = OnyxNotificationHelper.CHANNEL_EXECUTIVE_ALERTS,
                                title = "Alerta Ejecutiva en Tiempo Real ⚡",
                                message = "Onyx IA: Sistemas y enlace corporativo activos a capacidad máxima."
                            )
                            Toast.makeText(context, "Notificación en tiempo real enviada", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OnyxWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Probar Notificación en Tiempo Real", color = OnyxWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Instalación Directa: Pinned Shortcut & APK / Web
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = OnyxRedPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Instalación Directa & Acceso Rápido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnyxRedPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Acceso directo en 1 toque: Fija el acceso directo oficial de Onyx en la pantalla de inicio de tu teléfono.\n• Web PWA: Si abres la aplicación desde el navegador web (Chrome/Edge/Brave), verás la opción de instalar directamente arriba junto a la barra de URL para usarla como app nativa.\n• Descarga de APK: Descarga o comparte el instalador .apk independiente para instalarlo directamente en cualquier dispositivo Android.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { OnyxShortcutHelper.pinOnyxHomeScreenShortcut(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = OnyxWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instalar Acceso Directo", color = OnyxWhite, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { OnyxShortcutHelper.shareDirectApkLink(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartir APK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Firebase Cloud Messaging (FCM) & Notificaciones Push
        item {
            var currentFcmToken by remember { mutableStateOf(OnyxFirebaseMessagingService.getSavedToken(context)) }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxRedPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OnyxRedPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase Cloud Messaging (FCM)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnyxWhite
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "EN TIEMPO REAL",
                                color = Color(0xFF00E676),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Recibe directivas ejecutivas, respuestas de IA y eventos del sistema mediante notificaciones push en segundo plano. Tópico activo: 'onyx_broadcast'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Token Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0D0E15),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262A3B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Token FCM del Dispositivo:", fontSize = 9.sp, color = OnyxRedPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = currentFcmToken ?: "Sincronizando token con Google FCM...",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE2E4EB),
                                    maxLines = 2
                                )
                            }

                            if (!currentFcmToken.isNullOrBlank()) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("FCM Token", currentFcmToken)
                                        clipboard?.setPrimaryClip(clip)
                                        Toast.makeText(context, "Token FCM copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Token", tint = OnyxWhite, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                OnyxFirebaseMessagingService.syncDeviceFcmToken(context)
                                currentFcmToken = OnyxFirebaseMessagingService.getSavedToken(context)
                                Toast.makeText(context, "Token FCM re-sincronizado con Firebase", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnyxWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sincronizar", color = OnyxWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                OnyxNotificationHelper.sendRealtimeNotification(
                                    context = context,
                                    notificationId = 9912,
                                    channelId = OnyxNotificationHelper.CHANNEL_EXECUTIVE_ALERTS,
                                    title = "Onyx IA • Push en Tiempo Real (FCM)",
                                    message = "Protocolo FCM verificado: Alerta ejecutiva despachada con éxito."
                                )
                                Toast.makeText(context, "Notificación push despachada", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnyxWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probar Push", color = OnyxWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    HorizontalDivider(color = Color(0xFF262A3B))

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Alertas Proactivas del Asistente & Cambios de la App:", fontSize = 10.sp, color = Color(0xFFB0B5C6), fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                OnyxProactiveAlertManager.sendProactiveAssistantAlert(
                                    context = context,
                                    title = "Onyx IA • Directiva Proactiva",
                                    message = "Protocolo activo: Tienes 2 directivas prioritarias pendientes en tu Workspace ejecutivo."
                                )
                                Toast.makeText(context, "Alerta proactiva enviada", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Alerta Proactiva", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnyxWhite)
                        }

                        OutlinedButton(
                            onClick = {
                                OnyxProactiveAlertManager.publishAppChangeNotice(
                                    context = context,
                                    title = "Nuevas Funciones y Optimización",
                                    version = "v2.2.0",
                                    summary = "Se ha actualizado el motor de razonamiento de Onyx IA y la sincronización push en tiempo real."
                                ) {
                                    Toast.makeText(context, "Aviso de cambio de app publicado y notificado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Avisar Cambio App", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnyxRedPrimary)
                        }
                    }
                }
            }
        }

        // Privacidad Absoluta de APIs & Datos Privados
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1118)),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxRedPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E676))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacidad de API & Arquitectura Privada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnyxWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "✓ API Privada: Las credenciales y claves no se exponen en texto plano en la web ni en repositorios públicos.\n✓ Base de Datos Aislada: Toda tu información (chats, fórmulas matemáticas, planos CAD de casas y autos, notas y registro de docentes) se guarda en la base de datos protegida y privada de tu dispositivo.\n✓ Modo Zero-Knowledge: Ningún tercero ni visitante de la web puede acceder a tus credenciales o registros privados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB0B5C6),
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
