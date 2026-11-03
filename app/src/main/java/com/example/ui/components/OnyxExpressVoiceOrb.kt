package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.VoiceOrbMode
import com.example.ui.theme.OnyxRedDeep
import com.example.ui.theme.OnyxRedGlow
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

/**
 * Componente flotante circular minimalista sobre fondo negro mate con un sutil
 * borde y resplandor en rojo vibrante (el sello oficial de Onyx Express).
 *
 * Conectado a la entrada y salida de audio:
 * - Cuando el usuario habla (micrófono activo): genera ondas dinámicas simulando un pulso.
 * - Cuando la IA procesa la respuesta: el círculo gira y parpadea suavemente indicando que está respondiendo.
 */
@Composable
fun OnyxExpressVoiceOrb(
    state: VoiceOrbMode,
    micLevel: Float,
    amplitudes: List<Float>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 76.dp,
    showStatusLabel: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OnyxOrbTransitions")

    // Smooth continuous rotation for PROCESSING state
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ProcessingRotation"
    )

    // Soft blinking/pulsing for PROCESSING state ("parpadea suavemente")
    val softBlinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SoftBlinkAlpha"
    )

    val softBlinkScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SoftBlinkScale"
    )

    // Ambient breathing glow for IDLE state
    val idleGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleGlow"
    )

    // Expanding ripple waves for LISTENING state (User speaking to mic)
    val waveProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveRipple1"
    )
    val waveProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveRipple2"
    )
    val waveProgress3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveRipple3"
    )

    val interactionSource = remember { MutableInteractionSource() }

    // Dynamic scale driven by microphone input level when user speaks
    val micScale = if (state == VoiceOrbMode.LISTENING) {
        1.0f + (micLevel * 0.18f).coerceIn(0f, 0.25f)
    } else if (state == VoiceOrbMode.PROCESSING) {
        softBlinkScale
    } else {
        1.0f
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Outer Container with Canvas for Dynamic Waves & Glowing Halo
        Box(
            modifier = Modifier
                .size(sizeDp * 1.7f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // --- CANVAS LAYER: DYNAMIC VOICE WAVES & GLOWING HALO ---
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val coreRadius = (sizeDp.toPx() / 2f)

                // 1. Ambient vibrant red glow halo behind matte circle
                val haloAlpha = when (state) {
                    VoiceOrbMode.IDLE -> idleGlowAlpha
                    VoiceOrbMode.LISTENING -> (0.50f + micLevel * 0.45f).coerceIn(0.5f, 0.95f)
                    VoiceOrbMode.PROCESSING -> softBlinkAlpha * 0.8f
                    VoiceOrbMode.SPEAKING -> 0.75f
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            OnyxRedVibrant.copy(alpha = haloAlpha),
                            OnyxRedPrimary.copy(alpha = haloAlpha * 0.6f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = coreRadius * 1.45f
                    ),
                    radius = coreRadius * 1.45f,
                    center = centerOffset
                )

                // 2. Dynamic Wave Ripples when User speaks into microphone (LISTENING)
                if (state == VoiceOrbMode.LISTENING) {
                    val ripples = listOf(waveProgress1, waveProgress2, waveProgress3)
                    val maxRippleRadius = coreRadius * (1.6f + micLevel * 0.5f)

                    ripples.forEach { progress ->
                        val currentRadius = coreRadius + (maxRippleRadius - coreRadius) * progress
                        val alpha = (1.0f - progress).coerceIn(0f, 1f) * (0.4f + micLevel * 0.6f)
                        val strokeWidth = (2.2.dp.toPx() * (1f - progress * 0.5f)).coerceAtLeast(1f)

                        drawCircle(
                            color = OnyxRedVibrant.copy(alpha = alpha.coerceIn(0f, 1f)),
                            radius = currentRadius,
                            center = centerOffset,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }

                // 3. Audio Frequency Wave pulses when AI is speaking (SPEAKING)
                if (state == VoiceOrbMode.SPEAKING) {
                    val speakWaveRadius = coreRadius * (1.1f + (amplitudes.average().toFloat() * 0.45f))
                    drawCircle(
                        color = OnyxRedPrimary.copy(alpha = 0.5f),
                        radius = speakWaveRadius,
                        center = centerOffset,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                // 4. Rotating Glowing Arc when AI is processing (PROCESSING - "el círculo gira")
                if (state == VoiceOrbMode.PROCESSING) {
                    val arcRadius = coreRadius + 2.5.dp.toPx()
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                OnyxRedPrimary.copy(alpha = 0.4f),
                                OnyxRedVibrant,
                                OnyxWhite,
                                OnyxRedVibrant,
                                Color.Transparent
                            )
                        ),
                        startAngle = rotationAngle,
                        sweepAngle = 135f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - arcRadius, centerOffset.y - arcRadius),
                        size = androidx.compose.ui.geometry.Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // --- MINIMALIST MATTE BLACK CIRCULAR CORE ---
            // Sello de Onyx Express: Fondo negro mate profundo con borde sutil en rojo vibrante
            Box(
                modifier = Modifier
                    .size(sizeDp)
                    .scale(micScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF13151D),
                                Color(0xFF090A0D),
                                Color(0xFF050507)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            width = if (state == VoiceOrbMode.PROCESSING) 2.dp else 1.5.dp,
                            brush = if (state == VoiceOrbMode.PROCESSING) {
                                Brush.sweepGradient(
                                    colors = listOf(
                                        OnyxRedVibrant,
                                        OnyxWhite.copy(alpha = softBlinkAlpha),
                                        OnyxRedPrimary,
                                        OnyxRedDeep,
                                        OnyxRedVibrant
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        OnyxRedVibrant.copy(alpha = 0.95f),
                                        OnyxRedPrimary.copy(alpha = 0.70f),
                                        OnyxRedDeep.copy(alpha = 0.50f)
                                    )
                                )
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Interior Center Glyph / Dynamic Waveform Bars
                when (state) {
                    VoiceOrbMode.IDLE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Activar voz Onyx Express",
                                tint = OnyxWhite,
                                modifier = Modifier.size(sizeDp * 0.36f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "EXPRESS",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = OnyxRedPrimary
                            )
                        }
                    }

                    VoiceOrbMode.LISTENING -> {
                        // User Speaking: Real-time reactive pulse waveform bars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            val activeBars = amplitudes.take(7)
                            activeBars.forEachIndexed { i, amp ->
                                val barHeight = (sizeDp * 0.15f) + (sizeDp * 0.40f) * amp * (0.8f + micLevel * 0.5f)
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(barHeight.coerceIn(6.dp, sizeDp * 0.55f))
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    OnyxWhite,
                                                    OnyxRedVibrant,
                                                    OnyxRedPrimary
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }

                    VoiceOrbMode.PROCESSING -> {
                        // AI Processing: Softly blinking / rotating Onyx core
                        Box(
                            modifier = Modifier
                                .rotate(-rotationAngle)
                                .size(sizeDp * 0.45f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Onyx procesando",
                                tint = OnyxWhite.copy(alpha = softBlinkAlpha),
                                modifier = Modifier.size(sizeDp * 0.38f)
                            )
                        }
                    }

                    VoiceOrbMode.SPEAKING -> {
                        // AI Output: Speaking soundwaves
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Onyx hablando",
                            tint = OnyxRedPrimary,
                            modifier = Modifier.size(sizeDp * 0.38f)
                        )
                    }
                }
            }
        }

        // Status Label with subtle badge
        if (showStatusLabel) {
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0C0D12),
                border = BorderStroke(1.dp, OnyxRedPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when (state) {
                                    VoiceOrbMode.IDLE -> OnyxRedPrimary
                                    VoiceOrbMode.LISTENING -> OnyxRedVibrant
                                    VoiceOrbMode.PROCESSING -> Color(0xFFFFD600)
                                    VoiceOrbMode.SPEAKING -> Color(0xFF00E676)
                                }
                            )
                    )
                    Text(
                        text = state.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnyxWhite
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta Flotante Inteligente de Onyx Express con el orbe circular,
 * previsualización de voz en vivo y controles rápidos para la IA.
 */
@Composable
fun OnyxFloatingVoiceCard(
    state: VoiceOrbMode,
    transcribedText: String,
    micLevel: Float,
    amplitudes: List<Float>,
    onToggleListen: () -> Unit,
    onSendText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0A0B10),
        border = BorderStroke(1.5.dp, OnyxRedVibrant.copy(alpha = 0.65f)),
        tonalElevation = 12.dp,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(OnyxRedVibrant)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ONYX EXPRESS VOICE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = OnyxRedPrimary,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar voz",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Centerpiece Voice Orb
            OnyxExpressVoiceOrb(
                state = state,
                micLevel = micLevel,
                amplitudes = amplitudes,
                onClick = onToggleListen,
                sizeDp = 84.dp,
                showStatusLabel = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Transcription or Guidance
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF12141C),
                border = BorderStroke(1.dp, Color(0xFF222432))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = when (state) {
                            VoiceOrbMode.IDLE -> "Toca el círculo para hablar con Onyx Express."
                            VoiceOrbMode.LISTENING -> if (transcribedText.isBlank()) "Habla ahora... El micrófono está capturando tus ondas de voz." else transcribedText
                            VoiceOrbMode.PROCESSING -> "Onyx está razonando la solución, generando código o resolviendo tu tarea..."
                            VoiceOrbMode.SPEAKING -> "Onyx está reproduciendo la respuesta en voz alta."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (transcribedText.isNotBlank()) OnyxWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = if (transcribedText.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Cancelar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        if (transcribedText.isNotBlank()) {
                            onSendText(transcribedText)
                            onClose()
                        } else {
                            onToggleListen()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Icon(
                        imageVector = if (transcribedText.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                        contentDescription = null,
                        tint = OnyxWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (transcribedText.isNotBlank()) "Enviar a Onyx" else "Comenzar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnyxWhite
                    )
                }
            }
        }
    }
}

/**
 * Micrófono único de Onyx Express ubicado en la barra de chat.
 * Componente circular minimalista sobre fondo negro mate con un sutil borde y resplandor en rojo vibrante.
 * Conectado a la entrada y salida de audio:
 * - Cuando el usuario habla: reacciona con ondas dinámicas simulando un pulso.
 * - Cuando la IA procesa la respuesta: gira fluidamente a 360° y parpadea suavemente indicando que está respondiendo.
 */
@Composable
fun OnyxExpressChatMicButton(
    state: VoiceOrbMode,
    micLevel: Float,
    amplitudes: List<Float>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 42.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ChatMicOrbTransitions")

    // Smooth continuous rotation for PROCESSING state
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ChatMicRotation"
    )

    // Soft blinking/pulsing for PROCESSING state ("parpadea suavemente")
    val softBlinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChatMicBlinkAlpha"
    )

    val softBlinkScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChatMicBlinkScale"
    )

    // Ambient breathing glow for IDLE state
    val idleGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChatMicIdleGlow"
    )

    // Wave ripples for LISTENING state
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ChatMicWave"
    )

    val coreScale = if (state == VoiceOrbMode.LISTENING) {
        1.0f + (micLevel * 0.16f).coerceIn(0f, 0.2f)
    } else if (state == VoiceOrbMode.PROCESSING) {
        softBlinkScale
    } else 1.0f

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Canvas Glow & Waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val coreRadius = (sizeDp.toPx() / 2f) - 3.dp.toPx()

            // 1. Vibrant Red Resplandor / Halo
            val glowAlpha = when (state) {
                VoiceOrbMode.IDLE -> idleGlowAlpha
                VoiceOrbMode.LISTENING -> (0.6f + micLevel * 0.4f).coerceIn(0.6f, 1f)
                VoiceOrbMode.PROCESSING -> softBlinkAlpha * 0.85f
                VoiceOrbMode.SPEAKING -> 0.8f
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OnyxRedVibrant.copy(alpha = glowAlpha),
                        OnyxRedPrimary.copy(alpha = glowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width / 2f
                ),
                radius = size.width / 2f,
                center = centerOffset
            )

            // 2. Dynamic Wave Ripple when user speaks into microphone
            if (state == VoiceOrbMode.LISTENING) {
                val currentRadius = coreRadius + (size.width / 2f - coreRadius) * waveProgress
                val alpha = (1.0f - waveProgress) * (0.5f + micLevel * 0.5f)
                drawCircle(
                    color = OnyxRedVibrant.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = currentRadius,
                    center = centerOffset,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 3. Rotating glowing arc when AI processes response ("el círculo gira")
            if (state == VoiceOrbMode.PROCESSING) {
                val arcRadius = coreRadius + 1.5.dp.toPx()
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            OnyxRedPrimary.copy(alpha = 0.5f),
                            OnyxRedVibrant,
                            OnyxWhite,
                            OnyxRedVibrant,
                            Color.Transparent
                        )
                    ),
                    startAngle = rotationAngle,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - arcRadius, centerOffset.y - arcRadius),
                    size = androidx.compose.ui.geometry.Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Minimalist Matte Black Core with Subtle Vibrant Red Border
        Box(
            modifier = Modifier
                .size(sizeDp - 6.dp)
                .scale(coreScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF13151D),
                            Color(0xFF090A0D),
                            Color(0xFF050507)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        width = if (state == VoiceOrbMode.PROCESSING) 1.8.dp else 1.2.dp,
                        brush = if (state == VoiceOrbMode.PROCESSING) {
                            Brush.sweepGradient(
                                listOf(
                                    OnyxRedVibrant,
                                    OnyxWhite.copy(alpha = softBlinkAlpha),
                                    OnyxRedPrimary,
                                    OnyxRedDeep,
                                    OnyxRedVibrant
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    OnyxRedVibrant.copy(alpha = 0.95f),
                                    OnyxRedPrimary.copy(alpha = 0.70f)
                                )
                            )
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                VoiceOrbMode.IDLE -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Entrada de voz Onyx Express",
                        tint = OnyxWhite,
                        modifier = Modifier.size(19.dp)
                    )
                }
                VoiceOrbMode.LISTENING -> {
                    // Pulsing animated equalizer glyph reacting to mic level
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bars = amplitudes.take(4)
                        bars.forEach { amp ->
                            val h = 6.dp + (14.dp * amp * (0.8f + micLevel * 0.4f))
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(h.coerceIn(4.dp, 18.dp))
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(OnyxRedVibrant)
                            )
                        }
                    }
                }
                VoiceOrbMode.PROCESSING -> {
                    // Softly blinking/rotating core indicator
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Onyx procesando",
                        tint = OnyxWhite.copy(alpha = softBlinkAlpha),
                        modifier = Modifier.size(17.dp)
                    )
                }
                VoiceOrbMode.SPEAKING -> {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Onyx hablando",
                        tint = OnyxRedPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
