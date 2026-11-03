package com.example.ui.screens.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.engine.DocumentScanEngine
import com.example.engine.DocumentType
import com.example.engine.ScannedDocumentResult
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

@Composable
fun DocumentScannerScreen(
    onSolveScannedMath: (String) -> Unit,
    onSaveScannedNote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<ScannedDocumentResult?>(null) }
    var customDocInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Scanner Viewport Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F1018))
                .border(2.dp, OnyxRedPrimary.copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = OnyxRedPrimary,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Escaneo Inteligente con Visión IA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnyxWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Apunta a ejercicios matemáticos, hojas de cálculo o notas manuscritas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            isScanning = true
                            scanResult = DocumentScanEngine.analyzeDocument(customDocInput.ifBlank { null })
                            isScanning = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escanear Ahora", color = OnyxWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom formula or text paste option
        OutlinedTextField(
            value = customDocInput,
            onValueChange = { customDocInput = it },
            label = { Text("O pega texto / ejercicio para analizar") },
            placeholder = { Text("Ej: ∫ 4x³ dx o Problema de optimización...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Result Card
        scanResult?.let { res ->
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
                        Surface(
                            color = OnyxRedPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = res.detectedType.label,
                                color = OnyxRedPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Confianza: ${(res.confidenceScore * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = res.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = res.extractedRawText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (res.mathematicalFormula != null || res.detectedType == DocumentType.MATH_PROBLEM) {
                            Button(
                                onClick = {
                                    val formula = res.mathematicalFormula ?: res.extractedRawText.lines().first()
                                    onSolveScannedMath(formula)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resolver con IA", color = OnyxWhite, fontSize = 12.sp)
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                onSaveScannedNote(res.title, res.extractedRawText)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar en Notion", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
