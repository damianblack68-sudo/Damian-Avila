package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.EngineeringBlueprint
import com.example.ui.theme.OnyxRedPrimary

@Composable
fun BlueprintCanvas(
    blueprint: EngineeringBlueprint,
    modifier: Modifier = Modifier
) {
    var isFullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1424)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E3A5F))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = "Plano Técnico",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = blueprint.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2F1FF)
                        )
                        Text(
                            text = "${blueprint.type.scale} • Medidas en ${blueprint.type.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7BA6D6)
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, blueprint.title)
                                val textReport = buildString {
                                    appendLine("=== ONYX CAD BLUEPRINT ===")
                                    appendLine(blueprint.title)
                                    appendLine("Escala: ${blueprint.type.scale}")
                                    appendLine("Área Total: ${blueprint.totalAreaFormatted}")
                                    appendLine("Dimensiones: ${blueprint.totalWidthMeasure} x ${blueprint.totalHeightMeasure}")
                                    appendLine("Diseñador: ${blueprint.designerInfo}")
                                    appendLine("\nComponentes / Espacios:")
                                    blueprint.roomsOrComponents.forEach { r ->
                                        appendLine("• ${r.name}: ${r.widthMeasure} x ${r.heightMeasure} (${r.area} m²) - ${r.subLabel}")
                                    }
                                    appendLine("\nNotas del Ingeniero:")
                                    blueprint.engineerNotes.forEach { n -> appendLine("- $n") }
                                }
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Plano Técnico"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir Plano",
                            tint = Color(0xFF7BA6D6),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isFullscreen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Ver Pantalla Completa",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Technical Blueprint Drawing Viewport
            BlueprintDrawingCanvas(
                blueprint = blueprint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Engineer Title Block (Membrete Técnico)
            TechnicalTitleBlock(blueprint = blueprint)

            Spacer(modifier = Modifier.height(8.dp))

            // Engineering Notes
            if (blueprint.engineerNotes.isNotEmpty()) {
                Text(
                    text = "Notas Estructurales del Ingeniero:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.height(4.dp))
                blueprint.engineerNotes.take(3).forEach { note ->
                    Text(
                        text = "• $note",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = Color(0xFFB0CBE8),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF070F1C)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = blueprint.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Button(
                            onClick = { isFullscreen = false },
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                        ) {
                            Text("Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BlueprintDrawingCanvas(
                        blueprint = blueprint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    TechnicalTitleBlock(blueprint = blueprint)
                }
            }
        }
    }
}

@Composable
fun BlueprintDrawingCanvas(
    blueprint: EngineeringBlueprint,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0D1B2E), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw Technical Grid Lines (Blueprint Paper Effect)
            val gridStep = 24.dp.toPx()
            var x = 0f
            while (x <= width) {
                drawLine(
                    color = Color(0xFF142742),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y <= height) {
                drawLine(
                    color = Color(0xFF142742),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // 2. Outer Technical Border Frame
            val pad = 12f
            drawRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                topLeft = Offset(pad, pad),
                size = Size(width - 2 * pad, height - 2 * pad),
                style = Stroke(width = 2f)
            )

            // 3. Draw Rooms / Components
            blueprint.roomsOrComponents.forEach { room ->
                val rx = room.xRatio * width
                val ry = room.yRatio * height
                val rw = room.widthRatio * width
                val rh = room.heightRatio * height

                // Room fill
                drawRect(
                    color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                    topLeft = Offset(rx, ry),
                    size = Size(rw, rh)
                )

                // Structural thick walls
                drawRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(rx, ry),
                    size = Size(rw, rh),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Door swing arc simulation
                val doorRadius = 14.dp.toPx()
                drawArc(
                    color = Color(0xFFFF2A54),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(rx + rw - doorRadius * 2, ry),
                    size = Size(doorRadius * 2, doorRadius * 2),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 4. Draw Cotas (Dimension Witness Lines & Arrows)
            blueprint.cotas.forEach { cota ->
                val sx = cota.startXRatio * width
                val sy = cota.startYRatio * height
                val ex = cota.endXRatio * width
                val ey = cota.endYRatio * height

                // Dimension line
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 1.8f
                )

                // End ticks / witness marks
                val tick = 6.dp.toPx()
                if (cota.isHorizontal) {
                    drawLine(Color(0xFFFFD54F), Offset(sx, sy - tick), Offset(sx, sy + tick), strokeWidth = 2f)
                    drawLine(Color(0xFFFFD54F), Offset(ex, ey - tick), Offset(ex, ey + tick), strokeWidth = 2f)
                } else {
                    drawLine(Color(0xFFFFD54F), Offset(sx - tick, sy), Offset(sx + tick, sy), strokeWidth = 2f)
                    drawLine(Color(0xFFFFD54F), Offset(ex - tick, ey), Offset(ex + tick, ey), strokeWidth = 2f)
                }
            }
        }

        // Room Labels & Dimensions Overlay
        blueprint.roomsOrComponents.forEach { room ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (room.xRatio * 320).dp,
                        top = (room.yRatio * 220).dp
                    )
            ) {
                Column(
                    modifier = Modifier.padding(6.dp)
                ) {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${room.widthMeasure} x ${room.heightMeasure} m (${room.area} m²)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFD54F),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TechnicalTitleBlock(blueprint: EngineeringBlueprint) {
    Surface(
        color = Color(0xFF0F1F35),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF224470))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MEMBRETE TÉCNICO CAD • ONYX CAD v4.0",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = blueprint.designerInfo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = blueprint.totalAreaFormatted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "FECHA: ${blueprint.dateString} | ${blueprint.type.scale}",
                    fontSize = 9.sp,
                    color = Color(0xFF8AAAC8)
                )
            }
        }
    }
}
