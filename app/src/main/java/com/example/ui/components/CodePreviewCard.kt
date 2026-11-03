package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CodeProjectPayload
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.OnyxRedPrimary

@Composable
fun CodePreviewCard(
    payload: CodeProjectPayload,
    modifier: Modifier = Modifier,
    onAskChanges: ((CodeProjectPayload) -> Unit)? = null
) {
    val context = LocalContext.current
    var showWebPreview by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141C)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF263345))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Código",
                        tint = OnyxRedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = payload.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF0F4F8)
                        )
                        Text(
                            text = "${payload.filename} • ${payload.language.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8BA2B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(payload.filename, payload.code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "¡Código copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1E2838),
                        contentColor = Color(0xFFE2E8F0)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar código",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar código", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code Display Box with Monospace font and dark terminal styling
            Surface(
                color = Color(0xFF080B10),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1B2330))),
                modifier = Modifier.fillMaxWidth()
            ) {
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .horizontalScroll(scrollState)
                        .padding(12.dp)
                ) {
                    Text(
                        text = payload.code.take(1500) + if (payload.code.length > 1500) "\n\n/* ... Código completo listo para exportar ... */" else "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF64D2FF),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Web Preview, Pedir cambios, Export HTML, Connect with IDE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (payload.htmlPreviewContent != null) {
                    Button(
                        onClick = { showWebPreview = true },
                        colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("view_web_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Ver Web",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Ver Web", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Pedir cambios button
                Button(
                    onClick = { onAskChanges?.invoke(payload) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2838),
                        contentColor = Color(0xFF64D2FF)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("ask_changes_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Pedir cambios",
                        tint = Color(0xFF64D2FF),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Pedir cambios", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Export .html file / share
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/html"
                            putExtra(Intent.EXTRA_TITLE, payload.filename)
                            putExtra(Intent.EXTRA_TEXT, payload.code)
                            putExtra(Intent.EXTRA_SUBJECT, "Archivo exportado: ${payload.filename}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Exportar y Guardar ${payload.filename}"))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00E5FF))),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("export_html_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Exportar HTML",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Exportar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Connect with Programming apps
                IconButton(
                    onClick = {
                        val ideIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, payload.code)
                            putExtra(Intent.EXTRA_SUBJECT, "Código para Editor IDE: ${payload.filename}")
                        }
                        context.startActivity(Intent.createChooser(ideIntent, "Conectar con App de Programación (Spck, Acode, GitHub)"))
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E2838), RoundedCornerShape(10.dp))
                        .testTag("ide_connect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Conectar con IDE",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (payload.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = payload.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }

    if (showWebPreview && payload.htmlPreviewContent != null) {
        WebPreviewDialog(
            title = payload.title,
            htmlContent = payload.htmlPreviewContent,
            onDismiss = { showWebPreview = false },
            onAskChanges = {
                showWebPreview = false
                onAskChanges?.invoke(payload)
            }
        )
    }
}
