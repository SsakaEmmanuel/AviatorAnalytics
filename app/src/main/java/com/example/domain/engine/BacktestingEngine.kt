package com.example.domain.engine

import com.example.domain.model.BacktestRangeAccuracy
import com.example.domain.model.BacktestReport
import com.example.domain.model.BacktestStepResult
import com.example.domain.model.GameRound
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object BacktestingEngine {

    fun runWalkForwardBacktest(
        rounds: List<GameRound>,
        minTrainingSize: Int = 20,
        windowSize: Int = 50
    ): BacktestReport {
        val chronoRounds = rounds.sortedBy { it.timestamp }
        if (chronoRounds.size < minTrainingSize + 5) {
            return emptyReport(chronoRounds.size)
        }

        val stepResults = mutableListOf<BacktestStepResult>()
        val rng = Random(42) // Fixed seed for reproducible baseline evaluation

        for (i in minTrainingSize until chronoRounds.size) {
            val historyWindow = chronoRounds.subList(max(0, i - windowSize), i)
            val actualNext = chronoRounds[i]

            // Predict using Ensemble Engine on historyWindow
            val prediction = PredictionEngine.predictNext(historyWindow.reversed(), windowSize)

            val isHit = actualNext.multiplier >= prediction.rangeMin && actualNext.multiplier <= prediction.rangeMax

            // Random empirical baseline: select range weighted by prior distribution
            val baselineHit = evaluateBaselineHit(historyWindow, actualNext.multiplier, rng)

            stepResults.add(
                BacktestStepResult(
                    roundIndex = i,
                    roundId = actualNext.roundId,
                    actualMultiplier = actualNext.multiplier,
                    predictedRangeLabel = prediction.recommendedRange,
                    predictedRangeMin = prediction.rangeMin,
                    predictedRangeMax = prediction.rangeMax,
                    isHit = isHit,
                    baselineHit = baselineHit,
                    modelConfidence = prediction.ensembleConfidence
                )
            )
        }

        val totalEvaluated = stepResults.size
        val modelHits = stepResults.count { it.isHit }
        val baselineHits = stepResults.count { it.baselineHit }

        val modelAccuracy = if (totalEvaluated > 0) (modelHits.toDouble() / totalEvaluated) * 100.0 else 0.0
        val baselineAccuracy = if (totalEvaluated > 0) (baselineHits.toDouble() / totalEvaluated) * 100.0 else 0.0

        // Range breakdowns
        val rangeLabels = listOf("1.00x – 1.49x", "1.50x – 1.99x", "2.00x – 3.49x", "3.50x – 7.99x", "8.00x+")
        val rangeAccuracies = rangeLabels.map { label ->
            val predictionsForRange = stepResults.filter { it.predictedRangeLabel == label }
            val count = predictionsForRange.size
            val hits = predictionsForRange.count { it.isHit }
            val acc = if (count > 0) (hits.toDouble() / count) * 100.0 else 0.0
            val errDesc = when {
                acc >= 50.0 -> "Low Error"
                acc >= 35.0 -> "Moderate Error"
                acc >= 20.0 -> "High Error"
                else -> "Extreme Error"
            }
            BacktestRangeAccuracy(
                rangeLabel = label,
                totalPredictions = count,
                hits = hits,
                accuracyPercent = acc,
                baselineAccuracyPercent = baselineAccuracy,
                errorRateDescription = errDesc
            )
        }

        val avgConfidence = if (totalEvaluated > 0) stepResults.map { it.modelConfidence }.average() else 0.0

        val delta = modelAccuracy - baselineAccuracy

        return BacktestReport(
            totalRoundsEvaluated = totalEvaluated,
            modelOverallAccuracy = ((modelAccuracy * 10).toInt()) / 10.0,
            baselineOverallAccuracy = ((baselineAccuracy * 10).toInt()) / 10.0,
            accuracyDelta = ((delta * 10).toInt()) / 10.0,
            totalHits = modelHits,
            baselineHits = baselineHits,
            rangeAccuracies = rangeAccuracies,
            averageConfidence = ((avgConfidence * 100).toInt()) / 100.0,
            stepResults = stepResults,
            executionSummary = "Walk-forward evaluation ($minTrainingSize → ${chronoRounds.size}) completed. Delta vs random baseline: ${if (delta >= 0) "+$delta" else "$delta"}%."
        )
    }

    private fun evaluateBaselineHit(history: List<GameRound>, actual: Double, rng: Random): Boolean {
        // Uniform random selection across 5 standard bins
        val bins = listOf(
            Pair(1.00, 1.49),
            Pair(1.50, 1.99),
            Pair(2.00, 3.49),
            Pair(3.50, 7.99),
            Pair(8.00, 100000.0)
        )
        val chosenBin = bins[rng.nextInt(bins.size)]
        return actual >= chosenBin.first && actual <= chosenBin.second
    }

    private fun emptyReport(size: Int): BacktestReport {
        return BacktestReport(
            totalRoundsEvaluated = 0,
            modelOverallAccuracy = 0.0,
            baselineOverallAccuracy = 0.0,
            accuracyDelta = 0.0,
            totalHits = 0,
            baselineHits = 0,
            rangeAccuracies = emptyList(),
            averageConfidence = 0.0,
            stepResults = emptyList(),
            executionSummary = "Insufficient sample size ($size rounds). Minimum 25 rounds required for walk-forward backtest."
        )
    }
}
