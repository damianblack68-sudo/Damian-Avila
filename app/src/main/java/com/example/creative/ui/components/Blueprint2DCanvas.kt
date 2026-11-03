package com.example.creative.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creative.domain.model.ArchitecturalBrief

/**
 * Lienzo 2D interactivo con soporte de Pan & Zoom para visualización de planos arquitectónicos.
 * Dibuja muros con espesor, cotas de dimensiones, aberturas de puertas y ventanas,
 * y zonificación de habitaciones con metraje cuadrado.
 */
@Composable
fun Blueprint2DCanvas(
    brief: ArchitecturalBrief,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090A10))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.6f, 3.5f)
                    offset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Rejilla arquitectónica (Grid milimétrico tenue)
            val gridSize = 24f * scale
            val gridColor = Color(0xFF161A28)
            var x = (offset.x % gridSize)
            while (x < canvasW) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, canvasH), strokeWidth = 1f)
                x += gridSize
            }
            var y = (offset.y % gridSize)
            while (y < canvasH) {
                drawLine(gridColor, Offset(0f, y), Offset(canvasW, y), strokeWidth = 1f)
                y += gridSize
            }

            // 2. Coordenadas y límites del terreno
            val lotW = (brief.lotWidthMeters ?: 10f) * 22f * scale
            val lotH = (brief.lotLengthMeters ?: 20f) * 12f * scale
            val left = (canvasW - lotW) / 2f + offset.x
            val top = (canvasH - lotH) / 2f + offset.y

            // Límite de propiedad (Línea discontinua tenue)
            drawRect(
                color = Color(0xFF2B3045),
                topLeft = Offset(left, top),
                size = Size(lotW, lotH),
                style = Stroke(width = 2f)
            )

            // Retiro frontal y posterior (Áreas de jardín)
            val buildW = lotW * 0.88f
            val buildH = lotH * 0.72f
            val bLeft = left + (lotW - buildW) / 2f
            val bTop = top + lotH * 0.16f

            // 3. Muros Exteriores Portantes (Trazo blanco puro con resplandor)
            drawRect(
                color = Color(0xFFE2E4EE),
                topLeft = Offset(bLeft, bTop),
                size = Size(buildW, buildH),
                style = Stroke(width = 6f * scale)
            )

            // 4. Muros Interiores y Zonificación
            val midY = bTop + buildH * 0.48f
            val midX = bLeft + buildW * 0.52f

            // Muro divisorio horizontal (Zona Social vs Zona Íntima)
            drawLine(
                color = Color(0xFFC0C4D6),
                start = Offset(bLeft, midY),
                end = Offset(bLeft + buildW, midY),
                strokeWidth = 4f * scale
            )

            // Muro divisorio vertical inferior (Dormitorios)
            drawLine(
                color = Color(0xFFC0C4D6),
                start = Offset(midX, midY),
                end = Offset(midX, bTop + buildH),
                strokeWidth = 4f * scale
            )

            // Muro baño
            val bathW = buildW * 0.28f
            val bathH = buildH * 0.26f
            val bathLeft = bLeft + buildW - bathW
            drawRect(
                color = Color(0xFFC0C4D6),
                topLeft = Offset(bathLeft, midY),
                size = Size(bathW, bathH),
                style = Stroke(width = 3.5f * scale)
            )

            // 5. Puertas con Arco de Apertura (Estándar Arquitectónico)
            // Puerta principal
            val doorSize = 22f * scale
            val mainDoorX = bLeft + buildW * 0.25f
            drawLine(
                color = Color(0xFFFF2A54),
                start = Offset(mainDoorX, bTop),
                end = Offset(mainDoorX, bTop - doorSize),
                strokeWidth = 2.5f
            )
            drawArc(
                color = Color(0xFFFF2A54).copy(alpha = 0.5f),
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(mainDoorX - doorSize, bTop - doorSize),
                size = Size(doorSize * 2, doorSize * 2),
                style = Stroke(width = 1.5f)
            )

            // Ventanas (Doble trazo cian/azul)
            val winColor = Color(0xFF00E5FF)
            // Ventana Sala
            drawLine(winColor, Offset(bLeft + 15f * scale, bTop), Offset(bLeft + 65f * scale, bTop), strokeWidth = 3f)
            // Ventana Dormitorio 1
            drawLine(winColor, Offset(bLeft + 15f * scale, bTop + buildH), Offset(bLeft + 65f * scale, bTop + buildH), strokeWidth = 3f)
            // Ventana Dormitorio 2
            drawLine(winColor, Offset(midX + 20f * scale, bTop + buildH), Offset(midX + 70f * scale, bTop + buildH), strokeWidth = 3f)

            // 6. Rótulos y Cotas con Canvas Nativo de Android
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 10f * scale
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                // Rótulo Sala / Comedor
                drawText("SALA / COMEDOR", bLeft + buildW * 0.28f, bTop + buildH * 0.24f, paint)
                val smallPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#9E9EA7")
                    textSize = 8f * scale
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("Sup: 24.5 m²", bLeft + buildW * 0.28f, bTop + buildH * 0.24f + 14f * scale, smallPaint)

                // Rótulo Cocina
                drawText("COCINA", bLeft + buildW * 0.76f, bTop + buildH * 0.24f, paint)
                drawText("Sup: 12.0 m²", bLeft + buildW * 0.76f, bTop + buildH * 0.24f + 14f * scale, smallPaint)

                // Rótulo Dormitorio Principal
                drawText("DORMITORIO 1", bLeft + buildW * 0.26f, midY + buildH * 0.24f, paint)
                drawText("Sup: 16.0 m²", bLeft + buildW * 0.26f, midY + buildH * 0.24f + 14f * scale, smallPaint)

                // Rótulo Dormitorio Secundario
                drawText("DORMITORIO 2", midX + buildW * 0.22f, midY + buildH * 0.35f, paint)
                drawText("Sup: 12.5 m²", midX + buildW * 0.22f, midY + buildH * 0.35f + 14f * scale, smallPaint)

                // Rótulo Baño
                drawText("BAÑO", bathLeft + bathW * 0.5f, midY + bathH * 0.55f, paint)

                // Cotas de Dimensión
                val cotaPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#FF2A54")
                    textSize = 9f * scale
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                drawText("FRENTE: ${brief.lotWidthMeters ?: 10f} m", left + lotW / 2f, top - 8f, cotaPaint)
                drawText("FONDO: ${brief.lotLengthMeters ?: 20f} m", left - 24f, top + lotH / 2f, cotaPaint)
            }
        }

        // Overlay con controles táctiles e información de escala
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xD90E111A),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plano 2D Escala: ${(scale * 100).toInt()}% • Pellizca para zoom",
                    fontSize = 9.sp,
                    color = Color(0xFFC0C4D6),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
