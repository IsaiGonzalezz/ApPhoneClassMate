package com.example.classmate
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ClassMate::class],
    version = 3
)
abstract class ClassmateDb : RoomDatabase(){
    abstract fun classmateDao() : ClassmateDao
}