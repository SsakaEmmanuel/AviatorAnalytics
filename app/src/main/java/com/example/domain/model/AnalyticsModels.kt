package com.example.domain.model

data class DistributionRange(
    val label: String,
    val min: Double,
    val max: Double,
    val count: Int,
    val empiricalPercentage: Double,
    val theoreticalProbability: Double // Based on crash math: P(X >= x) = 0.97 / x
)

data class StreakInfo(
    val currentCrashStreak: Int, // consecutive < 2.0x
    val maxCrashStreak: Int,
    val currentFlyStreak: Int,   // consecutive >= 2.0x
    val maxFlyStreak: Int,
    val currentSub15Streak: Int, // consecutive < 1.5x
    val maxSub15Streak: Int
)

data class Percentiles(
    val p10: Double,
    val p25: Double,
    val p50: Double, // Median
    val p75: Double,
    val p90: Double,
    val p95: Double,
    val p99: Double
)

data class ComprehensiveAnalytics(
    val sampleSize: Int,
    val mean: Double,
    val median: Double,
    val minMultiplier: Double,
    val maxMultiplier: Double,
    val standardDeviation: Double,
    val variance: Double,
    val volatilityIndex: Double, // Coefficient of variation: stdDev / mean
    val percentiles: Percentiles,
    val distributionRanges: List<DistributionRange>,
    val streakInfo: StreakInfo,
    val houseEdgeEstimate: Double,
    val theoreticalRtp: Double = 97.0
)
