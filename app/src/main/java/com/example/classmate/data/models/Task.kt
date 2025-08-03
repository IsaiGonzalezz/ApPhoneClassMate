package com.example.classmate.data.models

data class Task(
    val id: String = "",
    val title: String = "",
    val dueDate: String = "",
    val description: String = "",
    val notification: Boolean = false,
    val reminder: String = "",
    val notes: String = "",
    val createdAt: Long = 0,
    val completed: Boolean = false,
    val progress: Int = 0,
)