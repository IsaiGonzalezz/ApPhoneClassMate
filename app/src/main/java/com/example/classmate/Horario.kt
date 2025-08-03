package com.example.classmate

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.classmate.data.repositories.HorarioRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.*
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson

@Composable
fun Horario(
    navController: NavController,
    horarioRepository: HorarioRepository,
    context: Context
) {
    val diaActual = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date())
        .replaceFirstChar { it.uppercase() }

    val clasesHoy by produceState<List<Map<String, String>>>(initialValue = emptyList()) {
        value = horarioRepository.obtenerClasesPorDia(diaActual)
            .sortedBy { it["hora_inicio"] ?: "00:00" }
    }

    // 1. Enviar horario al reloj cuando se actualice
    LaunchedEffect(clasesHoy) {
        if (clasesHoy.isNotEmpty()) {
            enviarHorarioAlReloj(context, clasesHoy)
        }
    }


    val coloresAlternativos = listOf(
        Color(0xFFF66262),  // Rojo claro
        Color(0xFFC53FDA),  // Morado claro
        Color(0xFF4298DE),  // Azul claro
        Color(0xFF29B9CC),  // Cyan claro
        Color(0xFF18AF1E),  // Verde claro
        Color(0xFFC59B00),  // Amarillo claro
        Color(0xFFDC5E37),  // Naranja claro
        Color(0xFF9A715F)   // Café claro
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título
        Text(
            text = diaActual,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de clases
        Column {
            clasesHoy.forEachIndexed { index, clase ->
                val bloque = (index + 1).toString()
                val horaInicio = clase["hora_inicio"] ?: "--:--"
                val horaFin = clase["hora_fin"] ?: "--:--"
                val materia = clase["materia"] ?: ""
                val profesor = clase["profesor"] ?: ""
                val color = coloresAlternativos[index % coloresAlternativos.size]

                Card(
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Número de bloque
                        Text(
                            text = bloque,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.width(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Horario
                        Text(
                            text = "$horaInicio - $horaFin",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(100.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Materia y profesor
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = materia,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (profesor.isNotEmpty()) {
                                Text(
                                    text = profesor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón
        Button(
            onClick = { navController.navigate(Screen.RegistrarHorario.route) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB71C1C),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TableView,
                contentDescription = "Registrar Horario",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Registrar Horario")
        }
    }
}


// 2. Función para enviar datos al reloj
private fun enviarHorarioAlReloj(context: Context, horario: List<Map<String, String>>) {
    val dataClient = Wearable.getDataClient(context)
    val horarioJson = Gson().toJson(horario)

    val putDataReq = PutDataMapRequest.create("/horario").apply {
        dataMap.putString("dia", horario.firstOrNull()?.get("dia") ?: "")
        dataMap.putString("clases", horarioJson)
        dataMap.putLong("timestamp", System.currentTimeMillis()) // Para forzar actualización
    }

    dataClient.putDataItem(putDataReq.asPutDataRequest()).addOnSuccessListener {
        println("✅ Horario enviado al reloj")
    }.addOnFailureListener { e ->
        println("❌ Error al enviar: ${e.message}")
    }
}
