package com.example.creative.domain.model

/**
 * Representa los requisitos técnicos y arquitectónicos recabados
 * por el asistente antes de autorizar y habilitar la generación de planos y renders 3D.
 */
data class ArchitecturalBrief(
    val projectType: String? = null,
    val lotWidthMeters: Float? = null,
    val lotLengthMeters: Float? = null,
    val totalFloors: Int? = null,
    val bedroomsCount: Int? = null,
    val bathroomsCount: Int? = null,
    val architecturalStyle: String? = null,
    val budgetLevel: String? = null,
    val specialSpaces: List<String> = emptyList(),
    val isConfirmedByUser: Boolean = false
) {
    val totalLotAreaM2: Float?
        get() = if (lotWidthMeters != null && lotLengthMeters != null) {
            lotWidthMeters * lotLengthMeters
        } else null

    val missingRequirements: List<String>
        get() {
            val missing = mutableListOf<String>()
            if (projectType.isNullOrBlank()) missing.add("Tipo de proyecto")
            if (lotWidthMeters == null || lotLengthMeters == null) missing.add("Dimensiones del terreno (frente × fondo)")
            if (totalFloors == null) missing.add("Número de plantas")
            if (bedroomsCount == null) missing.add("Cantidad de habitaciones")
            if (bathroomsCount == null) missing.add("Cantidad de baños")
            if (architecturalStyle.isNullOrBlank()) missing.add("Estilo arquitectónico")
            return missing
        }

    val completionPercentage: Float
        get() {
            val total = 6f
            var completed = 0f
            if (!projectType.isNullOrBlank()) completed += 1f
            if (lotWidthMeters != null && lotLengthMeters != null) completed += 1f
            if (totalFloors != null) completed += 1f
            if (bedroomsCount != null) completed += 1f
            if (bathroomsCount != null) completed += 1f
            if (!architecturalStyle.isNullOrBlank()) completed += 1f
            return (completed / total).coerceIn(0f, 1f)
        }

    val isReadyForGeneration: Boolean
        get() = missingRequirements.isEmpty()
}

/**
 * Mensaje dentro de la conversación del asistente arquitectónico
 */
data class ArchChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ArchSender,
    val text: String,
    val quickOptions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class ArchSender {
    ASSISTANT_AI,
    USER
}

fun ArchitecturalBrief.toEntity(
    id: String = java.util.UUID.randomUUID().toString(),
    title: String = "Proyecto: ${projectType ?: "Arquitectura"} (${architecturalStyle ?: "Personalizado"})",
    status: com.example.data.local.entity.ArchitecturalGenerationStatus = if (isReadyForGeneration) com.example.data.local.entity.ArchitecturalGenerationStatus.GENERADO else com.example.data.local.entity.ArchitecturalGenerationStatus.PENDIENTE
): com.example.data.local.entity.ArchitecturalProjectEntity {
    return com.example.data.local.entity.ArchitecturalProjectEntity(
        id = id,
        title = title,
        projectType = projectType,
        lotWidthMeters = lotWidthMeters,
        lotLengthMeters = lotLengthMeters,
        totalLotAreaM2 = totalLotAreaM2,
        totalFloors = totalFloors,
        bedroomsCount = bedroomsCount,
        bathroomsCount = bathroomsCount,
        architecturalStyle = architecturalStyle,
        budgetLevel = budgetLevel,
        specialSpacesCsv = specialSpaces.joinToString(", "),
        isConfirmedByUser = isConfirmedByUser,
        generationStatus = status
    )
}

fun com.example.data.local.entity.ArchitecturalProjectEntity.toBrief(): ArchitecturalBrief {
    return ArchitecturalBrief(
        projectType = projectType,
        lotWidthMeters = lotWidthMeters,
        lotLengthMeters = lotLengthMeters,
        totalFloors = totalFloors,
        bedroomsCount = bedroomsCount,
        bathroomsCount = bathroomsCount,
        architecturalStyle = architecturalStyle,
        budgetLevel = budgetLevel,
        specialSpaces = if (specialSpacesCsv.isBlank()) emptyList() else specialSpacesCsv.split(",").map { it.trim() },
        isConfirmedByUser = isConfirmedByUser
    )
}

