package com.example.ui.screens.contact

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.R
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OwnerChatMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String, // "BOT", "USER", "OWNER"
    val text: String,
    val options: List<String> = emptyList(),
    val isOwnerAlert: Boolean = false
)

@Composable
fun ContactOwnerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Chat Asistente Virtual con el Dueño, 1: Redes Directas

    // Interactive Assistant Chat State
    var selectedProblemCategory by remember { mutableStateOf<String?>(null) }
    var userCustomMessage by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()

    val defaultOptions = listOf(
        "💡 Pedir nueva función para la app",
        "🛠️ Reportar un error o bug",
        "📐 Duda sobre el motor matemático",
        "🎨 Personalización a medida",
        "💬 Hablar directamente con Damian"
    )

    var messages by remember {
        mutableStateOf(
            listOf(
                OwnerChatMessage(
                    id = 1L,
                    sender = "BOT",
                    text = "¡Hola! Soy el Asistente Virtual de Enlace de Onyx Open.\n\n¿Cuál es el problema o consulta que tienes? ¿Te gustaría contactar con el dueño directamente desde aquí o desde sus redes sociales?",
                    options = defaultOptions
                )
            )
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            chatListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = OnyxRedPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Chat Asistente con el Dueño", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Redes Sociales Directas", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (selectedTab == 0) {
            // Interactive Chat View
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Header badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E676),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Damian Ávila (Dueño) • En línea para atención directa",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Chat Messages
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.sender == "USER"
                        val isOwner = msg.sender == "OWNER"

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            // Sender Title
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isUser) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isOwner) OnyxRedPrimary else Color(0xFF1877F2),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (isOwner) "D" else "IA",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isOwner) "Damian Ávila (Dueño)" else "Asistente Virtual",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOwner) OnyxRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Tú",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Message Bubble
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isUser) 14.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 14.dp
                                ),
                                color = if (msg.isOwnerAlert) {
                                    Color(0xFFFF9100).copy(alpha = 0.15f)
                                } else if (isUser) {
                                    OnyxRedPrimary
                                } else if (isOwner) {
                                    Color(0xFF1B1D28)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier
                                    .widthIn(max = 320.dp)
                                    .border(
                                        width = if (msg.isOwnerAlert) 1.dp else 0.dp,
                                        color = if (msg.isOwnerAlert) Color(0xFFFF9100) else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isUser) OnyxWhite else MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp,
                                        fontSize = 12.sp
                                    )

                                    // Option Buttons
                                    if (msg.options.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        msg.options.forEach { opt ->
                                            FilledTonalButton(
                                                onClick = {
                                                    // User selects an option
                                                    val userMsg = OwnerChatMessage(
                                                        id = System.currentTimeMillis(),
                                                        sender = "USER",
                                                        text = opt
                                                    )
                                                    messages = messages + userMsg
                                                    selectedProblemCategory = opt

                                                    coroutineScope.launch {
                                                        delay(500)
                                                        // Bot asks where they want to contact
                                                        val followUp = OwnerChatMessage(
                                                            id = System.currentTimeMillis() + 1,
                                                            sender = "BOT",
                                                            text = "Entendido: \"$opt\".\n¿Te gustaría contactar con Damian (el dueño) directamente desde aquí en la app o a través de sus redes sociales?",
                                                            options = listOf(
                                                                "📲 Hablar con Damian aquí en la app",
                                                                "🟢 Escribir a su WhatsApp (+52 55 2815 2667)",
                                                                "🟣 Enviar DM a su Instagram (@damian_18avila)"
                                                            )
                                                        )
                                                        messages = messages + followUp
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = opt,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Bar for custom problem message
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userCustomMessage,
                            onValueChange = { userCustomMessage = it },
                            placeholder = { Text("Escribe tu duda o problema aquí...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val text = userCustomMessage.trim()
                                if (text.isNotBlank()) {
                                    userCustomMessage = ""
                                    val userMsg = OwnerChatMessage(
                                        id = System.currentTimeMillis(),
                                        sender = "USER",
                                        text = text
                                    )
                                    messages = messages + userMsg

                                    coroutineScope.launch {
                                        delay(600)
                                        // Chatbot triggers the notification to the owner
                                        val category = selectedProblemCategory ?: "Consulta general de usuario"
                                        val alertMsg = OwnerChatMessage(
                                            id = System.currentTimeMillis() + 1,
                                            sender = "BOT",
                                            text = "🔔 ¡Alerta para Damian (Dueño)!:\n\nUn cliente quiere hablar contigo.\n• Asunto: $category\n• Mensaje del cliente: \"$text\"\n\n¡Damian ha sido notificado!",
                                            isOwnerAlert = true
                                        )

                                        messages = messages + alertMsg
                                        delay(800)

                                        // Owner joins the chat
                                        val ownerReply = OwnerChatMessage(
                                            id = System.currentTimeMillis() + 2,
                                            sender = "OWNER",
                                            text = "¡Hola! Soy Damian Ávila, dueño de Onyx Open. Ya recibí tu notificación sobre '$category'. Si quieres que le metamos algo nuevo a la app, un cambio en el estudio o resolver algún detalle, dime con confianza y lo hacemos realidad."
                                        )
                                        messages = messages + ownerReply
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(OnyxRedPrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = OnyxWhite)
                        }
                    }
                }
            }
        } else {
            // Direct Social Media Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // WhatsApp Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=525528152667&text=${Uri.encode("¡Hola Damian! Me comunico directamente por Onyx Open.")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Abriendo WhatsApp: +52 55 2815 2667", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366).copy(alpha = 0.15f))
                                    .padding(8.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "WhatsApp Directo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF25D366).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "+52 55 2815 2667",
                                            color = Color(0xFF25D366),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Chatea directamente con Damian Ávila en tiempo real.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Instagram Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val url = "https://www.instagram.com/damian_18avila?rpxt=MWczeXJqeGJ0cXY0"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Abriendo Instagram: @damian_18avila", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_instagram),
                                contentDescription = "Instagram",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE1306C).copy(alpha = 0.15f))
                                    .padding(8.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Instagram Oficial",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE1306C).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "@damian_18avila",
                                            color = Color(0xFFE1306C),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Abre el perfil directo de Damian Ávila para enviar DM y novedades.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFFE1306C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Facebook Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val url = "https://www.facebook.com/"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo abrir Facebook", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_facebook),
                                contentDescription = "Facebook",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1877F2).copy(alpha = 0.15f))
                                    .padding(8.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Facebook & Comunidad",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Visita nuestra página oficial para conectar con la comunidad.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
