package com.example.creative.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.ArchitecturalProjectDao
import com.example.data.local.entity.ArchitecturalGenerationStatus
import com.example.data.local.entity.ArchitecturalProjectEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ArchitecturalStudioViewModel:
 * ViewModel para gestionar el ciclo de vida, persistencia y estado reactivo de proyectos arquitectónicos
 * dentro de ONYX CREATIVE STUDIO. Inyecta el ArchitecturalProjectDao para interactuar con Room.
 */
class ArchitecturalStudioViewModel(
    private val projectDao: ArchitecturalProjectDao
) : ViewModel() {

    /**
     * StateFlow reactivo que emite en tiempo real la lista de todos los proyectos arquitectónicos
     * ordenados por fecha de actualización.
     */
    val projects: StateFlow<List<ArchitecturalProjectEntity>> = projectDao
        .getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Consulta y devuelve la lista de proyectos arquitectónicos existentes expuesta mediante StateFlow.
     */
    fun getAllProjects(): StateFlow<List<ArchitecturalProjectEntity>> = projects

    /**
     * Guarda o actualiza un proyecto arquitectónico en la base de datos Room.
     */
    fun saveProject(project: ArchitecturalProjectEntity) {
        viewModelScope.launch {
            projectDao.insertProject(project)
        }
    }

    /**
     * Actualiza el estado de generación (PENDIENTE / GENERADO) de un proyecto existente.
     */
    fun updateGenerationStatus(projectId: String, status: ArchitecturalGenerationStatus) {
        viewModelScope.launch {
            projectDao.updateGenerationStatus(projectId, status)
        }
    }

    /**
     * Elimina un proyecto arquitectónico por su ID.
     */
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectDao.deleteProjectById(projectId)
        }
    }

    /**
     * Factory para proveer e inyectar el ArchitecturalProjectDao en ArchitecturalStudioViewModel.
     */
    class Factory(private val projectDao: ArchitecturalProjectDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArchitecturalStudioViewModel::class.java)) {
                return ArchitecturalStudioViewModel(projectDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
