package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.components.LiveFlightCanvas
import com.example.ui.components.MetricCard
import com.example.ui.components.StatisticalDisclaimerBanner
import com.example.ui.theme.AmberMedium
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.RubyCrash
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.FlightState
import com.example.ui.viewmodel.LiveSimulationViewModel

@Composable
fun LiveDataScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveSimulationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val statusColor = when (uiState.flightState) {
        FlightState.FLYING -> EmeraldSafe
        FlightState.WAITING -> AmberMedium
        FlightState.CRASHED -> RubyCrash
    }

    val statusLabel = when (uiState.flightState) {
        FlightState.FLYING -> "PLANE IN FLIGHT"
        FlightState.WAITING -> "WAITING (NEXT IN ${uiState.countdownSeconds}s)"
        FlightState.CRASHED -> "FLEW AWAY @ ${String.format("%.2f", uiState.lastCrashedMultiplier ?: 1.0)}x"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flight Canvas Frame
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(statusColor.copy(alpha = 0.5f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.FlightTakeoff, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = uiState.targetRoundId,
                        color = SlateTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                // Flight Canvas Visualizer
                LiveFlightCanvas(
                    currentMultiplier = uiState.currentMultiplier,
                    flightProgress = uiState.flightProgress,
                    isCrashed = uiState.flightState == FlightState.CRASHED,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                // Multiplier Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LIVE MULTIPLIER",
                            color = SlateTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.2f", uiState.currentMultiplier)}x",
                            color = if (uiState.flightState == FlightState.CRASHED) RubyCrash else CyanBright,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.togglePause() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isPaused) EmeraldSafe else SlateSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Play/Pause",
                                tint = if (uiState.isPaused) Color.Black else SlateTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isPaused) "Resume" else "Pause",
                                color = if (uiState.isPaused) Color.Black else SlateTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.skipToNext() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next Round", tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Live Telemetry Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Sim Rounds",
                value = "${uiState.totalRoundsSimulated}",
                subtitle = "Saved to local Room DB",
                valueColor = CyberCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "RNG Engine",
                value = "97.0% RTP",
                subtitle = "Provably Fair SHA-256",
                valueColor = EmeraldSafe,
                modifier = Modifier.weight(1f)
            )
        }

        // Live Telemetry Stream Box
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
                Text(
                    text = "REAL-TIME TELEMETRY LOG",
                    color = SlateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                if (uiState.telemetryTicks.isEmpty()) {
                    Text(
                        text = "Awaiting live round launch...",
                        color = SlateTextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    uiState.telemetryTicks.take(5).forEach { tick ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${String.format("%.2f", tick.multiplier)}x",
                                color = CyanBright,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Speed: ${String.format("%.1f", tick.speed)} m/s",
                                color = SlateTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Alt: ${(tick.altitudePct * 100).toInt()}%",
                                color = EmeraldSafe,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        StatisticalDisclaimerBanner()
        Spacer(modifier = Modifier.height(16.dp))
    }
}
