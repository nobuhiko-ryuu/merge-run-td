package com.example.mergeruntd.domain.run

import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.model.GameConfig
import com.example.mergeruntd.domain.model.UpgradeDef

fun applyUpgrade(
    state: RunState,
    upgrade: UpgradeDef,
    config: GameConfig,
): RunState {
    var nextState = state
    if (upgrade.type == "TRANSFORM" && state.transformUsed) {
        return state.copy(offeredUpgrade = null)
    }

    nextState =
        nextState.copy(
            atkMul = state.atkMul * (upgrade.atkMul ?: 1.0),
            aspdMul = state.aspdMul * (upgrade.aspdMul ?: 1.0),
            rerollCostDelta = state.rerollCostDelta + (upgrade.rerollCostDelta ?: 0),
            waveStartFreeRerollBonus = state.waveStartFreeRerollBonus + (upgrade.waveStartFreeRerollBonus ?: 0),
            transformUsed = state.transformUsed || upgrade.type == "TRANSFORM",
            offeredUpgrade = null,
            appliedUpgradeCount =
                state.appliedUpgradeCount +
                    (upgrade.id to ((state.appliedUpgradeCount[upgrade.id] ?: 0) + 1)),
        )

    if (upgrade.type == "TRANSFORM") {
        val target = upgrade.targetRole ?: return nextState
        val transformedRole =
            when (target) {
                "WALL" -> "SHOOTER"
                "SLOW" -> "SPLASH"
                else -> null
            }
        if (transformedRole != null) {
            val updatedCells =
                nextState.board.cells.map { unit ->
                    if (unit != null && unit.role == target) unit.copy(role = transformedRole) else unit
                }
            nextState = nextState.copy(board = nextState.board.copy(cells = updatedCells))
        }
    }

    return nextState
}
