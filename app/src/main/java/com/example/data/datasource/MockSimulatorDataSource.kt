package com.example.data.datasource

import com.example.domain.model.GameRound
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import kotlin.math.floor
import kotlin.math.max
import kotlin.random.Random

object MockSimulatorDataSource : GameDataSource {

    private var roundSequence = 100000L

    /**
     * Generates an authentic crash multiplier based on industry-standard Provably Fair math:
     * RTP = 97%, House Edge = 3%.
     */
    fun generateProvablyFairRound(
        customRoundId: String? = null,
        source: String = "SIMULATOR"
    ): GameRound {
        roundSequence++
        val roundId = customRoundId ?: "BET-${roundSequence}"
        val timestamp = System.currentTimeMillis()

        val serverSeed = sha256("server_secret_${roundSequence}_${Random.nextLong()}")
        val clientSeed = "betika_ke_${Random.nextInt(1000, 9999)}"
        val combinedHash = sha256("$serverSeed:$clientSeed:$roundSequence")

        // First 8 hex characters to integer
        val hexChunk = combinedHash.substring(0, 8)
        val intVal = hexChunk.toLong(16)
        val maxVal = 0xFFFFFFFFL
        val u = intVal.toDouble() / maxVal.toDouble()

        // 3% instant crash edge
        val multiplier = if (u < 0.03) {
            1.00
        } else {
            val raw = 0.97 / (1.0 - u)
            // Round to 2 decimal places down
            val scaled = floor(raw * 100.0) / 100.0
            max(1.00, scaled)
        }

        return GameRound(
            roundId = roundId,
            multiplier = multiplier,
            timestamp = timestamp,
            source = source,
            serverSeedHash = serverSeed,
            clientSeed = clientSeed,
            combinedHash = combinedHash
        )
    }

    fun generateHistoricalBatch(count: Int): List<GameRound> {
        val now = System.currentTimeMillis()
        val intervalMs = 25000L // 25s per round average
        val list = mutableListOf<GameRound>()

        for (i in count downTo 1) {
            val rId = "BET-${100000L + i}"
            val rTime = now - (i * intervalMs)
            val round = generateProvablyFairRound(customRoundId = rId, source = "HISTORICAL_SIM").copy(
                timestamp = rTime
            )
            list.add(round)
        }
        return list
    }

    override fun getLiveRoundsStream(): Flow<GameRound> = flow {
        while (true) {
            val nextRound = generateProvablyFairRound()
            emit(nextRound)
            // Crash game round cycle: flight time + 5s cooldown
            val flightDuration = minOf(15000L, maxOf(3000L, (nextRound.multiplier * 800).toLong()))
            delay(flightDuration + 4000L)
        }
    }

    override suspend fun fetchLatestRounds(limit: Int): List<GameRound> {
        return generateHistoricalBatch(limit)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
