package com.example.classmate

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.classmate.data.models.Task
import com.example.classmate.data.repositories.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.window.Dialog
import com.example.classmate.data.models.Examen
import com.example.classmate.data.models.Nota
import com.example.classmate.data.repositories.ExamenRepository
import com.example.classmate.data.repositories.NotasRepository
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.clickable
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    taskRepository: TaskRepository = remember { TaskRepository() },
    noteRepository: NotasRepository = remember { NotasRepository() },
    examenRepository : ExamenRepository = remember { ExamenRepository() },
    navController : NavController
) {
    val currentTime = remember { mutableStateOf(getFormattedTime()) }
    val tasks = remember { mutableStateOf<List<Task>>(emptyList()) }
    val notes = remember { mutableStateOf<List<Nota>>(emptyList()) }
    val examenes = remember { mutableStateOf<List<Examen>>(emptyList()) }

    val selectedTask = remember { mutableStateOf<Task?>(null) }
    val selectedNote = remember {mutableStateOf<Nota?>(null)}
    val selectedExamen = remember { mutableStateOf<Examen?>(null)}

    // Estado de snackbar de Material3
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    val motivationalMessages = listOf(
        "¡Todo lo que quieres está al otro lado del miedo!",
        "¡Tú puedes con todo hoy! 💪✨",
        "Un pequeño progreso cada día suma grandes resultados",
        "El éxito es la suma de pequeños esfuerzos repetidos día tras día",
        "No te rindas, los comienzos siempre son difíciles",
        "Cree en ti mismo y llegarás más lejos de lo que imaginas",
        "La disciplina es el puente entre tus metas y tus logros",
        "Cada día es una nueva oportunidad para ser mejor",
        "El esfuerzo de hoy es el éxito de mañana",
        "Las grandes cosas nunca vienen de la zona de confort"
    )
    val currentMessage = remember { mutableStateOf(motivationalMessages.random()) }
    LaunchedEffect(Unit) {
        // Actualizar hora cada minuto
        launch {
            while (true) {
                currentTime.value = getFormattedTime()
                delay(60_000)
            }
        }

        // Cargar tareas
        launch {
            try {
                tasks.value = taskRepository.getAllTasksSortedByDueDate()
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Error cargando tareas: ${e.message ?: "Desconocido"}")
                }
            }
        }

        // Cargar examenes
        launch {
            try {
                examenes.value = examenRepository.getAllExamenesSortedByDueDate()
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Error cargando Examenes: ${e.message ?: "Desconocido"}")
                }
            }
        }

        // Cargar Notas
        launch {
            try {
                notes.value = noteRepository.getAllNotesSortedByDueDate()
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Error cargando Notas: ${e.message ?: "Desconocido"}")
                }
            }
        }

        // Actualizar mensaje cada hora
        launch {
            while (true) {
                delay(3_600_000) // 1 hora en milisegundos
                currentMessage.value = motivationalMessages.random()
            }
        }
    }

    if (selectedTask.value != null) {
        TaskDetailDialog(
            task = selectedTask.value!!,
            onDismiss = { selectedTask.value = null },
            onTaskUpdated = { updatedTask ->
                tasks.value = tasks.value.map {
                    if (it.id == updatedTask.id) updatedTask else it
                }
            },
            onTaskDeleted = { taskId ->
                tasks.value = tasks.value.filter { it.id != taskId }
            }
        )
    }

    if (selectedNote.value != null){
        NoteDetailDialog(
            note = selectedNote.value!!,
            onDismiss = { selectedNote.value = null },
            onNoteUpdated = { updatedNote ->
                notes.value = notes.value.map{
                    if (it.id == updatedNote.id) updatedNote else it
                }
            },
            onNoteDeleted = { noteId ->
                notes.value = notes.value.filter { it.id != noteId }
            }
        )
    }

    if (selectedExamen.value != null){
        ExamenDetailDialog(
            examen = selectedExamen.value!!,
            onDismiss = { selectedExamen.value = null },
            onExamenUpdated = { updatedExamen ->
                examenes.value = examenes.value.map{
                    if (it.id == updatedExamen.id) updatedExamen else it
                }
            },
            onExamenDeleted = { examenId ->
                examenes.value = examenes.value.filter { it.id != examenId }
            }
        )
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x121212))
        ) {
            //Usa LazyColumn en lugar de Column
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { HeaderSection(currentTime.value) }

                item {
                    Section(
                        title = "📕Tareas",
                        items = tasks.value,
                        onItemClick = { task -> selectedTask.value = task },
                        navController = navController
                    )
                }

                item {
                    Section(
                        title = "📑Exámenes",
                        items = examenes.value,
                        onItemClick = { examen -> selectedExamen.value = examen },
                        navController = navController
                    )
                }

                item {
                    Section(
                        title = "🗒️Notas",
                        items = notes.value,
                        onItemClick = { note -> selectedNote.value = note },
                        navController = navController
                    )
                }

                item {
                    MotivationalMessage(
                        message = currentMessage.value,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamenDetailDialog(
    examen: Examen,
    onDismiss: () -> Unit,
    onExamenUpdated: (Examen) -> Unit,
    onExamenDeleted: (String) -> Unit,
    examenRepository: ExamenRepository = remember { ExamenRepository() }
) {
    val coroutineScope = rememberCoroutineScope()
    var editableExamen by remember { mutableStateOf(examen) }
    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mostrar snackbar si hay mensajes
                SnackbarHost(hostState = snackbarHostState)

                if (isEditing) {
                    // Formulario de edición
                    OutlinedTextField(
                        value = editableExamen.materia,
                        onValueChange = { editableExamen = editableExamen.copy(materia = it) },
                        label = { Text("Materia") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editableExamen.temas,
                        onValueChange = { editableExamen = editableExamen.copy(temas = it) },
                        label = { Text("Temas") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        examenRepository.updateExamen(editableExamen)
                                        onExamenUpdated(editableExamen)
                                        isEditing = false
                                        onDismiss()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error al guardar: ${e.message ?: "Desconocido"}")
                                    }
                                }
                            }
                        ) {
                            Text("Guardar")
                        }

                        Button(
                            onClick = { isEditing = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                } else {
                    // Vista de solo lectura
                    Text(
                        text = examen.materia,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (examen.temas.isNotBlank()) {
                        Text(
                            text = examen.temas,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "Fecha: ${examen.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        examenRepository.deleteExamen(examen.id)
                                        onExamenDeleted(examen.id)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            "Error al eliminar: ${e.message ?: "Error desconocido"}"
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("Eliminar")
                        }

                        Button(
                            onClick = { isEditing = true }
                        ) {
                            Text("Editar")
                        }

                        Button(
                            onClick = onDismiss
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailDialog(
    note: Nota,
    onDismiss: () -> Unit,
    onNoteUpdated: (Nota) -> Unit,
    onNoteDeleted: (String) -> Unit,
    noteRepository: NotasRepository = remember { NotasRepository() }
) {
    val coroutineScope = rememberCoroutineScope()
    var editableNote by remember { mutableStateOf(note) }
    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mostrar snackbar si hay mensajes
                SnackbarHost(hostState = snackbarHostState)

                if (isEditing) {
                    // Formulario de edición
                    OutlinedTextField(
                        value = editableNote.title,
                        onValueChange = { editableNote = editableNote.copy(title = it) },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editableNote.description,
                        onValueChange = { editableNote = editableNote.copy(description = it) },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        noteRepository.updateNote(editableNote)
                                        onNoteUpdated(editableNote)
                                        isEditing = false
                                        onDismiss()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error al guardar: ${e.message ?: "Desconocido"}")
                                    }
                                }
                            }
                        ) {
                            Text("Guardar")
                        }

                        Button(
                            onClick = { isEditing = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                } else {
                    // Vista de solo lectura
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (note.description.isNotBlank()) {
                        Text(
                            text = note.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "Fecha: ${note.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        noteRepository.deleteNote(note.id)
                                        onNoteDeleted(note.id)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            "Error al eliminar: ${e.message ?: "Error desconocido"}"
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("Eliminar")
                        }

                        Button(
                            onClick = { isEditing = true }
                        ) {
                            Text("Editar")
                        }

                        Button(
                            onClick = onDismiss
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    task: Task,
    onDismiss: () -> Unit,
    onTaskUpdated: (Task) -> Unit,
    onTaskDeleted: (String) -> Unit,
    taskRepository: TaskRepository = remember { TaskRepository() }
) {
    val coroutineScope = rememberCoroutineScope()
    var editableTask by remember { mutableStateOf(task) }
    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mostrar snackbar si hay mensajes
                SnackbarHost(hostState = snackbarHostState)

                if (isEditing) {
                    // Formulario de edición
                    OutlinedTextField(
                        value = editableTask.title,
                        onValueChange = { editableTask = editableTask.copy(title = it) },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editableTask.description,
                        onValueChange = { editableTask = editableTask.copy(description = it) },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    var showDatePicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState()

                    OutlinedTextField(
                        value = editableTask.dueDate,
                        onValueChange = {}, // lo dejamos vacío para que no se edite manualmente
                        label = { Text("Fecha de entrega") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    )
                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val millis = datePickerState.selectedDateMillis
                                    val zonedDateTime = Instant.ofEpochMilli(millis!!).atZone(ZoneId.systemDefault())
                                    val formatted = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale("es", "MX"))
                                        .format(zonedDateTime)
                                        .replaceFirstChar { it.uppercase() } // Para poner "Domingo" con mayúscula

                                    editableTask = editableTask.copy(dueDate = formatted)
                                    showDatePicker = false
                                }) {
                                    Text("Aceptar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancelar")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // Selector de progreso
                    Column {
                        Text(
                            text = "Progreso: ${editableTask.progress}%",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Slider(
                            value = editableTask.progress.toFloat(),
                            onValueChange = { editableTask = editableTask.copy(progress = it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0%", style = MaterialTheme.typography.labelSmall)
                            Text("100%", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        taskRepository.updateTask(editableTask)
                                        onTaskUpdated(editableTask)
                                        isEditing = false
                                        onDismiss()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error al guardar: ${e.message ?: "Desconocido"}")
                                    }
                                }
                            }
                        ) {
                            Text("Guardar")
                        }

                        Button(
                            onClick = { isEditing = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                } else {
                    // Vista de solo lectura
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Detalles de progreso
                    Column {
                        Text(
                            text = "Progreso: ${task.progress}%",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LinearProgressIndicator(
                            progress = task.progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Text(
                        text = "Fecha de entrega: ${task.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        taskRepository.deleteTask(task.id)
                                        onTaskDeleted(task.id)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            "Error al eliminar: ${e.message ?: "Error desconocido"}"
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("Completada")
                        }

                        Button(
                            onClick = { isEditing = true }
                        ) {
                            Text("Editar")
                        }

                        Button(
                            onClick = onDismiss
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> Section(
    title: String,
    items: List<T>,
    onItemClick: (T) -> Unit = {},
    navController: NavController // Añade el navController como parámetro
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            // Botón + pequeño
            IconButton(
                onClick = {
                    // Navega a la pantalla correspondiente según el título
                    when (title) {
                        "📕Tareas" -> navController.navigate(Screen.AddTask.route)
                        "📑Exámenes" -> navController.navigate(Screen.AddExamen.route)
                        "🗒️Notas" -> navController.navigate(Screen.AddNote.route)
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Agregar $title",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                when (item) {
                    is Task -> TaskCard(task = item, onClick = { onItemClick(item) })
                    is Nota -> NoteCard(note = item, onClick = { onItemClick(item)})
                    is Examen -> ExamenCard(examen = item, onClick = { onItemClick(item) })
                    is String -> SimpleCard(text = item, onClick = { onItemClick(item) })
                    else -> SimpleCard(text = item.toString(), onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(170.dp)
            .height(135.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7F76F9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                if (task.dueDate.isNotBlank()) {
                    Text(
                        text = "Entrega: ${task.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Column {
                Text(
                    text = "${task.progress}% completado",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LinearProgressIndicator(
                    progress = task.progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFFFF0048),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: Nota, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(170.dp)
            .height(110.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEC260)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4D4D4B)
                )
                if (note.dueDate.isNotBlank()) {
                    Text(
                        text = "Fecha: ${note.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B6B59)
                    )
                }
            }
        }
    }
}

@Composable
fun ExamenCard(examen: Examen, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(170.dp)
            .height(90.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6AAD63)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = examen.materia,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                if (examen.dueDate.isNotBlank()) {
                    Text(
                        text = "Se aplica el: ${examen.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}


@Composable
fun SimpleCard(text: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(170.dp)
            .height(130.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF48839F)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun HeaderSection(datetime: String) {
    Text(
        text = datetime,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}

@Composable
fun Section(title: String, items: List<String>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(100.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0064CE)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(text = item)
                    }
                }
            }
        }
    }
}

@Composable
fun MotivationalMessage(message: String, modifier: Modifier) {
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF6B6B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(24.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

fun getFormattedTime(): String {
    val sdf = SimpleDateFormat("EEEE dd 'de' MMMM - HH:mm", Locale("es", "MX"))
    return sdf.format(Date()).replaceFirstChar { it.uppercase() }
}

// FUNCIÓN PARA PARSEAR FECHAS A UN FORMATO COMPARABLE:
fun parseCustomDate(dateString: String): Date? {
    return try {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateWithYear = "$dateString de $currentYear"
        val fullFormat = SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy", Locale("es", "MX"))
        fullFormat.parse(dateWithYear)
    } catch (e: Exception) {
        null
    }
}
