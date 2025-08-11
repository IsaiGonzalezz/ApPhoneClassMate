package com.example.classmate.data.repositories


import com.example.classmate.ClassmateDao
import com.example.classmate.data.models.Task
import com.example.classmate.parseCustomDate
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val classmateDao: ClassmateDao
) {
    private val db = FirebaseFirestore.getInstance()
    private suspend fun getTaskCollection(): CollectionReference {
        // Obtiene el id_Fr actual de la base de datos local
        val systemId = withContext(Dispatchers.IO) {
            classmateDao.getAll().firstOrNull()?.id_Fr
                ?: throw IllegalStateException("No hay sistema activo")
        }

        return db.collection("systems").document(systemId).collection("tasks")
    }

    // Actualizar tarea
    suspend fun updateTask(task: Task) {
        getTaskCollection().document(task.id)
            .set(task)
            .await()
    }

    // Eliminar tarea
    suspend fun deleteTask(taskId: String) {
        getTaskCollection().document(taskId)
            .delete()
            .await()
    }

    // Obtener todas las tareas
    suspend fun getAllTasks(): List<Task> {
        return getTaskCollection().get()
            .await()
            .map { document ->
                document.toObject(Task::class.java).copy(id = document.id)
            }
    }

    // Obtener tarea por ID
    suspend fun getTaskById(taskId: String): Task? {
        return getTaskCollection().document(taskId)
            .get()
            .await()
            .toObject(Task::class.java)
            ?.copy(id = taskId)
    }

    suspend fun getAllTasksSortedByDueDate(): List<Task> {
        return getTaskCollection().get()
            .await()
            .map { document ->
                document.toObject(Task::class.java).copy(id = document.id)
            }
            .sortedBy { task ->
                parseCustomDate(task.dueDate) ?: Date(Long.MAX_VALUE)
            }
    }

    suspend fun obtenerTareasParaHoy(): List<Task> {
        val hoy = SimpleDateFormat("EEEE dd 'de' MMMM", Locale("es", "ES"))
            .format(java.util.Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

        val snapshot = getTaskCollection()  // Usa la misma colección estructurada
            .whereEqualTo("notification", true)
            .whereEqualTo("reminder", hoy)
            .get()
            .await()

        return snapshot.map { it.toObject(Task::class.java) }
    }
}