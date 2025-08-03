package com.example.classmate.data.repositories


import com.example.classmate.data.models.Task
import com.example.classmate.parseCustomDate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class TaskRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")

    // Crear tarea (ya lo tienes funcionando)
    suspend fun createTask(task: Task): String {
        val documentRef = tasksCollection.add(task).await()
        val taskId = documentRef.id
        documentRef.update("id", taskId).await()
        return taskId
    }

    // Actualizar tarea
    suspend fun updateTask(task: Task) {
        tasksCollection.document(task.id)
            .set(task)
            .await()
    }

    // Eliminar tarea
    suspend fun deleteTask(taskId: String) {
        tasksCollection.document(taskId)
            .delete()
            .await()
    }

    // Obtener todas las tareas
    suspend fun getAllTasks(): List<Task> {
        return tasksCollection.get()
            .await()
            .map { document ->
                document.toObject(Task::class.java).copy(id = document.id)
            }
    }

    // Obtener tarea por ID
    suspend fun getTaskById(taskId: String): Task? {
        return tasksCollection.document(taskId)
            .get()
            .await()
            .toObject(Task::class.java)
            ?.copy(id = taskId)
    }

    suspend fun getAllTasksSortedByDueDate(): List<Task> {
        return tasksCollection.get()
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

        val snapshot = FirebaseFirestore.getInstance()
            .collection("tasks")
            .whereEqualTo("notification", true)
            .whereEqualTo("reminder", hoy)
            .get()
            .await()

        return snapshot.map { it.toObject(Task::class.java) }
    }
}