package com.example.classmate

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.classmate.data.repositories.TaskRepository


fun mostrarNotificacionesDeHoy(context: Context, taskRepository: TaskRepository) {

    CoroutineScope(Dispatchers.IO).launch {
        val tareas = withContext(Dispatchers.IO) {
            taskRepository.obtenerTareasParaHoy()
        }

        withContext(Dispatchers.Main) {
            val manager = NotificationManagerCompat.from(context)

            if (!manager.areNotificationsEnabled()) return@withContext

            tareas.forEachIndexed { index, tarea ->
                val noti = NotificationCompat.Builder(context, "canal_tareas_hoy")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Recordatorio: ${tarea.title}")
                    .setContentText(tarea.description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                manager.notify(index, noti)
            }
        }
    }
}


fun crearCanalDeNotificacion(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            "canal_tareas_hoy",
            "Tareas del día",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones para tareas programadas hoy"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }
}