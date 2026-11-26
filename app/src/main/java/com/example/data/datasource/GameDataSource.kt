package com.example.data.datasource

import com.example.domain.model.GameRound
import kotlinx.coroutines.flow.Flow

interface GameDataSource {
    fun getLiveRoundsStream(): Flow<GameRound>
    suspend fun fetchLatestRounds(limit: Int): List<GameRound>
}
