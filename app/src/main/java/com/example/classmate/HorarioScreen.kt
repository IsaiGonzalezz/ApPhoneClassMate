package com.example.classmate

import android.R
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color
import org.w3c.dom.Text
import androidx.compose.runtime.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HorarioScreen(navController: NavController, onClaseAdded: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val app by lazy { context.applicationContext as ClassmateApp }
    var systems by remember { mutableStateOf(listOf<ClassMate>()) }
    var idFr by remember { mutableStateOf("") }


    // Estados
    var materia by remember { mutableStateOf("") }
    var profesor by remember { mutableStateOf("") }
    var diaSeleccionado by remember { mutableStateOf("") }

    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes","Sábado","Domingo")
    var expanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    var horaInicio by remember { mutableStateOf("06") }
    var minutoInicio by remember { mutableStateOf("30") }

    var horaFin by remember { mutableStateOf("07") }
    var minutoFin by remember { mutableStateOf("30") }


    LaunchedEffect(Unit) {
        systems = withContext(Dispatchers.IO) {
            app.room.classmateDao().getAll()
        }
        idFr = systems.firstOrNull()?.id_Fr ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Regresar")
        }

        Text(
            text = "Registrar Clase",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 22.sp
        )

        Text(text = "Día")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp)) // Borde simulado
                .padding(16.dp) // Espacio interno como un TextField real
        ) {
            Text(
                text = diaSeleccionado.ifEmpty { "Selecciona un día" },
                color = if (diaSeleccionado.isEmpty()) Color.Gray else Color.White,
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                diasSemana.forEach { dia ->
                    DropdownMenuItem(
                        text = { Text(dia) },
                        onClick = {
                            diaSeleccionado = dia
                            expanded = false
                        }
                    )
                }
            }
        }

        FormTextField("Materia", materia) { materia = it }
        FormTextField("Profesor", profesor) { profesor = it }

        SelectorHora(
            label = "Hora Inicio",
            horaSeleccionada = horaInicio,
            minutoSeleccionado = minutoInicio,
            onHoraChange = { horaInicio = it },
            onMinutoChange = { minutoInicio = it }
        )

        SelectorHora(
            label = "Hora Fin",
            horaSeleccionada = horaFin,
            minutoSeleccionado = minutoFin,
            onHoraChange = { horaFin = it },
            onMinutoChange = { minutoFin = it }
        )

        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (materia.isBlank() || horaInicio.isBlank() || horaFin.isBlank() || diaSeleccionado.isBlank()) {
                    errorMessage = "Por favor completa todos los campos"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                val clase = mapOf(
                    "materia" to materia,
                    "profesor" to profesor,
                    "hora_inicio" to "$horaInicio:$minutoInicio",
                    "hora_fin" to "$horaFin:$minutoFin"
                )

                // 1. Primero obtén el id_Fr (como ya lo tienes en tus sistemas)
                val idFr = systems.firstOrNull()?.id_Fr ?: ""

                // 2. Estructura la referencia con el id_Fr como documento padre
                val docRef = FirebaseFirestore.getInstance()
                    .collection("systems")       // Colección raíz
                    .document(idFr)             // Documento del sistema específico
                    .collection("horarios")     // Subcolección de horarios
                    .document(diaSeleccionado)  // Documento del día específico

                scope.launch {
                    try {
                        docRef.update("clases", FieldValue.arrayUnion(clase)).await()
                    } catch (e: FirebaseFirestoreException) {
                        if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                            docRef.set(mapOf("clases" to listOf(clase))).await()
                        } else {
                            errorMessage = "Error al guardar: ${e.message ?: "Desconocido"}"
                        }
                    } finally {
                        isLoading = false
                        materia = ""
                        profesor = ""
                        horaInicio = "06"
                        minutoInicio = "30"
                        horaFin = "07"
                        minutoFin = "30"
                        showSuccess = true
                        onClaseAdded()
                    }
                }

            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Registrar Clase")
            }
        }
    }
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Éxito") },
            text = { Text("Clase registrada correctamente") },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) {
                    Text("Aceptar")
                }
            },
            containerColor = Color(0xFF2C2C2C)
        )
    }
}



@Composable
fun SelectorHora(
    label: String,
    horaSeleccionada: String,
    minutoSeleccionado: String,
    onHoraChange: (String) -> Unit,
    onMinutoChange: (String) -> Unit
) {
    val horas = (0..23).map { it.toString().padStart(2, '0') }
    val minutos = (0..59).map { it.toString().padStart(2, '0') }

    var expandHora by remember { mutableStateOf(false) }
    var expandMinuto by remember { mutableStateOf(false) }

    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Selector de Hora - Versión Custom
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { expandHora = true }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = horaSeleccionada.ifEmpty { "HH" },
                        color = if (horaSeleccionada.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = expandHora,
                    onDismissRequest = { expandHora = false }
                ) {
                    horas.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                onHoraChange(it)
                                expandHora = false
                            }
                        )
                    }
                }
            }

            Text(" : ", Modifier.padding(horizontal = 4.dp))

            // Selector de Minuto - Versión Custom
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { expandMinuto = true }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = minutoSeleccionado.ifEmpty { "MM" },
                        color = if (minutoSeleccionado.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = expandMinuto,
                    onDismissRequest = { expandMinuto = false }
                ) {
                    minutos.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                onMinutoChange(it)
                                expandMinuto = false
                            }
                        )
                    }
                }
            }
        }
    }
}