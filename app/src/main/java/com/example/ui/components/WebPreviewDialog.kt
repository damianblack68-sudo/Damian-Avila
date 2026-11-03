package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.OnyxNotificationHelper
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

enum class EmulatorDeviceType(val label: String, val widthDp: Int, val heightDp: Int) {
    PHONE_PORTRAIT("Móvil (375x720)", 360, 680),
    PHONE_LANDSCAPE("Móvil Horizontal", 640, 360),
    TABLET("Tablet (540x720)", 540, 700)
}

/**
 * OnyxAppEmulatorStudio (WebPreviewDialog):
 * Emulador de dispositivo móvil interactivo integrado directamente dentro de Onyx Open.
 * Permite a los usuarios ver, ejecutar, rotar y probar sus aplicaciones creadas
 * dentro de un chasis realista de smartphone con barra de estado, cámara frontal y consola JS.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPreviewDialog(
    title: String,
    htmlContent: String,
    onDismiss: () -> Unit,
    onAskChanges: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var selectedDevice by remember { mutableStateOf(EmulatorDeviceType.PHONE_PORTRAIT) }
    var showConsoleLogs by remember { mutableStateOf(false) }
    val consoleLogs = remember { mutableStateListOf<String>() }

    // Enviar notificación en tiempo real al abrir el emulador
    LaunchedEffect(title) {
        OnyxNotificationHelper.sendRealtimeNotification(
            context = context,
            notificationId = 101,
            channelId = OnyxNotificationHelper.CHANNEL_APP_CREATION,
            title = "App Lista en el Emulador Onyx 📱",
            message = "Tu aplicación '$title' está lista y ejecutándose en el emulador interactivo."
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07080C)),
            color = Color(0xFF07080C)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Top Control Bar: Emulator Title & Device Options
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10121A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232635)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "EMULADOR ONYX • $title",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = OnyxRedPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Entorno de Prueba Privado & Seguro (Offline)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = Color(0xFF9E9EA7)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Device Mode Toggle
                            IconButton(
                                onClick = {
                                    selectedDevice = when (selectedDevice) {
                                        EmulatorDeviceType.PHONE_PORTRAIT -> EmulatorDeviceType.PHONE_LANDSCAPE
                                        EmulatorDeviceType.PHONE_LANDSCAPE -> EmulatorDeviceType.TABLET
                                        EmulatorDeviceType.TABLET -> EmulatorDeviceType.PHONE_PORTRAIT
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = when (selectedDevice) {
                                        EmulatorDeviceType.PHONE_PORTRAIT -> Icons.Default.Smartphone
                                        EmulatorDeviceType.PHONE_LANDSCAPE -> Icons.Default.ScreenRotation
                                        EmulatorDeviceType.TABLET -> Icons.Default.Tablet
                                    },
                                    contentDescription = "Cambiar dispositivo",
                                    tint = OnyxWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Console Log Inspector
                            IconButton(
                                onClick = { showConsoleLogs = !showConsoleLogs },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeveloperMode,
                                    contentDescription = "Consola JS",
                                    tint = if (showConsoleLogs) OnyxRedPrimary else OnyxWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Reload
                            IconButton(
                                onClick = {
                                    consoleLogs.clear()
                                    webViewRef?.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Recargar app",
                                    tint = OnyxWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Copy Code button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("$title.html", htmlContent)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "¡Código copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("emulator_copy_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar código",
                                    tint = OnyxWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Pedir cambios button
                            if (onAskChanges != null) {
                                IconButton(
                                    onClick = {
                                        onDismiss()
                                        onAskChanges()
                                    },
                                    modifier = Modifier.size(32.dp).testTag("emulator_ask_changes_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Pedir cambios",
                                        tint = Color(0xFF64D2FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Share / Export code
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/html"
                                        putExtra(Intent.EXTRA_TEXT, htmlContent)
                                        putExtra(Intent.EXTRA_SUBJECT, "$title.html")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Exportar Código de App"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Exportar",
                                    tint = OnyxWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Close Emulator
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = OnyxWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // CENTER: REALISTIC SMARTPHONE CHASSIS EMULATOR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Smartphone Hardware Bezel
                    Surface(
                        modifier = Modifier
                            .widthIn(max = selectedDevice.widthDp.dp)
                            .heightIn(max = selectedDevice.heightDp.dp)
                            .padding(4.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xFF13151D),
                        border = androidx.compose.foundation.BorderStroke(
                            2.5.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2C2F3E),
                                    OnyxRedPrimary.copy(alpha = 0.5f),
                                    Color(0xFF1A1C25)
                                )
                            )
                        ),
                        shadowElevation = 20.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Simulated Phone Notch / Punch-hole Camera
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "09:41",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB0B5C6)
                                )
                                // Front camera & speaker cutout
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF090A0E))
                                            .border(1.dp, Color(0xFF282C3D), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF1D202C))
                                    )
                                }
                                Text(
                                    text = "5G • 100%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB0B5C6)
                                )
                            }

                            // Interactive Screen Viewport running the generated App
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color.White)
                            ) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.useWideViewPort = true
                                            settings.loadWithOverviewMode = true
                                            settings.allowContentAccess = true
                                            webViewClient = WebViewClient()
                                            webChromeClient = object : WebChromeClient() {
                                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                                    consoleMessage?.let {
                                                        consoleLogs.add("[${it.messageLevel()}]: ${it.message()} (Línea ${it.lineNumber()})")
                                                    }
                                                    return true
                                                }
                                            }
                                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                                            webViewRef = this
                                        }
                                    },
                                    update = { view ->
                                        webViewRef = view
                                    }
                                )
                            }

                            // Simulated Home Gesture Indicator Bar
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .width(84.dp)
                                    .height(3.5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF4A4E63))
                            )
                        }
                    }
                }

                // Collapsible JavaScript Console Drawer
                AnimatedVisibility(visible = showConsoleLogs) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xF2090A0E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25293A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONSOLA JS • ${consoleLogs.size} EVENTOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnyxRedPrimary
                                )
                                Text(
                                    text = "Limpiar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF888E9F),
                                    modifier = Modifier.clickable { consoleLogs.clear() }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (consoleLogs.isEmpty()) {
                                Text(
                                    text = "Sin errores de script. La aplicación se ejecuta de forma óptima.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFF00E676)
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(consoleLogs) { log ->
                                        Text(
                                            text = log,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = if (log.contains("ERROR")) Color(0xFFFF5252) else OnyxWhite,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Footer Device Info & Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dispositivo: ${selectedDevice.label} • Aislamiento Privado",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color(0xFF8E93A4)
                    )

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Cerrar Emulador", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
