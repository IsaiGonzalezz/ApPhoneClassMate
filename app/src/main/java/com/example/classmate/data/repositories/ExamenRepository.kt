package com.example.classmate.data.repositories
import com.example.classmate.data.models.Examen
import com.example.classmate.data.models.Nota
import com.example.classmate.data.models.Task
import com.example.classmate.parseCustomDate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.sql.Date
import javax.inject.Inject

class ExamenRepository @Inject constructor () {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("examenes")

    // Actualizar Nota
    suspend fun updateExamen(examen: Examen) {
        tasksCollection.document(examen.id)
            .set(examen)
            .await()
    }

    // Eliminar tarea
    suspend fun deleteExamen(examenId: String) {
        tasksCollection.document(examenId)
            .delete()
            .await()
    }

    // Obtener todas las tareas
    suspend fun getAllExamen(): List<Examen> {
        return tasksCollection.get()
            .await()
            .map { document ->
                document.toObject(Examen::class.java).copy(id = document.id)
            }
    }


    suspend fun getAllExamenesSortedByDueDate(): List<Examen> {
        return tasksCollection.get()
            .await()
            .map { document ->
                document.toObject(Examen::class.java).copy(id = document.id)
            }
            .sortedBy { examen ->
                parseCustomDate(examen.dueDate) ?: Date(Long.MAX_VALUE)
            }
    }
}