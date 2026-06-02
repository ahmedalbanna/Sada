package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_notes")
data class VoiceNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val rawText: String,
    val summary: String,
    val category: String, // e.g. "عمل", "شخصي", "أفكار", "اجتماع", "عام"
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val notes: String = "",
    val tags: String = "" // Comma-separated tags
)

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long = 0,
    val taskDescription: String,
    val dueDate: Long? = null,
    val priority: String = "Medium", // "High" (عالية), "Medium" (متوسطة), "Low" (منخفضة)
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "help_messages")
data class HelpMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageText: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
