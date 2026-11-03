package com.example.creative.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creative.domain.model.ArchitecturalBrief
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visualizador 3D Interactivo para Android.
 * Proyecta volumétricamente la edificación arquitectónica en perspectiva 3D,
 * permitiendo al usuario rotar 360°, cambiar elevación e inspeccionar volúmenes interiores.
 */
@Composable
fun Architectural3DCanvas(
    brief: ArchitecturalBrief,
    modifier: Modifier = Modifier
) {
    var rotX by remember { mutableFloatStateOf(28f) } // Elevación / Pitch
    var rotY by remember { mutableFloatStateOf(45f) } // Giro / Yaw

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF07080C))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rotY = (rotY + dragAmount.x * 0.5f) % 360f
                    rotX = (rotX - dragAmount.y * 0.4f).coerceIn(10f, 85f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + 30f

            val radX = Math.toRadians(rotX.toDouble())
            val radY = Math.toRadians(rotY.toDouble())

            val cosX = cos(radX).toFloat()
            val sinX = sin(radX).toFloat()
            val cosY = cos(radY).toFloat()
            val sinY = sin(radY).toFloat()

            // Función de proyección 3D a pantalla 2D (Isométrica / Perspectiva suave)
            fun project(x: Float, y: Float, z: Float): Offset {
                // Rotación en Y
                val x1 = x * cosY - z * sinY
                val z1 = x * sinY + z * cosY

                // Rotación en X
                val y2 = y * cosX - z1 * sinX
                val z2 = y * sinX + z1 * cosX

                // Perspectiva débil
                val distance = 650f
                val scale = distance / (distance + z2)
                return Offset(cx + x1 * scale, cy - y2 * scale)
            }

            // 1. Terreno / Plano Base
            val groundSize = 180f
            val gP1 = project(-groundSize, 0f, -groundSize)
            val gP2 = project(groundSize, 0f, -groundSize)
            val gP3 = project(groundSize, 0f, groundSize)
            val gP4 = project(-groundSize, 0f, groundSize)

            val groundPath = Path().apply {
                moveTo(gP1.x, gP1.y)
                lineTo(gP2.x, gP2.y)
                lineTo(gP3.x, gP3.y)
                lineTo(gP4.x, gP4.y)
                close()
            }
            drawPath(groundPath, color = Color(0xFF10131E))
            drawPath(groundPath, color = Color(0xFF262D45), style = Stroke(width = 1.5f))

            // 2. Parámetros de la Casa 3D
            val floors = brief.totalFloors ?: 1
            val wallHeight = 55f * floors
            val w = 120f
            val l = 140f

            // Vértices inferiores
            val b1 = project(-w / 2f, 0f, -l / 2f)
            val b2 = project(w / 2f, 0f, -l / 2f)
            val b3 = project(w / 2f, 0f, l / 2f)
            val b4 = project(-w / 2f, 0f, l / 2f)

            // Vértices superiores
            val t1 = project(-w / 2f, wallHeight, -l / 2f)
            val t2 = project(w / 2f, wallHeight, -l / 2f)
            val t3 = project(w / 2f, wallHeight, l / 2f)
            val t4 = project(-w / 2f, wallHeight, l / 2f)

            // 3. Volumetría y Fachadas con Sombrado
            val frontWall = Path().apply {
                moveTo(b3.x, b3.y)
                lineTo(b4.x, b4.y)
                lineTo(t4.x, t4.y)
                lineTo(t3.x, t3.y)
                close()
            }
            drawPath(frontWall, color = Color(0xFF1E2235).copy(alpha = 0.85f))
            drawPath(frontWall, color = Color(0xFFFF2A54), style = Stroke(width = 2.5f))

            val sideWall = Path().apply {
                moveTo(b2.x, b2.y)
                lineTo(b3.x, b3.y)
                lineTo(t3.x, t3.y)
                lineTo(t2.x, t2.y)
                close()
            }
            drawPath(sideWall, color = Color(0xFF161826).copy(alpha = 0.85f))
            drawPath(sideWall, color = Color(0xFF9E9EA7), style = Stroke(width = 2f))

            // 4. Muros Interiores Transparentes y Líneas de Estructura
            val roof = Path().apply {
                moveTo(t1.x, t1.y)
                lineTo(t2.x, t2.y)
                lineTo(t3.x, t3.y)
                lineTo(t4.x, t4.y)
                close()
            }
            drawPath(roof, color = Color(0xFF2C3147).copy(alpha = 0.7f))
            drawPath(roof, color = Color(0xFF00E5FF), style = Stroke(width = 2f))

            // Si tiene 2 plantas, dibujar losa intermedia
            if (floors >= 2) {
                val midHeight = wallHeight / floors
                val m1 = project(-w / 2f, midHeight, -l / 2f)
                val m2 = project(w / 2f, midHeight, -l / 2f)
                val m3 = project(w / 2f, midHeight, l / 2f)
                val m4 = project(-w / 2f, midHeight, l / 2f)

                val midSlab = Path().apply {
                    moveTo(m1.x, m1.y)
                    lineTo(m2.x, m2.y)
                    lineTo(m3.x, m3.y)
                    lineTo(m4.x, m4.y)
                    close()
                }
                drawPath(midSlab, color = Color(0xFFFF0D3E).copy(alpha = 0.3f))
                drawPath(midSlab, color = Color(0xFFFF2A54), style = Stroke(width = 1.8f))
            }

            // Ventanales frontales en 3D
            val winB1 = project(-w / 4f, 15f, l / 2f + 0.5f)
            val winB2 = project(w / 4f, 15f, l / 2f + 0.5f)
            val winT2 = project(w / 4f, 40f, l / 2f + 0.5f)
            val winT1 = project(-w / 4f, 40f, l / 2f + 0.5f)

            val windowPath = Path().apply {
                moveTo(winB1.x, winB1.y)
                lineTo(winB2.x, winB2.y)
                lineTo(winT2.x, winT2.y)
                lineTo(winT1.x, winT1.y)
                close()
            }
            drawPath(windowPath, color = Color(0x6600E5FF))
            drawPath(windowPath, color = Color(0xFF00E5FF), style = Stroke(width = 2f))
        }

        // Barra de Control 3D
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xD90E111A),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color(0xFFFF2A54), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ángulo: ${rotY.toInt()}° | Elev: ${rotX.toInt()}°",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        rotX = 28f
                        rotY = 45f
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reiniciar", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
