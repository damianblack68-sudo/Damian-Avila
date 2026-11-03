package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE profile = :profile ORDER BY updatedAt DESC")
    fun getNotesByProfile(profile: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE profile = :profile ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksByProfile(profile: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions WHERE profile = :profile ORDER BY createdAt DESC")
    fun getSessionsByProfile(profile: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET lastMessage = :lastMessage WHERE id = :sessionId")
    suspend fun updateSessionLastMessage(sessionId: String, lastMessage: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date AND groupName = :groupName AND teacherProfile = :teacherProfile ORDER BY studentName ASC")
    fun getAttendanceByDateAndGroup(date: String, groupName: String, teacherProfile: String): Flow<List<com.example.data.local.entity.AttendanceRecordEntity>>

    @Query("SELECT DISTINCT groupName FROM attendance_records WHERE teacherProfile = :teacherProfile ORDER BY groupName ASC")
    fun getGroupsForTeacher(teacherProfile: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: com.example.data.local.entity.AttendanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<com.example.data.local.entity.AttendanceRecordEntity>)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM attendance_records WHERE studentName = :studentName AND groupName = :groupName")
    suspend fun deleteStudentFromGroup(studentName: String, groupName: String)
}

@Dao
interface BlockedContactDao {
    @Query("SELECT * FROM blocked_contacts WHERE ownerUsername = :ownerUsername ORDER BY timestamp DESC")
    fun getBlockedContacts(ownerUsername: String): Flow<List<com.example.data.local.entity.BlockedContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockContact(entity: com.example.data.local.entity.BlockedContactEntity): Long

    @Query("DELETE FROM blocked_contacts WHERE ownerUsername = :ownerUsername AND blockedUsername = :blockedUsername")
    suspend fun unblockContact(ownerUsername: String, blockedUsername: String)

    @Query("SELECT COUNT(*) > 0 FROM blocked_contacts WHERE ownerUsername = :ownerUsername AND blockedUsername = :blockedUsername")
    suspend fun isBlocked(ownerUsername: String, blockedUsername: String): Boolean
}

