package com.example.textbookexchange.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.textbookexchange.Book

@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "textbook_exchange_database"
                )
                    .fallbackToDestructiveMigration() // Handle schema changes during development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}