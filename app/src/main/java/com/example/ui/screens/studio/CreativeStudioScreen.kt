package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.StoryboardScene
import com.example.domain.model.StudyTopic
import com.example.domain.model.VideoScript
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

@Composable
fun CreativeStudioScreen(
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptTopic by remember { mutableStateOf("Inteligencia Artificial y Cálculo Vectorial") }
    var generatedVideoScript by remember { mutableStateOf<VideoScript?>(null) }

    val studyRecommendations = remember {
        listOf(
            StudyTopic("Esencia del Cálculo: Derivadas y Límites Explicados", "YouTube / 3Blue1Brown", "18 min", "Cálculo"),
            StudyTopic("Álgebra Lineal Visual: Transformaciones y Vectores", "YouTube / Khan Academy", "24 min", "Álgebra"),
            StudyTopic("Redes Neuronales y Retropropagación Matemática", "MIT OpenCourseWare", "32 min", "IA"),
            StudyTopic("Optimización Convexa y Multiplicadores de Lagrange", "Stanford Online", "40 min", "Matemáticas")
        )
    }

    LaunchedEffect(promptTopic) {
        generatedVideoScript = VideoScript(
            title = "Video-Guion: Fundamentos de $promptTopic",
            targetTopic = promptTopic,
            totalDurationSeconds = 90,
            scenes = listOf(
                StoryboardScene(
                    sceneNumber = 1,
                    title = "Introducción & Problema Inicial",
                    narration = "Bienvenidos a Onyx Open. Hoy desglosamos cómo el cálculo vectorial potencia los algoritmos modernos.",
                    visualPrompt = "Animación 3D de un hiperplano con vectores en rojo y blanco orbitando un plano cartesiano.",
                    durationSeconds = 15
                ),
                StoryboardScene(
                    sceneNumber = 2,
                    title = "Desarrollo Teórico y Gradientes",
                    narration = "El vector gradiente apunta en la dirección de máximo crecimiento de la función escalar f(x, y).",
                    visualPrompt = "Trazado dinámico de líneas de nivel y curvas de contorno con flechas rojas brillantes.",
                    durationSeconds = 30
                ),
                StoryboardScene(
                    sceneNumber = 3,
                    title = "Aplicación Computacional",
                    narration = "En cada iteración, el algoritmo de descenso del gradiente actualiza los pesos w = w - η∇L.",
                    visualPrompt = "Visualización de superficie de pérdida convexa con un punto deslizándose hacia el mínimo global.",
                    durationSeconds = 30
                ),
                StoryboardScene(
                    sceneNumber = 4,
                    title = "Síntesis y Próximos Pasos",
                    narration = "Revisa las notas adjuntas en tu espacio Notion de Onyx para resolver los ejercicios propuestos.",
                    visualPrompt = "Esfera de Onyx Open con anillos orbitales y resumen de fórmulas clave.",
                    durationSeconds = 15
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
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = OnyxRedPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estudio Creativo & Video-Guiones IA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genera guiones cinemáticos para video, conceptos visuales y recomendaciones personalizadas de estudio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Topic selector
        item {
            OutlinedTextField(
                value = promptTopic,
                onValueChange = { promptTopic = it },
                label = { Text("Tema de estudio o concepto a sintetizar") },
                trailingIcon = {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = OnyxRedPrimary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Storyboard & Video Script
        generatedVideoScript?.let { script ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = script.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnyxRedPrimary
                        )
                        Text(
                            text = "Duración estimada: ${script.totalDurationSeconds}s • ${script.scenes.size} Escenas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            val fullNarration = script.scenes.joinToString(". ") { it.narration }
                            onSpeak(fullNarration)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reproducir Guion", color = OnyxWhite, fontSize = 11.sp)
                    }
                }
            }

            items(script.scenes) { scene ->
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
                            Text(
                                text = "Escena ${scene.sceneNumber}: ${scene.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = OnyxRedPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${scene.durationSeconds}s",
                                    color = OnyxRedPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "🎙️ Locución: \"${scene.narration}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = OnyxRedPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Visual Prompt: ${scene.visualPrompt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recommended Videos Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Videos Recomendados para tu Base de Conocimiento",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnyxRedPrimary
            )
        }

        items(studyRecommendations) { topic ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${topic.platform} • ${topic.duration}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = OnyxRedPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = topic.tag,
                            color = OnyxRedPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
