package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VoiceNote::class, TaskItem::class, HelpMessage::class], version = 1, exportSchema = false)
abstract class SadaDatabase : RoomDatabase() {
    abstract fun sadaDao(): SadaDao

    companion object {
        @Volatile
        private var INSTANCE: SadaDatabase? = null

        fun getDatabase(context: Context): SadaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SadaDatabase::class.java,
                    "sada_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
