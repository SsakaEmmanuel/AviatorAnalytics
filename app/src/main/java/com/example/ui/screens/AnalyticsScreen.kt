package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.engine.PredictionEngine
import com.example.ui.components.MetricCard
import com.example.ui.components.MultiplierDistributionChart
import com.example.ui.components.PercentilesTableCard
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val analytics by viewModel.analyticsState.collectAsStateWithLifecycle()
    val allRounds by viewModel.allRounds.collectAsStateWithLifecycle()
    val recentRounds by viewModel.recentRounds.collectAsStateWithLifecycle()
    val prediction by viewModel.predictionState.collectAsStateWithLifecycle()
    val siteSyncState by viewModel.siteSyncState.collectAsStateWithLifecycle()

    var calculatedNextFigure by remember { mutableStateOf<Double?>(null) }
    var nextRangeLabel by remember { mutableStateOf<String?>("Range: 1.50x – 2.50x") }
    var roundCycleIndex by remember { mutableIntStateOf(101) }
    var isPredictingNext by remember { mutableStateOf(false) }
    var isAutoSyncActive by remember { mutableStateOf(true) }
    var showExportLogsDialog by remember { mutableStateOf(false) }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Function to calculate next figure
    fun calculateNextFigure(manualTrigger: Boolean = false) {
        isPredictingNext = true
        coroutineScope.launch {
            if (manualTrigger) {
                delay(200)
            }
            val currentList = allRounds
            val pred = if (currentList.isNotEmpty()) {
                PredictionEngine.predictNext(currentList, 50)
            } else {
                null
            }

            val baseTarget = pred?.expectedMultiplier ?: 1.95
            val seedVariance = ((System.currentTimeMillis() % 40) - 20) / 100.0
            val adjustedFigure = (baseTarget + seedVariance).coerceIn(1.15, 9.80)

            calculatedNextFigure = adjustedFigure
            nextRangeLabel = pred?.recommendedRange ?: "Range: 1.50x – 2.50x"
            isPredictingNext = false
            roundCycleIndex++

            if (manualTrigger) {
                Toast.makeText(context, "Next Flight Figure calculated: ${String.format(Locale.US, "%.2f", adjustedFigure)}x", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto calculate when new flight rounds arrive
    LaunchedEffect(allRounds.size) {
        if (allRounds.isNotEmpty()) {
            calculateNextFigure(manualTrigger = false)
        }
    }

    // Function to copy flight logs to Clipboard
    fun copyFlightLogs(formatType: String) {
        val listToCopy = recentRounds
        if (listToCopy.isEmpty()) {
            Toast.makeText(context, "No flight rounds available to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val textToCopy = when (formatType) {
            "CSV" -> {
                listToCopy.joinToString(", ") { "${String.format(Locale.US, "%.2f", it.multiplier)}x" }
            }
            "JSON" -> {
                val items = listToCopy.joinToString(",\n  ") { item ->
                    """{"id": "#${item.roundId}", "multiplier": ${item.multiplier}, "time": "${item.formattedTime}", "target_150_hit": ${item.multiplier >= 1.50}}"""
                }
                "[\n  $items\n]"
            }
            else -> {
                val sb = StringBuilder()
                sb.append("=== AVIATOR ANALYTICS LIVE FLIGHT LOGS ===\n")
                sb.append("Total Intercepted Flights: ${listToCopy.size}\n")
                sb.append("Exported At: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                sb.append("-----------------------------------------\n")
                listToCopy.forEach { item ->
                    val status = if (item.multiplier >= 1.50) "✅ 1.50x WON" else "❌ BUST (<1.50x)"
                    sb.append("#${item.roundId} | ${item.formattedTime} | ${String.format(Locale.US, "%.2f", item.multiplier)}x | $status\n")
                }
                sb.toString()
            }
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Aviator Flight Logs", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied ${listToCopy.size} flight logs ($formatType) to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // =========================================================================
        // 1. TWO-DIVISION UPPER SECTION TRANSFERRED FROM LIVE TAB:
        //    LEFT DIVISION: NEXT FLIGHT DATA DIVISION (FIGURE & CALCULATE BUTTON)
        //    RIGHT DIVISION: LIVE DATA DIVISION (REAL-TIME MULTIPLIER STREAM)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LEFT DIVISION: NEXT FLIGHT DATA DIVISION
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(CyberCyan.copy(alpha = 0.85f), PurpleModel.copy(alpha = 0.6f))
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "NEXT FLIGHT #$roundCycleIndex",
                                color = CyberCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { isAutoSyncActive = !isAutoSyncActive }
                                .background(
                                    if (isAutoSyncActive) CyberCyan.copy(alpha = 0.2f) else SlateSurfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .alpha(if (isAutoSyncActive) pulseAlpha else 0.5f)
                                    .background(if (isAutoSyncActive) CyanBright else SlateTextMuted, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isAutoSyncActive) "LIVE AUTO" else "PAUSED",
                                color = if (isAutoSyncActive) CyanBright else SlateTextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        val figureText = calculatedNextFigure?.let { String.format(Locale.US, "%.2f", it) + "x" } ?: "2.34x"
                        val figureValue = calculatedNextFigure ?: 2.34
                        val figureColor = when {
                            figureValue >= 5.0 -> PurpleModel
                            figureValue >= 2.0 -> EmeraldSafe
                            figureValue >= 1.5 -> AmberMedium
                            else -> RubyCrash
                        }

                        Text(
                            text = figureText,
                            color = figureColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = nextRangeLabel ?: "Range: 1.50x – 2.50x",
                            color = SlateTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { calculateNextFigure(manualTrigger = true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = SlateCard
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 5.dp)
                    ) {
                        if (isPredictingNext) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Calculating",
                                tint = SlateCard,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "CALCULATING...",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SlateCard,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "NEXT FLIGHT FIGURE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // RIGHT DIVISION: LIVE DATA DIVISION (REAL-TIME INTERCEPTED FLIGHTS)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(EmeraldSafe.copy(alpha = 0.85f), SlateBorder)
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldSafe,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "LIVE FLIGHT DATA",
                                color = EmeraldSafe,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                calculateNextFigure(manualTrigger = true)
                            },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Fetch latest",
                                tint = SlateTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    val displayRounds = recentRounds.take(6)
                    if (displayRounds.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Awaiting live flights...",
                                color = SlateTextMuted,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                displayRounds.take(3).forEach { round ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = when {
                                                    round.multiplier >= 10.0 -> PurpleModel.copy(alpha = 0.25f)
                                                    round.multiplier >= 2.0 -> EmeraldSafe.copy(alpha = 0.22f)
                                                    else -> RubyCrash.copy(alpha = 0.22f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                width = 0.8.dp,
                                                color = when {
                                                    round.multiplier >= 10.0 -> PurpleModel
                                                    round.multiplier >= 2.0 -> EmeraldSafe
                                                    else -> RubyCrash
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.2f", round.multiplier) + "x",
                                            color = when {
                                                round.multiplier >= 10.0 -> PurpleModel
                                                round.multiplier >= 2.0 -> EmeraldSafe
                                                else -> RubyCrash
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                displayRounds.drop(3).take(3).forEach { round ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = when {
                                                    round.multiplier >= 10.0 -> PurpleModel.copy(alpha = 0.18f)
                                                    round.multiplier >= 2.0 -> EmeraldSafe.copy(alpha = 0.18f)
                                                    else -> RubyCrash.copy(alpha = 0.18f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                width = 0.8.dp,
                                                color = when {
                                                    round.multiplier >= 10.0 -> PurpleModel.copy(alpha = 0.6f)
                                                    round.multiplier >= 2.0 -> EmeraldSafe.copy(alpha = 0.6f)
                                                    else -> RubyCrash.copy(alpha = 0.6f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.2f", round.multiplier) + "x",
                                            color = when {
                                                round.multiplier >= 10.0 -> PurpleModel
                                                round.multiplier >= 2.0 -> EmeraldSafe
                                                else -> RubyCrash
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avg = if (displayRounds.isNotEmpty()) displayRounds.map { it.multiplier }.average() else 0.0
                        Text(
                            text = "AVG: ${String.format(Locale.US, "%.2f", avg)}x",
                            color = SlateTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "MAX: ${String.format(Locale.US, "%.2f", displayRounds.maxOfOrNull { it.multiplier } ?: 0.0)}x",
                            color = SlateTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 2. EXPORT & LOG RECORDER PANEL FOR ANALYSTS
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(CyberCyan.copy(alpha = 0.6f), PurpleModel.copy(alpha = 0.6f)))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "FLIGHT LOGS RECORDER",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "(${allRounds.size} Flights)",
                        color = EmeraldSafe,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { copyFlightLogs("TABLE") },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = SlateCard,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "COPY LOGS",
                            color = SlateCard,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    OutlinedButton(
                        onClick = { showExportLogsDialog = true },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateCard),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataArray,
                            contentDescription = "Formats",
                            tint = CyberCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "FORMATS",
                            color = SlateTextPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 3. EMPIRICAL PARAMETERS SUMMARY HEADER
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(CyberCyan.copy(alpha = 0.4f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EMPIRICAL PARAMETERS",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "N = ${analytics?.sampleSize ?: allRounds.size} Rounds",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Crash Game Probability Distribution",
                    color = SlateTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Computed from verified historical database entries using heavy-tail Pareto distribution equations.",
                    color = SlateTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // =========================================================================
        // 4. PRIMARY METRICS GRID
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Sample Mean (μ)",
                value = if (analytics != null) "${String.format(Locale.US, "%.2f", analytics!!.mean)}x" else "--.--x",
                subtitle = "Arithmetic average",
                valueColor = EmeraldSafe,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Sample Median",
                value = if (analytics != null) "${String.format(Locale.US, "%.2f", analytics!!.median)}x" else "--.--x",
                subtitle = "50th percentile (P50)",
                valueColor = CyberCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Std Deviation (σ)",
                value = if (analytics != null) String.format(Locale.US, "%.2f", analytics!!.standardDeviation) else "--",
                subtitle = "Sample dispersion",
                valueColor = PurpleModel,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Volatility Ratio",
                value = if (analytics != null) String.format(Locale.US, "%.2f", analytics!!.volatilityIndex) else "--",
                subtitle = "σ / μ normalized risk",
                valueColor = AmberMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Min Observed",
                value = if (analytics != null) "${String.format(Locale.US, "%.2f", analytics!!.minMultiplier)}x" else "--",
                subtitle = "Lowest multiplier",
                valueColor = RubyCrash,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Max Observed",
                value = if (analytics != null) "${String.format(Locale.US, "%.2f", analytics!!.maxMultiplier)}x" else "--",
                subtitle = "Peak historical multiplier",
                valueColor = CyanBright,
                modifier = Modifier.weight(1f)
            )
        }

        // Distribution Chart
        if (analytics != null) {
            MultiplierDistributionChart(distributionRanges = analytics!!.distributionRanges)
        }

        // Percentiles Table
        if (analytics != null) {
            PercentilesTableCard(percentiles = analytics!!.percentiles)
        }

        // Streak Analysis Card
        if (analytics != null) {
            val s = analytics!!.streakInfo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(SlateBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CONSECUTIVE RUN & STREAK DYNAMICS",
                        color = SlateTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StreakMetricBox("Current <2.0x Streak", "${s.currentCrashStreak}", RubyCrash, Modifier.weight(1f))
                        StreakMetricBox("Max <2.0x Streak", "${s.maxCrashStreak}", RubyCrash, Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StreakMetricBox("Current ≥2.0x Streak", "${s.currentFlyStreak}", EmeraldSafe, Modifier.weight(1f))
                        StreakMetricBox("Max ≥2.0x Streak", "${s.maxFlyStreak}", EmeraldSafe, Modifier.weight(1f))
                    }
                }
            }
        }

        StatisticalDisclaimerBanner()
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Export Logs Dialog
    if (showExportLogsDialog) {
        AlertDialog(
            onDismissRequest = { showExportLogsDialog = false },
            title = {
                Text(
                    text = "Export Flight Logs",
                    color = SlateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select format to copy ${recentRounds.size} active flight rounds to clipboard:",
                        color = SlateTextSecondary,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = {
                            showExportLogsDialog = false
                            copyFlightLogs("TABLE")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("Formatted Text Table (.txt)", color = SlateCard, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showExportLogsDialog = false
                            copyFlightLogs("CSV")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant)
                    ) {
                        Text("CSV (Comma Separated Multipliers)", color = CyberCyan, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showExportLogsDialog = false
                            copyFlightLogs("JSON")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant)
                    ) {
                        Text("JSON Array Data (.json)", color = PurpleModel, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportLogsDialog = false }) {
                    Text("Close", color = CyberCyan)
                }
            },
            containerColor = SlateCard
        )
    }
}

@Composable
private fun StreakMetricBox(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSurfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = SlateTextMuted, fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
