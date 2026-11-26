package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PredictionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionRecordDao {
    @Query("SELECT * FROM prediction_records ORDER BY timestamp DESC")
    fun getAllPredictionsFlow(): Flow<List<PredictionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionRecordEntity): Long

    @Query("SELECT * FROM prediction_records WHERE targetRoundId = :roundId")
    suspend fun getPredictionsForRound(roundId: String): List<PredictionRecordEntity>

    @Query("DELETE FROM prediction_records")
    suspend fun clearAllPredictions()
}
