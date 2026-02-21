package com.example.mergeruntd

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.engine.GameEngine
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
    private val engine by lazy { GameEngine(loader.loadAll()) }

    private var runState: RunState? = null
    private var tickJob: Job? = null

    fun startRun() {
        runCatching {
            val initial = engine.newRun(stageIndex = 0, seed = 7L)
            runState = initial
            _uiState.value = RunUiState.Running(initial)
        }.onFailure { throwable ->
            runState = null
            _uiState.value = RunUiState.Error(throwable.message ?: "Failed to start run")
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
        _uiState.update {
            RunUiState.Running(result.state)
        }
    }

    companion object {
        private const val TICK_MS = 100L
    }
}
