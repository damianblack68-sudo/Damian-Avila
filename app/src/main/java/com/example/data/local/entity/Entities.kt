package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String, // Comma-separated tags e.g. "Matemáticas, Álgebra, Notion"
    val profile: String = "PERSONAL", // PERSONAL or LABORAL
    val updatedAt: Long = System.currentTimeMillis(),
    val mindMapJson: String? = null
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "ALTA", // URGENTE, ALTA, MEDIA, BAJA
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val tags: String = "",
    val profile: String = "PERSONAL",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String, // UUID
    val title: String,
    val profile: String = "PERSONAL",
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessage: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val sender: String, // "USER" or "ONYX"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMath: Boolean = false,
    val chartJson: String? = null,
    val clarifyingQuestionsJson: String? = null,
    val codePayloadJson: String? = null,
    val blueprintJson: String? = null,
    val conceptMapJson: String? = null,
    val attachmentJson: String? = null
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentName: String,
    val studentUsername: String = "",
    val studentEmail: String = "",
    val groupName: String,
    val date: String, // YYYY-MM-DD
    val status: String, // ASISTIO, RETARDO, FALTO
    val notes: String = "",
    val teacherProfile: String = "LABORAL",
    val teacherUsername: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_contacts")
data class BlockedContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val blockedUsername: String,
    val ownerUsername: String,
    val reason: String = "Bloqueado por el usuario",
    val timestamp: Long = System.currentTimeMillis()
)

