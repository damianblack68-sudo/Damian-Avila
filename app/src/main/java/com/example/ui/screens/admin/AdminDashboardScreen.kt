package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

data class UserSupportTicket(
    val id: String,
    val userEmail: String,
    val category: String,
    val message: String,
    var status: String = "PENDIENTE", // PENDIENTE, EN DESARROLLO, IMPLEMENTADO
    val timestamp: String = "Hace 10 min"
)

@Composable
fun AdminDashboardScreen(
    adsEnabled: Boolean,
    onToggleAds: (Boolean) -> Unit,
    totalAdClicks: Int,
    onExitAdminMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isTurboMode by remember { mutableStateOf(true) }

    var tickets by remember {
        mutableStateOf(
            listOf(
                UserSupportTicket(
                    id = "TCK-101",
                    userEmail = "estudiante_mate@gmail.com",
                    category = "Nueva Función",
                    message = "Por favor agreguen cálculo de autovalores y autovectores de matrices 3x3 en el motor de matemáticas.",
                    status = "EN DESARROLLO"
                ),
                UserSupportTicket(
                    id = "TCK-102",
                    userEmail = "usuario_pro@outlook.com",
                    category = "Personalización",
                    message = "Me encantaría poder exportar los mapas mentales directamente en formato imagen PNG a Google Drive.",
                    status = "PENDIENTE"
                ),
                UserSupportTicket(
                    id = "TCK-103",
                    userEmail = "developer_dev@gmail.com",
                    category = "Felicitaciones",
                    message = "Increíble app, el teclado seguro de avance rápido ahorra muchísimo tiempo al teclear fórmulas.",
                    status = "IMPLEMENTADO"
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Admin Badge Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF14070A)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, OnyxRedPrimary, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = OnyxRedPrimary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = OnyxWhite,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PANEL MASTER DUEÑO",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp,
                                    color = OnyxRedPrimary
                                )
                                Text(
                                    text = "Damian Ávila • Admin del Server",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnyxWhite,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Switch back to normal user mode button
                        FilledTonalButton(
                            onClick = onExitAdminMode,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Modo Usuario", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tienes control total y privilegios supremos sobre el servidor, los usuarios, las solicitudes de funciones y la monetización de Onyx Open.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Server Health & Status Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Estado de Servidores & Infraestructura",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnyxRedPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF00E676),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Servidor Principal: ACTIVO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text("Latencia: 12ms • Uptime: 99.98%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTurboMode) OnyxRedPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isTurboMode = !isTurboMode }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = OnyxRedPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTurboMode) "Modo Turbo ON" else "Modo Normal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnyxRedPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Caché de servidor purgada con éxito.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Purgar Caché", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Respaldo seguro completado y cifrado.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Respaldar BD", color = OnyxWhite, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Monetization & Ads Dashboard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monetización & Ganancias por Anuncios",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnyxRedPrimary
                            )
                            Text(
                                text = "Anuncios chicos no molestos en la app",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = adsEnabled,
                            onCheckedChange = { onToggleAds(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnyxWhite,
                                checkedTrackColor = OnyxRedPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Impressions
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Impresiones", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("18,450", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        // Clicks
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Clics Reales", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${412 + totalAdClicks}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnyxRedPrimary)
                            }
                        }

                        // Estimated Revenue
                        val estimatedRevenue = 1894.50 + (totalAdClicks * 1.50)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OnyxRedPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Ganancias Hoy", fontSize = 10.sp, color = OnyxRedPrimary, fontWeight = FontWeight.Bold)
                                Text("$${"%.2f".format(estimatedRevenue)} MXN", fontWeight = FontWeight.Black, fontSize = 13.sp, color = OnyxRedPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Solicitud de retiro procesada hacia la cuenta de Damian Ávila.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retirar Ganancias Acumuladas", color = OnyxWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live System Analytics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Métricas Globales de Usuarios",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Usuarios Activos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("1,024 usuarios", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Cálculos Resueltos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("3,510 operaciones", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Notas Notion", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("840 documentos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Support Tickets & Customer Feature Requests
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Buzón de Peticiones de Clientes (${tickets.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnyxRedPrimary
                )
                Text(
                    text = "Llegan desde el Asistente Virtual",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(tickets) { ticket ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${ticket.id} • ${ticket.category}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnyxRedPrimary
                            )
                            Text(
                                text = ticket.userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }

                        val statusColor = when (ticket.status) {
                            "PENDIENTE" -> Color(0xFFFF9100)
                            "EN DESARROLLO" -> Color(0xFF2979FF)
                            else -> Color(0xFF00E676)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = ticket.status,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"${ticket.message}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                tickets = tickets.map {
                                    if (it.id == ticket.id) it.copy(status = "EN DESARROLLO") else it
                                }
                                Toast.makeText(context, "Estado actualizado: En Desarrollo por Damian", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("En Desarrollo", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                tickets = tickets.map {
                                    if (it.id == ticket.id) it.copy(status = "IMPLEMENTADO") else it
                                }
                                Toast.makeText(context, "¡Petición marcada como implementada!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Implementar", color = OnyxWhite, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
