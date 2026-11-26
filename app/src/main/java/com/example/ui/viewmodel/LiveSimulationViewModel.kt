package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datasource.MockSimulatorDataSource
import com.example.data.repository.GameRepository
import com.example.domain.model.GameRound
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.min

enum class FlightState {
    WAITING,
    FLYING,
    CRASHED
}

data class LiveTelemetryTick(
    val timestamp: Long,
    val multiplier: Double,
    val speed: Double,
    val altitudePct: Float
)

data class LiveSimulationUiState(
    val flightState: FlightState = FlightState.WAITING,
    val currentMultiplier: Double = 1.00,
    val targetMultiplier: Double = 1.00,
    val targetRoundId: String = "BET-100001",
    val countdownSeconds: Int = 3,
    val flightProgress: Float = 0f,
    val isPaused: Boolean = false,
    val telemetryTicks: List<LiveTelemetryTick> = emptyList(),
    val totalRoundsSimulated: Int = 0,
    val lastCrashedMultiplier: Double? = null
)

class LiveSimulationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository.getInstance(application)

    private val _uiState = MutableStateFlow(LiveSimulationUiState())
    val uiState: StateFlow<LiveSimulationUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null

    init {
        startSimulationLoop()
    }

    fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                if (_uiState.value.isPaused) {
                    delay(500)
                    continue
                }

                // 1. WAITING / BETTING PHASE (3s)
                val nextRound = MockSimulatorDataSource.generateProvablyFairRound()
                _uiState.value = _uiState.value.copy(
                    flightState = FlightState.WAITING,
                    currentMultiplier = 1.00,
                    targetMultiplier = nextRound.multiplier,
                    targetRoundId = nextRound.roundId,
                    countdownSeconds = 3,
                    flightProgress = 0f
                )

                for (cd in 3 downTo 1) {
                    _uiState.value = _uiState.value.copy(countdownSeconds = cd)
                    delay(1000)
                    if (_uiState.value.isPaused) break
                }

                if (_uiState.value.isPaused) continue

                // 2. FLYING PHASE
                _uiState.value = _uiState.value.copy(
                    flightState = FlightState.FLYING,
                    telemetryTicks = emptyList()
                )

                val target = nextRound.multiplier
                var current = 1.00
                val startTime = System.currentTimeMillis()
                val ticks = mutableListOf<LiveTelemetryTick>()

                // Flight progression rate: Exponential rise ~0.06 per sec
                val tickInterval = 50L
                while (current < target && !_uiState.value.isPaused) {
                    delay(tickInterval)
                    val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                    // Multiplier growth formula: e^(0.065 * t)
                    val rawMult = exp(0.065 * elapsedSeconds * 2.2)
                    current = min(target, ((rawMult * 100).toInt()) / 100.0)

                    val progress = min(1f, (current.toFloat() / min(10.0, target).toFloat()))
                    val tick = LiveTelemetryTick(
                        timestamp = System.currentTimeMillis(),
                        multiplier = current,
                        speed = (current - 1.0) * 12.0 + 10.0,
                        altitudePct = progress
                    )
                    ticks.add(0, tick)
                    if (ticks.size > 20) ticks.removeAt(ticks.size - 1)

                    _uiState.value = _uiState.value.copy(
                        currentMultiplier = current,
                        flightProgress = progress,
                        telemetryTicks = ticks.toList()
                    )
                }

                // 3. CRASHED PHASE
                _uiState.value = _uiState.value.copy(
                    flightState = FlightState.CRASHED,
                    currentMultiplier = target,
                    lastCrashedMultiplier = target,
                    totalRoundsSimulated = _uiState.value.totalRoundsSimulated + 1
                )

                // Live simulation UI state update only (does not pollute real browser repository)

                // Crash display duration (2.5s)
                delay(2500)
            }
        }
    }

    fun togglePause() {
        val paused = !_uiState.value.isPaused
        _uiState.value = _uiState.value.copy(isPaused = paused)
    }

    fun skipToNext() {
        startSimulationLoop()
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
