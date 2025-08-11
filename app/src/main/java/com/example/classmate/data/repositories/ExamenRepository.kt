package com.example.classmate.data.repositories
import com.example.classmate.ClassmateDao
import com.example.classmate.data.models.Examen
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

class ExamenRepository @Inject constructor (
    private val classmateDao: ClassmateDao
) {
    private val db = FirebaseFirestore.getInstance()
    private suspend fun getExamenCollection(): CollectionReference {
        // Obtiene el id_Fr actual de la base de datos local
        val systemId = withContext(Dispatchers.IO) {
            classmateDao.getAll().firstOrNull()?.id_Fr
                ?: throw IllegalStateException("No hay sistema activo")
        }

        return db.collection("systems").document(systemId).collection("examenes")
    }

    // Actualizar Nota
    suspend fun updateExamen(examen: Examen) {
        getExamenCollection().document(examen.id)
            .set(examen)
            .await()
    }

    // Eliminar tarea
    suspend fun deleteExamen(examenId: String) {
        getExamenCollection().document(examenId)
            .delete()
            .await()
    }


    suspend fun getAllExamenesSortedByDueDate(): List<Examen> {
        return getExamenCollection().get()
            .await()
            .map { document ->
                document.toObject(Examen::class.java).copy(id = document.id)
            }
            .sortedBy { examen ->
                parseCustomDate(examen.dueDate) ?: Date(Long.MAX_VALUE)
            }
    }
}