package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.GameRoundDao
import com.example.data.local.dao.PredictionRecordDao
import com.example.data.local.entity.GameRoundEntity
import com.example.data.local.entity.PredictionRecordEntity

@Database(
    entities = [
        GameRoundEntity::class,
        PredictionRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRoundDao(): GameRoundDao
    abstract fun predictionRecordDao(): PredictionRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aviator_analytics.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
