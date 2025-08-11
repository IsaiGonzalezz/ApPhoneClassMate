package com.example.classmate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ClassmateDao {
    @Query("SELECT * FROM classmate")
    fun getAll(): List<ClassMate>

    @Insert
    fun insert(classmate: ClassMate)  // Insert para un solo item

    // Si necesitas insertar múltiples items:
    @Insert
    fun insertAll(classmates: List<ClassMate>)
}