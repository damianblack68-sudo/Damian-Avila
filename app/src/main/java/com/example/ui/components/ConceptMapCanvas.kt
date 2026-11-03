package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ConceptMapData
import com.example.ui.theme.OnyxRedPrimary

@Composable
fun ConceptMapCanvas(
    conceptMap: ConceptMapData,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2C223B))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = "Mapa Conceptual",
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Mapa Conceptual de Estudio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF3E8FF)
                    )
                    Text(
                        text = conceptMap.centralTheme,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFC084FC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Central Concept Node
            val coreNode = conceptMap.nodes.find { it.category == "CORE" } ?: conceptMap.nodes.firstOrNull()
            if (coreNode != null) {
                Surface(
                    color = Color(0xFF4A1D96),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF9333EA))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CONCEPTO CENTRAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE9D5FF),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = coreNode.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (coreNode.detail.isNotBlank()) {
                            Text(
                                text = coreNode.detail,
                                fontSize = 11.sp,
                                color = Color(0xFFD8B4FE),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subnodes and Relational Connectors
            Text(
                text = "Relaciones y Proposiciones Estructuradas:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC084FC)
            )
            Spacer(modifier = Modifier.height(6.dp))

            conceptMap.relationships.forEach { rel ->
                val source = conceptMap.nodes.find { it.id == rel.sourceId }?.label ?: rel.sourceId
                val target = conceptMap.nodes.find { it.id == rel.targetId }?.label ?: rel.targetId

                Surface(
                    color = Color(0xFF1E1729),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF2E1065),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = source,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF3E8FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "  ── ${rel.connectorWord} ──▶  ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA855F7)
                        )

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = target,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF67E8F9),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (conceptMap.studySummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF1A1325),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Síntesis",
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = conceptMap.studySummary,
                            fontSize = 11.sp,
                            color = Color(0xFFE9D5FF),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
