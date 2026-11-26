package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datasource.MockSimulatorDataSource
import com.example.data.repository.GameRepository
import com.example.domain.engine.AnalyticsEngine
import com.example.domain.engine.PredictionEngine
import com.example.domain.model.ComprehensiveAnalytics
import com.example.domain.model.EnsemblePrediction
import com.example.domain.model.GameRound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ManualEntryState(
    val currentInput: String = "",
    val customRoundId: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isBatchMode: Boolean = false,
    val batchInput: String = ""
)

data class SiteSyncState(
    val isSiteConnected: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val siteBalance: String? = null,
    val lastSyncTimestamp: Long = 0L,
    val totalSiteRoundsSynced: Int = 0,
    val latestSyncedMultiplier: Double? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository.getInstance(application)

    val allRounds: StateFlow<List<GameRound>> = repository.allRoundsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentRounds: StateFlow<List<GameRound>> = repository.getRecentRoundsFlow(30)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val roundCount: StateFlow<Int> = repository.roundCountFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _manualEntryState = MutableStateFlow(ManualEntryState())
    val manualEntryState: StateFlow<ManualEntryState> = _manualEntryState.asStateFlow()

    private val _analyticsState = MutableStateFlow<ComprehensiveAnalytics?>(null)
    val analyticsState: StateFlow<ComprehensiveAnalytics?> = _analyticsState.asStateFlow()

    private val _predictionState = MutableStateFlow<EnsemblePrediction?>(null)
    val predictionState: StateFlow<EnsemblePrediction?> = _predictionState.asStateFlow()

    private val _predictionWindowSize = MutableStateFlow(50)
    val predictionWindowSize: StateFlow<Int> = _predictionWindowSize.asStateFlow()

    private val _botDecisionState = MutableStateFlow<com.example.domain.engine.BotDecision?>(null)
    val botDecisionState: StateFlow<com.example.domain.engine.BotDecision?> = _botDecisionState.asStateFlow()

    private val _siteSyncState = MutableStateFlow(SiteSyncState())
    val siteSyncState: StateFlow<SiteSyncState> = _siteSyncState.asStateFlow()

    private val browserManager = com.example.data.local.PersistentBrowserManager.getInstance(application)

    val isAviatorDetected: StateFlow<Boolean> = browserManager.isAviatorDetected
    val detectedSiteName: StateFlow<String> = browserManager.detectedSiteName
    val detectionMethod: StateFlow<String> = browserManager.detectionMethod
    val browserCurrentUrl: StateFlow<String> = browserManager.currentUrl

    init {
        // Connect Persistent Browser Manager callbacks so live browser feeds app directly
        browserManager.onLiveFlightReceived = { mult ->
            onSiteFlightReceived(mult)
        }
        browserManager.onFlightsBatchReceived = { mults ->
            onSiteFlightsBatchReceived(mults)
        }
        browserManager.onAuthStatusChanged = { loggedIn ->
            updateSiteStatus(loggedIn, null)
        }

        // Recalculate analytics, prediction and bot knowledge evaluation ONLY when Aviator is detected
        // and live rounds have been ingested from the browser. Otherwise, leave as null (display dashes).
        viewModelScope.launch {
            combine(allRounds, browserManager.isAviatorDetected, _predictionWindowSize) { rounds, isDetected, windowSize ->
                Triple(rounds, isDetected, windowSize)
            }.collect { (rounds, isDetected, windowSize) ->
                if (isDetected && rounds.isNotEmpty()) {
                    _analyticsState.value = AnalyticsEngine.computeAnalytics(rounds)
                    _predictionState.value = PredictionEngine.predictNext(rounds, windowSize)
                    _botDecisionState.value = com.example.domain.engine.BotAdvisorEngine.evaluateEntry(rounds)
                } else {
                    _analyticsState.value = null
                    _predictionState.value = null
                    _botDecisionState.value = null
                }
            }
        }
    }

    /**
     * Updates authentication status and balance detected from the site
     */
    fun updateSiteStatus(isLoggedIn: Boolean, balance: String?) {
        _siteSyncState.value = _siteSyncState.value.copy(
            isSiteConnected = true,
            isUserLoggedIn = isLoggedIn,
            siteBalance = balance ?: _siteSyncState.value.siteBalance,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Ingests a single real flight multiplier from the live site into the entire app system.
     * Automatically updates the local database, which triggers real-time recalculation
     * of all analytics, predictions, trends, and history across every screen.
     */
    fun onSiteFlightReceived(multiplier: Double, customRoundId: String? = null) {
        if (multiplier < 1.0) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recent = repository.getRecentRoundsList(5)
            val latest = recent.firstOrNull()

            // Deduplicate: if the most recent round has the exact same multiplier within 4 seconds, skip duplicate
            if (latest != null && latest.multiplier == multiplier && (now - latest.timestamp) < 4000) {
                return@launch
            }

            val rId = customRoundId ?: "SITE-${now % 1000000}"
            val newRound = GameRound(
                roundId = rId,
                multiplier = multiplier,
                timestamp = now,
                source = "LIVE_SITE"
            )
            repository.insertRound(newRound)

            _siteSyncState.value = _siteSyncState.value.copy(
                isSiteConnected = true,
                lastSyncTimestamp = now,
                totalSiteRoundsSynced = _siteSyncState.value.totalSiteRoundsSynced + 1,
                latestSyncedMultiplier = multiplier
            )

            // Also mirror to manual entry input for convenience
            _manualEntryState.value = _manualEntryState.value.copy(
                currentInput = String.format(java.util.Locale.US, "%.2f", multiplier)
            )
        }
    }

    /**
     * Ingests a batch of real flight multipliers scraped from the live site DOM history.
     */
    fun onSiteFlightsBatchReceived(multipliers: List<Double>) {
        val valid = multipliers.filter { it >= 1.0 }
        if (valid.isEmpty()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recent = repository.getRecentRoundsList(15)
            val recentMultipliers = recent.map { it.multiplier }

            val roundsToInsert = mutableListOf<GameRound>()
            for ((index, mult) in valid.withIndex()) {
                // If top multiplier matches our latest recorded round, don't duplicate
                if (index == 0 && recentMultipliers.firstOrNull() == mult) {
                    continue
                }

                roundsToInsert.add(
                    GameRound(
                        roundId = "SITE-${(now + index) % 1000000}",
                        multiplier = mult,
                        timestamp = now - ((valid.size - 1 - index) * 12000L),
                        source = "LIVE_SITE"
                    )
                )
            }

            if (roundsToInsert.isNotEmpty()) {
                repository.insertRounds(roundsToInsert)
                _siteSyncState.value = _siteSyncState.value.copy(
                    isSiteConnected = true,
                    lastSyncTimestamp = now,
                    totalSiteRoundsSynced = _siteSyncState.value.totalSiteRoundsSynced + roundsToInsert.size,
                    latestSyncedMultiplier = valid.firstOrNull()
                )
            }
        }
    }

    fun setPredictionWindowSize(size: Int) {
        _predictionWindowSize.value = size
        val currentRounds = allRounds.value
        if (currentRounds.isNotEmpty()) {
            _predictionState.value = PredictionEngine.predictNext(currentRounds, size)
        }
    }

    // Manual Entry Keypad / Input Actions
    fun onKeypadDigit(digit: String) {
        val current = _manualEntryState.value.currentInput
        if (digit == "." && current.contains(".")) return
        if (current.length >= 7) return
        _manualEntryState.value = _manualEntryState.value.copy(
            currentInput = current + digit,
            errorMessage = null
        )
    }

    fun onKeypadBackspace() {
        val current = _manualEntryState.value.currentInput
        if (current.isNotEmpty()) {
            _manualEntryState.value = _manualEntryState.value.copy(
                currentInput = current.dropLast(1),
                errorMessage = null
            )
        }
    }

    fun onKeypadClear() {
        _manualEntryState.value = _manualEntryState.value.copy(
            currentInput = "",
            errorMessage = null,
            successMessage = null
        )
    }

    fun onSetQuickMultiplier(value: Double) {
        _manualEntryState.value = _manualEntryState.value.copy(
            currentInput = String.format("%.2f", value),
            errorMessage = null
        )
    }

    fun submitManualEntry() {
        val inputStr = _manualEntryState.value.currentInput.trim()
        val mult = inputStr.toDoubleOrNull()
        if (mult == null || mult < 1.00 || mult > 100000.0) {
            _manualEntryState.value = _manualEntryState.value.copy(
                errorMessage = "Please enter a valid multiplier (≥ 1.00x)"
            )
            return
        }

        viewModelScope.launch {
            val customId = _manualEntryState.value.customRoundId.ifBlank {
                "BET-${System.currentTimeMillis() % 1000000}"
            }
            val newRound = GameRound(
                roundId = customId,
                multiplier = mult,
                source = "MANUAL"
            )
            repository.insertRound(newRound)
            _manualEntryState.value = ManualEntryState(
                successMessage = "Recorded round $customId @ ${String.format("%.2f", mult)}x"
            )
        }
    }

    fun setBatchInput(input: String) {
        _manualEntryState.value = _manualEntryState.value.copy(
            batchInput = input,
            errorMessage = null
        )
    }

    fun toggleBatchMode() {
        _manualEntryState.value = _manualEntryState.value.copy(
            isBatchMode = !_manualEntryState.value.isBatchMode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun submitBatchEntry() {
        val raw = _manualEntryState.value.batchInput
        val tokens = raw.split(",", " ", "\n", "\t").filter { it.isNotBlank() }
        val roundsToAdd = mutableListOf<GameRound>()

        for ((index, t) in tokens.withIndex()) {
            val clean = t.replace("x", "", ignoreCase = true)
            val mult = clean.toDoubleOrNull()
            if (mult != null && mult >= 1.0) {
                roundsToAdd.add(
                    GameRound(
                        roundId = "BATCH-${System.currentTimeMillis() % 100000}-$index",
                        multiplier = mult,
                        source = "MANUAL_BATCH"
                    )
                )
            }
        }

        if (roundsToAdd.isEmpty()) {
            _manualEntryState.value = _manualEntryState.value.copy(
                errorMessage = "No valid multipliers found. Provide values separated by commas or newlines."
            )
            return
        }

        viewModelScope.launch {
            repository.insertRounds(roundsToAdd)
            _manualEntryState.value = ManualEntryState(
                isBatchMode = true,
                successMessage = "Successfully inserted ${roundsToAdd.size} rounds."
            )
        }
    }

    fun generateSimulatedRound() {
        viewModelScope.launch {
            val sim = MockSimulatorDataSource.generateProvablyFairRound()
            repository.insertRound(sim)
        }
    }

    fun generateBatchRounds(count: Int = 20) {
        viewModelScope.launch {
            val batch = MockSimulatorDataSource.generateHistoricalBatch(count)
            repository.insertRounds(batch)
        }
    }

    fun deleteRound(id: Long) {
        viewModelScope.launch {
            repository.deleteRound(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun exportCsvData(): String {
        return repository.exportToCsvString(allRounds.value)
    }

    fun importCsvData(csvText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importFromCsvString(csvText)
            onComplete(count)
        }
    }
}
