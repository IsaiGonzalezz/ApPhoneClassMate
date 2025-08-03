package com.example.classmate.data.repositories
import com.example.classmate.data.models.Nota
import com.example.classmate.data.models.Task
import com.example.classmate.parseCustomDate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.sql.Date
import javax.inject.Inject


class NotasRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("notes")

    // Actualizar Nota
    suspend fun updateNote(nota: Nota) {
        tasksCollection.document(nota.id)
            .set(nota)
            .await()
    }

    // Eliminar tarea
    suspend fun deleteNote(notaId: String) {
        tasksCollection.document(notaId)
            .delete()
            .await()
    }

    // Obtener todas las tareas
    suspend fun getAllNotes(): List<Nota> {
        return tasksCollection.get()
            .await()
            .map { document ->
                document.toObject(Nota::class.java).copy(id = document.id)
            }
    }

    // Obtener tarea por ID
    suspend fun getNotesById(notaId: String): Nota? {
        return tasksCollection.document(notaId)
            .get()
            .await()
            .toObject(Nota::class.java)
            ?.copy(id = notaId)
    }

    suspend fun getAllNotesSortedByDueDate(): List<Nota> {
        return tasksCollection.get()
            .await()
            .map { document ->
                document.toObject(Nota::class.java).copy(id = document.id)
            }
            .sortedBy { nota ->
                parseCustomDate(nota.dueDate) ?: Date(Long.MAX_VALUE)
            }
    }
}