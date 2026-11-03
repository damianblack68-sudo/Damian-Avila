package com.example.ui.screens.workspace

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.MindMapNode
import com.example.domain.model.PriorityLevel
import com.example.domain.model.ProfileType
import com.example.engine.DiagramGeneratorEngine
import com.example.engine.SmartTagEngine
import com.example.ui.components.FlowchartView
import com.example.ui.components.MindMapView
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite

import com.example.data.local.entity.AttendanceRecordEntity
import com.example.domain.model.UserProfile
import com.example.ui.screens.attendance.AttendanceTrackerScreen

@Composable
fun WorkspaceScreen(
    currentProfile: ProfileType,
    tasks: List<TaskEntity>,
    notes: List<NoteEntity>,
    onSaveTask: (TaskEntity) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onSaveNote: (NoteEntity) -> Unit,
    onDeleteNote: (Long) -> Unit,
    attendanceRecords: List<AttendanceRecordEntity> = emptyList(),
    selectedAttendanceDate: String = "",
    selectedAttendanceGroup: String = "",
    onAttendanceDateChange: (String) -> Unit = {},
    onAttendanceGroupChange: (String) -> Unit = {},
    onCycleAttendanceStatus: (AttendanceRecordEntity) -> Unit = {},
    onSetAttendanceStatus: (AttendanceRecordEntity, String) -> Unit = { _, _ -> },
    onUpdateAttendanceNotes: (AttendanceRecordEntity, String) -> Unit = { _, _ -> },
    onMarkAllAttendance: (String) -> Unit = {},
    onAddStudentAttendance: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onDeleteStudentAttendance: (Long, String) -> Unit = { _, _ -> },
    onExportAttendanceReport: () -> String = { "" },
    currentUser: UserProfile? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Tareas, 1: Notas & Diagramas, 2: Registro Docente
    var selectedPriorityFilter by remember { mutableStateOf<String?>("TODAS") }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    // Note viewing state for interactive diagrams
    var activeMindMap by remember { mutableStateOf<Pair<String, MindMapNode>?>(null) }
    var activeFlowchart by remember { mutableStateOf<Pair<String, List<String>>?>(null) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab row: Tasks vs Notes vs Teacher Attendance
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = OnyxRedPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Tareas (${tasks.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Notas (${notes.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Docente (${attendanceRecords.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (selectedTab == 0) {
            // Priority Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TODAS", "URGENTE", "ALTA", "MEDIA", "BAJA").forEach { prio ->
                    FilterChip(
                        selected = selectedPriorityFilter == prio,
                        onClick = { selectedPriorityFilter = prio },
                        label = { Text(prio, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OnyxRedPrimary,
                            selectedLabelColor = OnyxWhite
                        )
                    )
                }
            }

            // Task List
            val filteredTasks = tasks.filter {
                if (selectedPriorityFilter == "TODAS" || selectedPriorityFilter == null) true
                else it.priority.equals(selectedPriorityFilter, ignoreCase = true)
            }

            Box(modifier = Modifier.weight(1f)) {
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = OnyxRedPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin tareas pendientes en este filtro",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskItemCard(
                                task = task,
                                onToggle = { onToggleTask(task) },
                                onDelete = { onDeleteTask(task.id) }
                            )
                        }
                    }
                }

                // Add Task FAB
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = OnyxRedPrimary,
                    contentColor = OnyxWhite,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Tarea")
                }
            }
        } else if (selectedTab == 1) {
            // Notes & Diagrams View
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Active Mind Map Viewer if selected
                    activeMindMap?.let { (noteTitle, mapNode) ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Mapa Mental: $noteTitle",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = OnyxRedPrimary
                                        )
                                        IconButton(onClick = { activeMindMap = null }) {
                                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MindMapView(rootNode = mapNode)
                                }
                            }
                        }
                    }

                    // Active Flowchart Viewer if selected
                    activeFlowchart?.let { (noteTitle, steps) ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Diagrama de Flujo: $noteTitle",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = OnyxRedPrimary
                                        )
                                        IconButton(onClick = { activeFlowchart = null }) {
                                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowchartView(steps = steps)
                                }
                            }
                        }
                    }

                    items(notes, key = { it.id }) { note ->
                        NoteItemCard(
                            note = note,
                            onGenerateMindMap = {
                                val map = DiagramGeneratorEngine.generateMindMapFromText(note.title, note.content)
                                activeMindMap = note.title to map
                            },
                            onGenerateFlowchart = {
                                val steps = DiagramGeneratorEngine.generateFlowchartSteps(note.content)
                                activeFlowchart = note.title to steps
                            },
                            onExport = {
                                val exportText = buildString {
                                    appendLine("# ${note.title}")
                                    appendLine("Etiquetas: ${note.tags}")
                                    appendLine()
                                    appendLine(note.content)
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, exportText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Exportar Nota a Google Drive / Notion"))
                            },
                            onDelete = { onDeleteNote(note.id) }
                        )
                    }
                }

                // Add Note FAB
                FloatingActionButton(
                    onClick = { showAddNoteDialog = true },
                    containerColor = OnyxRedPrimary,
                    contentColor = OnyxWhite,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = "Nueva Nota")
                }
            }
        } else if (selectedTab == 2) {
            // Teacher Attendance & Absence Register (Docentes)
            AttendanceTrackerScreen(
                records = attendanceRecords,
                selectedDate = selectedAttendanceDate,
                selectedGroup = selectedAttendanceGroup,
                onDateChange = onAttendanceDateChange,
                onGroupChange = onAttendanceGroupChange,
                onCycleStatus = onCycleAttendanceStatus,
                onSetStatus = onSetAttendanceStatus,
                onUpdateNotes = onUpdateAttendanceNotes,
                onMarkAll = onMarkAllAttendance,
                onAddStudent = onAddStudentAttendance,
                onDeleteStudent = onDeleteStudentAttendance,
                onExportReport = onExportAttendanceReport,
                currentUser = currentUser,
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("ALTA") }
        var dueDate by remember { mutableStateOf("Hoy") }

        val suggestedTags = remember(title, description) {
            SmartTagEngine.suggestTags("$title $description")
        }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Nueva Tarea Prioritaria", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la tarea") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción / Parámetros") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("URGENTE", "ALTA", "MEDIA", "BAJA").forEach { prio ->
                            FilterChip(
                                selected = priority == prio,
                                onClick = { priority = prio },
                                label = { Text(prio, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OnyxRedPrimary, selectedLabelColor = OnyxWhite)
                            )
                        }
                    }

                    Text("Etiquetas automáticas sugeridas:", style = MaterialTheme.typography.labelSmall, color = OnyxRedPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        suggestedTags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text("#$tag", fontSize = 10.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSaveTask(
                                TaskEntity(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    dueDate = dueDate,
                                    tags = suggestedTags.joinToString(", "),
                                    profile = currentProfile.name
                                )
                            )
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Guardar Tarea", color = OnyxWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }

        val suggestedTags = remember(noteTitle, noteContent) {
            SmartTagEngine.suggestTags("$noteTitle $noteContent")
        }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Crear Documento / Nota Notion", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Título del documento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Contenido (Markdown / Bloques)") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 8
                    )
                    Text("Etiquetas automáticas sugeridas por IA:", style = MaterialTheme.typography.labelSmall, color = OnyxRedPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        suggestedTags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text("#$tag", fontSize = 10.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            onSaveNote(
                                NoteEntity(
                                    title = noteTitle.trim(),
                                    content = noteContent.trim(),
                                    tags = suggestedTags.joinToString(", "),
                                    profile = currentProfile.name
                                )
                            )
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Guardar Nota", color = OnyxWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = OnyxRedPrimary)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val prioColor = when (task.priority) {
                        "URGENTE" -> Color(0xFFFF1744)
                        "ALTA" -> Color(0xFFFF5252)
                        "MEDIA" -> Color(0xFFFF9100)
                        else -> Color(0xFF00E676)
                    }

                    Surface(
                        color = prioColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = task.priority,
                            color = prioColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (task.dueDate.isNotBlank()) {
                        Text(
                            text = task.dueDate,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun NoteItemCard(
    note: NoteEntity,
    onGenerateMindMap: () -> Unit,
    onGenerateFlowchart: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tags row
            if (note.tags.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    note.tags.split(",").forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#${tag.trim()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = OnyxRedPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Buttons: Mind Map, Flowchart, Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onGenerateMindMap,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = OnyxRedPrimary.copy(alpha = 0.12f),
                        contentColor = OnyxRedPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mapa Mental", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onGenerateFlowchart,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flujograma", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onExport,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Exportar", tint = OnyxRedPrimary)
                }
            }
        }
    }
}
