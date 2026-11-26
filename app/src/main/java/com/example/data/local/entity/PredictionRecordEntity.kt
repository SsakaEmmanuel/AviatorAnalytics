package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prediction_records")
data class PredictionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetRoundId: String,
    val modelName: String,
    val predictedRangeMin: Double,
    val predictedRangeMax: Double,
    val confidence: Double,
    val actualMultiplier: Double? = null,
    val isCorrect: Boolean? = null,
    val timestamp: Long = System.currentTimeMillis()
)
