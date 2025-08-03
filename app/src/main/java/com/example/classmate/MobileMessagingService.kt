package com.example.classmate

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson




class MobileMessagingService : Service() {
    private lateinit var dataClient: DataClient

    // 1. Implementación obligatoria de onBind
    override fun onBind(intent: Intent?): IBinder? {
        return null // Para servicios no vinculados
    }

    override fun onCreate() {
        super.onCreate()
        dataClient = Wearable.getDataClient(this)
    }

    fun sendHorarioToWatch(horario: List<Map<String, String>>) {
        // 2. Manejo seguro de nulos
        val dia = horario.firstOrNull()?.get("dia") ?: ""
        val putDataReq = PutDataMapRequest.create("/horario").apply {
            dataMap.putString("dia", dia)
            dataMap.putString("clases", Gson().toJson(horario))
        }
        dataClient.putDataItem(putDataReq.asPutDataRequest())
    }
}