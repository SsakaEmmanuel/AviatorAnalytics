package com.example.domain.engine

import com.example.domain.model.ComprehensiveAnalytics
import com.example.domain.model.DistributionRange
import com.example.domain.model.GameRound
import com.example.domain.model.Percentiles
import com.example.domain.model.StreakInfo
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object AnalyticsEngine {

    fun computeAnalytics(rounds: List<GameRound>): ComprehensiveAnalytics {
        if (rounds.isEmpty()) {
            return emptyAnalytics()
        }

        val multipliers = rounds.map { it.multiplier }
        val n = multipliers.size
        val sorted = multipliers.sorted()

        val mean = multipliers.average()
        val median = if (n % 2 == 1) {
            sorted[n / 2]
        } else {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        }

        val minMult = sorted.first()
        val maxMult = sorted.last()

        val variance = if (n > 1) {
            multipliers.sumOf { (it - mean).pow(2) } / (n - 1)
        } else {
            0.0
        }
        val stdDev = sqrt(variance)
        val volatility = if (mean > 0) stdDev / mean else 0.0

        val percentiles = Percentiles(
            p10 = getPercentile(sorted, 10.0),
            p25 = getPercentile(sorted, 25.0),
            p50 = median,
            p75 = getPercentile(sorted, 75.0),
            p90 = getPercentile(sorted, 90.0),
            p95 = getPercentile(sorted, 95.0),
            p99 = getPercentile(sorted, 99.0)
        )

        val distributionRanges = computeDistributionRanges(multipliers)
        val streakInfo = computeStreaks(rounds)

        // Estimated house edge calculation from sub-1.05x / instant crashes:
        // In standard Aviator / Crash games, instant bust (1.00x) occurs ~1-3% of the time,
        // and RTP is typically 97% (House Edge ~3%).
        val instantBusts = multipliers.count { it < 1.05 }
        val houseEdgeEstimate = (instantBusts.toDouble() / n.toDouble()) * 100.0

        return ComprehensiveAnalytics(
            sampleSize = n,
            mean = mean,
            median = median,
            minMultiplier = minMult,
            maxMultiplier = maxMult,
            standardDeviation = stdDev,
            variance = variance,
            volatilityIndex = volatility,
            percentiles = percentiles,
            distributionRanges = distributionRanges,
            streakInfo = streakInfo,
            houseEdgeEstimate = max(1.0, min(10.0, houseEdgeEstimate)),
            theoreticalRtp = 97.0
        )
    }

    private fun getPercentile(sorted: List<Double>, p: Double): Double {
        if (sorted.isEmpty()) return 0.0
        val index = (p / 100.0) * (sorted.size - 1)
        val lower = index.toInt()
        val upper = min(lower + 1, sorted.size - 1)
        val weight = index - lower
        return sorted[lower] * (1.0 - weight) + sorted[upper] * weight
    }

    private fun computeDistributionRanges(multipliers: List<Double>): List<DistributionRange> {
        val total = multipliers.size.toDouble()
        if (total == 0.0) return emptyList()

        val bins = listOf(
            Triple("1.00x – 1.49x (Low Crash)", 1.00, 1.49),
            Triple("1.50x – 1.99x (Medium Low)", 1.50, 1.99),
            Triple("2.00x – 4.99x (Standard Safe)", 2.00, 4.99),
            Triple("5.00x – 9.99x (High Payout)", 5.00, 9.99),
            Triple("10.00x+ (Jackpot Flight)", 10.00, 100000.0)
        )

        return bins.map { (label, minVal, maxVal) ->
            val count = multipliers.count { it in minVal..maxVal }
            val empiricalPct = (count / total) * 100.0
            // Theoretical probability in crash games P(X >= a) = 0.97 / a
            val theoPct = when {
                maxVal >= 10000.0 -> (0.97 / minVal) * 100.0
                else -> ((0.97 / minVal) - (0.97 / (maxVal + 0.01))) * 100.0
            }
            DistributionRange(
                label = label,
                min = minVal,
                max = maxVal,
                count = count,
                empiricalPercentage = empiricalPct,
                theoreticalProbability = max(0.0, theoPct)
            )
        }
    }

    private fun computeStreaks(rounds: List<GameRound>): StreakInfo {
        // Chronological order (oldest to newest)
        val chronological = rounds.sortedBy { it.timestamp }
        if (chronological.isEmpty()) {
            return StreakInfo(0, 0, 0, 0, 0, 0)
        }

        var curCrash = 0
        var maxCrash = 0
        var curFly = 0
        var maxFly = 0
        var curSub15 = 0
        var maxSub15 = 0

        for (r in chronological) {
            val m = r.multiplier
            // Crash streak: < 2.0x
            if (m < 2.0) {
                curCrash++
                curFly = 0
                maxCrash = max(maxCrash, curCrash)
            } else {
                curFly++
                curCrash = 0
                maxFly = max(maxFly, curFly)
            }

            // Sub-1.5x streak
            if (m < 1.5) {
                curSub15++
                maxSub15 = max(maxSub15, curSub15)
            } else {
                curSub15 = 0
            }
        }

        return StreakInfo(
            currentCrashStreak = curCrash,
            maxCrashStreak = maxCrash,
            currentFlyStreak = curFly,
            maxFlyStreak = maxFly,
            currentSub15Streak = curSub15,
            maxSub15Streak = maxSub15
        )
    }

    private fun emptyAnalytics(): ComprehensiveAnalytics {
        return ComprehensiveAnalytics(
            sampleSize = 0,
            mean = 0.0,
            median = 0.0,
            minMultiplier = 0.0,
            maxMultiplier = 0.0,
            standardDeviation = 0.0,
            variance = 0.0,
            volatilityIndex = 0.0,
            percentiles = Percentiles(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            distributionRanges = emptyList(),
            streakInfo = StreakInfo(0, 0, 0, 0, 0, 0),
            houseEdgeEstimate = 3.0,
            theoreticalRtp = 97.0
        )
    }
}
