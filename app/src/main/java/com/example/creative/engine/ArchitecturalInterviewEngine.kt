package com.example.creative.engine

import com.example.creative.domain.model.ArchChatMessage
import com.example.creative.domain.model.ArchSender
import com.example.creative.domain.model.ArchitecturalBrief

/**
 * Motor de Entrevista Inteligente para Diseño Arquitectónico.
 * Analiza las respuestas del usuario, extrae dimensiones y requisitos técnicos,
 * y guía la conversación paso a paso antes de habilitar la generación de planos.
 */
object ArchitecturalInterviewEngine {

    fun getInitialGreeting(): ArchChatMessage {
        return ArchChatMessage(
            sender = ArchSender.ASSISTANT_AI,
            text = "Bienvenido a Onyx Creative Studio • Asistente de Arquitectura.\n\n" +
                    "Antes de dibujar o calcular planos, necesito comprender a fondo tu proyecto para garantizar una propuesta funcional y viable.\n\n" +
                    "Para comenzar: ¿Qué tipo de edificación deseas diseñar?",
            quickOptions = listOf(
                "Casa Familiar (1 o 2 Plantas)",
                "Dúplex Residencial",
                "Cabaña Moderna",
                "Local / Oficina Comercial"
            )
        )
    }

    fun processUserResponse(
        userText: String,
        currentBrief: ArchitecturalBrief
    ): Pair<ArchitecturalBrief, ArchChatMessage> {
        val lower = userText.lowercase().trim()
        var updated = currentBrief

        // 1. Extraer Tipo de Proyecto
        if (updated.projectType == null) {
            when {
                lower.contains("casa") -> updated = updated.copy(projectType = "Casa Residencial")
                lower.contains("duplex") || lower.contains("dúplex") -> updated = updated.copy(projectType = "Dúplex Residencial")
                lower.contains("cabaña") -> updated = updated.copy(projectType = "Cabaña Moderna")
                lower.contains("local") || lower.contains("comercial") || lower.contains("oficina") -> updated = updated.copy(projectType = "Espacio Comercial")
                else -> updated = updated.copy(projectType = userText.take(40))
            }
        }

        // 2. Extraer Dimensiones del Terreno
        if (updated.lotWidthMeters == null || updated.lotLengthMeters == null) {
            val dimensionsRegex = Regex("""(\d+(?:[.,]\d+)?)\s*(?:m|metros)?\s*(?:x|por|\*)\s*(\d+(?:[.,]\d+)?)\s*(?:m|metros)?""")
            val match = dimensionsRegex.find(lower)
            if (match != null) {
                val w = match.groupValues[1].replace(",", ".").toFloatOrNull()
                val l = match.groupValues[2].replace(",", ".").toFloatOrNull()
                if (w != null && l != null && w > 0 && l > 0) {
                    updated = updated.copy(lotWidthMeters = minOf(w, l), lotLengthMeters = maxOf(w, l))
                }
            } else if (lower.contains("10x20") || lower.contains("10 x 20")) {
                updated = updated.copy(lotWidthMeters = 10f, lotLengthMeters = 20f)
            } else if (lower.contains("8x15") || lower.contains("8 x 15")) {
                updated = updated.copy(lotWidthMeters = 8f, lotLengthMeters = 15f)
            } else if (lower.contains("12x25") || lower.contains("12 x 25")) {
                updated = updated.copy(lotWidthMeters = 12f, lotLengthMeters = 25f)
            }
        }

        // 3. Extraer Plantas / Pisos
        if (updated.totalFloors == null) {
            when {
                lower.contains("1 planta") || lower.contains("un piso") || lower.contains("una planta") || lower.contains("1 piso") -> updated = updated.copy(totalFloors = 1)
                lower.contains("2 plantas") || lower.contains("dos pisos") || lower.contains("dos plantas") || lower.contains("2 pisos") -> updated = updated.copy(totalFloors = 2)
                lower.contains("3 plantas") || lower.contains("tres pisos") || lower.contains("3 pisos") -> updated = updated.copy(totalFloors = 3)
            }
        }

        // 4. Extraer Habitaciones
        if (updated.bedroomsCount == null) {
            val bedsRegex = Regex("""(\d+)\s*(?:hab|habitacion|dormitorio|cuarto)""")
            val match = bedsRegex.find(lower)
            if (match != null) {
                val count = match.groupValues[1].toIntOrNull()
                if (count != null) updated = updated.copy(bedroomsCount = count)
            } else when {
                lower.contains("2 habitaciones") || lower.contains("2 dormitorios") -> updated = updated.copy(bedroomsCount = 2)
                lower.contains("3 habitaciones") || lower.contains("3 dormitorios") -> updated = updated.copy(bedroomsCount = 3)
                lower.contains("4 habitaciones") || lower.contains("4 dormitorios") -> updated = updated.copy(bedroomsCount = 4)
                lower.contains("1 habitacion") || lower.contains("1 dormitorio") -> updated = updated.copy(bedroomsCount = 1)
            }
        }

        // 5. Extraer Baños
        if (updated.bathroomsCount == null) {
            val bathsRegex = Regex("""(\d+)\s*(?:baño|servicio)""")
            val match = bathsRegex.find(lower)
            if (match != null) {
                val count = match.groupValues[1].toIntOrNull()
                if (count != null) updated = updated.copy(bathroomsCount = count)
            } else when {
                lower.contains("1 baño") -> updated = updated.copy(bathroomsCount = 1)
                lower.contains("2 baños") -> updated = updated.copy(bathroomsCount = 2)
                lower.contains("3 baños") -> updated = updated.copy(bathroomsCount = 3)
            }
        }

        // 6. Extraer Estilo Arquitectónico
        if (updated.architecturalStyle == null) {
            when {
                lower.contains("minimalista") -> updated = updated.copy(architecturalStyle = "Minimalista Moderno")
                lower.contains("industrial") -> updated = updated.copy(architecturalStyle = "Industrial / Loft")
                lower.contains("rústico") || lower.contains("rustico") -> updated = updated.copy(architecturalStyle = "Rústico Contemporáneo")
                lower.contains("contemporáneo") || lower.contains("contemporaneo") || lower.contains("moderno") -> updated = updated.copy(architecturalStyle = "Contemporáneo de Lujo")
            }
        }

        // Determinar siguiente pregunta o confirmación
        val nextMessage = formulateNextPrompt(updated)
        return Pair(updated, nextMessage)
    }

    private fun formulateNextPrompt(brief: ArchitecturalBrief): ArchChatMessage {
        // ¿Falta dimensiones del terreno?
        if (brief.lotWidthMeters == null || brief.lotLengthMeters == null) {
            return ArchChatMessage(
                sender = ArchSender.ASSISTANT_AI,
                text = "Entendido: ${brief.projectType ?: "Proyecto residencial"}.\n\n" +
                        "¿Cuáles son las dimensiones de tu terreno? Especifica frente y fondo en metros (ejemplo: 10m de frente por 20m de fondo = 200 m²).\n\n" +
                        "Nota técnica: No inventaremos medidas arbitrarias sin tu confirmación.",
                quickOptions = listOf(
                    "Terreno Estándar 10 × 20 m (200 m²)",
                    "Lote Compacto 8 × 15 m (120 m²)",
                    "Lote Amplio 12 × 25 m (300 m²)"
                )
            )
        }

        // ¿Falta plantas?
        if (brief.totalFloors == null) {
            val area = brief.totalLotAreaM2 ?: 200f
            return ArchChatMessage(
                sender = ArchSender.ASSISTANT_AI,
                text = "Terreno registrado: ${brief.lotWidthMeters} m × ${brief.lotLengthMeters} m (${area.toInt()} m²).\n\n" +
                        "¿Cuántas plantas o niveles deseas construir?",
                quickOptions = listOf(
                    "1 Planta (Distribución en Planta Baja)",
                    "2 Plantas (Zona Social abajo, Privada arriba)",
                    "3 Plantas (Con Terraza / Roof Garden)"
                )
            )
        }

        // ¿Falta habitaciones?
        if (brief.bedroomsCount == null) {
            return ArchChatMessage(
                sender = ArchSender.ASSISTANT_AI,
                text = "Perfecto: edificación de ${brief.totalFloors} nivel(es).\n\n" +
                        "¿Cuántas habitaciones o dormitorios requiere tu familia o proyecto?",
                quickOptions = listOf(
                    "2 Habitaciones",
                    "3 Habitaciones (1 Principal + 2 Secundarias)",
                    "4 Habitaciones + Estudio"
                )
            )
        }

        // ¿Falta baños?
        if (brief.bathroomsCount == null) {
            return ArchChatMessage(
                sender = ArchSender.ASSISTANT_AI,
                text = "Registrado: ${brief.bedroomsCount} habitaciones.\n\n" +
                        "¿Cuántos baños deseas incorporar (incluyendo medios baños de visita o en suite)?",
                quickOptions = listOf(
                    "1 Baño Completo",
                    "2 Baños (1 en Suite + 1 Compartido)",
                    "3 Baños (2 Completos + 1 Toilette Social)"
                )
            )
        }

        // ¿Falta estilo?
        if (brief.architecturalStyle == null) {
            return ArchChatMessage(
                sender = ArchSender.ASSISTANT_AI,
                text = "Excelente distribución base.\n\n" +
                        "Por último: ¿Qué estilo arquitectónico y de fachada prefieres para este diseño?",
                quickOptions = listOf(
                    "Minimalista Moderno (Líneas rectas, ventanales)",
                    "Industrial Contemporáneo (Hormigón, acero y madera)",
                    "Rústico Moderno (Piedra, maderas cálidas)",
                    "Contemporáneo de Lujo (Voladizos, iluminación cálida)"
                )
            )
        }

        // Requisitos completos: habilitar botones y resumir
        val area = brief.totalLotAreaM2 ?: 200f
        return ArchChatMessage(
            sender = ArchSender.ASSISTANT_AI,
            text = "¡Excelente! Hemos recopilado todos los requisitos necesarios para tu diseño:\n\n" +
                    "• Proyecto: ${brief.projectType}\n" +
                    "• Terreno: ${brief.lotWidthMeters} m × ${brief.lotLengthMeters} m (${area.toInt()} m²)\n" +
                    "• Niveles: ${brief.totalFloors} planta(s)\n" +
                    "• Distribución: ${brief.bedroomsCount} habitaciones, ${brief.bathroomsCount} baños, sala, cocina y comedor con ventilación natural\n" +
                    "• Estilo: ${brief.architecturalStyle}\n\n" +
                    "✅ Se han DESBLOQUEADO los botones de generación arquitectónica en la parte inferior. Puedes generar el Plano 2D y el Render Conceptual ahora.",
            quickOptions = listOf(
                "Confirmar y Generar Plano 2D",
                "Quiero modificar el estilo",
                "Añadir cochera para 2 autos"
            )
        )
    }
}
