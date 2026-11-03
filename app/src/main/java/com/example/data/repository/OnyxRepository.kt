package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

import com.example.data.local.entity.ArchitecturalGenerationStatus
import com.example.data.local.entity.ArchitecturalProjectEntity

class OnyxRepository(private val database: AppDatabase) {

    private val noteDao = database.noteDao()
    private val taskDao = database.taskDao()
    private val chatDao = database.chatDao()
    private val attendanceDao = database.attendanceDao()
    private val blockedContactDao = database.blockedContactDao()
    private val architecturalProjectDao = database.architecturalProjectDao()

    // --- Architectural Projects (Onyx Creative Studio) ---

    fun getArchitecturalProjects(): Flow<List<ArchitecturalProjectEntity>> =
        architecturalProjectDao.getAllProjects()

    fun getArchitecturalProjectsByStatus(status: ArchitecturalGenerationStatus): Flow<List<ArchitecturalProjectEntity>> =
        architecturalProjectDao.getProjectsByStatus(status)

    suspend fun getArchitecturalProject(id: String): ArchitecturalProjectEntity? =
        architecturalProjectDao.getProjectById(id)

    suspend fun saveArchitecturalProject(project: ArchitecturalProjectEntity) =
        architecturalProjectDao.insertProject(project)

    suspend fun updateArchitecturalGenerationStatus(id: String, status: ArchitecturalGenerationStatus) =
        architecturalProjectDao.updateGenerationStatus(id, status)

    suspend fun deleteArchitecturalProject(id: String) =
        architecturalProjectDao.deleteProjectById(id)

    // --- Notes ---

    fun getNotes(profile: String): Flow<List<NoteEntity>> =
        noteDao.getNotesByProfile(profile)

    suspend fun getNote(id: Long): NoteEntity? =
        noteDao.getNoteById(id)

    suspend fun saveNote(note: NoteEntity): Long =
        noteDao.insertOrUpdate(note)

    suspend fun deleteNote(id: Long) =
        noteDao.deleteById(id)

    fun getTasks(profile: String): Flow<List<TaskEntity>> =
        taskDao.getTasksByProfile(profile)

    suspend fun saveTask(task: TaskEntity): Long =
        taskDao.insertOrUpdate(task)

    suspend fun toggleTaskComplete(task: TaskEntity) {
        taskDao.update(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(id: Long) =
        taskDao.deleteById(id)

    fun getChatSessions(profile: String): Flow<List<ChatSessionEntity>> =
        chatDao.getSessionsByProfile(profile)

    suspend fun createChatSession(title: String, profile: String): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSessionEntity(
            id = id,
            title = title,
            profile = profile,
            lastMessage = "Sesión iniciada con Onyx Open"
        )
        chatDao.insertSession(session)
        return id
    }

    fun getChatMessages(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun addMessage(
        sessionId: String,
        sender: String,
        text: String,
        isMath: Boolean = false,
        chartJson: String? = null,
        clarifyingJson: String? = null,
        codePayloadJson: String? = null,
        blueprintJson: String? = null,
        conceptMapJson: String? = null,
        attachmentJson: String? = null
    ): Long {
        val msg = ChatMessageEntity(
            sessionId = sessionId,
            sender = sender,
            text = text,
            isMath = isMath,
            chartJson = chartJson,
            clarifyingQuestionsJson = clarifyingJson,
            codePayloadJson = codePayloadJson,
            blueprintJson = blueprintJson,
            conceptMapJson = conceptMapJson,
            attachmentJson = attachmentJson
        )
        val id = chatDao.insertMessage(msg)
        val snippet = if (text.length > 50) text.take(47) + "..." else text
        chatDao.updateSessionLastMessage(sessionId, snippet)
        return id
    }

    suspend fun deleteChatSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    // --- Teacher Attendance & Absence Register ("Cada quien en su casa") ---
    fun getAttendanceRecords(date: String, groupName: String, teacherProfile: String): Flow<List<com.example.data.local.entity.AttendanceRecordEntity>> =
        attendanceDao.getAttendanceByDateAndGroup(date, groupName, teacherProfile)

    fun getTeacherGroups(teacherProfile: String): Flow<List<String>> =
        attendanceDao.getGroupsForTeacher(teacherProfile)

    suspend fun saveAttendanceRecord(record: com.example.data.local.entity.AttendanceRecordEntity): Long =
        attendanceDao.insertOrUpdate(record)

    suspend fun saveAllAttendance(records: List<com.example.data.local.entity.AttendanceRecordEntity>) =
        attendanceDao.insertAll(records)

    suspend fun deleteAttendanceRecord(id: Long) =
        attendanceDao.deleteRecord(id)

    suspend fun deleteStudentFromGroup(studentName: String, groupName: String) =
        attendanceDao.deleteStudentFromGroup(studentName, groupName)

    suspend fun seedDefaultStudentsIfEmpty(date: String, groupName: String, teacherProfile: String) {
        val defaultStudents = listOf(
            Triple("Alejandro Morales Cruz", "alejandro_mc", "alejandro.m@estudiante.mx"),
            Triple("Brenda Valeria Torres", "brenda_val", "brenda.t@estudiante.mx"),
            Triple("Carlos Daniel Mendoza", "carlos_ia", "carlos.m@estudiante.mx"),
            Triple("Diana Paola Castillo", "diana_castillo", "diana.c@estudiante.mx"),
            Triple("Eduardo Javier Silva", "eduardo_silva", "eduardo.s@estudiante.mx"),
            Triple("Fernanda Sofía Ramos", "fer_ramos", "fernanda.r@estudiante.mx"),
            Triple("Gabriel Alonso Ortiz", "gabriel_ortiz", "gabriel.o@estudiante.mx"),
            Triple("Helena María Vega", "helena_vega", "helena.v@estudiante.mx"),
            Triple("Iván Rodrigo Gutiérrez", "ivan_gutierrez", "ivan.g@estudiante.mx"),
            Triple("Jimena Renata Flores", "jimena_flores", "jimena.f@estudiante.mx")
        )
        val records = defaultStudents.map { (name, uname, email) ->
            com.example.data.local.entity.AttendanceRecordEntity(
                studentName = name,
                studentUsername = uname,
                studentEmail = email,
                groupName = groupName,
                date = date,
                status = "ASISTIO",
                notes = "",
                teacherProfile = teacherProfile,
                teacherUsername = "damianblack68@gmail.com"
            )
        }
        attendanceDao.insertAll(records)
    }

    // --- Local Isolated Blocked Contacts ---
    fun getBlockedContacts(ownerUsername: String): Flow<List<com.example.data.local.entity.BlockedContactEntity>> =
        blockedContactDao.getBlockedContacts(ownerUsername)

    suspend fun blockContact(ownerUsername: String, blockedUsername: String, reason: String = "Bloqueado por usuario"): Long =
        blockedContactDao.blockContact(
            com.example.data.local.entity.BlockedContactEntity(
                ownerUsername = ownerUsername,
                blockedUsername = blockedUsername,
                reason = reason
            )
        )

    suspend fun unblockContact(ownerUsername: String, blockedUsername: String) =
        blockedContactDao.unblockContact(ownerUsername, blockedUsername)

    suspend fun isContactBlocked(ownerUsername: String, blockedUsername: String): Boolean =
        blockedContactDao.isBlocked(ownerUsername, blockedUsername)


    suspend fun seedInitialDataIfNeeded() {
        val initialSessionId = "onyx-welcome-session"
        val existingSession = chatDao.getSessionById(initialSessionId)
        if (existingSession == null) {
            chatDao.insertSession(
                ChatSessionEntity(
                    id = initialSessionId,
                    title = "Introducción a Onyx Open",
                    profile = "PERSONAL",
                    lastMessage = "¡Hola! Soy Onyx Open, tu inteligencia artificial."
                )
            )

            addMessage(
                sessionId = initialSessionId,
                sender = "ONYX",
                text = "¡Hola! Soy Onyx Open, tu inteligencia artificial personal con arquitectura de alto rendimiento. Puedo resolver ecuaciones matemáticas paso a paso, generar gráficos en tiempo real, transformar tus notas en diagramas de flujo y mapas mentales, sintetizar voz y estructurar tu tiempo como en Notion. ¿En qué problema o proyecto trabajamos hoy?",
                isMath = false,
                clarifyingJson = """[{"question":"¿Qué te gustaría explorar primero?","options":["Resolver ejercicio matemático","Crear diagrama de flujo","Organizar tareas de alta prioridad","Escanear documento o fórmula"]}]"""
            )

            // Seed sample task
            taskDao.insertOrUpdate(
                TaskEntity(
                    title = "Revisar cálculo diferencial y derivadas parciales",
                    description = "Resolver ejercicios de optimización multivariable con análisis gráfico",
                    priority = "URGENTE",
                    dueDate = "Hoy, 18:00",
                    tags = "Matemáticas, Examen, Cálculo",
                    profile = "PERSONAL"
                )
            )

            taskDao.insertOrUpdate(
                TaskEntity(
                    title = "Diseñar estructura del informe de Inteligencia Artificial",
                    description = "Exportar resumen a Google Drive y generar mapa mental de conceptos",
                    priority = "ALTA",
                    dueDate = "Mañana, 10:00",
                    tags = "Proyecto, IA, Documentación",
                    profile = "LABORAL"
                )
            )

            // Seed sample notes
            noteDao.insertOrUpdate(
                NoteEntity(
                    title = "Análisis Matemático: Ecuaciones Cuadráticas y Parábolas",
                    content = """# Resumen Teórico: Ecuaciones de Segundo Grado
Forma canónica: f(x) = ax² + bx + c
Vértice: x = -b / (2a)
Discriminante: Δ = b² - 4ac
- Si Δ > 0: Dos raíces reales distintas
- Si Δ = 0: Raíz doble en el vértice
- Si Δ < 0: Raíces complejas conjugadas

Ejemplo resuelto: 2x² - 8x + 6 = 0
Factorización: 2(x - 1)(x - 3) = 0
Raíces: x₁ = 1, x₂ = 3""",
                    tags = "Matemáticas, Álgebra, Cuadráticas",
                    profile = "PERSONAL"
                )
            )

            noteDao.insertOrUpdate(
                NoteEntity(
                    title = "Planificación Sprint de Productividad",
                    content = """## Objetivos Principales
- Implementar sincronización multiplataforma
- Gestión de prioridades basada en la matriz de Eisenhower
- Mapas mentales interactivos a partir de texto
- Cifrado seguro local y exportación rápida""",
                    tags = "Productividad, Notion, Metas",
                    profile = "LABORAL"
                )
            )
        }
    }
}
