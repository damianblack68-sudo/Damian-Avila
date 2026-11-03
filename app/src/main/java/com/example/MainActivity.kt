package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.engine.OnyxNotificationHelper
import com.example.ui.screens.voice.OnyxQuickVoiceActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.credentials.CredentialManager
import com.example.auth.FirebaseAuthHelper
import com.example.engine.OnyxProactiveAlertManager
import com.example.service.OnyxFirebaseMessagingService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.domain.model.ChatAttachment
import com.example.ui.components.OnyxCompactAdBanner
import com.example.ui.components.OnyxTopBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.creative.ui.OnyxCreativeStudioScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.contact.ContactOwnerScreen
import com.example.ui.screens.messenger.UserMessengerScreen
import com.example.ui.screens.profile.UserProfileScreen
import com.example.ui.screens.qr.OnyxQrPassScreen
import com.example.ui.screens.settings.SettingsSyncScreen
import com.example.ui.screens.splash.OnyxSplashIntro
import com.example.ui.screens.subscription.SubscriptionShopScreen
import com.example.ui.screens.workspace.WorkspaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NAV_TAB = "EXTRA_NAV_TAB"
    }

    private val viewModel: OnyxViewModel by viewModels()
    private val requestedNavTab = mutableStateOf<Int?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasExtra(EXTRA_NAV_TAB)) {
            requestedNavTab.value = intent.getIntExtra(EXTRA_NAV_TAB, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OnyxNotificationHelper.initChannels(this)

        if (intent.hasExtra(EXTRA_NAV_TAB)) {
            requestedNavTab.value = intent.getIntExtra(EXTRA_NAV_TAB, 0)
        }

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val credentialManager = remember { CredentialManager.create(context) }

                val isFromWidget = remember { intent.hasExtra(EXTRA_NAV_TAB) }
                var showSplashIntro by remember { mutableStateOf(!isFromWidget) }
                var currentUser by remember {
                    mutableStateOf<UserProfile?>(
                        Firebase.auth.currentUser?.let { FirebaseAuthHelper.mapFirebaseUserToProfile(it) }
                    )
                }
                var isOwnerAdminMode by remember {
                    mutableStateOf(currentUser?.role == UserRole.ADMIN_CREATOR)
                }
                var showSubscriptionShop by remember { mutableStateOf(false) }
                var showCreativeStudio by remember { mutableStateOf(false) }
                var adsEnabled by remember { mutableStateOf(true) }
                var totalAdClicks by remember { mutableIntStateOf(0) }

                DisposableEffect(Unit) {
                    val authListener = FirebaseAuth.AuthStateListener { auth ->
                        val fbUser = auth.currentUser
                        if (fbUser != null) {
                            val profile = FirebaseAuthHelper.mapFirebaseUserToProfile(fbUser)
                            currentUser = profile
                            if (profile.role == UserRole.ADMIN_CREATOR) {
                                isOwnerAdminMode = true
                            }
                        }
                    }
                    Firebase.auth.addAuthStateListener(authListener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(authListener)
                    }
                }

                LaunchedEffect(currentUser) {
                    OnyxFirebaseMessagingService.syncDeviceFcmToken(context)
                    if (currentUser != null) {
                        OnyxProactiveAlertManager.startListeningForAppUpdates(context)
                    }
                }

                if (showCreativeStudio) {
                    BackHandler { showCreativeStudio = false }
                    OnyxCreativeStudioScreen(
                        onNavigateBack = { showCreativeStudio = false }
                    )
                } else if (showSplashIntro) {
                    OnyxSplashIntro(
                        onSplashFinished = { showSplashIntro = false }
                    )
                } else if (currentUser == null) {
                    // Registration & Login Screen with Google, FB, IG, Email and Admin auto-detection
                    AuthScreen(
                        onLoginSuccess = { user ->
                            currentUser = user
                            if (user.role == UserRole.ADMIN_CREATOR) {
                                isOwnerAdminMode = true
                            }
                        }
                    )
                } else {
                    val user = currentUser!!
                    val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
                    val currentTone by viewModel.currentTone.collectAsStateWithLifecycle()
                    val currentPersona by viewModel.currentVoicePersona.collectAsStateWithLifecycle()
                    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
                    val isListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
                    val amplitudes by viewModel.voiceManager.waveformAmplitudes.collectAsStateWithLifecycle()
                    val micLevel by viewModel.voiceManager.micLevel.collectAsStateWithLifecycle()
                    val isAiProcessing by viewModel.isAiProcessing.collectAsStateWithLifecycle()

                    val sessions by viewModel.chatSessions.collectAsStateWithLifecycle()
                    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
                    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
                    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
                    val notes by viewModel.notes.collectAsStateWithLifecycle()
                    val attendanceRecords by viewModel.attendanceRecords.collectAsStateWithLifecycle()
                    val selectedAttendanceDate by viewModel.selectedAttendanceDate.collectAsStateWithLifecycle()
                    val selectedAttendanceGroup by viewModel.selectedAttendanceGroup.collectAsStateWithLifecycle()

                    val externalRequestedTab by requestedNavTab
                    var currentNavIndex by remember { mutableIntStateOf(externalRequestedTab ?: 0) }

                    LaunchedEffect(externalRequestedTab) {
                        externalRequestedTab?.let { tabIndex ->
                            currentNavIndex = tabIndex
                            requestedNavTab.value = null
                        }
                    }
                    var pendingAudioCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

                    val audioPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            pendingAudioCallback?.let { callback ->
                                viewModel.voiceManager.startListening(callback)
                            }
                        } else {
                            Toast.makeText(context, "Permiso de micrófono denegado para entrada de voz", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Navigation Back Handler: Return to Home (Chat) tab instead of quitting app
                    BackHandler(enabled = currentNavIndex != 0) {
                        currentNavIndex = 0
                    }

                    // Notification Permission Launcher for Android 13+ (POST_NOTIFICATIONS)
                    val notifPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { /* granted */ }

                    LaunchedEffect(Unit) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            OnyxTopBar(
                                currentProfile = currentProfile,
                                onProfileChange = { viewModel.setProfile(it) },
                                currentTone = currentTone,
                                onToneChange = { viewModel.setTone(it) },
                                isSpeaking = isSpeaking,
                                amplitudes = amplitudes,
                                currentUser = user,
                                isOwnerMode = isOwnerAdminMode,
                                onToggleOwnerMode = {
                                    if (user.role == UserRole.ADMIN_CREATOR) {
                                        isOwnerAdminMode = !isOwnerAdminMode
                                        val msg = if (isOwnerAdminMode) "¡Bienvenido Damian Ávila! Acceso Maestro al Servidor concedido." else "Cambiado a vista de Usuario Estándar."
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "El servidor maestro está reservado para el creador damianblack68@gmail.com", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onOpenSubscriptionShop = { showSubscriptionShop = true },
                                onOpenCreativeStudio = { showCreativeStudio = true },
                                onLogout = {
                                    FirebaseAuthHelper.signOut(context, credentialManager, onSignOutComplete = {
                                        currentUser = null
                                        isOwnerAdminMode = false
                                        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                                    }, coroutineScope)
                                },
                                onOpenQuickVoice = {
                                    val intent = Intent(context, OnyxQuickVoiceActivity::class.java).apply {
                                        putExtra(OnyxQuickVoiceActivity.EXTRA_AUTO_START_RECORDING, true)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        },
                        bottomBar = {
                            if (!isOwnerAdminMode) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = OnyxRedPrimary,
                                    windowInsets = WindowInsets.navigationBars
                                ) {
                                    val items = listOf(
                                        Triple(0, "Onyx IA", Icons.Default.ChatBubbleOutline),
                                        Triple(1, "Workspace", Icons.Default.Checklist),
                                        Triple(2, "Mensajes", Icons.Default.Chat),
                                        Triple(3, "Pase QR", Icons.Default.QrCode2),
                                        Triple(4, "Dueño & Ajustes", Icons.Default.SupportAgent)
                                    )

                                    items.forEach { (index, label, icon) ->
                                        NavigationBarItem(
                                            selected = currentNavIndex == index,
                                            onClick = { currentNavIndex = index },
                                            icon = {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (currentNavIndex == index) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = OnyxWhite,
                                                selectedTextColor = OnyxRedPrimary,
                                                indicatorColor = OnyxRedPrimary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Non-intrusive compact ad banner: Only shown if ads are active, in standard user mode, and user is on free tier
                            val shouldShowAds = adsEnabled && !isOwnerAdminMode && (user.role != UserRole.ADMIN_CREATOR) && (user.subscription.hasAds)
                            if (shouldShowAds) {
                                OnyxCompactAdBanner(
                                    onAdClick = {
                                        totalAdClicks++
                                        Toast.makeText(context, "Patrocinador abierto • Contribuyendo al servidor de Onyx Open", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                if (isOwnerAdminMode) {
                                    // Dedicated Platform for Owner / Master Admin Server
                                    AdminDashboardScreen(
                                        adsEnabled = adsEnabled,
                                        onToggleAds = { adsEnabled = it },
                                        totalAdClicks = totalAdClicks,
                                        onExitAdminMode = { isOwnerAdminMode = false }
                                    )
                                } else {
                                    // User Platform
                                    Crossfade(targetState = currentNavIndex, label = "ScreenTransition") { screenIndex ->
                                        when (screenIndex) {
                                            0 -> ChatScreen(
                                                currentProfile = currentProfile,
                                                sessions = sessions,
                                                currentSessionId = currentSessionId,
                                                messages = messages,
                                                onSelectSession = { viewModel.selectChatSession(it) },
                                                onCreateSession = { viewModel.createChatSession(it) },
                                                onDeleteSession = { viewModel.deleteChatSession(it) },
                                                onSendMessage = { text, isMath, chartJson, clarifyingJson, engine, attachment ->
                                                    viewModel.sendMessage(text, isMath, chartJson, clarifyingJson, engine, attachment)
                                                },
                                                onSpeak = { text -> viewModel.voiceManager.speak(text) },
                                                onStartVoiceInput = { callback ->
                                                    pendingAudioCallback = callback
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                        viewModel.voiceManager.startListening(callback)
                                                    } else {
                                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                },
                                                isListening = isListening,
                                                amplitudes = amplitudes,
                                                isAiProcessing = isAiProcessing,
                                                micLevel = micLevel,
                                                isSpeaking = isSpeaking,
                                                onStopSpeaking = { viewModel.voiceManager.stop() },
                                                onStopVoiceInput = { viewModel.voiceManager.stopListening() },
                                                currentUser = user,
                                                onOpenSubscriptionShop = { showSubscriptionShop = true }
                                            )

                                            1 -> WorkspaceScreen(
                                                currentProfile = currentProfile,
                                                tasks = tasks,
                                                notes = notes,
                                                onSaveTask = { viewModel.saveTask(it) },
                                                onToggleTask = { viewModel.toggleTask(it) },
                                                onDeleteTask = { viewModel.deleteTask(it) },
                                                onSaveNote = { viewModel.saveNote(it) },
                                                onDeleteNote = { viewModel.deleteNote(it) },
                                                attendanceRecords = attendanceRecords,
                                                selectedAttendanceDate = selectedAttendanceDate,
                                                selectedAttendanceGroup = selectedAttendanceGroup,
                                                onAttendanceDateChange = { viewModel.setAttendanceDate(it) },
                                                onAttendanceGroupChange = { viewModel.setAttendanceGroup(it) },
                                                onCycleAttendanceStatus = { viewModel.cycleAttendanceStatus(it) },
                                                onSetAttendanceStatus = { record, status -> viewModel.setAttendanceStatus(record, status) },
                                                onUpdateAttendanceNotes = { record, notes -> viewModel.updateStudentNotes(record, notes) },
                                                onMarkAllAttendance = { viewModel.markAllStudents(it) },
                                                onAddStudentAttendance = { name, uname, email, notes -> viewModel.addStudentToCurrentGroup(name, uname, email, notes) },
                                                onDeleteStudentAttendance = { id, name -> viewModel.deleteStudentFromCurrentGroup(id, name) },
                                                onExportAttendanceReport = { viewModel.exportAttendanceSummary() },
                                                currentUser = user
                                            )

                                            2 -> UserMessengerScreen(
                                                currentUser = user,
                                                onShowMyQr = { currentNavIndex = 3 }
                                            )

                                            3 -> OnyxQrPassScreen(
                                                userEmail = user.email,
                                                username = user.username,
                                                qrCodeId = user.qrCodeId
                                            )

                                            4 -> {
                                                var settingsSubTab by remember { mutableStateOf(0) }
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                    TabRow(
                                                        selectedTabIndex = settingsSubTab,
                                                        containerColor = MaterialTheme.colorScheme.surface,
                                                        contentColor = OnyxRedPrimary
                                                    ) {
                                                        Tab(
                                                            selected = settingsSubTab == 0,
                                                            onClick = { settingsSubTab = 0 },
                                                            text = { Text("Mi Perfil", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        )
                                                        Tab(
                                                            selected = settingsSubTab == 1,
                                                            onClick = { settingsSubTab = 1 },
                                                            text = { Text("Contactar al Dueño", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                                            icon = { Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        )
                                                        Tab(
                                                            selected = settingsSubTab == 2,
                                                            onClick = { settingsSubTab = 2 },
                                                            text = { Text("Voces & Nube", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                                            icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        )
                                                    }

                                                    when (settingsSubTab) {
                                                        0 -> UserProfileScreen(
                                                            currentUser = user,
                                                            onUpdateProfile = { updated -> currentUser = updated },
                                                            onOpenShop = { showSubscriptionShop = true }
                                                        )
                                                        1 -> ContactOwnerScreen()
                                                        else -> SettingsSyncScreen(
                                                            currentProfile = currentProfile,
                                                            onProfileChange = { viewModel.setProfile(it) },
                                                            currentPersona = currentPersona,
                                                            onPersonaChange = { viewModel.setVoicePersona(it) },
                                                            onTestVoice = {
                                                                viewModel.voiceManager.speak("Hola, soy ${currentPersona.title}. Sistema de voz sintética listo.")
                                                            }
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

                    // Subscription Shop Dialog
                    if (showSubscriptionShop) {
                        Dialog(
                            onDismissRequest = { showSubscriptionShop = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                SubscriptionShopScreen(
                                    currentTier = user.subscription,
                                    onUpgradeTier = { newTier, paymentCard ->
                                        currentUser = user.copy(
                                            subscription = newTier,
                                            isVerified = newTier.isVerified || user.isVerified,
                                            savedPaymentCard = paymentCard ?: user.savedPaymentCard
                                        )
                                    },
                                    onDismiss = { showSubscriptionShop = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
