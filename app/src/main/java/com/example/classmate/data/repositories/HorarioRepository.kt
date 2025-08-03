package com.example.classmate.data.repositories

import com.example.classmate.data.models.Horario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HorarioRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val horariosCollection = db.collection("horarios")

    // Registrar o actualizar clase
    suspend fun updateClaseHorario(claseHorario: Horario) {
        horariosCollection.document(claseHorario.id)
            .set(claseHorario)
            .await()
    }

    // Eliminar clase de horario
    suspend fun deleteClaseHorario(claseId: String) {
        horariosCollection.document(claseId)
            .delete()
            .await()
    }

    // Obtener todas las clases
    suspend fun getAllClaseHorario(): List<Horario> {
        return horariosCollection.get()
            .await()
            .map { document ->
                document.toObject(Horario::class.java).copy(id = document.id)
            }
    }

    // Filtrar por día
    suspend fun getHorarioPorDia(dia: String): List<Horario> {
        return horariosCollection.whereEqualTo("dia", dia).get()
            .await()
            .map { document ->
                document.toObject(Horario::class.java).copy(id = document.id)
            }
    }

    suspend fun obtenerClasesPorDia(dia: String): List<Map<String, String>> {
        val doc = FirebaseFirestore.getInstance()
            .collection("horarios")
            .document(dia)
            .get()
            .await()

        return doc.get("clases") as? List<Map<String, String>> ?: emptyList()
    }
}