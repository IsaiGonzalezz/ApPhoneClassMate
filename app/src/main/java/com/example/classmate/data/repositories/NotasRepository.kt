package com.example.classmate.data.repositories
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.classmate.ClassmateDao
import com.example.classmate.data.models.Nota
import com.example.classmate.data.models.Task
import com.example.classmate.parseCustomDate
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.sql.Date
import javax.inject.Inject

class NotasRepository @Inject constructor(
    private val classmateDao: ClassmateDao
) {
    private val db = FirebaseFirestore.getInstance()
    private suspend fun getNotesCollection(): CollectionReference {
        // Obtiene el id_Fr actual de la base de datos local
        val systemId = withContext(Dispatchers.IO) {
            classmateDao.getAll().firstOrNull()?.id_Fr
                ?: throw IllegalStateException("No hay sistema activo")
        }

        return db.collection("systems").document(systemId).collection("notes")
    }

    // Actualizar Nota
    suspend fun updateNote(nota: Nota) {
        getNotesCollection().document(nota.id)
            .set(nota)
            .await()
    }

    // Eliminar tarea
    suspend fun deleteNote(notaId: String) {
        getNotesCollection().document(notaId)
            .delete()
            .await()
    }


    suspend fun getAllNotesSortedByDueDate(): List<Nota> {
        return getNotesCollection().get()
            .await()
            .map { document ->
                document.toObject(Nota::class.java).copy(id = document.id)
            }
            .sortedBy { nota ->
                parseCustomDate(nota.dueDate) ?: Date(Long.MAX_VALUE)
            }
    }
}