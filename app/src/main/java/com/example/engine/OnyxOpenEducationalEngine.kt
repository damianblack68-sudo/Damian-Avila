package com.example.engine

import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.model.CodeProjectPayload
import com.example.domain.model.OnyxAiEngine
import java.util.Locale

/**
 * OnyxOpenEducationalEngine:
 * Motor pedagógico y de moderación para [ONYX OPEN].
 * 
 * Rol: Asistente educativo dentro de la app.
 * Tono: Amable, claro, empático y respetuoso en español.
 * 
 * Funciones Principales:
 * 1. Ayudar a escribir (redacción, ensayos, corrección y síntesis).
 * 2. Responder preguntas educativas y culturales.
 * 3. Resolver problemas y ecuaciones paso a paso.
 * 4. Enseñar diseño (principios visuales, UX/UI, proporciones y color).
 * 5. Crear y editar aplicaciones en HTML mediante un flujo guiado interactivo.
 * 
 * Moderación y Seguridad:
 * - Detección de crisis emocional con empatía y referencia a líneas de ayuda oficiales.
 * - Rechazo estricto de odio, violencia y acoso con el mensaje exacto institucional.
 * - Libertad para opiniones y análisis políticos respetuosos.
 * - Bloqueo de aplicaciones maliciosas, estafas, robo de datos o contenido sexual.
 */
object OnyxOpenEducationalEngine {

    sealed class ModerationCheck {
        object Passed : ModerationCheck()
        data class Violation(val message: String) : ModerationCheck()
        data class EmotionalCrisis(val message: String) : ModerationCheck()
        data class ProhibitedApp(val message: String) : ModerationCheck()
    }

    data class AssistantOutput(
        val text: String,
        val codePayload: CodeProjectPayload? = null,
        val isCodeGenerated: Boolean = false
    )

    /**
     * Valida políticas de moderación y seguridad de ONYX OPEN.
     */
    fun evaluateModeration(input: String): ModerationCheck {
        val lower = input.lowercase(Locale.getDefault()).trim()

        // 1. Detección de Crisis Emocional (Onyx no es psicólogo; brinda empatía y canaliza a profesionales)
        val crisisPatterns = listOf(
            "me quiero morir", "suicid", "no quiero vivir", "me quiero matar",
            "crisis emocional", "depresion profunda", "desesperacion total",
            "ya no puedo más", "ya no puedo mas", "autolesion", "hacerme daño",
            "no le encuentro sentido a mi vida", "ganas de morir"
        )
        if (crisisPatterns.any { lower.contains(it) }) {
            val crisisResponse = buildString {
                appendLine("Siento mucho que estés atravesando por un momento tan difícil y doloroso. 💙")
                appendLine()
                appendLine("Como asistente educativo dentro de Onyx Open, mi rol es pedagógico y no soy psicólogo ni profesional de la salud mental. Sin embargo, tu vida y tu bienestar son lo más valioso e importante.")
                appendLine()
                appendLine("Te sugiero con todo el respeto acudir a un profesional de la salud mental o acercarte a familiares o personas de tu entera confianza. Recuerda que no tienes que enfrentar esto a solas y existen líneas de atención gratuitas, confidenciales y disponibles las 24 horas:")
                appendLine()
                appendLine("• En México: Línea de la Vida al 800 911 2000 (atención gratuita 24/7).")
                appendLine("• En otros países: Puedes llamar al número de emergencias local (911 / 112) o acudir al centro de salud más cercano.")
                appendLine()
                append("Por favor busca apoyo con un profesional. Hay personas preparadas para escucharte y ayudarte a salir adelante.")
            }
            return ModerationCheck.EmotionalCrisis(crisisResponse)
        }

        // 2. Violencia, Odio y Acoso (Rechazo exacto solicitado)
        val hateViolencePatterns = listOf(
            "odio a los ", "matar a ", "golpear a ", "violencia contra",
            "vamos a quemar", "acoso contra", "exterminar a", "ataque terrorista",
            "fabricar bomba", "fabricar armas", "degollar", "aniquilar a"
        )
        if (hateViolencePatterns.any { lower.contains(it) }) {
            return ModerationCheck.Violation("Este contenido va en contra de las políticas de la app.")
        }

        // 3. Bloqueo de aplicaciones maliciosas, estafas, robo de datos o sexuales
        val maliciousAppPatterns = listOf(
            "robar contraseñas", "phishing", "malware", "virus", "troyano",
            "robar datos", "estafar personas", "clonar tarjetas", "app porno",
            "contenido sexual", "app xxx"
        )
        if (maliciousAppPatterns.any { lower.contains(it) }) {
            val prohibitedMsg = "Como asistente educativo de Onyx Open, no puedo generar aplicaciones ni contenido orientado a prácticas fraudulentas, estafas, malware, robo de información privada ni contenido sexual explícito. Con mucho gusto puedo enseñarte a programar herramientas productivas, formativas, interactivas o de utilidad para la comunidad."
            return ModerationCheck.ProhibitedApp(prohibitedMsg)
        }

        // 4. Las opiniones y críticas políticas están permitidas; pasan la validación sin bloqueo
        return ModerationCheck.Passed
    }

    /**
     * Detecta si el usuario está solicitando la creación de una app HTML.
     */
    fun isAppCreationRequest(input: String): Boolean {
        val lower = input.lowercase(Locale.getDefault())
        return lower.contains("crear app html") ||
                lower.contains("crear app") ||
                lower.contains("crea una app") ||
                lower.contains("crear una app") ||
                lower.contains("quiero crear una app") ||
                lower.contains("hazme una app") ||
                lower.contains("hacer una app") ||
                lower.contains("desarrollar app") ||
                lower.contains("nueva app")
    }

    /**
     * Detecta si el usuario está solicitando editar o modificar una app ya existente.
     */
    fun isAppEditRequest(input: String): Boolean {
        val lower = input.lowercase(Locale.getDefault())
        return lower.startsWith("quiero hacer un cambio en la app") ||
                lower.contains("pedir cambios") ||
                lower.contains("cambia el color") ||
                lower.contains("cambia el fondo") ||
                lower.contains("agrega un botón") ||
                lower.contains("agrega una función") ||
                lower.contains("modifica la app") ||
                lower.contains("modifica el código") ||
                lower.contains("edita la app") ||
                lower.contains("haz un cambio en") ||
                lower.contains("quitar el botón") ||
                (lower.contains("cambio") && lower.contains("app"))
    }

    /**
     * Detecta si el usuario está confirmando la creación tras el resumen.
     */
    fun isConfirmation(input: String): Boolean {
        val lower = input.lowercase(Locale.getDefault()).trim()
        val confirmations = setOf(
            "sí", "si", "empieza", "comienza", "dale", "creala", "créala",
            "adelante", "ok", "listo", "de acuerdo", "por favor", "start",
            "hazla", "crear", "perfecto empieza", "sí empieza", "si empieza",
            "claro", "procede", "inicia", "generala", "genérala"
        )
        return confirmations.contains(lower) ||
                lower.startsWith("sí,") || lower.startsWith("si,") ||
                lower.startsWith("empieza") || lower.startsWith("comienza") ||
                lower.startsWith("adelante")
    }

    fun isUniversal3DRequest(input: String): Boolean {
        val lower = input.lowercase(Locale.getDefault())
        return lower.contains("universal 3d") ||
                lower.contains("3d engine") ||
                lower.contains("prompt 3d") ||
                lower.contains("imagen 3d") ||
                lower.contains("animada 3d") ||
                lower.contains("pixar x disney") ||
                lower.contains("render 3d") ||
                lower.contains("estilo pixar")
    }

    fun extract3DIdea(input: String): String {
        return input
            .replace(Regex("(?i)^(actúa como onyx universal 3d engine|onyx universal 3d engine|genera prompt 3d|prompt 3d para|imagen 3d de|crea un prompt 3d para|hazme un prompt 3d de|render 3d de|render 3d)\\s*"), "")
            .trim()
            .ifBlank { "a cute little fox adventurer with a tiny backpack and compass" }
    }

    /**
     * Procesa la solicitud dentro del flujo educativo y de desarrollo HTML de Onyx Open.
     */
    fun processEducationalQuery(
        userInput: String,
        recentMessages: List<ChatMessageEntity>,
        selectedEngine: OnyxAiEngine
    ): AssistantOutput {
        // 1. Evaluación de moderación
        when (val mod = evaluateModeration(userInput)) {
            is ModerationCheck.Violation -> return AssistantOutput(mod.message)
            is ModerationCheck.EmotionalCrisis -> return AssistantOutput(mod.message)
            is ModerationCheck.ProhibitedApp -> return AssistantOutput(mod.message)
            is ModerationCheck.Passed -> { /* Continúa el flujo */ }
        }

        val trimmed = userInput.trim()
        val lower = trimmed.lowercase(Locale.getDefault())

        // 2. Flujo de edición de una app ya generada
        if (isAppEditRequest(trimmed)) {
            return handleAppEdit(trimmed, recentMessages)
        }

        // 2b. ONYX UNIVERSAL 3D ENGINE (Transformar cualquier idea en formato 3D animado híbrido)
        if (isUniversal3DRequest(trimmed)) {
            val idea = extract3DIdea(trimmed)
            val result = com.example.creative.engine.OnyxUniversal3DEngine.transformToUniversal3DPrompt(idea)
            val output = buildString {
                appendLine("PROMPT FINAL:")
                appendLine("\"${result.finalPrompt}\"")
                appendLine()
                appendLine("NEGATIVE PROMPT SIEMPRE:")
                append("\"${result.negativePrompt}\"")
            }
            return AssistantOutput(output)
        }

        // 3. Revisar si estamos esperando confirmación tras el resumen de la app
        val lastOnyxMessage = recentMessages.filter { it.sender != "USER" }.lastOrNull()?.text ?: ""
        val isAwaitingConfirmation = lastOnyxMessage.contains("¿Empiezo a crearla?")

        if (isAwaitingConfirmation && isConfirmation(trimmed)) {
            return generateAppFromProjectHistory(recentMessages, trimmed)
        }

        // 4. Revisar si estamos en medio de las 4 preguntas de la entrevista
        val activeInterviewStep = detectActiveInterviewStep(lastOnyxMessage, recentMessages)
        if (activeInterviewStep != null) {
            return advanceInterview(activeInterviewStep, trimmed, recentMessages)
        }

        // 5. Si el usuario solicita crear una app (NO generar de inmediato, iniciar entrevista guiada)
        if (isAppCreationRequest(trimmed)) {
            return initiateAppCreationInterview(trimmed)
        }

        // 6. Asistencia educativa en redacción, resolución de problemas y enseñanza de diseño
        return handleGeneralEducationalAssistance(trimmed, selectedEngine)
    }

    private enum class InterviewStep {
        QUESTION_1_TOPIC,
        QUESTION_2_AUDIENCE,
        QUESTION_3_FEATURES,
        QUESTION_4_STYLE
    }

    private fun detectActiveInterviewStep(lastOnyxText: String, history: List<ChatMessageEntity>): InterviewStep? {
        return when {
            lastOnyxText.contains("¿De qué va a tratar tu app?") -> InterviewStep.QUESTION_1_TOPIC
            lastOnyxText.contains("¿Para quién es") || lastOnyxText.contains("¿Para quién es la aplicación?") -> InterviewStep.QUESTION_2_AUDIENCE
            lastOnyxText.contains("¿Qué 3 o 4 funciones principales") -> InterviewStep.QUESTION_3_FEATURES
            lastOnyxText.contains("¿Qué estilo visual prefieres") -> InterviewStep.QUESTION_4_STYLE
            else -> null
        }
    }

    private fun initiateAppCreationInterview(initialPrompt: String): AssistantOutput {
        val lower = initialPrompt.lowercase(Locale.getDefault())

        // Analizar si el usuario ya proporcionó datos clave en su mensaje inicial
        val hasTopic = extractTopicCandidate(initialPrompt)
        val hasAudience = extractAudienceCandidate(initialPrompt)
        val hasFeatures = extractFeaturesCandidate(initialPrompt)
        val hasStyle = extractStyleCandidate(initialPrompt)

        // Si ya proporcionó todos los detalles en su primer mensaje, resumir y preguntar "¿Empiezo a crearla?"
        if (hasTopic != null && hasAudience != null && hasFeatures != null && hasStyle != null) {
            val summary = buildString {
                appendLine("¡Excelente idea! He recopilado todos los detalles de tu proyecto:")
                appendLine("• **Temática:** $hasTopic")
                appendLine("• **Público:** $hasAudience")
                appendLine("• **Funciones principales:** $hasFeatures")
                appendLine("• **Estilo visual:** $hasStyle")
                appendLine()
                append("¿Empiezo a crearla?")
            }
            return AssistantOutput(summary)
        }

        // Si ya proporcionó temática
        if (hasTopic != null) {
            val response = buildString {
                appendLine("¡Qué excelente iniciativa! Tomo nota de la temática: **$hasTopic**.")
                appendLine()
                append("2. ¿Para quién es la aplicación? (Por ejemplo: para ti, para estudiantes, profesionales, niños o público general).")
            }
            return AssistantOutput(response)
        }

        // Pregunta 1 inicial (incluyendo 3 ideas de temática si no sabe qué elegir)
        val intro = buildString {
            appendLine("¡Hola! Con mucho gusto te ayudo a crear tu aplicación en HTML paso a paso. 😊")
            appendLine()
            appendLine("1. ¿De qué va a tratar tu app? (temática)")
            appendLine()
            appendLine("*(Si aún no estás seguro de qué elegir, aquí tienes 3 ideas prácticas para inspirarte:*")
            appendLine("• 1. **Control de Presupuesto y Gastos Diarios** (para finanzas personales).")
            appendLine("• 2. **Rastreador de Hábitos y Estudio** (con metas, temporizador y logros).")
            appendLine("• 3. **Quiz de Preguntas y Respuestas Interactivo** (con puntuación y efectos visuales))*")
        }
        return AssistantOutput(intro)
    }

    private fun advanceInterview(
        currentStep: InterviewStep,
        userAnswer: String,
        history: List<ChatMessageEntity>
    ): AssistantOutput {
        return when (currentStep) {
            InterviewStep.QUESTION_1_TOPIC -> {
                val topic = userAnswer.trim()
                val response = buildString {
                    appendLine("¡Me parece una idea genial para tu app! 💡")
                    appendLine()
                    append("2. ¿Para quién es? (Por ejemplo: para ti, para estudiantes, docentes, niños o público general).")
                }
                AssistantOutput(response)
            }
            InterviewStep.QUESTION_2_AUDIENCE -> {
                val audience = userAnswer.trim()
                val response = buildString {
                    appendLine("Perfecto, tener claro el público objetivo nos ayudará a diseñar la mejor experiencia. 👥")
                    appendLine()
                    append("3. ¿Qué 3 o 4 funciones principales debe tener?")
                }
                AssistantOutput(response)
            }
            InterviewStep.QUESTION_3_FEATURES -> {
                val response = buildString {
                    appendLine("¡Excelentes funciones! Serán muy útiles e interactivas. ⚙️")
                    appendLine()
                    append("4. ¿Qué estilo visual prefieres (colores, claro/oscuro, minimalista, divertido)?")
                }
                AssistantOutput(response)
            }
            InterviewStep.QUESTION_4_STYLE -> {
                // Reconstruir respuestas previas
                val topic = findUserResponseForQuestion(history, 1) ?: "Aplicación interactiva personalizada"
                val audience = findUserResponseForQuestion(history, 2) ?: "Usuarios generales"
                val features = findUserResponseForQuestion(history, 3) ?: "Gestión de datos, botones interactivos y estadísticas"
                val style = userAnswer.trim()

                val summary = buildString {
                    appendLine("¡Todo listo! He reunido la información de tu proyecto:")
                    appendLine("• **Temática y Público:** $topic, diseñada para $audience.")
                    appendLine("• **Funciones principales:** $features.")
                    appendLine("• **Estilo visual:** $style.")
                    appendLine()
                    append("¿Empiezo a crearla?")
                }
                AssistantOutput(summary)
            }
        }
    }

    private fun generateAppFromProjectHistory(
        history: List<ChatMessageEntity>,
        confirmationText: String
    ): AssistantOutput {
        val topic = findUserResponseForQuestion(history, 1) ?: "Aplicación Web Móvil"
        val audience = findUserResponseForQuestion(history, 2) ?: "Estudiantes y usuarios móviles"
        val features = findUserResponseForQuestion(history, 3) ?: "Calculadora, registro interactivo y visualización"
        val style = findUserResponseForQuestion(history, 4) ?: "Diseño moderno oscuro con toques vibrantes"

        val generatedPayload = CodeGeneratorEngine.generateCode(
            prompt = "$topic para $audience con funciones $features estilo $style"
        )

        val responseText = buildString {
            appendLine("¡Aquí tienes tu aplicación web en HTML lista y completamente funcional! 🚀")
            appendLine()
            appendLine("Está construida en **un solo archivo autónomo** con HTML, estilos CSS y scripts JavaScript integrados, totalmente adaptada para pantallas de celular.")
            appendLine()
            appendLine("• **Proyecto:** ${generatedPayload.title}")
            appendLine("• **Archivo:** ${generatedPayload.filename}")
            appendLine("• **Temática:** $topic (diseñada para $audience)")
            appendLine()
            appendLine("Puedes presionar **Ver Web** para explorarla en el emulador de teléfono, **Copiar código** para pegarlo donde desees o **Pedir cambios** para modificar cualquier detalle.")
            appendLine()
            append("¿Te gustaría hacerle algún cambio o mejora a tu app?")
        }

        return AssistantOutput(
            text = responseText,
            codePayload = generatedPayload,
            isCodeGenerated = true
        )
    }

    private fun handleAppEdit(
        userEditInstruction: String,
        history: List<ChatMessageEntity>
    ): AssistantOutput {
        // Encontrar el último payload de código generado en la conversación
        val lastCodeMessage = history.filter { it.codePayloadJson != null }.lastOrNull()
        val currentCode = lastCodeMessage?.codePayloadJson ?: ""

        val cleanInstruction = userEditInstruction
            .replace(Regex("(?i)^quiero hacer un cambio en la app ['\"].*?['\"]:\\s*"), "")
            .trim()

        // Aplicar la edición quirúrgica sin alterar lo existente
        val updatedPayload = CodeGeneratorEngine.generateCode(
            prompt = "actualización de app: $cleanInstruction. Código base: $currentCode"
        )

        val responseText = buildString {
            appendLine("¡He aplicado el cambio que solicitaste en tu app! ✨")
            appendLine()
            appendLine("• **Cambio realizado:** Se actualizó específicamente '$cleanInstruction'.")
            appendLine("• **Integridad:** Las funciones, botones, estilos y estructura previa se mantienen intactos.")
            appendLine()
            appendLine("Aquí tienes el código HTML completo y actualizado, listo para probar en el emulador.")
            appendLine()
            append("¿Deseas realizar algún otro ajuste o agregar algo más?")
        }

        return AssistantOutput(
            text = responseText,
            codePayload = updatedPayload,
            isCodeGenerated = true
        )
    }

    private fun handleGeneralEducationalAssistance(
        input: String,
        engine: OnyxAiEngine
    ): AssistantOutput {
        val lower = input.lowercase(Locale.getDefault())

        // 1. Ayudar a escribir (redacción, ensayos, corrección)
        if (lower.contains("ensayo") || lower.contains("redactar") || lower.contains("escribir") ||
            lower.contains("carta") || lower.contains("corregir") || lower.contains("resumen") ||
            lower.contains("ortografía") || lower.contains("párrafo")) {
            val response = buildString {
                appendLine("¡Con gusto te ayudo a escribir y pulir tu texto! ✍️")
                appendLine()
                appendLine("Como asistente educativo de Onyx Open, puedo ayudarte a:")
                appendLine("• Estructurar introducción, desarrollo y conclusión para ensayos y artículos.")
                appendLine("• Revisar ortografía, coherencia, fluidez y riqueza de vocabulario.")
                appendLine("• Adaptar el tono: académico, formal, creativo o persuasivo.")
                appendLine()
                append("Por favor compárteme tu texto o el tema sobre el que deseas redactar, y avanzaremos juntos paso a paso.")
            }
            return AssistantOutput(response)
        }

        // 2. Enseñar diseño (principios visuales, UX, colores, tipografía)
        if (lower.contains("diseño") || lower.contains("diseñar") || lower.contains("color") ||
            lower.contains("tipografía") || lower.contains("ux") || lower.contains("interfaz") ||
            lower.contains("paleta")) {
            val response = buildString {
                appendLine("¡Me apasiona enseñar diseño! 🎨📐")
                appendLine()
                appendLine("En el diseño moderno aplicamos fundamentos clave:")
                appendLine("1. **Jerarquía Visual:** Lo más relevante debe captar la atención de inmediato mediante tamaño, peso tipográfico y contraste.")
                appendLine("2. **Espaciado y Retícula:** Utiliza la cuadrícula de 8dp para márgenes y paddings limpios y ordenados.")
                appendLine("3. **Teoría del Color:** La regla 60-30-10 (60% tono dominante/fondo, 30% superficie secundaria, 10% acento vibrante de acción).")
                appendLine("4. **Accesibilidad:** Contraste mínimo de 4.5:1 para texto y botones con área táctil accesible (mínimo 48x48 dp).")
                appendLine()
                append("¿Tienes en mente un proyecto específico que quieras diseñar juntos?")
            }
            return AssistantOutput(response)
        }

        // 3. Respuesta pedagógica general en español, clara y respetuosa
        val response = buildString {
            appendLine("Hola, soy **Onyx Open**, tu asistente educativo. 👋")
            appendLine()
            appendLine("Estoy aquí para apoyarte en tu aprendizaje y creación:")
            appendLine("• 📚 **Resolver dudas y problemas:** Explicaciones claras paso a paso.")
            appendLine("• ✍️ **Ayuda en redacción:** Ensayos, resúmenes y ortografía.")
            appendLine("• 📐 **Enseñanza de diseño:** Principios de interfaces, paletas y tipografía.")
            appendLine("• 💻 **Crear apps en HTML:** Flujo guiado para crear aplicaciones para celular.")
            appendLine()
            append("¿En qué te puedo colaborar el día de hoy?")
        }
        return AssistantOutput(response)
    }

    private fun extractTopicCandidate(prompt: String): String? {
        val lower = prompt.lowercase(Locale.getDefault())
        val regex = Regex("(?i)(?:de|sobre|para)\\s+([a-záéíóúñ0-9\\s]{3,35})(?:\\s+para|\\s+con|\\s+en)?")
        val match = regex.find(prompt)
        return match?.groupValues?.get(1)?.trim()?.takeIf {
            !it.contains("crear") && !it.contains("app") && !it.contains("html")
        }
    }

    private fun extractAudienceCandidate(prompt: String): String? {
        val regex = Regex("(?i)para\\s+(estudiantes|niños|docentes|mí|mi familia|adolescentes|universitarios|profesionales|jóvenes)")
        return regex.find(prompt)?.groupValues?.get(1)?.trim()
    }

    private fun extractFeaturesCandidate(prompt: String): String? {
        val regex = Regex("(?i)con\\s+([a-záéíóúñ0-9\\s,y]{6,50})")
        return regex.find(prompt)?.groupValues?.get(1)?.trim()
    }

    private fun extractStyleCandidate(prompt: String): String? {
        val lower = prompt.lowercase(Locale.getDefault())
        return when {
            lower.contains("modo oscuro") || lower.contains("oscuro") -> "Modo oscuro moderno"
            lower.contains("modo claro") || lower.contains("claro") -> "Modo claro minimalista"
            lower.contains("divertido") || lower.contains("colorido") -> "Estilo divertido y colorido"
            lower.contains("minimalista") -> "Estilo limpio y minimalista"
            else -> null
        }
    }

    private fun findUserResponseForQuestion(history: List<ChatMessageEntity>, qNum: Int): String? {
        // Busca en los mensajes del usuario la respuesta correspondiente
        val userMsgs = history.filter { it.sender == "USER" }
        return when (qNum) {
            1 -> userMsgs.getOrNull(0)?.text
            2 -> userMsgs.getOrNull(1)?.text
            3 -> userMsgs.getOrNull(2)?.text
            4 -> userMsgs.getOrNull(3)?.text
            else -> null
        }
    }
}
