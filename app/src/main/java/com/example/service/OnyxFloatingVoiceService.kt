package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.domain.model.VoiceOrbMode
import com.example.domain.model.VoicePersona
import com.example.engine.ExecutiveVoiceResponse
import com.example.engine.OnyxExecutiveAssistant
import com.example.engine.VoiceManager
import com.example.ui.components.OnyxExpressVoiceOrb
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * OnyxFloatingVoiceService:
 * Burbuja flotante circular minimalista que permanece en la pantalla de inicio
 * y sobre cualquier aplicación, permitiendo hablar con Onyx IA inmediatamente
 * sin entrar a la aplicación ni interrumpir lo que el usuario está haciendo.
 */
class OnyxFloatingVoiceService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        const val ACTION_START_LISTENING = "ACTION_START_LISTENING"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val NOTIFICATION_ID = 901
        private const val CHANNEL_ID = "onyx_floating_voice_channel"

        fun isOverlayPermitted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        }

        fun startService(context: Context) {
            val intent = Intent(context, OnyxFloatingVoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, OnyxFloatingVoiceService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var floatingView: FrameLayout? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private lateinit var voiceManager: VoiceManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var autoCollapseJob: Job? = null

    // Reactive states
    private var isExpandedState by mutableStateOf(false)
    private var transcriptionText by mutableStateOf("")
    private var currentResponse by mutableStateOf<ExecutiveVoiceResponse?>(null)
    private var isProcessingState by mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        voiceManager = VoiceManager(applicationContext)
        voiceManager.setPersona(VoicePersona.ONYX_EXECUTIVE)

        startForegroundNotification()

        if (isOverlayPermitted(this)) {
            setupFloatingWindow()
        }
    }

    private fun startForegroundNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Onyx IA Asistente Flotante",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene el círculo de voz ejecutiva activo en la pantalla de inicio"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, OnyxFloatingVoiceService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Onyx IA • Asistente Flotante Activo")
            .setContentText("Toca el círculo en tu pantalla para hablar sin entrar a la app")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Ocultar", stopPending)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 300
        }

        floatingView = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OnyxFloatingVoiceService)
            setViewTreeViewModelStoreOwner(this@OnyxFloatingVoiceService)
            setViewTreeSavedStateRegistryOwner(this@OnyxFloatingVoiceService)

            setContent {
                MyApplicationTheme(darkTheme = true) {
                    FloatingOrbContent()
                }
            }
        }

        floatingView?.addView(composeView)

        // Drag & Touch movement listener
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var hasMoved = false

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val params = windowParams ?: return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        hasMoved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            hasMoved = true
                            params.x = initialX + dx
                            params.y = initialY + dy
                            try {
                                windowManager?.updateViewLayout(floatingView, params)
                            } catch (_: Exception) {}
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!hasMoved) {
                            // Tap detected: Toggle voice listening right on the home screen!
                            onOrbTapped()
                        }
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager?.addView(floatingView, windowParams)
        } catch (_: Exception) {}
    }

    private fun onOrbTapped() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
            isExpandedState = false
        } else if (voiceManager.isSpeaking.value) {
            voiceManager.stop()
            isExpandedState = false
        } else {
            startFloatingVoiceInteraction()
        }
    }

    private fun startFloatingVoiceInteraction() {
        autoCollapseJob?.cancel()
        isExpandedState = true
        transcriptionText = ""
        currentResponse = null
        isProcessingState = false

        voiceManager.startListening { transcribed ->
            transcriptionText = transcribed
            isProcessingState = true
            serviceScope.launch {
                val response = OnyxExecutiveAssistant.processDirective(transcribed, isQuickSettingsLaunch = true)
                currentResponse = response
                isProcessingState = false

                // Speak with deep masculine executive voice
                voiceManager.speak(response.spokenText)

                // Schedule auto collapse back to minimal circle
                autoCollapseJob = serviceScope.launch {
                    delay(4500)
                    if (!voiceManager.isSpeaking.value) {
                        isExpandedState = false
                        transcriptionText = ""
                        currentResponse = null
                    }
                }
            }
        }
    }

    @Composable
    private fun FloatingOrbContent() {
        val isListening by voiceManager.isListening.collectAsStateWithLifecycle()
        val isSpeaking by voiceManager.isSpeaking.collectAsStateWithLifecycle()
        val micLevel by voiceManager.micLevel.collectAsStateWithLifecycle()
        val amplitudes by voiceManager.waveformAmplitudes.collectAsStateWithLifecycle()

        val voiceOrbState = when {
            isSpeaking -> VoiceOrbMode.SPEAKING
            isProcessingState -> VoiceOrbMode.PROCESSING
            isListening -> VoiceOrbMode.LISTENING
            else -> VoiceOrbMode.IDLE
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Minimalist Circular Animated Onyx Express Orb
            // Reacciona en tiempo real a los niveles de audio en la pantalla de inicio
            OnyxExpressVoiceOrb(
                state = voiceOrbState,
                micLevel = micLevel,
                amplitudes = amplitudes,
                onClick = { onOrbTapped() },
                sizeDp = if (isExpandedState) 68.dp else 56.dp,
                showStatusLabel = false
            )

            // Sleek Speech Bubble HUD appearing on tap/voice
            AnimatedVisibility(
                visible = isExpandedState,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xF2090A0E),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        Brush.horizontalGradient(listOf(OnyxRedVibrant, OnyxRedPrimary))
                    ),
                    shadowElevation = 12.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ONYX IA • PROTOCOLO EJECUTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = OnyxRedPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E212D))
                                    .clickable {
                                        voiceManager.stop()
                                        voiceManager.stopListening()
                                        isExpandedState = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Ocultar",
                                    tint = OnyxWhite,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle status & response
                        val messageToShow = when {
                            currentResponse != null -> currentResponse?.spokenText ?: ""
                            isProcessingState -> "Analizando directiva ejecutiva..."
                            isListening -> if (transcriptionText.isBlank()) "Escuchando directiva..." else "\"$transcriptionText\""
                            else -> "Listo. Di tu orden ejecutiva."
                        }

                        Text(
                            text = messageToShow,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = OnyxWhite
                        )
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
            ACTION_START_LISTENING -> {
                startFloatingVoiceInteraction()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        appViewModelStore.clear()

        voiceManager.stop()
        voiceManager.shutdown()
        autoCollapseJob?.cancel()

        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
