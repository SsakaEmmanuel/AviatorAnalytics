package com.example.domain.model

enum class PredictionModelType(val displayName: String, val description: String) {
    ENSEMBLE("Weighted Ensemble", "Combines frequency, rolling window, quantile, and Markov transitions."),
    FREQUENCY("Empirical Frequency", "Historical bin density matching based on observed crash rates."),
    ROLLING_WINDOW("Adaptive Rolling Window", "Exponential moving average & short-term trend evaluation."),
    PERCENTILE("Quantile / Percentile", "Statistical quantile estimation (IQR & median boundaries)."),
    STREAK_MARKOV("Streak / Markov State", "State transition probabilities between sub-2x and high multipliers.")
}

data class RangeProbability(
    val rangeLabel: String,
    val rangeMin: Double,
    val rangeMax: Double,
    val probabilityPercent: Double,
    val historicalHitRate: Double
)

data class ModelPredictionResult(
    val modelType: PredictionModelType,
    val predictedRangeMin: Double,
    val predictedRangeMax: Double,
    val predictedRangeLabel: String,
    val expectedMultiplier: Double,
    val confidenceScore: Double, // 0.0 - 1.0 (e.g. 0.68)
    val probabilities: List<RangeProbability>,
    val rationale: String
)

data class EnsemblePrediction(
    val recommendedRange: String,
    val rangeMin: Double,
    val rangeMax: Double,
    val expectedMultiplier: Double,
    val ensembleConfidence: Double,
    val modelOutputs: Map<PredictionModelType, ModelPredictionResult>,
    val riskLevel: String, // Low, Moderate, High, Extreme
    val statisticalCaveat: String = "Crash games operate on cryptographically independent RNG. Statistical predictions estimate empirical tendencies, not guaranteed outcomes."
)
