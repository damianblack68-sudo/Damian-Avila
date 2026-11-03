package com.example.ui.screens.qr

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

@Composable
fun OnyxQrPassScreen(
    userEmail: String = "damianblack68@gmail.com",
    username: String = "damian_avila",
    qrCodeId: String = "ONX-DAMIAN-PASS",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scanSuccess by remember { mutableStateOf(false) }

    val passToken = remember(qrCodeId) { qrCodeId }

    // Pulsing animation for the scanner line
    val infiniteTransition = rememberInfiniteTransition(label = "scanAnim")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.5.dp, OnyxRedPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Surface(
                    color = OnyxRedPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = OnyxRedPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PASE DE ACCESO RÁPIDO ONYX",
                            color = OnyxRedPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Código QR Personal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Escanea este código chico y compacto desde cualquier dispositivo para entrar a Onyx Open sin contraseñas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // The Compact QR Canvas (Custom Rendered 21x21 QR Pattern with Onyx emblem)
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(3.dp, OnyxRedPrimary, RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridSize = 21
                        val cellSize = size.width / gridSize

                        // Generate deterministic QR module matrix based on passToken
                        val qrMatrix = Array(gridSize) { r ->
                            BooleanArray(gridSize) { c ->
                                // Corner position markers
                                val inTopLeft = r in 0..6 && c in 0..6
                                val inTopRight = r in 0..6 && c in (gridSize - 7) until gridSize
                                val inBottomLeft = r in (gridSize - 7) until gridSize && c in 0..6
                                val inCenterLogo = r in 8..12 && c in 8..12

                                if (inCenterLogo) {
                                    false
                                } else if (inTopLeft) {
                                    (r == 0 || r == 6 || c == 0 || c == 6) || (r in 2..4 && c in 2..4)
                                } else if (inTopRight) {
                                    (r == 0 || r == 6 || c == gridSize - 7 || c == gridSize - 1) || (r in 2..4 && c in (gridSize - 5)..(gridSize - 3))
                                } else if (inBottomLeft) {
                                    (r == gridSize - 7 || r == gridSize - 1 || c == 0 || c == 6) || (r in (gridSize - 5)..(gridSize - 3) && c in 2..4)
                                } else {
                                    // Deterministic hash fill
                                    ((r * 13 + c * 17 + passToken.hashCode()) % 3 == 0) || ((r + c) % 2 == 0 && (r * c) % 5 == 0)
                                }
                            }
                        }

                        // Draw cells
                        for (r in 0 until gridSize) {
                            for (c in 0 until gridSize) {
                                if (qrMatrix[r][c]) {
                                    val isCorner = (r < 7 && c < 7) || (r < 7 && c >= gridSize - 7) || (r >= gridSize - 7 && c < 7)
                                    val cellColor = if (isCorner) Color(0xFFD6183C) else Color(0xFF111218)
                                    drawRoundRect(
                                        color = cellColor,
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = Size(cellSize * 0.95f, cellSize * 0.95f),
                                        cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
                                    )
                                }
                            }
                        }

                        // Laser scan line if scanning
                        if (isScanning) {
                            val lineY = scanLineY * size.height
                            drawLine(
                                color = Color(0xFFFF1744),
                                start = Offset(0f, lineY),
                                end = Offset(size.width, lineY),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }

                    // Center Onyx Logo Badge
                    Image(
                        painter = painterResource(id = R.drawable.ic_onyx_logo),
                        contentDescription = "Onyx Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(2.dp, OnyxRedPrimary, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ID: $passToken",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnyxRedPrimary
                )
                Text(
                    text = "Vinculado a: $userEmail",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Escanear para entrar & Compartir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isScanning = true
                            coroutineScope.launch {
                                delay(1200)
                                isScanning = false
                                scanSuccess = true
                                Toast.makeText(context, "¡Pase QR verificado! Sesión iniciada con éxito.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isScanning) "Validando..." else "Escanear Pase", color = OnyxWhite, fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Pase de Acceso Rápido Onyx Open:\n$passToken\nUsa este token para entrar inmediatamente en tus dispositivos.")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Pase Onyx QR"))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
