package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ChartData
import com.example.domain.model.ChartType
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

@Composable
fun InteractiveChart(
    chartData: ChartData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chartData.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (chartData.type == ChartType.LINE) "Curva Continua" else "Histograma",
                style = MaterialTheme.typography.labelSmall,
                color = OnyxRedPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (chartData.points.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin datos para graficar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val maxVal = chartData.points.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
            val minVal = chartData.points.minOfOrNull { it.value }?.coerceAtMost(0f) ?: 0f
            val range = (maxVal - minVal).coerceAtLeast(0.1f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height
                val paddingBottom = 24.dp.toPx()
                val graphHeight = h - paddingBottom
                val pointCount = chartData.points.size
                val stepX = if (pointCount > 1) w / (pointCount - 1) else w / 2

                // Draw subtle horizontal grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = (graphHeight / gridLines) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (chartData.type == ChartType.LINE) {
                    val path = Path()
                    val fillPath = Path()

                    chartData.points.forEachIndexed { index, point ->
                        val x = index * stepX
                        val normalized = (point.value - minVal) / range
                        val y = graphHeight - (normalized * graphHeight)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, graphHeight)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevNorm = (chartData.points[index - 1].value - minVal) / range
                            val prevY = graphHeight - (prevNorm * graphHeight)
                            val cX1 = prevX + (x - prevX) / 2
                            val cY1 = prevY
                            val cX2 = prevX + (x - prevX) / 2
                            val cY2 = y
                            path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                            fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                        }

                        if (index == pointCount - 1) {
                            fillPath.lineTo(x, graphHeight)
                            fillPath.close()
                        }
                    }

                    // Draw glowing fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                OnyxRedPrimary.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw main line
                    drawPath(
                        path = path,
                        color = OnyxRedPrimary,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw points
                    chartData.points.forEachIndexed { index, point ->
                        val x = index * stepX
                        val normalized = (point.value - minVal) / range
                        val y = graphHeight - (normalized * graphHeight)

                        drawCircle(
                            color = OnyxWhite,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = OnyxRedVibrant,
                            radius = 2.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                } else {
                    // Bar Chart
                    val barWidth = (w / pointCount) * 0.55f
                    chartData.points.forEachIndexed { index, point ->
                        val centerX = (index * (w / pointCount)) + (w / pointCount) / 2f
                        val normalized = (point.value - minVal) / range
                        val barHeight = (normalized * graphHeight).coerceAtLeast(4.dp.toPx())
                        val y = graphHeight - barHeight

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(OnyxRedVibrant, OnyxRedPrimary)
                            ),
                            topLeft = Offset(centerX - barWidth / 2f, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // X-Axis labels row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartData.points.take(5).forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
