package com.example.data.repository

import android.content.Context
import com.example.data.datasource.CsvDataHandler
import com.example.data.datasource.MockSimulatorDataSource
import com.example.data.local.AppDatabase
import com.example.data.local.dao.GameRoundDao
import com.example.data.local.dao.PredictionRecordDao
import com.example.domain.model.GameRound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GameRepository(
    private val gameRoundDao: GameRoundDao,
    private val predictionRecordDao: PredictionRecordDao
) {
    val allRoundsFlow: Flow<List<GameRound>> = gameRoundDao.getAllRoundsFlow()
        .map { entities -> entities.map { GameRound.fromEntity(it) } }

    fun getRecentRoundsFlow(limit: Int = 30): Flow<List<GameRound>> = gameRoundDao.getRecentRoundsFlow(limit)
        .map { entities -> entities.map { GameRound.fromEntity(it) } }

    val roundCountFlow: Flow<Int> = gameRoundDao.getRoundCountFlow()

    suspend fun insertRound(round: GameRound): Long = withContext(Dispatchers.IO) {
        gameRoundDao.insertRound(round.toEntity())
    }

    suspend fun insertRounds(rounds: List<GameRound>): List<Long> = withContext(Dispatchers.IO) {
        gameRoundDao.insertRounds(rounds.map { it.toEntity() })
    }

    suspend fun deleteRound(id: Long) = withContext(Dispatchers.IO) {
        gameRoundDao.deleteRoundById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        gameRoundDao.clearAllRounds()
        predictionRecordDao.clearAllPredictions()
    }

    suspend fun getAllRoundsList(): List<GameRound> = withContext(Dispatchers.IO) {
        gameRoundDao.getAllRoundsList().map { GameRound.fromEntity(it) }
    }

    suspend fun getRecentRoundsList(limit: Int = 30): List<GameRound> = withContext(Dispatchers.IO) {
        gameRoundDao.getRecentRoundsList(limit).map { GameRound.fromEntity(it) }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // No-op: only live game data from inbuilt browser is ingested
    }

    fun exportToCsvString(rounds: List<GameRound>): String {
        return CsvDataHandler.exportToCsv(rounds)
    }

    suspend fun importFromCsvString(csv: String): Int = withContext(Dispatchers.IO) {
        val parsed = CsvDataHandler.parseCsv(csv)
        if (parsed.isNotEmpty()) {
            gameRoundDao.insertRounds(parsed.map { it.toEntity() })
        }
        parsed.size
    }

    companion object {
        @Volatile
        private var INSTANCE: GameRepository? = null

        fun getInstance(context: Context): GameRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val instance = GameRepository(db.gameRoundDao(), db.predictionRecordDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
