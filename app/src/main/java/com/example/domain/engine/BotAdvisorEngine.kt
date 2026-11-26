package com.example.domain.engine

import com.example.domain.model.GameRound
import kotlin.math.max
import kotlin.math.min

enum class BotAction {
    ENTER_NOW,     // Strong safe signal to enter round @ 1.50x
    ENTER_CAUTIOUS, // Favorable but moderate caution @ 1.50x
    WAIT_AND_WATCH, // Unstable momentum, skip this round
    HIGH_RISK_AVOID // Multiple low crashes or cold streak active, do not enter
}

enum class BotRiskLevel {
    LOW_RISK,      // High confidence for 1.50x exit (>72% win prob)
    MEDIUM_RISK,   // Standard statistical range for 1.50x exit (58% - 72%)
    HIGH_RISK      // Elevated crash danger (<58% win prob)
}

data class BotDecision(
    val action: BotAction,
    val targetOdd: Double = 1.50,
    val winProbabilityPercent: Double,
    val riskLevel: BotRiskLevel,
    val reasonTitle: String,
    val detailedKnowledgeAnalysis: String,
    val recommendedExitOdd: Double = 1.50,
    val consecutiveUnder150Count: Int,
    val recentSuccessRateAt150: Double,
    val safeEntryScore: Int // 0 to 100
)

object BotAdvisorEngine {

    const val TARGET_BET_ODD: Double = 1.50

    /**
     * Evaluates live game telemetry and historical rounds to give specific
     * Aviator entry/exit decisions calibrated to the 1.50x bet odd risk level.
     */
    fun evaluateEntry(rounds: List<GameRound>): BotDecision {
        if (rounds.isEmpty()) {
            return BotDecision(
                action = BotAction.WAIT_AND_WATCH,
                targetOdd = TARGET_BET_ODD,
                winProbabilityPercent = 64.0,
                riskLevel = BotRiskLevel.MEDIUM_RISK,
                reasonTitle = "Calibrating with Site Stream",
                detailedKnowledgeAnalysis = "Analyzing incoming round stream to build statistical certainty for 1.50x cashout target.",
                recommendedExitOdd = TARGET_BET_ODD,
                consecutiveUnder150Count = 0,
                recentSuccessRateAt150 = 65.0,
                safeEntryScore = 50
            )
        }

        val sample = rounds.take(30)
        val multipliers = sample.map { it.multiplier }

        // 1. Success rate at 1.50x over last 20-30 rounds
        val countAtLeast150 = multipliers.count { it >= TARGET_BET_ODD }
        val successRate150 = (countAtLeast150.toDouble() / multipliers.size) * 100.0

        // 2. Count consecutive rounds under 1.50x at the very front of stream (latest rounds)
        var streakUnder150 = 0
        for (m in multipliers) {
            if (m < TARGET_BET_ODD) {
                streakUnder150++
            } else {
                break
            }
        }

        // 3. Rolling EMA of recent 5 rounds
        val recent5 = multipliers.take(5)
        val recentMean = if (recent5.isNotEmpty()) recent5.average() else 1.80
        val lastMultiplier = multipliers.firstOrNull() ?: 1.50

        // 4. Calculate Safe Entry Score (0 - 100)
        var score = 50

        // Success rate contribution (+/- 20)
        if (successRate150 >= 70.0) score += 18
        else if (successRate150 >= 60.0) score += 8
        else if (successRate150 < 45.0) score -= 18

        // Streak dynamics
        when (streakUnder150) {
            0 -> {
                // Last round was >= 1.50x
                if (lastMultiplier >= 2.50) {
                    // Strong round just happened, momentum is healthy
                    score += 12
                } else {
                    score += 6
                }
            }
            1 -> {
                // One low crash (<1.50x) just occurred - standard variance
                score += 8 // high probability of bounce back over 1.50x
            }
            2 -> {
                // Two consecutive low crashes - mean reversion bounce zone
                score += 15
            }
            3 -> {
                // Three consecutive low crashes - potential cold cluster
                score -= 10
            }
            else -> {
                // 4+ crashes under 1.50x - extreme cold cluster, avoid until breakout
                score -= 30
            }
        }

        // Recent average contribution
        if (recentMean >= 2.0) score += 10
        else if (recentMean < 1.30) score -= 15

        // Clamp score between 5 and 95
        score = max(5, min(95, score))

        // Calculate estimated Win Probability at 1.50x
        val estimatedWinProb = max(38.0, min(88.0, 52.0 + (score - 50) * 0.45))

        val riskLevel = when {
            score >= 70 -> BotRiskLevel.LOW_RISK
            score >= 48 -> BotRiskLevel.MEDIUM_RISK
            else -> BotRiskLevel.HIGH_RISK
        }

        val (action, title, analysis) = when {
            score >= 75 -> Triple(
                BotAction.ENTER_NOW,
                "STRONG ENTER SIGNAL @ 1.50x",
                "Favorable volatility curve detected. High mathematical probability of clearing the 1.50x safe cashout threshold."
            )
            score >= 58 -> Triple(
                BotAction.ENTER_CAUTIOUS,
                "FAVORABLE ENTRY @ 1.50x",
                "Momentum is aligned with 1.50x target. Stick strictly to auto-cashout at 1.50x odd."
            )
            score >= 42 -> Triple(
                BotAction.WAIT_AND_WATCH,
                "STANDBY / WAIT FOR CLEARANCE",
                "Current round cluster is transitional. Recommend waiting 1 round for confirmation above 1.50x."
            )
            else -> Triple(
                BotAction.HIGH_RISK_AVOID,
                "HIGH CRASH RISK - DO NOT ENTER",
                "Cold cluster active ($streakUnder150 consecutive rounds under 1.50x). Protect capital and wait for cluster to break."
            )
        }

        return BotDecision(
            action = action,
            targetOdd = TARGET_BET_ODD,
            winProbabilityPercent = ((estimatedWinProb * 10).toInt()) / 10.0,
            riskLevel = riskLevel,
            reasonTitle = title,
            detailedKnowledgeAnalysis = analysis,
            recommendedExitOdd = TARGET_BET_ODD,
            consecutiveUnder150Count = streakUnder150,
            recentSuccessRateAt150 = ((successRate150 * 10).toInt()) / 10.0,
            safeEntryScore = score
        )
    }
}
