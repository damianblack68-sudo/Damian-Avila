package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.BlockedContactDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.AttendanceRecordEntity
import com.example.data.local.entity.BlockedContactEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.dao.ArchitecturalProjectDao
import com.example.data.local.entity.ArchitecturalProjectEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        AttendanceRecordEntity::class,
        BlockedContactEntity::class,
        ArchitecturalProjectEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun chatDao(): ChatDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun blockedContactDao(): BlockedContactDao
    abstract fun architecturalProjectDao(): ArchitecturalProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "onyx_open_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
