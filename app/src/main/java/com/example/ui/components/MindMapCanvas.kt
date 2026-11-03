package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MindMapNode
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

@Composable
fun MindMapView(
    rootNode: MindMapNode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        // Central Node Pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(OnyxRedDeep(), OnyxRedVibrant)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = rootNode.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnyxWhite
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas for branching connection lines
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            val rootCenter = Offset(size.width / 2f, 0f)
            val subCount = rootNode.subnodes.size.coerceAtLeast(1)

            rootNode.subnodes.forEachIndexed { index, _ ->
                val branchX = (size.width / (subCount + 1)) * (index + 1)
                val target = Offset(branchX, size.height)

                val path = Path().apply {
                    moveTo(rootCenter.x, rootCenter.y)
                    cubicTo(
                        rootCenter.x, size.height / 2f,
                        branchX, size.height / 2f,
                        branchX, size.height
                    )
                }

                drawPath(
                    path = path,
                    color = OnyxRedPrimary.copy(alpha = 0.6f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Subnodes Horizontal Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            rootNode.subnodes.forEach { sub ->
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = sub.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                maxLines = 2
                            )

                            if (sub.subnodes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                sub.subnodes.take(3).forEach { leaf ->
                                    Text(
                                        text = "• ${leaf.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
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

@Composable
fun FlowchartView(
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        steps.forEachIndexed { index, step ->
            val isStartOrEnd = index == 0 || index == steps.lastIndex
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isStartOrEnd) OnyxRedPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(if (isStartOrEnd) 20.dp else 8.dp),
                modifier = Modifier.fillMaxWidth(0.88f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}. $step",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isStartOrEnd) FontWeight.Bold else FontWeight.Medium,
                        color = if (isStartOrEnd) OnyxRedPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            }

            if (index < steps.lastIndex) {
                Canvas(
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                ) {
                    drawLine(
                        color = OnyxRedPrimary,
                        start = Offset(size.width / 2f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Arrowhead
                    val arrowY = size.height
                    val path = Path().apply {
                        moveTo(size.width / 2f - 4.dp.toPx(), arrowY - 4.dp.toPx())
                        lineTo(size.width / 2f, arrowY)
                        lineTo(size.width / 2f + 4.dp.toPx(), arrowY - 4.dp.toPx())
                    }
                    drawPath(path = path, color = OnyxRedPrimary, style = Stroke(width = 2.dp.toPx()))
                }
            }
        }
    }
}

private fun OnyxRedDeep(): Color = Color(0xFFD6183C)
