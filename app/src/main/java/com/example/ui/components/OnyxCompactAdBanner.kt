package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

data class SponsoredAd(
    val title: String,
    val description: String,
    val cta: String,
    val cpmValue: Double = 0.05
)

@Composable
fun OnyxCompactAdBanner(
    onAdClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    val sponsoredAds = remember {
        listOf(
            SponsoredAd(
                title = "Servidores Cloud Ultra",
                description = "99.9% uptime y baja latencia para tus proyectos",
                cta = "Ver oferta"
            ),
            SponsoredAd(
                title = "DataMath Pro",
                description = "Certificaciones en cálculo y optimización con IA",
                cta = "Aprender"
            ),
            SponsoredAd(
                title = "Hosting NVMe Rápido",
                description = "Despliega tus bases de datos con 40% de descuento",
                cta = "Explorar"
            )
        )
    }

    var currentAdIndex by remember { mutableIntStateOf(0) }
    val currentAd = sponsoredAds[currentAdIndex]

    AnimatedVisibility(visible = isVisible) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable {
                    onAdClick()
                    currentAdIndex = (currentAdIndex + 1) % sponsoredAds.size
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Small "Ad" Tag
                    Surface(
                        color = OnyxRedPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "AD",
                            color = OnyxRedPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentAd.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = OnyxRedPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = currentAd.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }

                // CTA Button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = OnyxRedPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onAdClick()
                            currentAdIndex = (currentAdIndex + 1) % sponsoredAds.size
                        }
                ) {
                    Text(
                        text = currentAd.cta,
                        color = OnyxWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Dismiss X
                IconButton(
                    onClick = { isVisible = false },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar anuncio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
