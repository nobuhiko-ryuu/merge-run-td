package com.example.mergeruntd.domain.run

import com.example.mergeruntd.domain.board.UnitInstance
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Rng
import com.example.mergeruntd.domain.core.RngState
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.model.UnitDef
import com.example.mergeruntd.domain.shop.ShopSlot

sealed interface RunIntent {
    data class BuyFromShop(val slotIndex: Int) : RunIntent

    data object RerollShop : RunIntent

    data class SellAt(val cellIndex: Int) : RunIntent

    data class Merge(val fromCellIndex: Int, val toCellIndex: Int) : RunIntent
}

object RunReducer {
    fun reduce(
        state: RunState,
        intent: RunIntent,
        unitDefs: List<UnitDef>,
    ): Result<RunState> =
        when (intent) {
            is RunIntent.BuyFromShop -> buyFromShop(state, intent.slotIndex, unitDefs)
            RunIntent.RerollShop -> rerollShop(state, unitDefs)
            is RunIntent.SellAt -> sellAt(state, intent.cellIndex)
            is RunIntent.Merge -> merge(state, intent.fromCellIndex, intent.toCellIndex)
        }

    private fun buyFromShop(
        state: RunState,
        slotIndex: Int,
        unitDefs: List<UnitDef>,
    ): Result<RunState> {
        if (slotIndex !in state.shop.slots.indices) return failure("Invalid shop slot")
        if (state.coins < GameConstants.BUY_COST) return failure("Not enough coins")

        val unitId = state.shop.slots[slotIndex].unitId ?: return failure("Selected slot is empty")
        val unitDef = unitDefs.firstOrNull { it.id == unitId } ?: return failure("Unit definition missing")

        val emptyCellIndex = state.board.cells.indexOfFirst { it == null }
        if (emptyCellIndex == -1) return failure("Board is full")

        val (instanceId, nextRng) = nextUnitId(state)
        val updatedCells = state.board.cells.toMutableList()
        updatedCells[emptyCellIndex] = UnitInstance(id = instanceId, role = unitDef.role, level = 1)

        val updatedSlots = state.shop.slots.toMutableList()
        updatedSlots[slotIndex] = ShopSlot(unitId = null)

        return Result.success(
            state.copy(
                coins = state.coins - GameConstants.BUY_COST,
                board = state.board.copy(cells = updatedCells),
                shop = state.shop.copy(slots = updatedSlots),
                rng = nextRng,
            ),
        )
    }

    private fun rerollShop(
        state: RunState,
        unitDefs: List<UnitDef>,
    ): Result<RunState> {
        if (unitDefs.isEmpty()) return failure("No units available for reroll")

        val isFree = state.freeRerollLeft > 0
        if (!isFree && state.coins < GameConstants.REROLL_COST) return failure("Not enough coins")

        var nextRng = state.rng
        val rerolledSlots = state.shop.slots.map {
            val (roll, updatedRng) = Rng.nextDouble(nextRng)
            nextRng = updatedRng
            val index = (roll * unitDefs.size).toInt().coerceIn(0, unitDefs.lastIndex)
            ShopSlot(unitId = unitDefs[index].id)
        }

        return Result.success(
            state.copy(
                coins = if (isFree) state.coins else state.coins - GameConstants.REROLL_COST,
                freeRerollLeft = if (isFree) state.freeRerollLeft - 1 else state.freeRerollLeft,
                shop = state.shop.copy(slots = rerolledSlots, refillTimerMs = 0L),
                rng = nextRng,
            ),
        )
    }

    private fun sellAt(
        state: RunState,
        cellIndex: Int,
    ): Result<RunState> {
        if (cellIndex !in state.board.cells.indices) return failure("Invalid board cell")
        if (state.board.cells[cellIndex] == null) return failure("Cell is already empty")

        val updatedCells = state.board.cells.toMutableList()
        updatedCells[cellIndex] = null

        return Result.success(
            state.copy(
                coins = state.coins + GameConstants.SELL_REFUND,
                board = state.board.copy(cells = updatedCells),
            ),
        )
    }

    private fun merge(
        state: RunState,
        fromCellIndex: Int,
        toCellIndex: Int,
    ): Result<RunState> {
        if (fromCellIndex == toCellIndex) return failure("Cannot merge the same cell")
        if (fromCellIndex !in state.board.cells.indices || toCellIndex !in state.board.cells.indices) {
            return failure("Invalid board cell")
        }

        val from = state.board.cells[fromCellIndex] ?: return failure("Source cell is empty")
        val to = state.board.cells[toCellIndex] ?: return failure("Target cell is empty")

        if (from.role != to.role || from.level != to.level) {
            return failure("Merge requires same role and level")
        }

        val (mergedId, nextRng) = nextUnitId(state)
        val updatedCells = state.board.cells.toMutableList()
        updatedCells[fromCellIndex] = null
        updatedCells[toCellIndex] = to.copy(id = mergedId, level = to.level + 1)

        return Result.success(
            state.copy(
                board = state.board.copy(cells = updatedCells),
                rng = nextRng,
            ),
        )
    }

    private fun nextUnitId(state: RunState): Pair<String, RngState> {
        val (value, rng) = Rng.nextLong(state.rng)
        return "u-${value.toULong()}" to rng
    }

    private fun failure(message: String): Result<RunState> = Result.failure(IllegalStateException(message))
}
