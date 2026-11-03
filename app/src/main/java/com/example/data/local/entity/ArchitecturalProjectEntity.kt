package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Estado del ciclo de vida de la generación arquitectónica.
 */
enum class ArchitecturalGenerationStatus(val label: String) {
    PENDIENTE("Pendiente"),
    GENERADO("Generado")
}

/**
 * Entidad Room para persistir proyectos arquitectónicos en curso y completados.
 * Almacena los metadatos recabados por el chat interactivo, cotas métricas del terreno,
 * requerimientos de diseño y el estado actual de la generación.
 */
@Entity(tableName = "architectural_projects")
data class ArchitecturalProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val projectType: String? = null,
    val lotWidthMeters: Float? = null,
    val lotLengthMeters: Float? = null,
    val totalLotAreaM2: Float? = null,
    val totalFloors: Int? = null,
    val bedroomsCount: Int? = null,
    val bathroomsCount: Int? = null,
    val architecturalStyle: String? = null,
    val budgetLevel: String? = null,
    val specialSpacesCsv: String = "", // e.g. "Cochera, Jardín, Estudio"
    val isConfirmedByUser: Boolean = false,
    val generationStatus: ArchitecturalGenerationStatus = ArchitecturalGenerationStatus.PENDIENTE,
    val blueprint2DJson: String? = null,
    val model3DJson: String? = null,
    val zoningReport: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
