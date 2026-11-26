package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DistributionRange
import com.example.domain.model.GameRound
import com.example.domain.model.Percentiles
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Composable
fun LiveFlightCanvas(
    currentMultiplier: Double,
    flightProgress: Float,
    isCrashed: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = flightProgress,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "flightProgress"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B14))
            .border(1.dp, if (isCrashed) RubyCrash.copy(alpha = 0.6f) else CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Subdued Grid Lines
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.4f)
            val rows = 4
            val cols = 5

            for (i in 1..rows) {
                val y = h * (i.toFloat() / (rows + 1))
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }

            for (j in 1..cols) {
                val x = w * (j.toFloat() / (cols + 1))
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }

            // 2. Draw Parabolic Flight Curve
            val path = Path()
            val startX = 20.dp.toPx()
            val startY = h - 20.dp.toPx()
            path.moveTo(startX, startY)

            val currentX = startX + (w - 60.dp.toPx()) * animatedProgress
            val currentY = startY - (h - 60.dp.toPx()) * (animatedProgress.pow(1.6f))

            val controlX = startX + (currentX - startX) * 0.45f
            val controlY = startY - 5.dp.toPx()

            path.quadraticTo(controlX, controlY, currentX, currentY)

            // Flight Area Fill
            val fillPath = Path().apply {
                addPath(path)
                lineTo(currentX, startY)
                lineTo(startX, startY)
                close()
            }

            val glowBrush = Brush.verticalGradient(
                colors = if (isCrashed) {
                    listOf(RubyCrash.copy(alpha = 0.35f), Color.Transparent)
                } else {
                    listOf(CyberCyan.copy(alpha = 0.35f), Color.Transparent)
                },
                startY = currentY,
                endY = startY
            )
            drawPath(fillPath, brush = glowBrush)

            // Flight Stroke Line
            val strokeColor = if (isCrashed) RubyCrash else CyanBright
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Airplane Head / Crash Marker
            if (isCrashed) {
                drawCircle(
                    color = RubyCrash,
                    radius = 10.dp.toPx(),
                    center = Offset(currentX, currentY)
                )
                drawCircle(
                    color = RubyCrash.copy(alpha = 0.6f),
                    radius = 5.dp.toPx(),
                    center = Offset(currentX, currentY)
                )
            } else {
                // Airplane glow halo
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.4f),
                    radius = 12.dp.toPx(),
                    center = Offset(currentX, currentY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}

@Composable
fun MultiplierDistributionChart(
    distributionRanges: List<DistributionRange>?,
    modifier: Modifier = Modifier
) {
    val defaultRanges = remember {
        listOf(
            DistributionRange("< 1.50x", 1.0, 1.5, 0, 0.0, 33.3),
            DistributionRange("1.50x – 2.00x", 1.5, 2.0, 0, 0.0, 16.7),
            DistributionRange("2.00x – 5.00x", 2.0, 5.0, 0, 0.0, 30.0),
            DistributionRange("5.00x – 10.00x", 5.0, 10.0, 0, 0.0, 10.0),
            DistributionRange("10.00x+", 10.0, 100000.0, 0, 0.0, 10.0)
        )
    }
    val ranges = if (!distributionRanges.isNullOrEmpty()) distributionRanges else defaultRanges
    val isLive = !distributionRanges.isNullOrEmpty()

    Card(
        modifier = modifier.fillMaxWidth(),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EMPIRICAL MULTIPLIER DISTRIBUTION",
                    color = SlateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = if (isLive) "Observed vs Theoretical" else "Standby (Waiting for Detection)",
                    color = SlateTextMuted,
                    fontSize = 11.sp
                )
            }

            ranges.forEach { range ->
                val barColor = when {
                    range.min >= 10.0 -> CyberCyan
                    range.min >= 5.0 -> PurpleModel
                    range.min >= 2.0 -> EmeraldSafe
                    range.min >= 1.5 -> AmberMedium
                    else -> RubyCrash
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = range.label,
                            color = SlateTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isLive) "${range.count} hits (${String.format("%.1f", range.empiricalPercentage)}%)" else "0 hits (--%)",
                            color = if (isLive) barColor else SlateTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Dual Bar: Empirical vs Theoretical
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(SlateSurfaceVariant)
                    ) {
                        // Observed Bar
                        if (isLive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((range.empiricalPercentage / 100.0).toFloat().coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(barColor)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Theoretical: ${String.format("%.1f", range.theoreticalProbability)}%",
                            color = SlateTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PercentilesTableCard(
    percentiles: Percentiles?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = "STATISTICAL QUANTILES & PERCENTILES",
                color = SlateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PercentileCell("P10", percentiles?.p10, RubyCrash, Modifier.weight(1f))
                PercentileCell("P25", percentiles?.p25, AmberMedium, Modifier.weight(1f))
                PercentileCell("P50 (Med)", percentiles?.p50, EmeraldSafe, Modifier.weight(1f))
                PercentileCell("P75", percentiles?.p75, CyberCyan, Modifier.weight(1f))
                PercentileCell("P90", percentiles?.p90, PurpleModel, Modifier.weight(1f))
                PercentileCell("P99", percentiles?.p99, CyanBright, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PercentileCell(
    label: String,
    value: Double?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateSurfaceVariant)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = SlateTextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (value != null) "${String.format("%.2f", value)}x" else "--.--x",
            color = if (value != null) color else SlateTextMuted,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun StreakTimelineVisualizer(
    rounds: List<GameRound>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT OUTCOME SEQUENCE",
                    color = SlateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = if (rounds.isNotEmpty()) "Last ${min(24, rounds.size)} rounds" else "Standby (Awaiting live stream)",
                    color = SlateTextMuted,
                    fontSize = 11.sp
                )
            }

            if (rounds.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(12) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SlateSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = SlateTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            } else {
                val recent = rounds.take(24).reversed()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recent.forEach { r ->
                        val color = when {
                            r.multiplier >= 10.0 -> CyberCyan
                            r.multiplier >= 2.0 -> EmeraldSafe
                            r.multiplier >= 1.5 -> AmberMedium
                            else -> RubyCrash
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RubyCrash))
                    Text("< 1.50x", color = SlateTextMuted, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberMedium))
                    Text("1.50x–1.99x", color = SlateTextMuted, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSafe))
                    Text("2.00x–9.99x", color = SlateTextMuted, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberCyan))
                    Text("10.00x+", color = SlateTextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun ManualEntryKeypad(
    currentInput: String,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onQuickMultiplier: (Double) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Presets Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(1.20, 1.50, 2.00, 3.50, 5.00, 10.0).forEach { preset ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateSurfaceVariant)
                        .clickable { onQuickMultiplier(preset) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${String.format("%.2f", preset)}x",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Numeric Keypad Grid
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "DEL")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (key == "DEL") SlateSurfaceVariant else SlateCard)
                            .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                when (key) {
                                    "DEL" -> onBackspaceClick()
                                    else -> onDigitClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Delete",
                                tint = RubyCrash,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                color = SlateTextPrimary,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onClearClick,
                modifier = Modifier
                    .weight(0.35f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant)
            ) {
                Text("CLEAR", color = SlateTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .weight(0.65f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSafe)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("LOG MULTIPLIER", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
