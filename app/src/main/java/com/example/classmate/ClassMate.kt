package com.example.classmate

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ClassMate(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val base_activa: Boolean = false,
    val id_Fr : String
)
