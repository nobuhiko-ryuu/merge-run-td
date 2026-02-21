package com.example.mergeruntd

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.engine.GameEngine
import com.example.mergeruntd.domain.run.RunIntent
import com.example.mergeruntd.domain.run.RunReducer
import com.example.mergeruntd.infra.AndroidAssetProvider
import com.example.mergeruntd.ui.run.RunUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RunViewModel(
    context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow<RunUiState>(RunUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val loader by lazy { AssetConfigLoader(AndroidAssetProvider(context.assets)) }
    private val config by lazy { loader.loadAll() }
    private val engine by lazy { GameEngine(config) }

    private var runState: RunState? = null
    private var selectedCellIndex: Int? = null
    private var tickJob: Job? = null

    fun startRun() {
        runCatching {
            val initial = engine.newRun(stageIndex = 0, seed = 7L)
            runState = initial
            selectedCellIndex = null
            _uiState.value = RunUiState.Running(initial)
        }.onFailure { throwable ->
            runState = null
            selectedCellIndex = null
            _uiState.value = RunUiState.Error(throwable.message ?: "Failed to start run")
        }
    }

    fun buyFromShop(slotIndex: Int) {
        applyIntent(RunIntent.BuyFromShop(slotIndex))
    }

    fun rerollShop() {
        applyIntent(RunIntent.RerollShop)
    }

    fun sellSelected() {
        val selected = selectedCellIndex ?: return
        applyIntent(RunIntent.SellAt(selected))
    }

    fun onBoardCellTapped(cellIndex: Int) {
        val current = runState ?: return
        val selected = selectedCellIndex

        if (selected == null) {
            selectedCellIndex = if (current.board.cells[cellIndex] != null) cellIndex else null
            emitRunning(current)
            return
        }

        if (selected == cellIndex) {
            selectedCellIndex = null
            emitRunning(current)
            return
        }

        val targetOccupied = current.board.cells[cellIndex] != null
        if (targetOccupied) {
            applyIntent(RunIntent.Merge(fromCellIndex = selected, toCellIndex = cellIndex))
        } else {
            selectedCellIndex = null
            emitRunning(current, "Target cell is empty")
        }
    }

    fun onRunScreenActive(active: Boolean) {
        if (active) {
            startTicking()
        } else {
            stopTicking()
        }
    }

    private fun startTicking() {
        if (tickJob?.isActive == true) return
        tickJob =
            viewModelScope.launch {
                while (true) {
                    tickOnce()
                    delay(TICK_MS)
                }
            }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun tickOnce() {
        val current = runState ?: return
        val result = engine.tick(current, TICK_MS)
        runState = result.state
        emitRunning(result.state)
    }

    private fun applyIntent(intent: RunIntent) {
        val current = runState ?: return
        val result = RunReducer.reduce(current, intent, config.unitDefs)

        result.onSuccess { nextState ->
            runState = nextState
            selectedCellIndex =
                when (intent) {
                    is RunIntent.Merge,
                    is RunIntent.SellAt,
                    -> null
                    else -> selectedCellIndex?.takeIf { nextState.board.cells[it] != null }
                }
            emitRunning(nextState)
        }.onFailure { throwable ->
            emitRunning(current, throwable.message ?: "Action failed")
        }
    }

    private fun emitRunning(
        state: RunState,
        message: String? = null,
    ) {
        _uiState.update {
            RunUiState.Running(
                runState = state,
                selectedCellIndex = selectedCellIndex,
                message = message,
            )
        }
    }

    companion object {
        private const val TICK_MS = 100L
    }
}
