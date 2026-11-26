package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.GameRoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRoundDao {
    @Query("SELECT * FROM game_rounds ORDER BY timestamp DESC")
    fun getAllRoundsFlow(): Flow<List<GameRoundEntity>>

    @Query("SELECT * FROM game_rounds ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRoundsFlow(limit: Int): Flow<List<GameRoundEntity>>

    @Query("SELECT * FROM game_rounds ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentRoundsList(limit: Int): List<GameRoundEntity>

    @Query("SELECT * FROM game_rounds ORDER BY timestamp DESC")
    suspend fun getAllRoundsList(): List<GameRoundEntity>

    @Query("SELECT * FROM game_rounds ORDER BY timestamp ASC")
    suspend fun getAllRoundsChronological(): List<GameRoundEntity>

    @Query("SELECT COUNT(*) FROM game_rounds")
    fun getRoundCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_rounds")
    suspend fun getRoundCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: GameRoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRounds(rounds: List<GameRoundEntity>): List<Long>

    @Query("DELETE FROM game_rounds WHERE id = :id")
    suspend fun deleteRoundById(id: Long)

    @Query("DELETE FROM game_rounds")
    suspend fun clearAllRounds()

    @Query("SELECT * FROM game_rounds WHERE roundId LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRoundsFlow(query: String): Flow<List<GameRoundEntity>>
}
