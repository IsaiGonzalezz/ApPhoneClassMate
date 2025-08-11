package com.example.classmate
import android.app.Application
import androidx.room.Room

class ClassmateApp : Application(){
    lateinit var room: ClassmateDb
        private set
    override fun onCreate() {
        super.onCreate()
        room = Room.databaseBuilder(
            applicationContext,
            ClassmateDb::class.java,
            "apprenclassmates" //NOMBRE DE LA BASE DE DAT0S
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}