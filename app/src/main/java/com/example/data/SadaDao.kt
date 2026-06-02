package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SadaDao {

    // --- Voice Notes Queries ---
    @Query("SELECT * FROM voice_notes ORDER BY createdAt DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNote>>

    @Query("SELECT * FROM voice_notes WHERE id = :id")
    suspend fun getVoiceNoteById(id: Long): VoiceNote?

    @Query("SELECT * FROM voice_notes WHERE title LIKE :query OR rawText LIKE :query OR summary LIKE :query OR tags LIKE :query ORDER BY createdAt DESC")
    fun searchVoiceNotes(query: String): Flow<List<VoiceNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(voiceNote: VoiceNote): Long

    @Update
    suspend fun updateVoiceNote(voiceNote: VoiceNote)

    @Delete
    suspend fun deleteVoiceNote(voiceNote: VoiceNote)

    @Query("DELETE FROM voice_notes")
    suspend fun deleteAllVoiceNotes()


    // --- Tasks Queries ---
    @Query("SELECT * FROM task_items ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM task_items WHERE noteId = :noteId ORDER BY createdAt DESC")
    fun getTasksForNote(noteId: Long): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(taskItem: TaskItem): Long

    @Update
    suspend fun updateTask(taskItem: TaskItem)

    @Delete
    suspend fun deleteTask(taskItem: TaskItem)

    @Query("DELETE FROM task_items WHERE noteId = :noteId")
    suspend fun deleteTasksForNote(noteId: Long)

    @Query("DELETE FROM task_items")
    suspend fun deleteAllTasks()


    // --- Support Help Chat Queries ---
    @Query("SELECT * FROM help_messages ORDER BY timestamp ASC")
    fun getAllHelpMessages(): Flow<List<HelpMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelpMessage(message: HelpMessage)

    @Query("DELETE FROM help_messages")
    suspend fun clearHelpChat()
}
