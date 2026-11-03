package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ArchitecturalGenerationStatus
import com.example.data.local.entity.ArchitecturalProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para proyectos de arquitectura en Room.
 */
@Dao
interface ArchitecturalProjectDao {

    @Query("SELECT * FROM architectural_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ArchitecturalProjectEntity>>

    @Query("SELECT * FROM architectural_projects WHERE id = :id")
    suspend fun getProjectById(id: String): ArchitecturalProjectEntity?

    @Query("SELECT * FROM architectural_projects WHERE generationStatus = :status ORDER BY updatedAt DESC")
    fun getProjectsByStatus(status: ArchitecturalGenerationStatus): Flow<List<ArchitecturalProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ArchitecturalProjectEntity)

    @Update
    suspend fun updateProject(project: ArchitecturalProjectEntity)

    @Query("UPDATE architectural_projects SET generationStatus = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateGenerationStatus(
        id: String,
        status: ArchitecturalGenerationStatus,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM architectural_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}
