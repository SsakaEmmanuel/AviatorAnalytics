package com.example.domain.model

import com.example.data.local.entity.GameRoundEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GameRound(
    val id: Long = 0,
    val roundId: String,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "MANUAL",
    val serverSeedHash: String? = null,
    val clientSeed: String? = null,
    val combinedHash: String? = null
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    fun toEntity(): GameRoundEntity {
        return GameRoundEntity(
            id = id,
            roundId = roundId,
            multiplier = multiplier,
            timestamp = timestamp,
            source = source,
            serverSeedHash = serverSeedHash,
            clientSeed = clientSeed,
            combinedHash = combinedHash
        )
    }

    companion object {
        fun fromEntity(entity: GameRoundEntity): GameRound {
            return GameRound(
                id = entity.id,
                roundId = entity.roundId,
                multiplier = entity.multiplier,
                timestamp = entity.timestamp,
                source = entity.source,
                serverSeedHash = entity.serverSeedHash,
                clientSeed = entity.clientSeed,
                combinedHash = entity.combinedHash
            )
        }
    }
}
