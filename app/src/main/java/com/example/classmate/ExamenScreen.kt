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
import androidx.compose.material3.LocalTextStyle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter



@Composable
fun AddExamenScreen(navController: NavController ,onExamenAdded: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var materia by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Selecciona una fecha") }
    var temas by remember { mutableStateOf("") }
    var notification by remember { mutableStateOf(false) }
    var reminder by remember { mutableStateOf("Selecciona una fecha") }
    val app by lazy { context.applicationContext as ClassmateApp }
    var systems by remember { mutableStateOf(listOf<ClassMate>()) }
    var idFr by remember { mutableStateOf("") }

    // Estados de UI
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        systems = withContext(Dispatchers.IO) {
            app.room.classmateDao().getAll()
        }
        idFr = systems.firstOrNull()?.id_Fr ?: ""
    }

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
            text = "Agregar Examen",
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FormTextField("Materia", materia) { materia = it }
        FormDatePicker("Fecha Entrega", dueDate) { dueDate = it }
        FormTextField("Temas", temas) { temas = it }
        FormSwitch("Notificación", notification) { notification = it }
        FormDatePicker("Fecha recordatorio", reminder) { reminder = it }

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
                if (materia.isBlank()) {
                    errorMessage = "La meteria es requerida"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                val taskData = hashMapOf(
                    "materia" to materia,
                    "dueDate" to dueDate,
                    "temas" to temas,
                    "notification" to notification,
                    "reminder" to reminder
                )

                scope.launch {
                    try {
                        // 1. Verificar que tenemos un idFr válido
                        if (idFr.isBlank()) {
                            errorMessage = "No se encontró ID de sistema ${idFr}"
                            return@launch
                        }

                        // 2. Crear referencia directa al documento padre
                        val parentDocRef = FirebaseFirestore.getInstance()
                            .collection("systems")  // 👈 Colección raíz
                            .document(idFr)         // 👈 Documento con tu ID

                        // 3. Crear la nota en la subcolección
                        val examenRef = parentDocRef
                            .collection("examenes")    // 👈 Subcolección
                            .document()             // 👈 Documento auto-generado

                        // 4. Crear el objeto con todos los datos
                        val examenData = hashMapOf(
                            "id" to examenRef.id,     // 👈 Usamos el ID auto-generado
                            "materia" to materia,
                            "dueDate" to dueDate,
                            "temas" to temas,
                            "reminder" to reminder
                        )

                        // 5. Guardar todo en una sola operación
                        examenRef.set(examenData).await()


                        // Limpiar formulario
                        materia = ""
                        dueDate = "Selecciona una fecha"
                        temas = ""
                        notification = false
                        reminder = "Selecciona una fecha"

                        showSuccess = true
                        onExamenAdded()
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
            text = { Text(text = "Examen agregada correctamente") },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) {
                    Text("Aceptar")
                }
            },
            containerColor = Color(0xFF2C2C2C)
        )
    }
}