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
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddNoteScreen(navController : NavController,onNoteAdded: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Selecciona una fecha") }
    var description by remember { mutableStateOf("") }


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
            text = "Agregar Nota",
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FormTextField("Título", title) { title = it }
        FormDatePicker("Fecha", dueDate) { dueDate = it }
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Nota") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 350.dp),
            maxLines = Int.MAX_VALUE,
            singleLine = false,
            textStyle = LocalTextStyle.current.copy(lineHeight = 20.sp)
        )

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

                isLoading = true
                errorMessage = null

                val noteData = hashMapOf(
                    "title" to title,
                    "dueDate" to dueDate,
                    "description" to description,
                    "createdAt" to System.currentTimeMillis(),
                )

                scope.launch {
                    try {
                        val documentRef = FirebaseFirestore.getInstance()
                            .collection("notes")
                            .add(noteData)
                            .await()

                        val noteId = documentRef.id
                        documentRef.update("id", noteId).await()

                        // Limpiar formulario
                        title = ""
                        dueDate = "Selecciona una fecha"
                        description = ""

                        showSuccess = true
                        onNoteAdded()
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
            text = { Text(text = "Nota agregada correctamente") },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) {
                    Text("Aceptar")
                }
            },
            containerColor = Color(0xFF2C2C2C)
        )
    }
}