package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

@Composable
fun OnyxSecureKeyboardBar(
    currentText: String,
    onInsertText: (String) -> Unit,
    onClearField: () -> Unit,
    onAdvanceNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPrivateMode by remember { mutableStateOf(true) }
    var selectedKeyboardTab by remember { mutableStateOf(0) } // 0: Avance Rápido, 1: Matemáticas, 2: Atajos Notion

    val quickPredictions = remember(currentText) {
        val lower = currentText.lowercase()
        when {
            lower.contains("x") -> listOf("x²", " + ", " - ", " = 0", "f(x) = ")
            lower.contains("resuel") || lower.contains("calc") -> listOf("paso a paso", "graficar", "derivada", "integral")
            lower.contains("notion") || lower.contains("tarea") -> listOf("Prioridad Alta", "Urgente", "Hoy", "Proyecto")
            else -> listOf("x²", "Resolver", "Explicar", "Diagrama", "Siguiente →")
        }
    }

    val mathQuickKeys = listOf("x²", "√", "∫", "d/dx", "π", "±", "Δ", "lim", "Σ", "=", "(", ")", "+", "-", "*", "/")
    val notionShortcuts = listOf("#Urgente", "#Matemáticas", "#Proyecto", "• Tarea", "1. Paso", "## Título", "✓ Listo")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Status Row: Private mode badge & keyboard mode switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isPrivateMode = !isPrivateMode }
                    .background(if (isPrivateMode) OnyxRedPrimary.copy(alpha = 0.15f) else Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isPrivateMode) Icons.Default.Shield else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (isPrivateMode) OnyxRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPrivateMode) "Teclado Privado & Seguro (Sin Registro)" else "Modo Estándar",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrivateMode) OnyxRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Keyboard category selectors
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Avance", "Math", "Atajos").forEachIndexed { index, name ->
                    val isSelected = selectedKeyboardTab == index
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OnyxRedPrimary else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedKeyboardTab = index }
                    ) {
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) OnyxWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Action Keys Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick advance button
            FilledTonalButton(
                onClick = onAdvanceNext,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = OnyxRedPrimary, contentColor = OnyxWhite)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Avanzar", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Avanzar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Clear button
            IconButton(
                onClick = onClearField,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }

            val currentItems = when (selectedKeyboardTab) {
                1 -> mathQuickKeys
                2 -> notionShortcuts
                else -> quickPredictions
            }

            currentItems.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onInsertText(item) }
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
