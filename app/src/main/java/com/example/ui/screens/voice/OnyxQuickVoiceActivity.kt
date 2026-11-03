package com.example.ui.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.VoicePersona
import com.example.engine.ExecutiveVoiceResponse
import com.example.engine.OnyxExecutiveAssistant
import com.example.engine.VoiceManager
import com.example.domain.model.VoiceOrbMode
import com.example.ui.components.OnyxExpressVoiceOrb
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnyxDarkBg
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

class OnyxQuickVoiceActivity : ComponentActivity() {

    companion object {
        const val EXTRA_AUTO_START_RECORDING = "EXTRA_AUTO_START_RECORDING"
    }

    private lateinit var voiceManager: VoiceManager

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startExecutiveListening()
        }
    }

    private var onTranscriptionCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = VoiceManager(applicationContext)
        voiceManager.setPersona(VoicePersona.ONYX_EXECUTIVE)

        val autoStart = intent.getBooleanExtra(EXTRA_AUTO_START_RECORDING, true)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                QuickVoiceOverlayScreen(
                    voiceManager = voiceManager,
                    autoStart = autoStart,
                    onRequestPermission = { callback ->
                        onTranscriptionCallback = callback
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            startExecutiveListening()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onDismiss = {
                        voiceManager.stop()
                        voiceManager.stopListening()
                        finish()
                    }
                )
            }
        }
    }

    private fun startExecutiveListening() {
        onTranscriptionCallback?.let { callback ->
            voiceManager.startListening(callback)
        }
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.shutdown()
    }
}

@Composable
fun QuickVoiceOverlayScreen(
    voiceManager: VoiceManager,
    autoStart: Boolean,
    onRequestPermission: (((String) -> Unit)) -> Unit,
    onDismiss: () -> Unit
) {
    val isListening by voiceManager.isListening.collectAsStateWithLifecycle()
    val isSpeaking by voiceManager.isSpeaking.collectAsStateWithLifecycle()
    val micLevel by voiceManager.micLevel.collectAsStateWithLifecycle()
    val amplitudes by voiceManager.waveformAmplitudes.collectAsStateWithLifecycle()

    var isProcessing by remember { mutableStateOf(false) }
    var currentTranscription by remember { mutableStateOf("") }
    var executiveResponse by remember { mutableStateOf<ExecutiveVoiceResponse?>(null) }

    val voiceOrbState = when {
        isSpeaking -> VoiceOrbMode.SPEAKING
        isProcessing -> VoiceOrbMode.PROCESSING
        isListening -> VoiceOrbMode.LISTENING
        else -> VoiceOrbMode.IDLE
    }

    fun initiateListening() {
        executiveResponse = null
        isProcessing = false
        currentTranscription = ""
        onRequestPermission { transcribedText ->
            currentTranscription = transcribedText
            isProcessing = true
            // Process with Onyx Executive AI Brain
            val response = OnyxExecutiveAssistant.processDirective(transcribedText, isQuickSettingsLaunch = true)
            executiveResponse = response
            isProcessing = false
            // Speak concisely in deep masculine executive tone
            voiceManager.speak(response.spokenText)
        }
    }

    LaunchedEffect(autoStart) {
        if (autoStart) {
            initiateListening()
        }
    }

    // Auto-dismiss smoothly when Onyx IA finishes speaking the response
    LaunchedEffect(isSpeaking, executiveResponse) {
        if (executiveResponse != null && !isSpeaking && !isProcessing && !isListening) {
            kotlinx.coroutines.delay(2800)
            onDismiss()
        }
    }

    // Pure Floating HUD: Transparent overlay directly over the home screen wallpaper or background app
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .clickable(enabled = false, onClick = {}),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimalist Circular Animated Onyx Express Orb
            // Reacciona en tiempo real a los niveles de audio durante la conversación
            OnyxExpressVoiceOrb(
                state = voiceOrbState,
                micLevel = micLevel,
                amplitudes = amplitudes,
                onClick = {
                    if (isListening) {
                        voiceManager.stopListening()
                    } else if (isSpeaking) {
                        voiceManager.stop()
                    } else {
                        initiateListening()
                    }
                },
                sizeDp = 88.dp,
                showStatusLabel = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sleek Floating Pill HUD (Transparent Dark Glass with Vibrant Red Border)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xF2090A0E),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(listOf(OnyxRedVibrant, OnyxRedPrimary, OnyxRedVibrant))
                ),
                shadowElevation = 16.dp,
                modifier = Modifier.widthIn(max = 380.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(OnyxRedVibrant)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ONYX IA • NIVEL EJECUTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 9.sp,
                                color = OnyxRedPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161822))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = OnyxWhite,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val displayText = when {
                        executiveResponse != null -> executiveResponse?.spokenText ?: ""
                        isProcessing -> "Procesando directiva con rigor ejecutivo..."
                        isListening -> if (currentTranscription.isBlank()) "Escuchando... Di tu orden ahora" else "\"$currentTranscription\""
                        else -> "Toca el círculo para dar una orden ejecutiva"
                    }

                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (executiveResponse != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = OnyxWhite,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
