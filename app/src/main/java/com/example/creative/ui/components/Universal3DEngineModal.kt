package com.example.creative.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.creative.engine.OnyxUniversal3DEngine
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

@Composable
fun Universal3DEngineModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var ideaInput by remember { mutableStateOf("un zorro explorador con mochila y brújula") }
    var promptResult by remember {
        mutableStateOf(OnyxUniversal3DEngine.transformToUniversal3DPrompt(ideaInput))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F121C),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B334D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFF2A54),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ONYX UNIVERSAL 3D ENGINE",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp,
                                color = OnyxWhite
                            )
                            Text(
                                text = "Pixar x Disney x Dreamworks Hybrid Style",
                                fontSize = 10.sp,
                                color = Color(0xFF64D2FF)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = OnyxWhite)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User Idea Input
                OutlinedTextField(
                    value = ideaInput,
                    onValueChange = {
                        ideaInput = it
                        if (it.isNotBlank()) {
                            promptResult = OnyxUniversal3DEngine.transformToUniversal3DPrompt(it)
                        }
                    },
                    label = { Text("Idea del usuario (personaje, animal, objeto, producto...)") },
                    placeholder = { Text("Ej: un dragón bebé leyendo un libro mágico...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("universal_3d_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnyxRedPrimary,
                        unfocusedBorderColor = Color(0xFF2C3550)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Output Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Category Badge
                    Surface(
                        color = Color(0xFF1E2838),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Categoría Detectada: ${promptResult.detectedCategory.label.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PROMPT FINAL:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFF2A54),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFF07090F),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF21283B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${promptResult.finalPrompt}\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "NEGATIVE PROMPT SIEMPRE:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFFA726),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFF07090F),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF21283B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${promptResult.negativePrompt}\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Copy Prompt Button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val textToCopy = "PROMPT FINAL:\n\"${promptResult.finalPrompt}\"\n\nNEGATIVE PROMPT:\n\"${promptResult.negativePrompt}\""
                        val clip = ClipData.newPlainText("Onyx Universal 3D Prompt", textToCopy)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "¡Prompt 3D universal copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("copy_universal_3d_prompt_button")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Prompt Universal 3D", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
