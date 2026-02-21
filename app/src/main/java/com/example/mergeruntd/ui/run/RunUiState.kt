package com.example.mergeruntd.ui.run

import com.example.mergeruntd.domain.core.RunState

sealed interface RunUiState {
    data object Idle : RunUiState

    data class Running(
        val runState: RunState,
        val selectedCellIndex: Int? = null,
        val message: String? = null,
    ) : RunUiState

    data class Error(val message: String) : RunUiState
}
