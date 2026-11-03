package com.example.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun OnyxSplashIntro(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(2800)
        onSplashFinished()
    }

    // Logo scale and alpha transitions
    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "logoAlpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, delayMillis = 600),
        label = "textAlpha"
    )

    // Orbital infinite rotation
    val infiniteTransition = rememberInfiniteTransition(label = "orbital")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )

    // Pulsing aura radius
    val pulseAura by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAura"
    )

    // Starfield particles generated once
    val starPoints = remember {
        List(40) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080B))
            .clickable { onSplashFinished() }, // Tap to skip
        contentAlignment = Alignment.Center
    ) {
        // Starfield Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            starPoints.forEach { (nx, ny, radius) ->
                val x = nx * size.width
                val y = ny * size.height
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = radius.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Glowing central rings
            val centerOffset = Offset(size.width / 2f, size.height / 2f - 30.dp.toPx())
            val baseRadius = 90.dp.toPx() * pulseAura

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OnyxRedPrimary.copy(alpha = 0.35f),
                        OnyxRedVibrant.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = baseRadius * 1.5f
                ),
                center = centerOffset,
                radius = baseRadius * 1.5f
            )

            // Orbital Rings with glowing satellite nodes
            val orbitRadius = baseRadius * 1.25f
            drawCircle(
                color = OnyxRedPrimary.copy(alpha = 0.3f),
                radius = orbitRadius,
                center = centerOffset,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Satellite dot 1
            val rad1 = Math.toRadians(orbitRotation.toDouble())
            val sat1X = centerOffset.x + (orbitRadius * cos(rad1)).toFloat()
            val sat1Y = centerOffset.y + (orbitRadius * sin(rad1)).toFloat()
            drawCircle(color = OnyxWhite, radius = 3.5.dp.toPx(), center = Offset(sat1X, sat1Y))
            drawCircle(color = OnyxRedVibrant, radius = 5.dp.toPx(), center = Offset(sat1X, sat1Y), style = Stroke(width = 1.5.dp.toPx()))

            // Satellite dot 2
            val rad2 = Math.toRadians((orbitRotation + 180).toDouble())
            val sat2X = centerOffset.x + (orbitRadius * cos(rad2)).toFloat()
            val sat2Y = centerOffset.y + (orbitRadius * sin(rad2)).toFloat()
            drawCircle(color = OnyxRedPrimary, radius = 3.dp.toPx(), center = Offset(sat2X, sat2Y))
        }

        // Center Branding Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Planet Logo
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_onyx_logo),
                    contentDescription = "Onyx Open Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cinematic Title
            Row(
                modifier = Modifier.alpha(textAlpha),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ONYX",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = OnyxRedPrimary,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OPEN",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = OnyxWhite,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "OPEN ARTIFICIAL INTELLIGENCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.75f),
                letterSpacing = 2.5.sp,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle status pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OnyxRedPrimary.copy(alpha = 0.15f),
                modifier = Modifier.alpha(textAlpha)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OnyxRedPrimary,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iniciando Núcleo Cuántico • Sistema Seguro",
                        color = OnyxRedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Skip Button top right
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .clickable { onSplashFinished() }
        ) {
            Text(
                text = "Saltar →",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
