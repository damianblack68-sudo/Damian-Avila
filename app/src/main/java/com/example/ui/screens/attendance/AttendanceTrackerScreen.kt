package com.example.ui.screens.attendance

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AttendanceRecordEntity
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxRedVibrant
import com.example.ui.theme.OnyxWhite
import java.text.SimpleDateFormat
import java.util.*

/**
 * Interfaz de registro de faltas para docentes con indicadores visuales de color:
 * - 🟢 Verde: Asistió (Presente)
 * - 🟡 Amarillo: No llegó (Retardo / En camino)
 * - 🔴 Rojo: Faltó (Inasistencia)
 * Vinculada directamente al perfil del usuario docente y a los perfiles de los estudiantes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTrackerScreen(
    records: List<AttendanceRecordEntity>,
    selectedDate: String,
    selectedGroup: String,
    onDateChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onCycleStatus: (AttendanceRecordEntity) -> Unit,
    onSetStatus: (AttendanceRecordEntity, String) -> Unit = { r, s -> },
    onUpdateNotes: (AttendanceRecordEntity, String) -> Unit = { _, _ -> },
    onMarkAll: (String) -> Unit,
    onAddStudent: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onDeleteStudent: (Long, String) -> Unit,
    onExportReport: () -> String,
    currentUser: UserProfile? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }
    var selectedRecordForNotes by remember { mutableStateOf<AttendanceRecordEntity?>(null) }
    var notesText by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("TODOS") } // TODOS, ASISTIO, RETARDO, FALTO

    val defaultGroups = listOf("Matemáticas 3°A", "Programación e IA", "Física II", "Cálculo Integral")

    // Stats calculations
    val total = records.size
    val present = records.count { it.status == "ASISTIO" }
    val late = records.count { it.status == "RETARDO" }
    val absent = records.count { it.status == "FALTO" }
    val percentage = if (total > 0) ((present.toFloat() + (late * 0.5f)) / total) * 100f else 0f

    // Filtered list
    val filteredRecords = records.filter { record ->
        val matchesSearch = searchQuery.isBlank() ||
                record.studentName.contains(searchQuery, ignoreCase = true) ||
                record.studentUsername.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilterStatus) {
            "ASISTIO" -> record.status == "ASISTIO"
            "RETARDO" -> record.status == "RETARDO"
            "FALTO" -> record.status == "FALTO"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        // --- 1. CABECERA: VINCULACIÓN AL PERFIL DEL USUARIO DOCENTE ---
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(OnyxRedVibrant, OnyxRedPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.displayName?.take(1) ?: "D").uppercase(),
                                fontWeight = FontWeight.Black,
                                color = OnyxWhite,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.displayName ?: "Damian Ávila",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verificado",
                                    tint = OnyxRedPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "Docente Titular • @${currentUser?.username ?: "damian_docente"} • ${currentUser?.email ?: "damianblack68@gmail.com"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Share/Export Button
                    FilledTonalButton(
                        onClick = {
                            val report = onExportReport()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, report)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Exportar Registro de Faltas"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Group & Date Selectors Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Group Selector Dropdown
                    var groupDropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedButton(
                            onClick = { groupDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnyxRedPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedGroup,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = groupDropdownExpanded,
                            onDismissRequest = { groupDropdownExpanded = false }
                        ) {
                            defaultGroups.forEach { grp ->
                                DropdownMenuItem(
                                    text = { Text(grp) },
                                    onClick = {
                                        onGroupChange(grp)
                                        groupDropdownExpanded = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("+ Crear Nuevo Grupo...", color = OnyxRedPrimary, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = OnyxRedPrimary) },
                                onClick = {
                                    groupDropdownExpanded = false
                                    showNewGroupDialog = true
                                }
                            )
                        }
                    }

                    // Date Navigator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val d = sdf.parse(selectedDate) ?: Date()
                                    val cal = Calendar.getInstance().apply { time = d; add(Calendar.DAY_OF_YEAR, -1) }
                                    onDateChange(sdf.format(cal.time))
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior", modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = selectedDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = {
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val d = sdf.parse(selectedDate) ?: Date()
                                    val cal = Calendar.getInstance().apply { time = d; add(Calendar.DAY_OF_YEAR, 1) }
                                    onDateChange(sdf.format(cal.time))
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. BARRA DE DISTRIBUCIÓN VISUAL MULTICOLOR ---
        if (total > 0) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    if (present > 0) {
                        Box(
                            modifier = Modifier
                                .weight(present.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF00E676)) // 🟢 Verde Asistió
                        )
                    }
                    if (late > 0) {
                        Box(
                            modifier = Modifier
                                .weight(late.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFFFFD600)) // 🟡 Amarillo No llegó
                        )
                    }
                    if (absent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(absent.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFFFF1744)) // 🔴 Rojo Faltó
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- 3. INDICADORES VISUALES DE COLOR (KPI CARDS) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🟢 ASISTIÓ (Verde)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0B2414),
                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedFilterStatus = if (selectedFilterStatus == "ASISTIO") "TODOS" else "ASISTIO" }
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Asistió", color = Color(0xFF00E676), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("$present", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
            }

            // 🟡 NO LLEGÓ / RETARDO (Amarillo)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF282008),
                border = BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedFilterStatus = if (selectedFilterStatus == "RETARDO") "TODOS" else "RETARDO" }
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD600))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("No llegó", color = Color(0xFFFFD600), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("$late", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
            }

            // 🔴 FALTÓ (Rojo)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2B0A0E),
                border = BorderStroke(1.dp, Color(0xFFFF1744).copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedFilterStatus = if (selectedFilterStatus == "FALTO") "TODOS" else "FALTO" }
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF1744))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Faltó", color = Color(0xFFFF1744), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("$absent", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
            }

            // % Cumplimiento
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Efectividad", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${percentage.toInt()}%", color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. ACCIONES RÁPIDAS & BÚSQUEDA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    onMarkAll("ASISTIO")
                    Toast.makeText(context, "Todos los alumnos marcados con Asistencia 🟢", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Todos Asistieron", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp), tint = OnyxRedPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Vincular Alumno", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnyxRedPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar & Filter chips
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar estudiante por nombre o @usuario...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OnyxRedPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Triple("TODOS", "Todos ($total)", Color.Gray),
                Triple("ASISTIO", "🟢 Asistió ($present)", Color(0xFF00E676)),
                Triple("RETARDO", "🟡 No llegó ($late)", Color(0xFFFFD600)),
                Triple("FALTO", "🔴 Faltó ($absent)", Color(0xFFFF1744))
            ).forEach { (statusKey, label, color) ->
                FilterChip(
                    selected = selectedFilterStatus == statusKey,
                    onClick = { selectedFilterStatus = statusKey },
                    label = { Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.2f),
                        selectedLabelColor = if (statusKey == "TODOS") MaterialTheme.colorScheme.onSurface else color
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedFilterStatus == statusKey) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 5. LISTA NOMINAL DE ESTUDIANTES VINCULADOS ---
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay alumnos que coincidan con el filtro.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    StudentAttendanceItem(
                        record = record,
                        onSetStatus = { newStatus -> onSetStatus(record, newStatus) },
                        onCycle = { onCycleStatus(record) },
                        onOpenNotes = {
                            selectedRecordForNotes = record
                            notesText = record.notes
                        },
                        onDelete = { onDeleteStudent(record.id, record.studentName) }
                    )
                }
            }
        }
    }

    // --- DIÁLOGO: VINCULAR NUEVO ALUMNO A PERFIL DE USUARIO ---
    if (showAddDialog) {
        var studentNameInput by remember { mutableStateOf("") }
        var studentUsernameInput by remember { mutableStateOf("") }
        var studentEmailInput by remember { mutableStateOf("") }
        var studentNotesInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = OnyxRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vincular Alumno a Perfil", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Registrar estudiante en el grupo '$selectedGroup' vinculado a su perfil de usuario en Onyx.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = studentNameInput,
                        onValueChange = { studentNameInput = it },
                        label = { Text("Nombre completo del estudiante") },
                        placeholder = { Text("ej. Carlos Daniel Mendoza") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = studentUsernameInput,
                        onValueChange = { studentUsernameInput = it },
                        label = { Text("Usuario en Onyx (@usuario)") },
                        placeholder = { Text("ej. carlos_ia") },
                        leadingIcon = { Text("@", fontWeight = FontWeight.Bold, color = OnyxRedPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = studentEmailInput,
                        onValueChange = { studentEmailInput = it },
                        label = { Text("Correo institucional o del tutor") },
                        placeholder = { Text("ej. carlos@estudiante.mx") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = studentNotesInput,
                        onValueChange = { studentNotesInput = it },
                        label = { Text("Notas iniciales / Matrícula") },
                        placeholder = { Text("ej. Matrícula MAT-2026-08") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentNameInput.isNotBlank()) {
                            onAddStudent(
                                studentNameInput.trim(),
                                studentUsernameInput.trim().removePrefix("@"),
                                studentEmailInput.trim(),
                                studentNotesInput.trim()
                            )
                            showAddDialog = false
                            Toast.makeText(context, "Estudiante vinculado con éxito al perfil", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Vincular & Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- DIÁLOGO: CREAR NUEVO GRUPO DOCENTE ---
    if (showNewGroupDialog) {
        var newGroupNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewGroupDialog = false },
            title = { Text("Crear Nuevo Grupo Docente") },
            text = {
                OutlinedTextField(
                    value = newGroupNameInput,
                    onValueChange = { newGroupNameInput = it },
                    label = { Text("Nombre de la materia y grupo") },
                    placeholder = { Text("ej. Robótica 1°A") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupNameInput.isNotBlank()) {
                            onGroupChange(newGroupNameInput.trim())
                            showNewGroupDialog = false
                            Toast.makeText(context, "Grupo activado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGroupDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- DIÁLOGO: EDITAR JUSTIFICANTE U OBSERVACIÓN ---
    selectedRecordForNotes?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRecordForNotes = null },
            title = { Text("Observaciones / Justificante") },
            text = {
                Column {
                    Text("Estudiante: ${record.studentName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Estado actual: ${record.status}", fontSize = 11.sp, color = OnyxRedPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Motivo / Justificante médico") },
                        placeholder = { Text("ej. Presentó receta médica por gripe") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateNotes(record, notesText.trim())
                        selectedRecordForNotes = null
                        Toast.makeText(context, "Observación guardada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRecordForNotes = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

/**
 * Ficha individual de estudiante con aro visual indicador de color,
 * vinculación al perfil de usuario y selector rápido de 3 colores (Verde, Amarillo, Rojo).
 */
@Composable
fun StudentAttendanceItem(
    record: AttendanceRecordEntity,
    onSetStatus: (String) -> Unit,
    onCycle: () -> Unit,
    onOpenNotes: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (record.status) {
            "ASISTIO" -> Color(0xFF00E676)
            "RETARDO" -> Color(0xFFFFD600)
            else -> Color(0xFFFF1744)
        },
        label = "statusBorderColor"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Avatar with dynamic color ring + Student info & linked profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(2.dp, statusColor, CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = record.studentName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = record.studentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val userTag = if (record.studentUsername.isNotBlank()) "@${record.studentUsername}" else "Perfil Alumno"
                            Text(
                                text = userTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnyxRedPrimary
                            )
                            if (record.studentEmail.isNotBlank()) {
                                Text(
                                    text = " • ${record.studentEmail}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar estudiante",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Optional note/justification badge if present
            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10192B),
                    border = BorderStroke(1.dp, Color(0xFF1E3A8A).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNotes() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = record.notes,
                            fontSize = 10.sp,
                            color = Color(0xFF93C5FD),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- SELECTOR DE ESTADO CON INDICADORES VISUALES DE 3 COLORES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🟢 ASISTIÓ
                val isAsistio = record.status == "ASISTIO"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAsistio) Color(0xFF00E676) else Color(0xFF0A1F13),
                    border = BorderStroke(1.dp, if (isAsistio) Color(0xFF00E676) else Color(0xFF00E676).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetStatus("ASISTIO") }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAsistio) "✓ Asistió" else "🟢 Asistió",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAsistio) Color.Black else Color(0xFF00E676)
                        )
                    }
                }

                // 🟡 NO LLEGÓ / RETARDO
                val isRetardo = record.status == "RETARDO"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isRetardo) Color(0xFFFFD600) else Color(0xFF261D07),
                    border = BorderStroke(1.dp, if (isRetardo) Color(0xFFFFD600) else Color(0xFFFFD600).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetStatus("RETARDO") }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRetardo) "⏱ No llegó" else "🟡 No llegó",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRetardo) Color.Black else Color(0xFFFFD600)
                        )
                    }
                }

                // 🔴 FALTÓ
                val isFalto = record.status == "FALTO"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFalto) Color(0xFFFF1744) else Color(0xFF24080B),
                    border = BorderStroke(1.dp, if (isFalto) Color(0xFFFF1744) else Color(0xFFFF1744).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetStatus("FALTO") }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFalto) "✕ Faltó" else "🔴 Faltó",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFalto) Color.White else Color(0xFFFF1744)
                        )
                    }
                }

                // Botón Nota / Justificante
                IconButton(
                    onClick = onOpenNotes,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Nota o justificante",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
