package com.example.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.domain.model.ChartData
import com.example.domain.model.ClarifyingQuestion
import com.example.domain.model.ProfileType
import com.example.engine.MathSolution
import com.example.engine.MathSolverEngine
import com.example.ui.components.InteractiveChart
import com.example.ui.components.SoundWaveVisualizer
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.domain.model.CodeProjectPayload
import com.example.domain.model.ConceptMapData
import com.example.domain.model.EngineeringBlueprint
import com.example.domain.model.OnyxAiEngine
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.ui.components.BlueprintCanvas
import com.example.ui.components.CodePreviewCard
import com.example.ui.components.ConceptMapCanvas
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.domain.model.AttachmentMediaType
import com.example.domain.model.ChatAttachment
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import com.example.domain.model.VoiceOrbMode
import com.example.ui.components.OnyxExpressVoiceOrb
import com.example.ui.components.OnyxExpressChatMicButton
import com.example.ui.components.OnyxFloatingVoiceCard
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentProfile: ProfileType,
    sessions: List<ChatSessionEntity>,
    currentSessionId: String,
    messages: List<ChatMessageEntity>,
    onSelectSession: (String) -> Unit,
    onCreateSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onSendMessage: (String, Boolean, String?, String?, OnyxAiEngine, ChatAttachment?) -> Unit,
    onSpeak: (String) -> Unit,
    onStartVoiceInput: ((String) -> Unit) -> Unit,
    isListening: Boolean,
    amplitudes: List<Float>,
    isAiProcessing: Boolean = false,
    micLevel: Float = 0.15f,
    isSpeaking: Boolean = false,
    onStopSpeaking: () -> Unit = {},
    onStopVoiceInput: () -> Unit = {},
    currentUser: UserProfile? = null,
    onOpenSubscriptionShop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showSessionsSheet by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newChatTitle by remember { mutableStateOf("") }
    var selectedEngine by remember { mutableStateOf(OnyxAiEngine.ONYX_X) }
    var showLockedDialog by remember { mutableStateOf(false) }
    var lockedEnginePrompt by remember { mutableStateOf<OnyxAiEngine?>(null) }
    var pendingAttachment by remember { mutableStateOf<ChatAttachment?>(null) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showFloatingVoiceModal by remember { mutableStateOf(false) }
    var liveTranscribedVoiceText by remember { mutableStateOf("") }

    val voiceOrbState = when {
        isListening -> VoiceOrbMode.LISTENING
        isAiProcessing -> VoiceOrbMode.PROCESSING
        isSpeaking -> VoiceOrbMode.SPEAKING
        else -> VoiceOrbMode.IDLE
    }

    val listState = rememberLazyListState()
    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    // Pickers for Image, Video, and Files/Documents
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingAttachment = ChatAttachment(
                uri = it.toString(),
                name = "foto_tarea_${System.currentTimeMillis() % 10000}.jpg",
                mediaType = AttachmentMediaType.IMAGE,
                sizeFormatted = "1.8 MB",
                mimeType = "image/jpeg"
            )
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingAttachment = ChatAttachment(
                uri = it.toString(),
                name = "video_laboratorio_${System.currentTimeMillis() % 10000}.mp4",
                mediaType = AttachmentMediaType.VIDEO,
                sizeFormatted = "12.4 MB",
                mimeType = "video/mp4"
            )
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingAttachment = ChatAttachment(
                uri = it.toString(),
                name = "documento_libro_${System.currentTimeMillis() % 10000}.pdf",
                mediaType = AttachmentMediaType.DOCUMENT,
                sizeFormatted = "3.5 MB",
                mimeType = "application/pdf"
            )
        }
    }

    LaunchedEffect(currentUser) {
        if (!selectedEngine.isUnlockedFor(currentUser)) {
            selectedEngine = OnyxAiEngine.ONYX_X
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Chat Header with session selector & Onyx AI Engine Selector
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSessionsSheet = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Chats",
                            tint = OnyxRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val activeTitle = sessions.find { it.id == currentSessionId }?.title ?: "Chat Activo"
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ONYX OPEN",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = OnyxRedPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = OnyxRedPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Asistente Educativo",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnyxRedPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = { showNewChatDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OnyxRedPrimary.copy(alpha = 0.15f),
                            contentColor = OnyxRedPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo Chat",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Top Engine Selector Bar: Onyx X, Onyx Pro, Onyx Plus, Onyx Evolution
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Motor IA:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OnyxAiEngine.values().forEach { engine ->
                            val isUnlocked = engine.isUnlockedFor(currentUser)
                            val isSelected = selectedEngine == engine

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    isSelected -> OnyxRedPrimary
                                    isUnlocked -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                tonalElevation = if (isSelected) 4.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        if (!isUnlocked) {
                                            lockedEnginePrompt = engine
                                            showLockedDialog = true
                                        } else {
                                            selectedEngine = engine
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (!isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Bloqueado",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = OnyxWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Text(
                                        text = engine.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = when {
                                            isSelected -> OnyxWhite
                                            isUnlocked -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        }
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isSelected -> OnyxWhite.copy(alpha = 0.25f)
                                            isUnlocked -> OnyxRedPrimary.copy(alpha = 0.12f)
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        }
                                    ) {
                                        Text(
                                            text = engine.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isSelected -> OnyxWhite
                                                isUnlocked -> OnyxRedPrimary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active engine status hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ ${selectedEngine.title} • Razonamiento ${selectedEngine.reasoningLevel} (${selectedEngine.speedMultiplier})",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnyxRedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val isUserFree = currentUser == null || (currentUser.role != UserRole.ADMIN_CREATOR && currentUser.subscription == SubscriptionTier.FREE)
                    if (isUserFree) {
                        Text(
                            text = "Pro / Plus / Evolution requieren suscripción",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "USER"

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(OnyxRedPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("O", color = OnyxWhite, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Onyx Open",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnyxRedPrimary
                            )
                        } else {
                            Text(
                                text = "Tú",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val hasHeavyCard = msg.codePayloadJson != null || msg.blueprintJson != null || msg.conceptMapJson != null || msg.chartJson != null
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        color = if (isUser) OnyxRedPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .widthIn(min = 40.dp, max = if (hasHeavyCard) 380.dp else 300.dp)
                            .wrapContentWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .wrapContentWidth()
                        ) {
                            // Render Attachment inside message bubble if present
                            if (msg.attachmentJson != null) {
                                val attachment = remember(msg.attachmentJson) {
                                    runCatching {
                                        moshi.adapter(ChatAttachment::class.java).fromJson(msg.attachmentJson)
                                    }.getOrNull()
                                }
                                if (attachment != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isUser) Color.Black.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .padding(bottom = 6.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            if (attachment.mediaType == AttachmentMediaType.IMAGE && attachment.uri.isNotBlank()) {
                                                AsyncImage(
                                                    model = attachment.uri,
                                                    contentDescription = attachment.name,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(max = 160.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when (attachment.mediaType) {
                                                        AttachmentMediaType.IMAGE -> Icons.Default.Image
                                                        AttachmentMediaType.VIDEO -> Icons.Default.VideoFile
                                                        AttachmentMediaType.DOCUMENT -> Icons.Default.Description
                                                    },
                                                    contentDescription = null,
                                                    tint = if (isUser) OnyxWhite else OnyxRedPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = attachment.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isUser) OnyxWhite else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (attachment.sizeFormatted.isNotBlank()) {
                                                    Text(
                                                        text = attachment.sizeFormatted,
                                                        fontSize = 9.sp,
                                                        color = if (isUser) OnyxWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (msg.text.isNotBlank()) {
                                Text(
                                    text = msg.text.trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) OnyxWhite else MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }

                            // Interactive Math Chart if present
                            if (msg.chartJson != null) {
                                val chartData = remember(msg.chartJson) {
                                    runCatching {
                                        moshi.adapter(ChartData::class.java).fromJson(msg.chartJson)
                                    }.getOrNull()
                                }
                                if (chartData != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    InteractiveChart(chartData = chartData)
                                }
                            }

                            // Interactive Code & Web App Card if present
                            if (msg.codePayloadJson != null) {
                                val codePayload = remember(msg.codePayloadJson) {
                                    runCatching {
                                        moshi.adapter(CodeProjectPayload::class.java).fromJson(msg.codePayloadJson)
                                    }.getOrNull()
                                }
                                if (codePayload != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    CodePreviewCard(
                                        payload = codePayload,
                                        onAskChanges = { proj ->
                                            inputText = "Quiero hacer un cambio en la app '${proj.title}': "
                                        }
                                    )
                                }
                            }

                            // Interactive Engineering Blueprint CAD if present
                            if (msg.blueprintJson != null) {
                                val blueprint = remember(msg.blueprintJson) {
                                    runCatching {
                                        moshi.adapter(EngineeringBlueprint::class.java).fromJson(msg.blueprintJson)
                                    }.getOrNull()
                                }
                                if (blueprint != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    BlueprintCanvas(blueprint = blueprint)
                                }
                            }

                            // Interactive Concept Map if present
                            if (msg.conceptMapJson != null) {
                                val conceptMap = remember(msg.conceptMapJson) {
                                    runCatching {
                                        moshi.adapter(ConceptMapData::class.java).fromJson(msg.conceptMapJson)
                                    }.getOrNull()
                                }
                                if (conceptMap != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    ConceptMapCanvas(conceptMap = conceptMap)
                                }
                            }

                            // Clarifying questions if present
                            if (msg.clarifyingQuestionsJson != null && !isUser) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Preguntas sugeridas:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnyxRedPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val options = listOf("Resolver paso a paso", "Graficar ecuación", "Exportar informe")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    options.forEach { opt ->
                                        SuggestionChip(
                                            onClick = {
                                                onSendMessage(opt, false, null, null, selectedEngine, null)
                                            },
                                            label = { Text(opt, fontSize = 10.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }
                                }
                            }

                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onSpeak(msg.text) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Escuchar respuesta",
                                            tint = OnyxRedPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Voice Listening Overlay
        AnimatedVisibility(visible = isListening) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Escuchando tu voz en tiempo real...",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnyxRedPrimary,
                    fontWeight = FontWeight.Bold
                )
                SoundWaveVisualizer(amplitudes = amplitudes, isActive = true)
            }
        }

        // Secure Private Keyboard Bar
        com.example.ui.components.OnyxSecureKeyboardBar(
            currentText = inputText,
            onInsertText = { token -> inputText += token },
            onClearField = { inputText = "" },
            onAdvanceNext = {
                if (inputText.isNotBlank() && !inputText.endsWith(" ")) {
                    inputText += " "
                }
            }
        )

        // Quick Assistant Superpower Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = {
                    onSendMessage("Quiero crear una app en HTML", false, null, null, selectedEngine, null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnyxRedPrimary,
                    contentColor = OnyxWhite
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_html_app_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Crear app HTML",
                    modifier = Modifier.size(16.dp),
                    tint = OnyxWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear app HTML", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnyxWhite)
            }
            SuggestionChip(
                onClick = { inputText = "Diseñar plano arquitectónico de una casa con medidas y cotas exactas" },
                label = { Text("📐 Plano Casa CAD", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color(0xFF0F2038),
                    labelColor = Color(0xFF00E5FF)
                )
            )
            SuggestionChip(
                onClick = { inputText = "Generar plano técnico automotriz de chasis y aerodinámica con medidas" },
                label = { Text("🏎️ Plano Auto Chasis", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color(0xFF1F2414),
                    labelColor = Color(0xFFFBBF24)
                )
            )
            SuggestionChip(
                onClick = { inputText = "Resolver y razonar paso a paso tarea de física con leyes de Newton y fricción" },
                label = { Text("🧠 Razonar Tarea 100%", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color(0xFF14241F),
                    labelColor = Color(0xFF34D399)
                )
            )
            SuggestionChip(
                onClick = { inputText = "Generar mapa conceptual estructurado de inteligencia artificial y software" },
                label = { Text("🗺️ Mapa Conceptual", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color(0xFF241628),
                    labelColor = Color(0xFFC084FC)
                )
            )
        }

        // Pending Attachment Preview Bar
        AnimatedVisibility(visible = pendingAttachment != null) {
            pendingAttachment?.let { att ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OnyxRedPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (att.mediaType) {
                                        AttachmentMediaType.IMAGE -> Icons.Default.Image
                                        AttachmentMediaType.VIDEO -> Icons.Default.VideoFile
                                        AttachmentMediaType.DOCUMENT -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = OnyxRedPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = att.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${att.mediaType.label} • ${att.sizeFormatted}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { pendingAttachment = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar archivo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Input bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment Button (+)
                IconButton(
                    onClick = { showAttachmentOptions = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Adjuntar Foto, Video o Libro",
                        tint = OnyxRedPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Único micrófono interactivo Onyx Express: componente circular minimalista sobre fondo negro mate,
                // resplandor en rojo vibrante, ondas dinámicas al hablar y rotación/parpadeo al procesar la IA.
                OnyxExpressChatMicButton(
                    state = voiceOrbState,
                    micLevel = micLevel,
                    amplitudes = amplitudes,
                    onClick = {
                        if (voiceOrbState == VoiceOrbMode.LISTENING) {
                            onStopVoiceInput()
                        } else if (voiceOrbState == VoiceOrbMode.SPEAKING) {
                            onStopSpeaking()
                        } else {
                            showFloatingVoiceModal = true
                            onStartVoiceInput { transcribed ->
                                liveTranscribedVoiceText = transcribed
                                inputText = transcribed
                            }
                        }
                    },
                    sizeDp = 42.dp
                )

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (pendingAttachment != null) "Añade una instrucción o pregunta..." else "Pregunta, tarea o ecuación (x² - 4x = 0)...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnyxRedPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        val currentAtt = pendingAttachment
                        if (text.isNotBlank() || currentAtt != null) {
                            val msgToSend = if (text.isBlank() && currentAtt != null) {
                                when (currentAtt.mediaType) {
                                    AttachmentMediaType.IMAGE -> "Por favor analiza y resuelve la tarea/imagen adjunta paso a paso."
                                    AttachmentMediaType.VIDEO -> "Por favor examina el video de laboratorio escolar adjunto."
                                    AttachmentMediaType.DOCUMENT -> "Por favor lee y razona los temas del libro/documento adjunto."
                                }
                            } else text

                            inputText = ""
                            pendingAttachment = null

                            // Check if mathematical
                            val isMath = msgToSend.contains("=") || msgToSend.contains("x²") || msgToSend.contains("derivada") || msgToSend.contains("integral")
                            val mathSolution = if (isMath) MathSolverEngine.solve(msgToSend) else null
                            val chartJson = mathSolution?.chartData?.let {
                                val adapter = moshi.adapter(ChartData::class.java)
                                adapter.toJson(it)
                            }
                            val clarifyingJson = if (mathSolution != null && mathSolution.clarifyingQuestions.isNotEmpty()) {
                                """[{"question":"¿Deseas profundizar?","options":["Graficar función","Ver pasos detallados","Exportar a Drive"]}]"""
                            } else null

                            onSendMessage(msgToSend, isMath, chartJson, clarifyingJson, selectedEngine, currentAtt)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(OnyxRedPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = OnyxWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }



    // Floating Voice Modal Card with real-time waveform, transcription, and instant response
    AnimatedVisibility(
        visible = showFloatingVoiceModal,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = Modifier
            .align(Alignment.Center)
            .padding(16.dp)
    ) {
        OnyxFloatingVoiceCard(
            state = voiceOrbState,
            transcribedText = liveTranscribedVoiceText,
            micLevel = micLevel,
            amplitudes = amplitudes,
            onToggleListen = {
                if (voiceOrbState == VoiceOrbMode.LISTENING) {
                    onStopVoiceInput()
                } else {
                    onStartVoiceInput { transcribed ->
                        liveTranscribedVoiceText = transcribed
                        inputText = transcribed
                    }
                }
            },
            onSendText = { text ->
                if (text.isNotBlank()) {
                    val isMath = text.contains("=") || text.contains("x²") || text.contains("derivada") || text.contains("integral")
                    val mathSolution = if (isMath) MathSolverEngine.solve(text) else null
                    val chartJson = mathSolution?.chartData?.let {
                        val adapter = moshi.adapter(ChartData::class.java)
                        adapter.toJson(it)
                    }
                    val clarifyingJson = if (mathSolution != null && mathSolution.clarifyingQuestions.isNotEmpty()) {
                        """[{"question":"¿Deseas profundizar?","options":["Graficar función","Ver pasos detallados","Exportar a Drive"]}]"""
                    } else null

                    onSendMessage(text, isMath, chartJson, clarifyingJson, selectedEngine, pendingAttachment)
                    pendingAttachment = null
                    inputText = ""
                    liveTranscribedVoiceText = ""
                    showFloatingVoiceModal = false
                }
            },
            onClose = {
                showFloatingVoiceModal = false
            }
        )
    }
}

    // Attachment Options Sheet
    if (showAttachmentOptions) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentOptions = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Subir Contenido para Razonamiento IA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnyxRedPrimary
                )
                Text(
                    text = "Onyx Open leerá, visualizará y razonará sobre tus tareas, apuntes y experimentos.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Photo
                Card(
                    onClick = {
                        showAttachmentOptions = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OnyxRedPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OnyxRedPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("📷 Subir Foto / Imagen de Tarea", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Fotografía de pizarras, exámenes, ejercicios o diagramas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Video
                Card(
                    onClick = {
                        showAttachmentOptions = false
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2979FF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color(0xFF2979FF))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("🎥 Subir Video / Experimento", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Grabaciones de clases prácticas, laboratorio y exposiciones", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Document / Book / PDF
                Card(
                    onClick = {
                        showAttachmentOptions = false
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "text/*",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF00C853))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("📄 Subir Archivo, Libro o Tarea PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Libros de texto, guías didácticas, resúmenes y documentos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Sessions Bottom Sheet
    if (showSessionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSessionsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Conversaciones Onyx",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnyxRedPrimary
                    )
                    IconButton(onClick = {
                        showSessionsSheet = false
                        showNewChatDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Chat", tint = OnyxRedPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                sessions.forEach { session ->
                    val isSelected = session.id == currentSessionId
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OnyxRedPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectSession(session.id)
                                showSessionsSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) OnyxRedPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = session.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    fontSize = 11.sp
                                )
                            }
                            if (sessions.size > 1) {
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Crear Nueva Sesión de Chat", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newChatTitle,
                    onValueChange = { newChatTitle = it },
                    placeholder = { Text("Ej: Análisis de Matrices, Proyecto IA...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = if (newChatTitle.isNotBlank()) newChatTitle.trim() else "Chat ${sessions.size + 1}"
                        onCreateSession(title)
                        newChatTitle = ""
                        showNewChatDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Crear", color = OnyxWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Locked Engine Subscription Requirement Dialog
    if (showLockedDialog && lockedEnginePrompt != null) {
        val locked = lockedEnginePrompt!!
        AlertDialog(
            onDismissRequest = { showLockedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = OnyxRedPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Necesita suscripción para usarla",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "El motor ${locked.title} (${locked.badge}) está limitado para cuentas con suscripción activa. Disfruta de mayor fluidez, comprensión y velocidad (${locked.speedMultiplier}).",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        color = OnyxRedPrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Suscripción recomendada: ${locked.requiredTier.title}",
                                fontWeight = FontWeight.Bold,
                                color = OnyxRedPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Inversión: $${locked.requiredTier.priceMxn} MXN (${locked.requiredTier.period})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = locked.requiredTier.description,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Los usuarios estándar pueden continuar usando Onyx X de forma libre. Si no deseas suscribirte ahora, puedes cancelar sin problema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLockedDialog = false
                        onOpenSubscriptionShop()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Suscripciones", color = OnyxWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockedDialog = false }) {
                    Text("Ahora no (Seguir en Onyx X)")
                }
            }
        )
    }
}
