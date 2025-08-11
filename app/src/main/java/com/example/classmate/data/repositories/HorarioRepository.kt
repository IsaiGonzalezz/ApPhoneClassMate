package com.example.classmate.data.repositories

import com.example.classmate.ClassmateDao
import com.example.classmate.data.models.Horario
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HorarioRepository @Inject constructor(
    private val classmateDao: ClassmateDao
) {
    private val db = FirebaseFirestore.getInstance()
    private suspend fun getHorarioCollection(): CollectionReference {
        // Obtiene el id_Fr actual de la base de datos local
        val systemId = withContext(Dispatchers.IO) {
            classmateDao.getAll().firstOrNull()?.id_Fr
                ?: throw IllegalStateException("No hay sistema activo")
        }

        return db.collection("systems").document(systemId).collection("horarios")
    }

    // Registrar o actualizar clase
    suspend fun updateClaseHorario(claseHorario: Horario) {
        getHorarioCollection().document(claseHorario.id)
            .set(claseHorario)
            .await()
    }

    // Eliminar clase de horario
    suspend fun deleteClaseHorario(claseId: String) {
        getHorarioCollection().document(claseId)
            .delete()
            .await()
    }

    // Filtrar por día
    suspend fun getHorarioPorDia(dia: String): List<Horario> {
        return getHorarioCollection().whereEqualTo("dia", dia).get()
            .await()
            .map { document ->
                document.toObject(Horario::class.java).copy(id = document.id)
            }
    }

    suspend fun obtenerClasesPorDia(dia: String): List<Map<String, String>> {
        val doc = getHorarioCollection()
            .document(dia)
            .get()
            .await()

        return doc.get("clases") as? List<Map<String, String>> ?: emptyList()
    }
}