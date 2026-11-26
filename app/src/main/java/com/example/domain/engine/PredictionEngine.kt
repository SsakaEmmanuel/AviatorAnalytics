package com.example.domain.engine

import com.example.domain.model.EnsemblePrediction
import com.example.domain.model.GameRound
import com.example.domain.model.ModelPredictionResult
import com.example.domain.model.PredictionModelType
import com.example.domain.model.RangeProbability
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

object PredictionEngine {

    private val STANDARD_RANGES = listOf(
        Triple("1.00x – 1.49x", 1.00, 1.49),
        Triple("1.50x – 1.99x", 1.50, 1.99),
        Triple("2.00x – 3.49x", 2.00, 3.49),
        Triple("3.50x – 7.99x", 3.50, 7.99),
        Triple("8.00x+", 8.00, 100000.0)
    )

    fun predictNext(rounds: List<GameRound>, windowSize: Int = 50): EnsemblePrediction {
        if (rounds.isEmpty()) {
            return fallbackPrediction()
        }

        // Take up to windowSize most recent rounds
        val sample = rounds.take(max(10, min(windowSize, rounds.size)))
        val multipliers = sample.map { it.multiplier }

        // Model 1: Empirical Frequency
        val freqResult = predictFrequencyModel(multipliers)

        // Model 2: Rolling Window Adaptive
        val rollingResult = predictRollingWindowModel(multipliers)

        // Model 3: Quantile / Percentile
        val percentileResult = predictPercentileModel(multipliers)

        // Model 4: Streak / Markov Transition
        val streakResult = predictStreakMarkovModel(sample)

        val modelOutputs = mapOf(
            PredictionModelType.FREQUENCY to freqResult,
            PredictionModelType.ROLLING_WINDOW to rollingResult,
            PredictionModelType.PERCENTILE to percentileResult,
            PredictionModelType.STREAK_MARKOV to streakResult
        )

        // Ensemble Weighting:
        // Rolling Window: 30%, Frequency: 25%, Percentile: 25%, Markov: 20%
        val weights = mapOf(
            PredictionModelType.ROLLING_WINDOW to 0.30,
            PredictionModelType.FREQUENCY to 0.25,
            PredictionModelType.PERCENTILE to 0.25,
            PredictionModelType.STREAK_MARKOV to 0.20
        )

        var expectedMultSum = 0.0
        var confidenceSum = 0.0
        var totalWeight = 0.0

        for ((type, model) in modelOutputs) {
            val w = weights[type] ?: 0.25
            expectedMultSum += model.expectedMultiplier * w
            confidenceSum += model.confidenceScore * w
            totalWeight += w
        }

        val finalExpectedMult = if (totalWeight > 0) expectedMultSum / totalWeight else 1.85
        val ensembleConfidence = if (totalWeight > 0) confidenceSum / totalWeight else 0.45

        // Determine recommended range matching final expected multiplier
        val targetRange = STANDARD_RANGES.find { finalExpectedMult in it.second..it.third }
            ?: STANDARD_RANGES.first()

        val riskLevel = when {
            finalExpectedMult >= 5.0 -> "Extreme Risk"
            finalExpectedMult >= 2.5 -> "High Risk"
            finalExpectedMult >= 1.7 -> "Moderate Risk"
            else -> "Conservative"
        }

        return EnsemblePrediction(
            recommendedRange = targetRange.first,
            rangeMin = targetRange.second,
            rangeMax = targetRange.third,
            expectedMultiplier = ((finalExpectedMult * 100).toInt()) / 100.0,
            ensembleConfidence = ((ensembleConfidence * 1000).toInt()) / 1000.0,
            modelOutputs = modelOutputs,
            riskLevel = riskLevel
        )
    }

    private fun predictFrequencyModel(multipliers: List<Double>): ModelPredictionResult {
        val total = multipliers.size.toDouble()
        val probs = STANDARD_RANGES.map { (label, minVal, maxVal) ->
            val count = multipliers.count { it in minVal..maxVal }
            val hitRate = if (total > 0) (count / total) * 100.0 else 20.0
            RangeProbability(
                rangeLabel = label,
                rangeMin = minVal,
                rangeMax = maxVal,
                probabilityPercent = hitRate,
                historicalHitRate = hitRate
            )
        }

        val topRange = probs.maxByOrNull { it.probabilityPercent } ?: probs.first()
        val expected = (topRange.rangeMin + min(topRange.rangeMax, 10.0)) / 2.0
        val confidence = min(0.75, max(0.30, topRange.probabilityPercent / 100.0))

        return ModelPredictionResult(
            modelType = PredictionModelType.FREQUENCY,
            predictedRangeMin = topRange.rangeMin,
            predictedRangeMax = topRange.rangeMax,
            predictedRangeLabel = topRange.rangeLabel,
            expectedMultiplier = expected,
            confidenceScore = confidence,
            probabilities = probs,
            rationale = "Highest historical occurrence in sample: ${topRange.rangeLabel} (${String.format("%.1f", topRange.probabilityPercent)}% density)."
        )
    }

    private fun predictRollingWindowModel(multipliers: List<Double>): ModelPredictionResult {
        // Exponential moving average favoring most recent entries
        var ema = multipliers.firstOrNull() ?: 1.5
        val alpha = 0.25 // smoothing factor
        for (m in multipliers) {
            ema = alpha * m + (1 - alpha) * ema
        }

        // Clamp EMA within reasonable crash expectations
        val clampedEma = max(1.10, min(15.0, ema))
        val targetRange = STANDARD_RANGES.find { clampedEma in it.second..it.third }
            ?: STANDARD_RANGES.first()

        val probs = STANDARD_RANGES.map { (label, minVal, maxVal) ->
            val dist = kotlin.math.abs(((minVal + min(maxVal, 10.0)) / 2.0) - clampedEma)
            val score = exp(-dist / 1.5) * 100.0
            RangeProbability(
                rangeLabel = label,
                rangeMin = minVal,
                rangeMax = maxVal,
                probabilityPercent = score,
                historicalHitRate = score
            )
        }
        val totalProb = probs.sumOf { it.probabilityPercent }
        val normalizedProbs = probs.map {
            it.copy(probabilityPercent = if (totalProb > 0) (it.probabilityPercent / totalProb) * 100.0 else 20.0)
        }

        return ModelPredictionResult(
            modelType = PredictionModelType.ROLLING_WINDOW,
            predictedRangeMin = targetRange.second,
            predictedRangeMax = targetRange.third,
            predictedRangeLabel = targetRange.first,
            expectedMultiplier = ((clampedEma * 100).toInt()) / 100.0,
            confidenceScore = 0.48,
            probabilities = normalizedProbs,
            rationale = "Exponential Moving Average (EMA) indicates dynamic momentum centered at ${String.format("%.2f", clampedEma)}x."
        )
    }

    private fun predictPercentileModel(multipliers: List<Double>): ModelPredictionResult {
        val sorted = multipliers.sorted()
        val n = sorted.size
        val median = if (n % 2 == 1) sorted[n / 2] else (sorted[max(0, n / 2 - 1)] + sorted[n / 2]) / 2.0

        val targetRange = STANDARD_RANGES.find { median in it.second..it.third }
            ?: STANDARD_RANGES.first()

        val probs = STANDARD_RANGES.map { (label, minVal, maxVal) ->
            val inRange = sorted.count { it in minVal..maxVal }
            val pct = if (n > 0) (inRange.toDouble() / n) * 100.0 else 20.0
            RangeProbability(
                rangeLabel = label,
                rangeMin = minVal,
                rangeMax = maxVal,
                probabilityPercent = pct,
                historicalHitRate = pct
            )
        }

        return ModelPredictionResult(
            modelType = PredictionModelType.PERCENTILE,
            predictedRangeMin = targetRange.second,
            predictedRangeMax = targetRange.third,
            predictedRangeLabel = targetRange.first,
            expectedMultiplier = median,
            confidenceScore = 0.44,
            probabilities = probs,
            rationale = "Sample median $median evaluates 50th percentile distribution baseline."
        )
    }

    private fun predictStreakMarkovModel(rounds: List<GameRound>): ModelPredictionResult {
        // Chronological
        val chrono = rounds.sortedBy { it.timestamp }
        if (chrono.size < 5) {
            val defaultRange = STANDARD_RANGES[1]
            return ModelPredictionResult(
                modelType = PredictionModelType.STREAK_MARKOV,
                predictedRangeMin = defaultRange.second,
                predictedRangeMax = defaultRange.third,
                predictedRangeLabel = defaultRange.first,
                expectedMultiplier = 1.75,
                confidenceScore = 0.35,
                probabilities = STANDARD_RANGES.map {
                    RangeProbability(it.first, it.second, it.third, 20.0, 20.0)
                },
                rationale = "Insufficient round sequence for Markov transition matrix calculation."
            )
        }

        // States: 0 = Low (<2.0x), 1 = High (>=2.0x)
        val states = chrono.map { if (it.multiplier < 2.0) 0 else 1 }
        val lastState = states.last()

        var t00 = 0
        var t01 = 0
        var t10 = 0
        var t11 = 0

        for (i in 0 until states.size - 1) {
            val curr = states[i]
            val next = states[i + 1]
            if (curr == 0 && next == 0) t00++
            if (curr == 0 && next == 1) t01++
            if (curr == 1 && next == 0) t10++
            if (curr == 1 && next == 1) t11++
        }

        val probNextIsLow = if (lastState == 0) {
            val total0 = t00 + t01
            if (total0 > 0) t00.toDouble() / total0 else 0.55
        } else {
            val total1 = t10 + t11
            if (total1 > 0) t10.toDouble() / total1 else 0.50
        }

        val predictedRange = if (probNextIsLow >= 0.50) STANDARD_RANGES[0] else STANDARD_RANGES[2]
        val expectedMult = if (probNextIsLow >= 0.50) 1.35 else 2.40

        val probs = listOf(
            RangeProbability(STANDARD_RANGES[0].first, STANDARD_RANGES[0].second, STANDARD_RANGES[0].third, probNextIsLow * 60.0, probNextIsLow * 60.0),
            RangeProbability(STANDARD_RANGES[1].first, STANDARD_RANGES[1].second, STANDARD_RANGES[1].third, probNextIsLow * 40.0, probNextIsLow * 40.0),
            RangeProbability(STANDARD_RANGES[2].first, STANDARD_RANGES[2].second, STANDARD_RANGES[2].third, (1.0 - probNextIsLow) * 55.0, (1.0 - probNextIsLow) * 55.0),
            RangeProbability(STANDARD_RANGES[3].first, STANDARD_RANGES[3].second, STANDARD_RANGES[3].third, (1.0 - probNextIsLow) * 30.0, (1.0 - probNextIsLow) * 30.0),
            RangeProbability(STANDARD_RANGES[4].first, STANDARD_RANGES[4].second, STANDARD_RANGES[4].third, (1.0 - probNextIsLow) * 15.0, (1.0 - probNextIsLow) * 15.0)
        )

        return ModelPredictionResult(
            modelType = PredictionModelType.STREAK_MARKOV,
            predictedRangeMin = predictedRange.second,
            predictedRangeMax = predictedRange.third,
            predictedRangeLabel = predictedRange.first,
            expectedMultiplier = expectedMult,
            confidenceScore = max(0.35, min(0.65, if (probNextIsLow >= 0.50) probNextIsLow else 1.0 - probNextIsLow)),
            probabilities = probs,
            rationale = "State transition matrix: Last state = ${if (lastState == 0) "<2.0x" else ">=2.0x"}, P(Low State) = ${String.format("%.1f", probNextIsLow * 100)}%."
        )
    }

    private fun fallbackPrediction(): EnsemblePrediction {
        val defaultRange = STANDARD_RANGES[0]
        return EnsemblePrediction(
            recommendedRange = defaultRange.first,
            rangeMin = defaultRange.second,
            rangeMax = defaultRange.third,
            expectedMultiplier = 1.65,
            ensembleConfidence = 0.40,
            modelOutputs = emptyMap(),
            riskLevel = "Moderate Risk"
        )
    }
}
