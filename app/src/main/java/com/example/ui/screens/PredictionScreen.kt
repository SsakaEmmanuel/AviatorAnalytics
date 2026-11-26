package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.PredictionModelType
import com.example.ui.components.MetricCard
import com.example.ui.components.StatisticalDisclaimerBanner
import com.example.ui.theme.AmberMedium
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PurpleModel
import com.example.ui.theme.RubyCrash
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.GameViewModel

import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import com.example.domain.engine.BotAction
import com.example.domain.engine.BotAdvisorEngine
import com.example.domain.engine.BotRiskLevel

@Composable
fun PredictionScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val prediction by viewModel.predictionState.collectAsStateWithLifecycle()
    val windowSize by viewModel.predictionWindowSize.collectAsStateWithLifecycle()
    val botDecision by viewModel.botDecisionState.collectAsStateWithLifecycle()
    val allRounds by viewModel.allRounds.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val modelTabs = listOf(
        PredictionModelType.ENSEMBLE,
        PredictionModelType.FREQUENCY,
        PredictionModelType.ROLLING_WINDOW,
        PredictionModelType.PERCENTILE,
        PredictionModelType.STREAK_MARKOV
    )

    val currentModelType = modelTabs[selectedTabIndex]

    // Active model data
    val activeResult = if (currentModelType == PredictionModelType.ENSEMBLE) {
        null
    } else {
        prediction?.modelOutputs?.get(currentModelType)
    }

    val displayRange = if (currentModelType == PredictionModelType.ENSEMBLE) {
        prediction?.recommendedRange ?: "1.50x – 1.99x"
    } else {
        activeResult?.predictedRangeLabel ?: "1.50x – 1.99x"
    }

    val displayExpected = if (currentModelType == PredictionModelType.ENSEMBLE) {
        prediction?.expectedMultiplier ?: 1.75
    } else {
        activeResult?.expectedMultiplier ?: 1.75
    }

    val displayConfidence = if (currentModelType == PredictionModelType.ENSEMBLE) {
        prediction?.ensembleConfidence ?: 0.48
    } else {
        activeResult?.confidenceScore ?: 0.45
    }

    val displayRationale = if (currentModelType == PredictionModelType.ENSEMBLE) {
        "Weighted bayesian ensemble combining Empirical Frequency (25%), Rolling EMA (30%), Quantile Estimation (25%), and Markov Transition states (20%)."
    } else {
        activeResult?.rationale ?: currentModelType.description
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Window Size Selector Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(SlateBorder)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Text("Sample Window Size", color = SlateTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(15, 30, 50, 100).forEach { size ->
                        val isSelected = windowSize == size
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyberCyan else SlateSurfaceVariant)
                                .clickable { viewModel.setPredictionWindowSize(size) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$size",
                                color = if (isSelected) Color.Black else SlateTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Artificial Bot 1.50x Bet Odd Entry & Risk Radar Card
        val activeBot = botDecision ?: BotAdvisorEngine.evaluateEntry(allRounds)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (activeBot.riskLevel) {
                    BotRiskLevel.LOW_RISK -> EmeraldSafe.copy(alpha = 0.12f)
                    BotRiskLevel.MEDIUM_RISK -> AmberMedium.copy(alpha = 0.12f)
                    BotRiskLevel.HIGH_RISK -> RubyCrash.copy(alpha = 0.12f)
                }
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(
                        when (activeBot.riskLevel) {
                            BotRiskLevel.LOW_RISK -> EmeraldSafe
                            BotRiskLevel.MEDIUM_RISK -> AmberMedium
                            BotRiskLevel.HIGH_RISK -> RubyCrash
                        },
                        PurpleModel
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BOT 1.50x ENTRY DECISION ENGINE",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (activeBot.action) {
                                    BotAction.ENTER_NOW -> EmeraldSafe
                                    BotAction.ENTER_CAUTIOUS -> EmeraldSafe.copy(alpha = 0.8f)
                                    BotAction.WAIT_AND_WATCH -> AmberMedium
                                    BotAction.HIGH_RISK_AVOID -> RubyCrash
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when (activeBot.action) {
                                BotAction.ENTER_NOW -> "🟢 ENTER @ 1.50x"
                                BotAction.ENTER_CAUTIOUS -> "🟢 ENTER CAUTIOUS"
                                BotAction.WAIT_AND_WATCH -> "🟡 WAIT / STANDBY"
                                BotAction.HIGH_RISK_AVOID -> "🔴 SKIP ROUND"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Target Odd: 1.50x",
                            color = SlateTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Risk Level: ${activeBot.riskLevel.name.replace("_", " ")}",
                            color = when (activeBot.riskLevel) {
                                BotRiskLevel.LOW_RISK -> EmeraldSafe
                                BotRiskLevel.MEDIUM_RISK -> AmberMedium
                                BotRiskLevel.HIGH_RISK -> RubyCrash
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("1.50x Win Prob", color = SlateTextMuted, fontSize = 10.sp)
                        Text(
                            text = "${activeBot.winProbabilityPercent}%",
                            color = EmeraldSafe,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Text(
                    text = activeBot.detailedKnowledgeAnalysis,
                    color = SlateTextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Model Selection Tabs
        ScrollableModelTabs(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            modelTabs = modelTabs
        )

        // Primary Prediction Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyberCyan.copy(alpha = 0.5f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATISTICAL MULTIPLIER ESTIMATE",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentModelType.displayName,
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = displayRange,
                            color = SlateTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Target Median: ${String.format("%.2f", displayExpected)}x",
                            color = SlateTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Confidence Gauge", color = SlateTextMuted, fontSize = 10.sp)
                        Text(
                            text = "${((displayConfidence) * 100).toInt()}%",
                            color = EmeraldSafe,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }

                // Confidence Bar
                LinearProgressIndicator(
                    progress = { displayConfidence.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EmeraldSafe,
                    trackColor = SlateSurfaceVariant,
                )
            }
        }

        // Model Rationale & Technical Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(SlateBorder)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Text(
                        text = "MODEL METHODOLOGY & RATIONALE",
                        color = SlateTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = displayRationale,
                    color = SlateTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Density & Probability Breakdown
        val probs = activeResult?.probabilities ?: prediction?.modelOutputs?.values?.firstOrNull()?.probabilities ?: emptyList()
        if (probs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SlateBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "RANGE PROBABILITY SPECTRUM",
                        color = SlateTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    probs.forEach { p ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(p.rangeLabel, color = SlateTextPrimary, fontSize = 12.sp)
                                Text(
                                    "${String.format("%.1f", p.probabilityPercent)}%",
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SlateSurfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth((p.probabilityPercent / 100.0).toFloat().coerceIn(0f, 1f))
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberCyan)
                                )
                            }
                        }
                    }
                }
            }
        }

        StatisticalDisclaimerBanner()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ScrollableModelTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modelTabs: List<PredictionModelType>
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modelTabs.forEachIndexed { index, model ->
            val isSelected = selectedTabIndex == index
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                label = {
                    Text(
                        text = model.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                    selectedLabelColor = CyberCyan,
                    containerColor = SlateCard,
                    labelColor = SlateTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = SlateBorder,
                    selectedBorderColor = CyberCyan
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}
