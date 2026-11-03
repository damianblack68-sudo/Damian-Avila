package com.example.creative.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creative.domain.model.ArchChatMessage
import com.example.creative.domain.model.ArchSender
import com.example.creative.domain.model.ArchitecturalBrief
import com.example.creative.domain.model.toEntity
import com.example.creative.engine.ArchitecturalInterviewEngine
import com.example.creative.ui.components.Architectural3DCanvas
import com.example.creative.ui.components.Blueprint2DCanvas
import com.example.ui.theme.OnyxDarkBg
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/**
 * Componente UI Principal para 'ONYX CREATIVE STUDIO'.
 * Integra un asistente conversacional de arquitectura que recaba los requisitos técnicos
 * obligatorios del proyecto (tipo de edificación, terreno, plantas, habitaciones, baños, estilo)
 * ANTES de habilitar los botones de generación de planos 2D y visualización 3D.
 */
@Composable
fun OnyxCreativeStudioScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ArchitecturalStudioViewModel? = null,
    modifier: Modifier = Modifier
) {
    var brief by remember { mutableStateOf(ArchitecturalBrief()) }
    var chatMessages by remember {
        mutableStateOf(listOf(ArchitecturalInterviewEngine.getInitialGreeting()))
    }
    var inputText by remember { mutableStateOf("") }

    // Estados de visualización de planos una vez desbloqueados
    var show2DBlueprint by remember { mutableStateOf(false) }
    var show3DModel by remember { mutableStateOf(false) }
    var showZoningReport by remember { mutableStateOf(false) }
    var showUniversal3DModal by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val studioViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ArchitecturalStudioViewModel.Factory(
            com.example.data.local.AppDatabase.getInstance(context).architecturalProjectDao()
        )
    )
    val savedProjects by studioViewModel.getAllProjects().collectAsStateWithLifecycle()
    val currentProjectId = remember { java.util.UUID.randomUUID().toString() }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun handleSend(text: String) {
        if (text.isBlank()) return
        val userMsg = ArchChatMessage(sender = ArchSender.USER, text = text)
        val (updatedBrief, aiMsg) = ArchitecturalInterviewEngine.processUserResponse(text, brief)

        brief = updatedBrief
        chatMessages = chatMessages + userMsg + aiMsg
        inputText = ""

        coroutineScope.launch {
            listState.animateScrollToItem(chatMessages.size - 1)
            // Persistir automáticamente el proyecto mediante ArchitecturalStudioViewModel
            studioViewModel.saveProject(
                updatedBrief.toEntity(id = currentProjectId)
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = OnyxWhite)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ONYX",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = OnyxRedPrimary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CREATIVE STUDIO",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = OnyxWhite
                                    )
                                }
                                Text(
                                    text = "Asistente de Arquitectura & Visualización IA",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Botón 3D Universal Pixar/Disney/Dreamworks
                            OutlinedButton(
                                onClick = { showUniversal3DModal = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64D2FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF264060)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF2A54), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("3D Universal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Badge de Estado del Proyecto
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (brief.isReadyForGeneration) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF2A54).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (brief.isReadyForGeneration) "LISTO PARA GENERAR" else "RECABANDO DATOS",
                                    color = if (brief.isReadyForGeneration) Color(0xFF00E676) else OnyxRedPrimary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Descargo de Responsabilidad Profesional Obligatorio
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131622),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF2C324B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Propuesta conceptual preliminar por IA. Requiere validación por un arquitecto o calculista antes de construir.",
                                fontSize = 9.sp,
                                color = Color(0xFFB0B5C6),
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Panel Fijo Inferior: Barra de Entrada de Chat y Botones de Generación Condicionales
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // SECCIÓN CRÍTICA: Botones de Generación (BLOQUEADOS hasta completar entrevista)
                    GenerationButtonsPanel(
                        isReady = brief.isReadyForGeneration,
                        missingRequirements = brief.missingRequirements,
                        onGenerate2D = {
                            show2DBlueprint = true
                            show3DModel = false
                            showZoningReport = false
                            studioViewModel.updateGenerationStatus(
                                projectId = currentProjectId,
                                status = com.example.data.local.entity.ArchitecturalGenerationStatus.GENERADO
                            )
                        },
                        onGenerate3D = {
                            show3DModel = true
                            show2DBlueprint = false
                            showZoningReport = false
                            studioViewModel.updateGenerationStatus(
                                projectId = currentProjectId,
                                status = com.example.data.local.entity.ArchitecturalGenerationStatus.GENERADO
                            )
                        },
                        onExplainZoning = {
                            showZoningReport = true
                            show2DBlueprint = false
                            show3DModel = false
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Campo de Texto para conversar con el asistente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Responde al asistente (ej: 10x20m, 3 habs...)", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OnyxRedPrimary,
                                unfocusedBorderColor = Color(0xFF2E3349)
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { handleSend(inputText) },
                            enabled = inputText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank()) OnyxRedPrimary else Color(0xFF232738))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = OnyxWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF090A0E))
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Tarjeta Resumen de Requisitos en Vivo (Frente al Usuario)
            item {
                RequirementsBriefLiveCard(brief = brief)
            }

            // 2. Vistas Renderizadas si ya se solicitaron (Plano 2D, Modelo 3D o Explicación)
            if (show2DBlueprint) {
                item {
                    Blueprint2DPreviewSection(brief = brief, onClose = { show2DBlueprint = false })
                }
            }

            if (show3DModel) {
                item {
                    Architectural3DPreviewSection(brief = brief, onClose = { show3DModel = false })
                }
            }

            if (showZoningReport) {
                item {
                    ArchitecturalZoningReportSection(brief = brief, onClose = { showZoningReport = false })
                }
            }

            // 3. Conversación Guiada con el Asistente
            item {
                Text(
                    text = "DIÁLOGO TÉCNICO CON EL ASISTENTE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8E92A4),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(chatMessages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onOptionClick = { handleSend(it) }
                )
            }
        }

        if (showUniversal3DModal) {
            com.example.creative.ui.components.Universal3DEngineModal(
                onDismiss = { showUniversal3DModal = false }
            )
        }
    }
}

/**
 * Panel de Botones de Generación Arquitectónica.
 * Bloquea la generación y muestra exactamente los requisitos faltantes
 * hasta que el usuario complete las respuestas en el chat.
 */
@Composable
fun GenerationButtonsPanel(
    isReady: Boolean,
    missingRequirements: List<String>,
    onGenerate2D: () -> Unit,
    onGenerate3D: () -> Unit,
    onExplainZoning: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isReady) {
            // Indicador de Bloqueo Pedagógico
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF131520),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C324A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = OnyxRedPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Generación Bloqueada • Faltan datos del proyecto",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnyxWhite
                        )
                        Text(
                            text = "Pendiente: " + missingRequirements.take(3).joinToString(", "),
                            fontSize = 9.sp,
                            color = Color(0xFF9E9EA7)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón 1: Plano 2D
            Button(
                onClick = onGenerate2D,
                enabled = isReady,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnyxRedPrimary,
                    disabledContainerColor = Color(0xFF1D202D),
                    disabledContentColor = Color(0xFF6B7085)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Architecture, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Plano 2D", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Botón 2: Modelo 3D
            Button(
                onClick = onGenerate3D,
                enabled = isReady,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E2235),
                    disabledContainerColor = Color(0xFF1D202D),
                    disabledContentColor = Color(0xFF6B7085)
                ),
                border = if (isReady) androidx.compose.foundation.BorderStroke(1.dp, OnyxRedPrimary.copy(alpha = 0.6f)) else null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isReady) OnyxRedPrimary else Color(0xFF6B7085))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modelo 3D", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Botón 3: Explicación de Zonificación
            OutlinedButton(
                onClick = onExplainZoning,
                enabled = isReady,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OnyxWhite,
                    disabledContentColor = Color(0xFF6B7085)
                ),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Zonificación", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Tarjeta en Vivo con el Progreso de Requisitos Recabados
 */
@Composable
fun RequirementsBriefLiveCard(brief: ArchitecturalBrief) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222638)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ficha Técnica del Proyecto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnyxWhite
                )

                Text(
                    text = "${(brief.completionPercentage * 100).toInt()}% Completado",
                    fontSize = 11.sp,
                    color = if (brief.isReadyForGeneration) Color(0xFF00E676) else OnyxRedPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { brief.completionPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (brief.isReadyForGeneration) Color(0xFF00E676) else OnyxRedPrimary,
                trackColor = Color(0xFF1E2235)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Requisitos en cuadrícula compacta
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BriefParamPill("Edificación", brief.projectType ?: "Por definir", modifier = Modifier.weight(1f))
                BriefParamPill("Terreno", if (brief.totalLotAreaM2 != null) "${brief.lotWidthMeters?.toInt()}x${brief.lotLengthMeters?.toInt()} m (${brief.totalLotAreaM2?.toInt()}m²)" else "Por definir", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BriefParamPill("Plantas", brief.totalFloors?.let { "$it planta(s)" } ?: "Por definir", modifier = Modifier.weight(1f))
                BriefParamPill("Distribución", if (brief.bedroomsCount != null) "${brief.bedroomsCount} Hab • ${brief.bathroomsCount ?: 1} Baños" else "Por definir", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                BriefParamPill("Estilo", brief.architecturalStyle ?: "Por definir", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun BriefParamPill(label: String, value: String, modifier: Modifier = Modifier) {
    val isPending = value == "Por definir"
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111420),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, if (isPending) Color(0xFF2A2E44) else OnyxRedPrimary.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(text = label, fontSize = 9.sp, color = Color(0xFF8F94A6))
            Text(
                text = value,
                fontSize = 10.5.sp,
                fontWeight = if (isPending) FontWeight.Normal else FontWeight.Bold,
                color = if (isPending) Color(0xFF6B7085) else OnyxWhite,
                maxLines = 1
            )
        }
    }
}

/**
 * Elemento de Mensaje del Chat
 */
@Composable
fun ChatMessageItem(
    message: ArchChatMessage,
    onOptionClick: (String) -> Unit
) {
    val isAi = message.sender == ArchSender.ASSISTANT_AI

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isAi) 2.dp else 16.dp,
                bottomEnd = if (isAi) 16.dp else 2.dp
            ),
            color = if (isAi) Color(0xFF151825) else OnyxRedPrimary,
            border = if (isAi) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF272D45)) else null,
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (isAi) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = OnyxRedPrimary, modifier = Modifier.size(16.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("O", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Asistente de Arquitectura Onyx",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnyxRedPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnyxWhite,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Opciones rápidas de respuesta si es un mensaje de la IA
        if (isAi && message.quickOptions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(message.quickOptions) { option ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1C2030),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxRedPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onOptionClick(option) }
                    ) {
                        Text(
                            text = option,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnyxWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección de Visualización del Plano 2D Generado
 */
@Composable
fun Blueprint2DPreviewSection(
    brief: ArchitecturalBrief,
    onClose: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, OnyxRedPrimary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Architecture, contentDescription = null, tint = OnyxRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plano Arquitectónico 2D (Escala y Cotas)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnyxWhite)
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Interactivo
            Blueprint2DCanvas(brief = brief)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Distribución propuesta: Zona social al frente con acceso a ventilación cruzada; dormitorios posteriores orientados hacia el jardín para aislamiento acústico.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Sección de Visualización del Modelo 3D Interactivo
 */
@Composable
fun Architectural3DPreviewSection(
    brief: ArchitecturalBrief,
    onClose: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volumetría Tridimensional 3D", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnyxWhite)
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas 3D Interactivo
            Architectural3DCanvas(brief = brief)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Arrastra con el dedo sobre el modelo para rotar en 360° y cambiar la elevación. Edificación de ${brief.totalFloors ?: 1} nivel(es) en estilo ${brief.architecturalStyle ?: "Moderno"}.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Reporte Técnico de Zonificación y Justificación Arquitectónica
 */
@Composable
fun ArchitecturalZoningReportSection(
    brief: ArchitecturalBrief,
    onClose: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10131E)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2F3652)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = OnyxRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Memoria Descriptiva & Zonificación", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnyxWhite)
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val area = brief.totalLotAreaM2 ?: 200f
            Text(
                text = "1. Zonificación Social (35%):\n• Sala y comedor integrados para maximizar la sensación espacial en ${brief.lotWidthMeters ?: 10f} m de frente.\n• Conexión visual directa con ventanales de iluminación matutina.\n\n" +
                        "2. Zonificación Húmeda (25%):\n• Cocina ventilada con mesada perimetral y baño social próximo a accesos.\n• Tuberías agrupadas en un solo núcleo para optimizar costos de instalación sanitaria.\n\n" +
                        "3. Zonificación Íntima (40%):\n• ${brief.bedroomsCount ?: 2} habitaciones retiradas del tránsito de la calle principal para garantizar silencio y descanso óptimo.",
                fontSize = 11.5.sp,
                color = Color(0xFFD4D8E8),
                lineHeight = 17.sp
            )
        }
    }
}
