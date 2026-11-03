package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

@Composable
fun SoundWaveVisualizer(
    amplitudes: List<Float>,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            val barCount = amplitudes.size.coerceAtLeast(1)
            val spacing = 6.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val availableWidth = size.width.coerceAtLeast(100f) * 0.85f
            val barWidth = ((availableWidth - totalSpacing) / barCount).coerceIn(4f, 14f)
            val startX = (size.width - ((barWidth * barCount) + totalSpacing)) / 2f
            val maxHeight = size.height

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    OnyxWhite,
                    OnyxRedVibrant,
                    OnyxRedPrimary
                )
            )

            amplitudes.forEachIndexed { index, amp ->
                val targetAmp = if (isActive) amp else 0.08f
                val barHeight = (targetAmp * maxHeight).coerceIn(4.dp.toPx(), maxHeight)
                val x = startX + index * (barWidth + spacing)
                val y = (maxHeight - barHeight) / 2f

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
