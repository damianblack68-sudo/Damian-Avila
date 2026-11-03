package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.OnyxRepository
import com.example.domain.model.ChatAttachment
import com.example.domain.model.AttachmentMediaType
import com.example.domain.model.AttendanceStatus
import com.example.domain.model.AttendanceStats
import com.example.data.local.entity.AttendanceRecordEntity
import com.example.domain.model.ChartData
import com.example.domain.model.EmotionalTone
import com.example.domain.model.ProfileType
import com.example.domain.model.VoicePersona
import com.example.domain.model.CodeProjectPayload
import com.example.domain.model.ConceptMapData
import com.example.domain.model.EngineeringBlueprint
import com.example.engine.AcademicReasoningEngine
import com.example.engine.CodeGeneratorEngine
import com.example.engine.ConceptMapEngine
import com.example.engine.EngineeringBlueprintEngine
import com.example.engine.MathSolverEngine
import com.example.engine.OnyxExecutiveAssistant
import com.example.engine.VoiceManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class OnyxViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = OnyxRepository(database)
    val voiceManager = VoiceManager(application)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val _currentProfile = MutableStateFlow(ProfileType.PERSONAL)
    val currentProfile: StateFlow<ProfileType> = _currentProfile.asStateFlow()

    private val _currentTone = MutableStateFlow(EmotionalTone.ANALITICO)
    val currentTone: StateFlow<EmotionalTone> = _currentTone.asStateFlow()

    private val _currentVoicePersona = MutableStateFlow(VoicePersona.ONYX_EXECUTIVE)
    val currentVoicePersona: StateFlow<VoicePersona> = _currentVoicePersona.asStateFlow()

    private val _currentSessionId = MutableStateFlow("onyx-welcome-session")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    // Teacher Attendance & Absence Register State
    private val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val _selectedAttendanceDate = MutableStateFlow(defaultDate)
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

    private val _selectedAttendanceGroup = MutableStateFlow("Matemáticas 3°A")
    val selectedAttendanceGroup: StateFlow<String> = _selectedAttendanceGroup.asStateFlow()

    val chatSessions: StateFlow<List<ChatSessionEntity>> = _currentProfile
        .flatMapLatest { profile -> repository.getChatSessions(profile.name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId -> repository.getChatMessages(sessionId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = _currentProfile
        .flatMapLatest { profile -> repository.getTasks(profile.name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = _currentProfile
        .flatMapLatest { profile -> repository.getNotes(profile.name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords: StateFlow<List<AttendanceRecordEntity>> = combine(
        _selectedAttendanceDate,
        _selectedAttendanceGroup,
        _currentProfile
    ) { date, group, profile ->
        Triple(date, group, profile.name)
    }.flatMapLatest { (date, group, profileName) ->
        repository.getAttendanceRecords(date, group, profileName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            repository.seedDefaultStudentsIfEmpty(defaultDate, "Matemáticas 3°A", ProfileType.LABORAL.name)
        }
    }

    fun setProfile(profile: ProfileType) {
        _currentProfile.value = profile
    }

    fun setTone(tone: EmotionalTone) {
        _currentTone.value = tone
    }

    fun setVoicePersona(persona: VoicePersona) {
        _currentVoicePersona.value = persona
        voiceManager.setPersona(persona)
    }

    fun selectChatSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun createChatSession(title: String) {
        viewModelScope.launch {
            val id = repository.createChatSession(title, _currentProfile.value.name)
            _currentSessionId.value = id
            repository.addMessage(
                sessionId = id,
                sender = "ONYX",
                text = "Nueva sesión de trabajo '$title' inicializada. ¿Qué problema o meta abordamos en este espacio?",
                isMath = false
            )
        }
    }

    fun deleteChatSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteChatSession(sessionId)
            val remaining = chatSessions.value.filter { it.id != sessionId }
            if (remaining.isNotEmpty()) {
                _currentSessionId.value = remaining.first().id
            } else {
                createChatSession("Nueva Conversación")
            }
        }
    }

    fun sendMessage(
        text: String,
        isMath: Boolean,
        chartJson: String?,
        clarifyingJson: String?,
        engine: com.example.domain.model.OnyxAiEngine = com.example.domain.model.OnyxAiEngine.ONYX_X,
        attachment: ChatAttachment? = null
    ) {
        val sessionId = _currentSessionId.value
        viewModelScope.launch {
            val attachmentJson = attachment?.let {
                moshi.adapter(ChatAttachment::class.java).toJson(it)
            }

            // Save User message with attachment
            repository.addMessage(
                sessionId = sessionId,
                sender = "USER",
                text = text,
                isMath = isMath,
                attachmentJson = attachmentJson
            )

            val engineHeader = "[${engine.title} • ${engine.reasoningLevel} ${engine.speedMultiplier}]"

            _isAiProcessing.value = true
            try {
                kotlinx.coroutines.delay(600)

            val attachmentReasoningIntro = attachment?.let {
                when (it.mediaType) {
                    AttachmentMediaType.IMAGE -> "📷 [Análisis Visual & Razonamiento de Imagen]: '${it.name}' procesada con éxito. Onyx ha analizado los diagramas, texto manuscrito y capturas adjuntas.\n\n"
                    AttachmentMediaType.VIDEO -> "🎥 [Inspección de Video]: Archivo '${it.name}' procesado. Secuencia de laboratorio/estudio analizada cuadro a cuadro.\n\n"
                    AttachmentMediaType.DOCUMENT -> "📄 [Lectura y Síntesis de Libro/Documento]: Archivo '${it.name}' indexado. Extrayendo conceptos nucleares, fórmulas y resolución de tarea escolar:\n\n"
                }
            } ?: ""

            // Moderation & Safety check (Violencia, odio, crisis emocional, apps prohibidas)
            val moderationCheck = com.example.engine.OnyxOpenEducationalEngine.evaluateModeration(text)
            if (moderationCheck !is com.example.engine.OnyxOpenEducationalEngine.ModerationCheck.Passed) {
                val responseText = when (moderationCheck) {
                    is com.example.engine.OnyxOpenEducationalEngine.ModerationCheck.Violation -> moderationCheck.message
                    is com.example.engine.OnyxOpenEducationalEngine.ModerationCheck.EmotionalCrisis -> moderationCheck.message
                    is com.example.engine.OnyxOpenEducationalEngine.ModerationCheck.ProhibitedApp -> moderationCheck.message
                    else -> ""
                }
                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = responseText,
                    isMath = false
                )
                return@launch
            }

            // Onyx Open: Flujo pedagógico para crear y editar apps en HTML (entrevista, resumen y confirmación)
            val historyMessages = repository.getChatMessages(sessionId).firstOrNull() ?: emptyList()
            val lastOnyxMsg = historyMessages.filter { it.sender != "USER" }.lastOrNull()?.text ?: ""
            val isAwaitingConfirmation = lastOnyxMsg.contains("¿Empiezo a crearla?")
            val isInterviewActive = lastOnyxMsg.contains("¿De qué va a tratar tu app?") ||
                    lastOnyxMsg.contains("¿Para quién es") ||
                    lastOnyxMsg.contains("¿Qué 3 o 4 funciones principales") ||
                    lastOnyxMsg.contains("¿Qué estilo visual prefieres")

            if (com.example.engine.OnyxOpenEducationalEngine.isAppCreationRequest(text) ||
                com.example.engine.OnyxOpenEducationalEngine.isAppEditRequest(text) ||
                com.example.engine.OnyxOpenEducationalEngine.isUniversal3DRequest(text) ||
                (isAwaitingConfirmation && com.example.engine.OnyxOpenEducationalEngine.isConfirmation(text)) ||
                isInterviewActive
            ) {
                val educationalResult = com.example.engine.OnyxOpenEducationalEngine.processEducationalQuery(
                    userInput = text,
                    recentMessages = historyMessages,
                    selectedEngine = engine
                )

                val codePayloadJson = educationalResult.codePayload?.let {
                    moshi.adapter(CodeProjectPayload::class.java).toJson(it)
                }

                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = educationalResult.text,
                    codePayloadJson = codePayloadJson,
                    isMath = false
                )
                return@launch
            }

            // 1. Code & Web App Generation
            if (CodeGeneratorEngine.isCodeRequest(text)) {
                val codeProject = CodeGeneratorEngine.generateCode(text)
                val codePayloadJson = moshi.adapter(CodeProjectPayload::class.java).toJson(codeProject)
                val responseText = buildString {
                    append(attachmentReasoningIntro)
                    appendLine("$engineHeader Código & Aplicación generada con éxito:")
                    appendLine("• Archivo: ${codeProject.filename}")
                    appendLine("• Lenguaje: ${codeProject.language.uppercase()}")
                    appendLine("• ${codeProject.explanation}")
                    appendLine()
                    appendLine("Puedes ver la vista previa web interactiva directamente en pantalla, exportar el archivo .html autónomo o conectarlo con apps de programación (Spck Editor, Acode, VS Code, GitHub).")
                }
                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = responseText,
                    codePayloadJson = codePayloadJson
                )
                return@launch
            }

            // 2. Engineering Blueprints & CAD with exact measurements
            if (EngineeringBlueprintEngine.isBlueprintRequest(text)) {
                val blueprint = EngineeringBlueprintEngine.generateBlueprint(text)
                val blueprintJson = moshi.adapter(EngineeringBlueprint::class.java).toJson(blueprint)
                val responseText = buildString {
                    append(attachmentReasoningIntro)
                    appendLine("$engineHeader Plano de ingeniería CAD generado con cotas técnicas:")
                    appendLine("• Título: ${blueprint.title}")
                    appendLine("• Escala: ${blueprint.type.scale} • Cotas métricas en ${blueprint.type.unit}")
                    appendLine("• Área Total: ${blueprint.totalAreaFormatted}")
                    appendLine("• Diseñado por: ${blueprint.designerInfo}")
                    appendLine()
                    appendLine("Plano renderizado en papel milimétrico azul técnico con muros de carga, cotas de dimensión y membrete técnico oficial.")
                }
                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = responseText,
                    blueprintJson = blueprintJson
                )
                return@launch
            }

            // 3. Concept Maps
            if (ConceptMapEngine.isConceptMapRequest(text)) {
                val conceptMap = ConceptMapEngine.generateConceptMap(text)
                val mapJson = moshi.adapter(ConceptMapData::class.java).toJson(conceptMap)
                val responseText = buildString {
                    append(attachmentReasoningIntro)
                    appendLine("$engineHeader Mapa Conceptual estructurado para estudio inteligente:")
                    appendLine("• Tema Central: ${conceptMap.centralTheme}")
                    appendLine("• Nodos y proposiciones conectivas generadas.")
                    appendLine("• ${conceptMap.studySummary}")
                }
                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = responseText,
                    conceptMapJson = mapJson
                )
                return@launch
            }

            // 4. Academic Homework Reasoning (100% Chain-of-Thought)
            if (AcademicReasoningEngine.isAcademicRequest(text) || attachment?.mediaType == AttachmentMediaType.IMAGE || attachment?.mediaType == AttachmentMediaType.DOCUMENT) {
                val academic = AcademicReasoningEngine.reason(text)
                val responseText = buildString {
                    append(attachmentReasoningIntro)
                    appendLine("$engineHeader Razonamiento Académico al 100% (Chain-of-Thought):")
                    appendLine("Materia: ${academic.subject}")
                    appendLine("Tema: ${academic.topicTitle}")
                    appendLine()
                    appendLine("--- 1. DATOS IDENTIFICADOS ---")
                    academic.initialData.forEach { (k, v) -> appendLine("• $k: $v") }
                    appendLine()
                    appendLine("--- 2. LEYES Y FÓRMULAS ---")
                    academic.lawsAndPrinciples.forEach { law -> appendLine("► $law") }
                    appendLine()
                    appendLine("--- 3. CADENA DE RAZONAMIENTO PASO A PASO ---")
                    academic.chainOfThoughtSteps.forEach { step ->
                        appendLine("Paso ${step.stepNumber}: ${step.stepTitle}")
                        appendLine("  ${step.description}")
                        if (step.formulaOrLaw != null) appendLine("  Fórmula: ${step.formulaOrLaw}")
                        appendLine("  → Deducción: ${step.deduction}")
                    }
                    appendLine()
                    appendLine("--- 4. CONCLUSIÓN FINAL ---")
                    appendLine("✓ ${academic.finalConclusion}")
                    if (academic.studyAdvice.isNotBlank()) {
                        appendLine()
                        appendLine("💡 Consejo para estudiar: ${academic.studyAdvice}")
                    }
                }
                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = responseText,
                    isMath = true
                )
                return@launch
            }

            // 5. Math Solver Engine
            if (isMath || text.contains("=") || text.contains("x²") || text.contains("derivada") || text.contains("integral") || text.contains("media")) {
                val solution = MathSolverEngine.solve(text)
                val chartDataJson = solution.chartData?.let {
                    val adapter = moshi.adapter(ChartData::class.java)
                    adapter.toJson(it)
                }

                val clarifyingDataJson = if (solution.clarifyingQuestions.isNotEmpty()) {
                    """[{"question":"¿Deseas profundizar en esta resolución?","options":["Graficar en otro intervalo","Ver deducción formal","Añadir a tareas pendientes"]}]"""
                } else null

                val responseText = buildString {
                    append(attachmentReasoningIntro)
                    appendLine("$engineHeader Análisis matemático completado:")
                    appendLine()
                    appendLine(solution.problemTitle)
                    appendLine()
                    solution.steps.forEach { step ->
                        appendLine(step)
                    }
                    appendLine()
                    append("Resultado final: ")
                    append(solution.finalAnswer)
                }

                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = responseText,
                    isMath = true,
                    chartJson = chartDataJson,
                    clarifyingJson = clarifyingDataJson
                )
            } else {
                // 6. Conversational Intelligence & Executive Directives
                val prefix = when (engine) {
                    com.example.domain.model.OnyxAiEngine.ONYX_X -> "[Onyx X • Estándar 1.0x]"
                    com.example.domain.model.OnyxAiEngine.ONYX_PRO -> "[Onyx Pro • Avanzado 1.8x - Deducción Rápida]"
                    com.example.domain.model.OnyxAiEngine.ONYX_PLUS -> "[Onyx Plus VIP • Experto 2.5x - Contexto Ampliado]"
                    com.example.domain.model.OnyxAiEngine.ONYX_EVOLUTION -> "[Onyx Evolution Master • Supremo 4.0x - Síntesis Cognitiva Total]"
                }

                val executiveRes = OnyxExecutiveAssistant.processDirective(text)

                val reply = buildString {
                    append(attachmentReasoningIntro)
                    appendLine("$prefix ${if (executiveRes.isEnglish) "ONYX EXECUTIVE PROTOCOL" else "PROTOCOLO EJECUTIVO ONYX"}")
                    appendLine()
                    append(executiveRes.displayText)
                }

                repository.addMessage(
                    sessionId = sessionId,
                    sender = "ONYX",
                    text = reply,
                    isMath = false
                )
            }
        } finally {
            _isAiProcessing.value = false
        }
    }
}

    // --- Teacher Attendance & Absence Operations (Docentes) ---

    fun setAttendanceDate(date: String) {
        _selectedAttendanceDate.value = date
    }

    fun setAttendanceGroup(group: String) {
        _selectedAttendanceGroup.value = group
    }

    fun cycleAttendanceStatus(record: AttendanceRecordEntity) {
        val nextStatus = when (record.status) {
            "ASISTIO" -> "RETARDO"
            "RETARDO" -> "FALTO"
            else -> "ASISTIO"
        }
        viewModelScope.launch {
            repository.saveAttendanceRecord(record.copy(status = nextStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setAttendanceStatus(record: AttendanceRecordEntity, status: String) {
        viewModelScope.launch {
            repository.saveAttendanceRecord(record.copy(status = status, updatedAt = System.currentTimeMillis()))
        }
    }

    fun markAllStudents(status: String) {
        val currentList = attendanceRecords.value
        viewModelScope.launch {
            val updated = currentList.map { it.copy(status = status, updatedAt = System.currentTimeMillis()) }
            repository.saveAllAttendance(updated)
        }
    }

    fun addStudentToCurrentGroup(
        studentName: String,
        studentUsername: String = "",
        studentEmail: String = "",
        notes: String = ""
    ) {
        if (studentName.isBlank()) return
        val date = _selectedAttendanceDate.value
        val group = _selectedAttendanceGroup.value
        val profile = _currentProfile.value.name
        viewModelScope.launch {
            val record = AttendanceRecordEntity(
                studentName = studentName.trim(),
                studentUsername = studentUsername.trim(),
                studentEmail = studentEmail.trim(),
                groupName = group,
                date = date,
                status = "ASISTIO",
                notes = notes.trim(),
                teacherProfile = profile,
                teacherUsername = "damianblack68@gmail.com"
            )
            repository.saveAttendanceRecord(record)
        }
    }

    fun updateStudentNotes(record: AttendanceRecordEntity, notes: String) {
        viewModelScope.launch {
            repository.saveAttendanceRecord(record.copy(notes = notes, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteStudentFromCurrentGroup(recordId: Long, studentName: String) {
        val group = _selectedAttendanceGroup.value
        viewModelScope.launch {
            repository.deleteAttendanceRecord(recordId)
            repository.deleteStudentFromGroup(studentName, group)
        }
    }

    fun calculateStats(records: List<AttendanceRecordEntity>): AttendanceStats {
        val total = records.size
        val present = records.count { it.status == "ASISTIO" }
        val late = records.count { it.status == "RETARDO" }
        val absent = records.count { it.status == "FALTO" }
        val percentage = if (total > 0) ((present.toFloat() + (late * 0.5f)) / total) * 100f else 0f
        return AttendanceStats(
            totalStudents = total,
            presentCount = present,
            lateCount = late,
            absentCount = absent,
            attendancePercentage = percentage
        )
    }

    fun exportAttendanceSummary(): String {
        val date = _selectedAttendanceDate.value
        val group = _selectedAttendanceGroup.value
        val records = attendanceRecords.value
        val stats = calculateStats(records)
        return buildString {
            appendLine("📋 REGISTRO DE FALTAS Y ASISTENCIA • ONYX OPEN DOCENTES")
            appendLine("📅 Fecha: $date | 🏫 Grupo: $group")
            appendLine("═════════════════════════════════════════")
            appendLine("📊 Resumen General:")
            appendLine("• Total Alumnos: ${stats.totalStudents}")
            appendLine("• 🟢 Asistencias: ${stats.presentCount}")
            appendLine("• 🟡 Retardos: ${stats.lateCount}")
            appendLine("• 🔴 Faltas: ${stats.absentCount}")
            appendLine("• % Asistencia Efectiva: ${String.format(Locale.getDefault(), "%.1f", stats.attendancePercentage)}%")
            appendLine("═════════════════════════════════════════")
            appendLine("Lista Nominal de Estudiantes:")
            records.forEachIndexed { idx, r ->
                val badge = when (r.status) {
                    "ASISTIO" -> "🟢 ASISTIÓ"
                    "RETARDO" -> "🟡 RETARDO / NO LLEGA"
                    "FALTO" -> "🔴 FALTÓ (INASISTENCIA)"
                    else -> r.status
                }
                appendLine("${idx + 1}. ${r.studentName} — $badge")
            }
            appendLine("═════════════════════════════════════════")
            appendLine("Generado con privacidad local protegida en Onyx Open.")
        }
    }


    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.saveTask(task)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskComplete(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.saveNote(note)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
