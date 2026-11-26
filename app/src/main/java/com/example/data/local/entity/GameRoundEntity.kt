package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_rounds",
    indices = [
        Index(value = ["roundId"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class GameRoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roundId: String,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "MANUAL", // MANUAL, SIMULATOR, CSV_IMPORT
    val serverSeedHash: String? = null,
    val clientSeed: String? = null,
    val combinedHash: String? = null
)
