package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.board.UnitInstance
import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Phase
import com.example.mergeruntd.domain.core.RngState
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.run.RunIntent
import com.example.mergeruntd.domain.run.RunReducer
import com.example.mergeruntd.domain.shop.ShopState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MergeTest {
    private val config =
        AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!)).loadAll()

    @Test
    fun mergeSucceedsOnlyForSameRoleAndLevel() {
        val cells = MutableList(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { null as UnitInstance? }
        cells[0] = UnitInstance("u-1", "SHOOTER", level = 1)
        cells[1] = UnitInstance("u-2", "SHOOTER", level = 1)
        val state = baseState(Board(cells = cells))

        val merged =
            RunReducer.reduce(state, RunIntent.Merge(0, 1), config.unitDefs).getOrThrow()

        assertEquals(5, merged.coins)
        assertNull(merged.board.cells[0])
        assertEquals(2, merged.board.cells[1]?.level)
        assertEquals(1, merged.board.cells.count { it != null })
    }

    @Test
    fun mergeFailsForDifferentUnits() {
        val cells = MutableList(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { null as UnitInstance? }
        cells[0] = UnitInstance("u-1", "SHOOTER", level = 1)
        cells[1] = UnitInstance("u-2", "SPLASH", level = 1)
        val state = baseState(Board(cells = cells))

        val result = RunReducer.reduce(state, RunIntent.Merge(0, 1), config.unitDefs)

        assertTrue(result.isFailure)
    }

    private fun baseState(board: Board): RunState =
        RunState(
            stageIndex = 0,
            waveIndex = 0,
            phase = Phase.PREP,
            baseHp = 40,
            coins = 5,
            freeRerollLeft = 0,
            board = board,
            shop = ShopState(),
            lane = LaneState(),
            timeMs = 0,
            rng = RngState(12L),
        )
}
