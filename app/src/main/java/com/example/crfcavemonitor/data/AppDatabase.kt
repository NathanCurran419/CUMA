package com.example.crfcavemonitor.data

import android.content.Context
import androidx.room.*
import androidx.room.TypeConverters

@Database(entities = [Report::class, SpeciesCount::class, Photo::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun speciesCountDao(): SpeciesCountDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crf_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}