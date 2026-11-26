package com.example.domain.model

data class BacktestStepResult(
    val roundIndex: Int,
    val roundId: String,
    val actualMultiplier: Double,
    val predictedRangeLabel: String,
    val predictedRangeMin: Double,
    val predictedRangeMax: Double,
    val isHit: Boolean,
    val baselineHit: Boolean,
    val modelConfidence: Double
)

data class BacktestRangeAccuracy(
    val rangeLabel: String,
    val totalPredictions: Int,
    val hits: Int,
    val accuracyPercent: Double,
    val baselineAccuracyPercent: Double,
    val errorRateDescription: String
)

data class BacktestReport(
    val totalRoundsEvaluated: Int,
    val modelOverallAccuracy: Double,
    val baselineOverallAccuracy: Double,
    val accuracyDelta: Double, // model - baseline
    val totalHits: Int,
    val baselineHits: Int,
    val rangeAccuracies: List<BacktestRangeAccuracy>,
    val averageConfidence: Double,
    val stepResults: List<BacktestStepResult>,
    val executionSummary: String
)
