package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.EmotionalTone
import com.example.domain.model.ProfileType
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

@Composable
fun OnyxTopBar(
    currentProfile: ProfileType,
    onProfileChange: (ProfileType) -> Unit,
    currentTone: EmotionalTone,
    onToneChange: (EmotionalTone) -> Unit,
    isSpeaking: Boolean,
    amplitudes: List<Float>,
    currentUser: UserProfile? = null,
    isOwnerMode: Boolean = false,
    onToggleOwnerMode: () -> Unit = {},
    onOpenSubscriptionShop: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenQuickVoice: () -> Unit = {},
    onOpenCreativeStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showToneMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Row: Logo, Mode toggles, Tone selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_onyx_logo),
                    contentDescription = "Onyx Open Logo",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ONYX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = OnyxRedPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "OPEN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Inteligencia Artificial",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Profile Switcher (Personal / Laboral)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        val next = if (currentProfile == ProfileType.PERSONAL) ProfileType.LABORAL else ProfileType.PERSONAL
                        onProfileChange(next)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentProfile == ProfileType.PERSONAL) Icons.Default.Person else Icons.Default.Business,
                        contentDescription = "Perfil",
                        tint = OnyxRedPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentProfile == ProfileType.PERSONAL) "Personal" else "Laboral",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Owner Master Admin Switcher (If Admin user or toggle)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isOwnerMode) OnyxRedPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleOwnerMode() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Plataforma Servidor Dueño",
                        tint = if (isOwnerMode) OnyxWhite else OnyxRedPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOwnerMode) "Servidor Admin" else "Admin",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isOwnerMode) OnyxWhite else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Botón de Acceso a ONYX CREATIVE STUDIO (Arquitectura e Imágenes IA)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF191C2C),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxRedPrimary.copy(alpha = 0.7f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenCreativeStudio() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = "Onyx Creative Studio",
                        tint = OnyxRedPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Studio",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = OnyxWhite
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Voice Assistant Button (Acceso Rápido Ejecutivo / Quick Settings)
            IconButton(
                onClick = onOpenQuickVoice,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141620))
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Onyx IA Voz Rápida",
                    tint = OnyxRedPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Emotional Tone Selector
            Box {
                IconButton(
                    onClick = { showToneMenu = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Tono Emocional",
                        tint = OnyxRedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showToneMenu,
                    onDismissRequest = { showToneMenu = false }
                ) {
                    EmotionalTone.values().forEach { tone ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = tone.label,
                                        fontWeight = if (tone == currentTone) FontWeight.Bold else FontWeight.Normal,
                                        color = if (tone == currentTone) OnyxRedPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = tone.promptHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onToneChange(tone)
                                showToneMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Sub Row: Current User Identity, Verification Badge, Subscription Pill & Logout
        if (currentUser != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User ID & Verification
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${currentUser.username}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                    if (currentUser.isVerified || currentUser.subscription.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Cuenta Verificada",
                            tint = Color(0xFF2979FF),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    if (currentUser.role == UserRole.ADMIN_CREATOR) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OnyxRedPrimary
                        ) {
                            Text(
                                text = "CREADOR & ADMIN",
                                color = OnyxWhite,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Subscription Badge & Upgrade trigger
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val tier = currentUser.subscription
                    val isFree = tier == SubscriptionTier.FREE
                    val isEvolution = tier == SubscriptionTier.EVOLUTION_1000

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEvolution) OnyxRedPrimary.copy(alpha = 0.2f) else if (isFree) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF00C853).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenSubscriptionShop() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isFree) "Plan Gratis • Mejorar 💎" else "⭐ ${tier.title}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEvolution) OnyxRedPrimary else if (isFree) OnyxRedPrimary else Color(0xFF00E676)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Switch account / logout
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Animated Sound Wave when speaking
        AnimatedVisibility(visible = isSpeaking) {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = OnyxRedPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Onyx está hablando...",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnyxRedPrimary,
                        fontSize = 11.sp
                    )
                }
                SoundWaveVisualizer(amplitudes = amplitudes, isActive = true)
            }
        }
    }
}
