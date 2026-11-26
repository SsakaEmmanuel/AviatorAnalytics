package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.GameRepository
import com.example.domain.engine.BacktestingEngine
import com.example.domain.model.BacktestReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BacktestUiState(
    val isRunning: Boolean = false,
    val report: BacktestReport? = null,
    val windowSize: Int = 30,
    val minTrainingSize: Int = 15,
    val errorMessage: String? = null
)

class BacktestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository.getInstance(application)

    private val _uiState = MutableStateFlow(BacktestUiState())
    val uiState: StateFlow<BacktestUiState> = _uiState.asStateFlow()

    init {
        runBacktest()
    }

    fun setWindowSize(size: Int) {
        _uiState.value = _uiState.value.copy(windowSize = size)
    }

    fun setMinTrainingSize(size: Int) {
        _uiState.value = _uiState.value.copy(minTrainingSize = size)
    }

    fun runBacktest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunning = true, errorMessage = null)
            val rounds = withContext(Dispatchers.IO) {
                repository.getAllRoundsList()
            }

            val updatedRounds = rounds

            val report = withContext(Dispatchers.Default) {
                BacktestingEngine.runWalkForwardBacktest(
                    rounds = updatedRounds,
                    minTrainingSize = _uiState.value.minTrainingSize,
                    windowSize = _uiState.value.windowSize
                )
            }

            _uiState.value = _uiState.value.copy(
                isRunning = false,
                report = report
            )
        }
    }
}
