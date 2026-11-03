package com.example.ui.screens.messenger

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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.domain.model.AttachmentMediaType
import com.example.domain.model.ChatAttachment
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.PeerContact
import com.example.domain.model.PeerMessage
import com.example.domain.model.UserProfile
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMessengerScreen(
    currentUser: UserProfile,
    onShowMyQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var contacts by remember {
        mutableStateOf(
            listOf(
                PeerContact(
                    username = "carlos_ia",
                    displayName = "Carlos Mendoza",
                    email = "carlos@gmail.com",
                    qrCodeId = "ONX-carlos_ia",
                    lastMessage = "¿Pudiste resolver la ecuación del examen?",
                    lastTimestamp = "14:22",
                    isBlocked = false,
                    chatCustomThemeColor = 0xFFFF2A54
                ),
                PeerContact(
                    username = "sofia_dev",
                    displayName = "Sofía Ramos",
                    email = "sofia@outlook.com",
                    qrCodeId = "ONX-sofia_dev",
                    lastMessage = "Te compartí el mapa mental de cálculo en Notion",
                    lastTimestamp = "11:05",
                    isNearby = true,
                    isBlocked = false,
                    chatCustomThemeColor = 0xFF2979FF
                ),
                PeerContact(
                    username = "prof_ramirez",
                    displayName = "Prof. Ramírez (Matemáticas)",
                    email = "ramirez@unam.mx",
                    qrCodeId = "ONX-prof_ramirez",
                    lastMessage = "Excelente deducción paso a paso.",
                    lastTimestamp = "Ayer",
                    isBlocked = false,
                    chatCustomThemeColor = 0xFF00E676
                )
            )
        )
    }

    var selectedContact by remember { mutableStateOf<PeerContact?>(null) }
    var p2pMessages by remember {
        mutableStateOf(
            listOf(
                PeerMessage(
                    id = 1L,
                    senderUsername = "carlos_ia",
                    receiverUsername = currentUser.username,
                    text = "¡Hola! ¿Usaste el motor matemático de Onyx para el informe?",
                    timestamp = System.currentTimeMillis() - 100000
                ),
                PeerMessage(
                    id = 2L,
                    senderUsername = currentUser.username,
                    receiverUsername = "carlos_ia",
                    text = "Sí, resolvió la parábola y me dio el gráfico en un segundo. Es buenísimo.",
                    timestamp = System.currentTimeMillis() - 50000
                )
            )
        )
    }

    var textInput by remember { mutableStateOf("") }
    var pendingP2pAttachment by remember { mutableStateOf<ChatAttachment?>(null) }
    var showP2pAttachmentOptions by remember { mutableStateOf(false) }

    var showScanQrDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<PeerContact?>(null) }
    var showBlockConfirmDialog by remember { mutableStateOf<PeerContact?>(null) }
    var showReportDialog by remember { mutableStateOf<PeerContact?>(null) }
    var showCustomizeChatDialog by remember { mutableStateOf(false) }
    var showChatOptionsDropdown by remember { mutableStateOf(false) }
    var newContactUsernameInput by remember { mutableStateOf("") }
    var newContactDisplayNameInput by remember { mutableStateOf("") }

    val chatListState = rememberLazyListState()

    // Pickers for peer chat attachments
    val p2pPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingP2pAttachment = ChatAttachment(
                uri = it.toString(),
                name = "foto_${System.currentTimeMillis() % 10000}.jpg",
                mediaType = AttachmentMediaType.IMAGE,
                sizeFormatted = "1.5 MB",
                mimeType = "image/jpeg"
            )
        }
    }

    val p2pVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingP2pAttachment = ChatAttachment(
                uri = it.toString(),
                name = "video_${System.currentTimeMillis() % 10000}.mp4",
                mediaType = AttachmentMediaType.VIDEO,
                sizeFormatted = "8.2 MB",
                mimeType = "video/mp4"
            )
        }
    }

    val p2pFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingP2pAttachment = ChatAttachment(
                uri = it.toString(),
                name = "archivo_${System.currentTimeMillis() % 10000}.pdf",
                mediaType = AttachmentMediaType.DOCUMENT,
                sizeFormatted = "2.1 MB",
                mimeType = "application/pdf"
            )
        }
    }

    LaunchedEffect(p2pMessages.size) {
        if (p2pMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(p2pMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (selectedContact == null) {
            // Contacts & Chats List (WhatsApp Style)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header with actions: Add QR contact & My QR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mensajería Privada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = OnyxRedPrimary
                        )
                        Text(
                            text = "Chats cifrados independientes tipo WhatsApp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onShowMyQr,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = "Mi QR", tint = OnyxRedPrimary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { showScanQrDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OnyxRedPrimary)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Guardar Contacto", tint = OnyxWhite, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nearby Devices banner if Evolution or Admin
                if (currentUser.subscription.hasNearbyConnect || currentUser.role == com.example.domain.model.UserRole.ADMIN_CREATOR) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OnyxRedPrimary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Buscando usuarios cercanos con Onyx Evolution...", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = OnyxRedPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Dispositivos Cercanos Activo (Evolution)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = OnyxRedPrimary
                                )
                                Text(
                                    text = "1 contacto detectado cerca para sincronizar",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Chat items
                if (contacts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No tienes contactos guardados", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(onClick = { showScanQrDialog = true }) {
                                Text("Guardar tu primer contacto con QR", color = OnyxRedPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contacts, key = { it.username }) { contact ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedContact = contact }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val avatarColor = Color(contact.chatCustomThemeColor)
                                    Surface(
                                        shape = CircleShape,
                                        color = avatarColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = contact.displayName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = avatarColor,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = contact.displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (contact.isBlocked) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFFF1744)
                                                    ) {
                                                        Text("BLOQUEADO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = contact.lastTimestamp,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (contact.isBlocked) "Contacto bloqueado" else contact.lastMessage,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Fast Delete Action
                                            IconButton(
                                                onClick = { showDeleteConfirmDialog = contact },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Borrar contacto",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Selected Contact Private Chat Room (P2P Room)
            val contact = selectedContact!!
            val themeColor = Color(contact.chatCustomThemeColor)

            Column(modifier = Modifier.fillMaxSize()) {
                // Chat Room Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedContact = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }

                        Surface(
                            shape = CircleShape,
                            color = themeColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = contact.displayName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (contact.isBlocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(Bloqueado)", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "@${contact.username} • Cifrado extremo a extremo",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Customize Chat Color
                        IconButton(onClick = { showCustomizeChatDialog = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Personalizar Chat", tint = themeColor)
                        }

                        // More options menu: Bloquear / Borrar
                        Box {
                            IconButton(onClick = { showChatOptionsDropdown = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }

                            DropdownMenu(
                                expanded = showChatOptionsDropdown,
                                onDismissRequest = { showChatOptionsDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Personalizar Fondo y Color") },
                                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = themeColor) },
                                    onClick = {
                                        showChatOptionsDropdown = false
                                        showCustomizeChatDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Silenciar Notificaciones") },
                                    leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) },
                                    onClick = {
                                        showChatOptionsDropdown = false
                                        Toast.makeText(context, "Notificaciones silenciadas por 8 horas", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reportar Chat", color = Color(0xFFFF9100)) },
                                    leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = Color(0xFFFF9100)) },
                                    onClick = {
                                        showChatOptionsDropdown = false
                                        showReportDialog = contact
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (contact.isBlocked) "Desbloquear Contacto" else "Bloquear Contacto", color = Color.Red) },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) },
                                    onClick = {
                                        showChatOptionsDropdown = false
                                        if (contact.isBlocked) {
                                            val updatedStatus = false
                                            contacts = contacts.map {
                                                if (it.username == contact.username) it.copy(isBlocked = updatedStatus) else it
                                            }
                                            selectedContact = contact.copy(isBlocked = updatedStatus)
                                            Toast.makeText(context, "Contacto desbloqueado", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showBlockConfirmDialog = contact
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar Chat", color = Color.Red) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                    onClick = {
                                        showChatOptionsDropdown = false
                                        showDeleteConfirmDialog = contact
                                    }
                                )
                            }
                        }
                    }
                }

                // Blocked banner if active
                if (contact.isBlocked) {
                    Surface(
                        color = Color(0xFF2C0B0E),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Has bloqueado a este contacto", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = {
                                contacts = contacts.map {
                                    if (it.username == contact.username) it.copy(isBlocked = false) else it
                                }
                                selectedContact = contact.copy(isBlocked = false)
                                Toast.makeText(context, "Contacto desbloqueado", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Desbloquear", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val messagesForThisChat = p2pMessages.filter {
                        (it.senderUsername == currentUser.username && it.receiverUsername == contact.username) ||
                                (it.senderUsername == contact.username && it.receiverUsername == currentUser.username)
                    }

                    items(messagesForThisChat, key = { it.id }) { msg ->
                        val isMe = msg.senderUsername == currentUser.username

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isMe) 14.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 14.dp
                                ),
                                color = if (isMe) themeColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .widthIn(min = 40.dp, max = 280.dp)
                                    .wrapContentWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .wrapContentWidth()
                                ) {
                                    // Attached Media inside Bubble
                                    if (msg.attachment != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isMe) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .padding(bottom = 4.dp)
                                                .wrapContentWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = when (msg.attachment.mediaType) {
                                                        AttachmentMediaType.IMAGE -> Icons.Default.Image
                                                        AttachmentMediaType.VIDEO -> Icons.Default.VideoFile
                                                        AttachmentMediaType.DOCUMENT -> Icons.Default.Description
                                                    },
                                                    contentDescription = null,
                                                    tint = if (isMe) OnyxWhite else themeColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = msg.attachment.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isMe) OnyxWhite else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    if (msg.text.isNotBlank()) {
                                        Text(
                                            text = msg.text.trim(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMe) OnyxWhite else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.wrapContentWidth()
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "12:30",
                                            fontSize = 9.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Pending Attachment Preview Bar in P2P Chat
                AnimatedVisibility(visible = pendingP2pAttachment != null) {
                    pendingP2pAttachment?.let { att ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (att.mediaType) {
                                            AttachmentMediaType.IMAGE -> Icons.Default.Image
                                            AttachmentMediaType.VIDEO -> Icons.Default.VideoFile
                                            AttachmentMediaType.DOCUMENT -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = att.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = { pendingP2pAttachment = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Chat Input Bar (Disabled if blocked)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (contact.isBlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Desbloquea a este contacto para enviar mensajes",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Attachment Button
                            IconButton(
                                onClick = { showP2pAttachmentOptions = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Adjuntar Foto, Video o Documento",
                                    tint = themeColor
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = {
                                    Text(
                                        if (pendingP2pAttachment != null) "Añade un texto..." else "Escribe un mensaje privado...",
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    val text = textInput.trim()
                                    val att = pendingP2pAttachment
                                    if (text.isNotBlank() || att != null) {
                                        textInput = ""
                                        pendingP2pAttachment = null
                                        val newMsg = PeerMessage(
                                            senderUsername = currentUser.username,
                                            receiverUsername = contact.username,
                                            text = if (text.isBlank()) (att?.name ?: "") else text,
                                            attachment = att
                                        )
                                        p2pMessages = p2pMessages + newMsg

                                        // Simulated friendly contact reply
                                        coroutineScope.launch {
                                            delay(1400)
                                            val reply = PeerMessage(
                                                senderUsername = contact.username,
                                                receiverUsername = currentUser.username,
                                                text = if (att != null) "¡Gracias por el archivo '${att.name}'! Lo tengo en mi Onyx." else "¡Recibido! Guardé tu mensaje en mi Onyx Open."
                                            )
                                            p2pMessages = p2pMessages + reply
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(themeColor)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = OnyxWhite)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Customize Chat Theme Color
    if (showCustomizeChatDialog && selectedContact != null) {
        val currentC = selectedContact!!
        val themePalettes = listOf(
            Pair("Rojo Onyx", 0xFFFF2A54),
            Pair("Azul Galaxia", 0xFF2979FF),
            Pair("Verde Esmeralda", 0xFF00E676),
            Pair("Púrpura Neón", 0xFFAA00FF),
            Pair("Dorado Ámbar", 0xFFFFD600),
            Pair("Grafito Oscuro", 0xFF37474F)
        )

        AlertDialog(
            onDismissRequest = { showCustomizeChatDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = OnyxRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Personalizar Chat con ${currentC.displayName}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Selecciona el color personalizado para las burbujas y estilo de este chat:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    themePalettes.forEach { (name, colorLong) ->
                        val col = Color(colorLong)
                        val isSelected = currentC.chatCustomThemeColor == colorLong
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) col.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    contacts = contacts.map {
                                        if (it.username == currentC.username) it.copy(chatCustomThemeColor = colorLong) else it
                                    }
                                    selectedContact = currentC.copy(chatCustomThemeColor = colorLong)
                                    showCustomizeChatDialog = false
                                    Toast.makeText(context, "Color de chat actualizado a $name", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = CircleShape, color = col, modifier = Modifier.size(20.dp)) {}
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                                if (isSelected) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.Check, contentDescription = null, tint = col, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomizeChatDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Dialog: Delete Contact Confirmation
    showDeleteConfirmDialog?.let { targetContact ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Eliminar a ${targetContact.displayName}?", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Se eliminará este contacto y el historial de mensajes privados de este chat en tu dispositivo.", fontSize = 11.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        contacts = contacts.filterNot { it.username == targetContact.username }
                        if (selectedContact?.username == targetContact.username) {
                            selectedContact = null
                        }
                        showDeleteConfirmDialog = null
                        Toast.makeText(context, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Add / Save Contact Dialog via QR or Username
    if (showScanQrDialog) {
        AlertDialog(
            onDismissRequest = { showScanQrDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = OnyxRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Nuevo Contacto Tipo WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Guarda un contacto escribiendo su @nombre_de_usuario o escaneando su Pase QR personal de Onyx Open:",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newContactDisplayNameInput,
                        onValueChange = { newContactDisplayNameInput = it },
                        label = { Text("Nombre o Apodo") },
                        placeholder = { Text("ej: María Gómez") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newContactUsernameInput,
                        onValueChange = { newContactUsernameInput = it },
                        label = { Text("Nombre de usuario (@usuario) o ID QR") },
                        placeholder = { Text("ej: maria_estudio o ONX-maria") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val cleanUser = newContactUsernameInput.trim().removePrefix("@")
                            val cleanName = if (newContactDisplayNameInput.isNotBlank()) newContactDisplayNameInput.trim() else cleanUser.capitalize()
                            if (cleanUser.isNotBlank()) {
                                val newContact = PeerContact(
                                    username = cleanUser,
                                    displayName = cleanName,
                                    email = "$cleanUser@onyx.app",
                                    qrCodeId = "ONX-$cleanUser",
                                    lastMessage = "Contacto guardado correctamente",
                                    lastTimestamp = "Ahora"
                                )
                                contacts = listOf(newContact) + contacts
                                showScanQrDialog = false
                                newContactUsernameInput = ""
                                newContactDisplayNameInput = ""
                                Toast.makeText(context, "¡$cleanName (@$cleanUser) guardado en tus contactos!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Contacto en Mi Agenda", color = OnyxWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showScanQrDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Dialog: Block Contact Confirmation ("¿Estás seguro de bloquear a esta persona?")
    showBlockConfirmDialog?.let { targetContact ->
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = null },
            icon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) },
            title = {
                Text(
                    text = "¿Estás seguro de bloquear a esta persona?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Bloquearás a ${targetContact.displayName} (@${targetContact.username}).",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Los contactos bloqueados no podrán enviarte mensajes ni ver tu actividad. Esta acción es estrictamente privada e independiente solo para tu cuenta.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        contacts = contacts.map {
                            if (it.username == targetContact.username) it.copy(isBlocked = true) else it
                        }
                        if (selectedContact?.username == targetContact.username) {
                            selectedContact = targetContact.copy(isBlocked = true)
                        }
                        showBlockConfirmDialog = null
                        Toast.makeText(context, "Has bloqueado a ${targetContact.displayName}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Bloquear", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Report Chat
    showReportDialog?.let { targetContact ->
        var selectedReason by remember { mutableStateOf("Spam o mensajes no solicitados") }
        AlertDialog(
            onDismissRequest = { showReportDialog = null },
            icon = { Icon(Icons.Default.Report, contentDescription = null, tint = Color(0xFFFF9100)) },
            title = {
                Text(
                    text = "Reportar a ${targetContact.displayName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Selecciona el motivo del reporte confidencial:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf(
                        "Spam o mensajes no solicitados",
                        "Acoso, insultos o amenazas",
                        "Contenido inapropiado o violento",
                        "Suplantación de identidad"
                    ).forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = null
                        Toast.makeText(context, "Reporte recibido confidencialmente: '$selectedReason'. Gracias por cuidar la comunidad.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                ) {
                    Text("Enviar Reporte", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // P2P Attachment Options Bottom Sheet
    if (showP2pAttachmentOptions) {
        ModalBottomSheet(
            onDismissRequest = { showP2pAttachmentOptions = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Enviar Archivo Privado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnyxRedPrimary
                )
                Text(
                    text = "Envía fotos de tareas, videos o documentos con cifrado de extremo a extremo.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    onClick = {
                        showP2pAttachmentOptions = false
                        p2pPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OnyxRedPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("📷 Enviar Foto / Imagen", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    onClick = {
                        showP2pAttachmentOptions = false
                        p2pVideoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color(0xFF2979FF))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("🎥 Enviar Video Grabación", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    onClick = {
                        showP2pAttachmentOptions = false
                        p2pFileLauncher.launch(arrayOf("*/*"))
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF00C853))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("📄 Enviar Archivo / Tarea / PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

