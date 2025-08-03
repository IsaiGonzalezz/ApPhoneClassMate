package com.example.classmate

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AddTaskScreen(navController: NavController,onTaskAdded: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Selecciona una fecha") }
    var description by remember { mutableStateOf("") }
    var notification by remember { mutableStateOf(false) }
    var reminder by remember { mutableStateOf("Selecciona una fecha") }
    var notes by remember { mutableStateOf("") }

    // Estados de UI
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón de regreso
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Regresar")
        }

        Text(
            text = "Agregar tarea",
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FormTextField("Título", title) { title = it }
        FormDatePicker("Fecha Entrega", dueDate) { dueDate = it }
        FormTextField("Descripción", description) { description = it }
        FormSwitch("Notificación", notification) { notification = it }
        FormDatePicker("Fecha recordatorio", reminder) { reminder = it }
        FormTextField("Notas", notes) { notes = it }

        Spacer(modifier = Modifier.weight(1f))

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "El título es requerido"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                val taskData = hashMapOf(
                    "title" to title,
                    "dueDate" to dueDate,
                    "description" to description,
                    "notification" to notification,
                    "reminder" to reminder,
                    "notes" to notes,
                    "createdAt" to System.currentTimeMillis(),
                    "completed" to false,
                    "progress" to 0
                )

                scope.launch {
                    try {
                        val documentRef = FirebaseFirestore.getInstance()
                            .collection("tasks")
                            .add(taskData)
                            .await()

                        val taskId = documentRef.id
                        documentRef.update("id", taskId).await()

                        // Limpiar formulario
                        title = ""
                        dueDate = "Selecciona una fecha"
                        description = ""
                        notification = false
                        reminder = "Selecciona una fecha"
                        notes = ""

                        showSuccess = true
                        onTaskAdded()
                    } catch (e: Exception) {
                        errorMessage = "Error al guardar: ${e.localizedMessage ?: "Desconocido"}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Agregar")
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text(text = "Éxito") },
            text = { Text(text = "Tarea agregada correctamente") },
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
fun FormTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors( // Cambia esto
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray,
                cursorColor = Color.White
            )
        )
        Divider(thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun FormDatePicker(label: String, value: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val selectedDate = Calendar.getInstance().apply {
                            set(year, month, day)
                        }
                        val formatted = SimpleDateFormat("EEEE dd 'de' MMMM", Locale("es", "MX"))
                            .format(selectedDate.time)
                        onDateSelected(formatted.replaceFirstChar { it.uppercase() })
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
        Divider(thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun FormSwitch(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
    Divider(thickness = 0.5.dp, color = Color.LightGray)
}
