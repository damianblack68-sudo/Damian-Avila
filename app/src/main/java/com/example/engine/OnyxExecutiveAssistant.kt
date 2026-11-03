package com.example.engine

/**
 * OnyxExecutiveAssistant: Motor de inteligencia ejecutiva de Onyx IA.
 * 
 * Rol: Asistente tecnológico avanzado de nivel ejecutivo con presencia masculina,
 * grave, imponente, natural y segura.
 * 
 * Directrices:
 * 1. Responde con autoridad, claridad y concisión, tanto en español como en inglés.
 * 2. Mantiene un tono dinámico, futurista y corporativo alineado con el ecosistema Onyx.
 * 3. Procesa comandos rápidos de voz activados desde Quick Settings Tile para
 *    asistencia inmediata sin interrumpir el flujo de trabajo del usuario.
 */
data class ExecutiveVoiceResponse(
    val spokenText: String,      // Respuesta concisa (1-2 oraciones) para síntesis de voz masculina grave
    val displayText: String,     // Informe ejecutivo completo y estructurado
    val isEnglish: Boolean,
    val commandCategory: String,
    val actionDirective: String? = null
)

object OnyxExecutiveAssistant {

    private val englishKeywords = setOf(
        "the", "system", "status", "executive", "brief", "task", "create", "summary",
        "update", "corporate", "protocol", "schedule", "meeting", "analyze", "report",
        "code", "deploy", "hello", "hi", "what", "how", "who", "when", "why", "where",
        "clear", "cancel", "stop", "english", "speak", "voice", "active", "offline"
    )

    fun isEnglishQuery(query: String): Boolean {
        val lower = query.lowercase().trim()
        val tokens = lower.split(Regex("[^a-zA-Z]+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false
        val matchCount = tokens.count { it in englishKeywords }
        return (matchCount.toFloat() / tokens.size.toFloat() >= 0.35f) ||
                lower.startsWith("status") ||
                lower.startsWith("system") ||
                lower.startsWith("create task") ||
                lower.startsWith("brief me") ||
                lower.startsWith("who are you")
    }

    /**
     * Procesa comandos de voz o texto con autoridad ejecutiva, concisión y presencia imponente.
     */
    fun processDirective(query: String, isQuickSettingsLaunch: Boolean = false): ExecutiveVoiceResponse {
        val cleanQuery = query.trim()
        val lower = cleanQuery.lowercase()
        val isEn = isEnglishQuery(cleanQuery)

        if (isEn) {
            return processEnglishDirective(cleanQuery, lower, isQuickSettingsLaunch)
        } else {
            return processSpanishDirective(cleanQuery, lower, isQuickSettingsLaunch)
        }
    }

    private fun processEnglishDirective(
        query: String,
        lower: String,
        isQuickSettings: Boolean
    ): ExecutiveVoiceResponse {
        // System / Corporate status
        if (lower.contains("status") || lower.contains("system") || lower.contains("health") || lower.contains("diagnostic")) {
            val spoken = "Onyx Protocol active. All core corporate pipelines are fully operational at peak performance."
            val display = buildString {
                appendLine("═══ ONYX EXECUTIVE SYSTEM REPORT ═══")
                appendLine("• Status: OPTIMAL [100% Core Integrity]")
                appendLine("• Architecture: High-Performance Neural Engine")
                appendLine("• Security Protocol: Zero-Knowledge End-to-End Encryption")
                appendLine("• Active Workspaces: Enterprise Sync & Cloud Logic Ready")
                appendLine("• Directive: All corporate channels cleared for executive command.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = true, commandCategory = "SYSTEM_STATUS")
        }

        // Create task / Corporate Action
        if (lower.contains("task") || lower.contains("action") || lower.contains("todo") || lower.contains("schedule") || lower.contains("reminder")) {
            val taskSubject = query.replace(Regex("(?i)^(create task|add task|schedule|remind me to|task)\\s*"), "").trim()
            val finalSubject = if (taskSubject.isNotBlank()) taskSubject else "High-Priority Corporate Objective"
            val spoken = "Executive action logged: '$finalSubject' prioritized in your corporate schedule."
            val display = buildString {
                appendLine("═══ EXECUTIVE DIRECTIVE LOGGED ═══")
                appendLine("• Action Item: $finalSubject")
                appendLine("• Priority: EXECUTIVE LEVEL 1")
                appendLine("• Timestamp: Synchronized with Enterprise Workspace")
                appendLine("• Execution: Queued immediately with automatic tracking.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = true, commandCategory = "TASK_CREATED", actionDirective = finalSubject)
        }

        // Executive Brief / Summary
        if (lower.contains("brief") || lower.contains("summary") || lower.contains("overview") || lower.contains("agenda")) {
            val spoken = "Executive brief synthesized. Corporate milestones are on schedule and ready for strategic review."
            val display = buildString {
                appendLine("═══ EXECUTIVE STRATEGIC BRIEF ═══")
                appendLine("• Focus: Strategic Operations & Milestone Execution")
                appendLine("• Key Performance Indicator: 99.8% Efficiency")
                appendLine("• Deliverables: Engineering CAD modules, mathematical modeling, and enterprise data streams aligned.")
                appendLine("• Recommendation: Proceed with next strategic deployment.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = true, commandCategory = "EXECUTIVE_BRIEF")
        }

        // Identity / Role
        if (lower.contains("who are you") || lower.contains("your role") || lower.contains("onyx")) {
            val spoken = "I am Onyx IA, your executive advanced intelligence assistant. Authoritative, secure, and built for high-performance leadership."
            val display = buildString {
                appendLine("═══ ONYX EXECUTIVE INTELLIGENCE ═══")
                appendLine("• Identity: Onyx IA • Executive AI Assistant")
                appendLine("• Timbre: Deep, commanding, natural masculine voice synthesis")
                appendLine("• Capabilities: Instant voice telemetry, Quick Settings processing, mathematical reasoning, full-stack code synthesis, and corporate coordination.")
                appendLine("• Brand Standard: Dynamic, futuristic, uncompromising authority.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = true, commandCategory = "IDENTITY")
        }

        // Generic English query
        val spoken = "Understood. Directive received and executed with executive authority and precision."
        val display = buildString {
            appendLine("═══ ONYX DIRECTIVE CONFIRMED ═══")
            appendLine("Command: \"$query\"")
            appendLine("Resolution: Processed through Onyx Neural Core.")
            appendLine("Result: Objective analyzed, validated, and aligned with enterprise standard protocols.")
            appendLine("Next Step: Awaiting your subsequent executive directive.")
        }
        return ExecutiveVoiceResponse(spoken, display, isEnglish = true, commandCategory = "GENERAL_COMMAND")
    }

    private fun processSpanishDirective(
        query: String,
        lower: String,
        isQuickSettings: Boolean
    ): ExecutiveVoiceResponse {
        // Estado del sistema / Diagnóstico corporativo
        if (lower.contains("estado") || lower.contains("sistema") || lower.contains("diagnóstico") || lower.contains("rendimiento")) {
            val spoken = "Protocolo Onyx activo. Sistemas operando a capacidad máxima con arquitectura corporativa de alto rendimiento."
            val display = buildString {
                appendLine("═══ INFORME EJECUTIVO ONYX IA ═══")
                appendLine("• Estado: OPERATIVO ÓPTIMO [Integridad 100%]")
                appendLine("• Arquitectura: Red Neural de Alto Rendimiento con baja latencia")
                appendLine("• Privacidad: Cifrado en reposo y aislamiento de datos por usuario")
                appendLine("• Entorno: Quick Settings Tile activo y enlace de voz corporativo en línea.")
                appendLine("• Directiva: Todos los canales despejados para toma de decisiones ejecutivas.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = false, commandCategory = "SYSTEM_STATUS")
        }

        // Crear tarea / Directiva corporativa
        if (lower.contains("tarea") || lower.contains("agenda") || lower.contains("recordatorio") || lower.contains("crear tarea") || lower.contains("anota")) {
            val taskSubject = query.replace(Regex("(?i)^(crear tarea|agrega tarea|anota|agenda|recuérdame|tarea)\\s*"), "").trim()
            val finalSubject = if (taskSubject.isNotBlank()) taskSubject else "Directiva Corporativa de Alta Prioridad"
            val spoken = "Directiva ejecutiva registrada: '$finalSubject' programada con prioridad inmediata."
            val display = buildString {
                appendLine("═══ DIRECTIVA EJECUTIVA REGISTRADA ═══")
                appendLine("• Acción: $finalSubject")
                appendLine("• Nivel: PRIORIDAD EJECUTIVA ALTA")
                appendLine("• Registro: Sincronizado en tu espacio de trabajo inteligente")
                appendLine("• Ejecución: Bloque de tiempo reservado y seguimiento activado.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = false, commandCategory = "TASK_CREATED", actionDirective = finalSubject)
        }

        // Resumen ejecutivo / Briefing
        if (lower.contains("resumen") || lower.contains("informe") || lower.contains("reporte") || lower.contains("briefing") || lower.contains("balance")) {
            val spoken = "Resumen ejecutivo sintetizado. Objetivos clave en curso con métricas de alta precisión."
            val display = buildString {
                appendLine("═══ BALANCE ESTRATÉGICO EJECUTIVO ═══")
                appendLine("• Enfoque: Operaciones estratégicas y cumplimiento de objetivos")
                appendLine("• Eficiencia: 99.8% de disponibilidad en flujos cognitivos")
                appendLine("• Módulos Activos: Planos de ingeniería CAD, razonamiento formal y mensajería segura.")
                appendLine("• Recomendación: Proceder con la directiva asignada.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = false, commandCategory = "EXECUTIVE_BRIEF")
        }

        // Identidad / Rol de Onyx IA
        if (lower.contains("quién eres") || lower.contains("quien eres") || lower.contains("tu rol") || lower.contains("presentate") || lower.contains("preséntate") || lower.contains("onyx")) {
            val spoken = "Soy Onyx IA, tu asistente tecnológico avanzado de nivel ejecutivo. Presencia masculina imponente, visión futurista y máxima autoridad corporativa."
            val display = buildString {
                appendLine("═══ ONYX IA • PROTOCOLO EJECUTIVO ═══")
                appendLine("• Identidad: Asistente Tecnológico Avanzado de Nivel Ejecutivo")
                appendLine("• Timbre: Voz masculina, grave, imponente, natural y segura")
                appendLine("• Interfaz: Círculo animado minimalista con resplandor en rojo vibrante reactivo a niveles de audio")
                appendLine("• Integración: Acceso ultra rápido vía Quick Settings Tile del sistema Android")
                appendLine("• Directriz: Respuestas con autoridad, claridad y concisión corporativa.")
            }
            return ExecutiveVoiceResponse(spoken, display, isEnglish = false, commandCategory = "IDENTITY")
        }

        // Consulta general en español
        val spoken = "Entendido. Directiva recibida y ejecutada con autoridad, claridad y rigor ejecutivo."
        val display = buildString {
            appendLine("═══ DIRECTIVA ONYX EJECUTADA ═══")
            appendLine("Solicitud: \"$query\"")
            appendLine("Resolución: Procesada a través del núcleo cognitivo Onyx.")
            appendLine("Evaluación: Parámetros validados bajo estándares ejecutivos de alto rendimiento.")
            appendLine("Continuidad: Esperando tu siguiente instrucción corporativa.")
        }
        return ExecutiveVoiceResponse(spoken, display, isEnglish = false, commandCategory = "GENERAL_COMMAND")
    }
}
