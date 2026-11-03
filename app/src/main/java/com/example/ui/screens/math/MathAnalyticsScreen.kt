package com.example.ui.screens.math

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MathSolution
import com.example.engine.MathSolverEngine
import com.example.ui.components.InteractiveChart
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite

@Composable
fun MathAnalyticsScreen(
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var formulaInput by remember { mutableStateOf("2x² - 8x + 6 = 0") }
    var currentSolution by remember { mutableStateOf<MathSolution?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        currentSolution = MathSolverEngine.solve(formulaInput)
    }

    val mathKeypadSymbols = listOf("x²", "x", "+", "-", "*", "/", "=", "√", "∫", "d/dx", "π", "Δ", "lim", "Σ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = OnyxRedPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Motor Matemático & Análisis de Datos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Resolución rigurosa paso a paso, análisis simbólico y trazado de curvas en tiempo real.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Preset Chips
            item {
                val presets = listOf(
                    "2x² - 8x + 6 = 0" to "Cuadrática",
                    "d/dx [3x³ - 5x² + 7x]" to "Derivada",
                    "∫ (4x³ + 6x² - 2x) dx" to "Integral",
                    "Media de 15, 22, 28, 35, 42" to "Estadística"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { (query, label) ->
                        FilterChip(
                            selected = formulaInput == query,
                            onClick = {
                                formulaInput = query
                                currentSolution = MathSolverEngine.solve(query)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OnyxRedPrimary,
                                selectedLabelColor = OnyxWhite
                            )
                        )
                    }
                }
            }

            // Formula Editor Input
            item {
                OutlinedTextField(
                    value = formulaInput,
                    onValueChange = { formulaInput = it },
                    label = { Text("Expresión matemática o conjunto de datos") },
                    trailingIcon = {
                        IconButton(onClick = {
                            currentSolution = MathSolverEngine.solve(formulaInput)
                        }) {
                            Icon(Icons.Default.Calculate, contentDescription = "Resolver", tint = OnyxRedPrimary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnyxRedPrimary,
                        focusedLabelColor = OnyxRedPrimary
                    )
                )
            }

            // Customizable Math Keypad
            item {
                Column {
                    Text(
                        text = "Teclado Matemático Rápido",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnyxRedPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        mathKeypadSymbols.forEach { sym ->
                            FilledTonalButton(
                                onClick = { formulaInput += sym },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(sym, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Solution View
            currentSolution?.let { sol ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sol.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnyxRedPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sol.problemTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = {
                                    onSpeak("Resultado: ${sol.finalAnswer}. Desglose: ${sol.steps.firstOrNull() ?: ""}")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Escuchar explicación",
                                        tint = OnyxRedPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Steps breakdown
                            Text(
                                text = "Deducción Paso a Paso:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            sol.steps.forEach { step ->
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Final verified result card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OnyxRedPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Resultado Final Verificado:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnyxRedPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sol.finalAnswer,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Chart View if available
                sol.chartData?.let { chart ->
                    item {
                        InteractiveChart(chartData = chart)
                    }
                }

                // Export and Share buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val reportText = buildString {
                                    appendLine("# Informe Matemático Onyx Open")
                                    appendLine("Categoría: ${sol.category}")
                                    appendLine("Problema: ${sol.problemTitle}")
                                    appendLine()
                                    appendLine("## Pasos de Resolución:")
                                    sol.steps.forEach { appendLine("- $it") }
                                    appendLine()
                                    appendLine("## Respuesta Final:")
                                    appendLine(sol.finalAnswer)
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Exportar Informe Matemático")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar Informe", color = OnyxWhite)
                        }
                    }
                }
            }
        }
    }
}
