package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.board.UnitInstance
import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Phase
import com.example.mergeruntd.domain.core.RngState
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.core.UpgradeOffer
import com.example.mergeruntd.domain.engine.GameEngine
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.run.RunIntent
import com.example.mergeruntd.domain.run.RunReducer
import com.example.mergeruntd.domain.shop.ShopState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunSafetyTest {
    private val config = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!)).loadAll()

    @Test
    fun stageIndexIsClampedToSupportedRange() {
        val state = GameEngine(config).newRun(stageIndex = 999, seed = 1L)
        assertEquals(GameConstants.MAX_STAGE_INDEX, state.stageIndex)
    }

    @Test
    fun actionsAreRejectedWhileUpgradeOfferIsOpen() {
        val offerState =
            baseState().copy(
                offeredUpgrade = UpgradeOffer(waveIndex = 2, options = config.upgrades.take(1), deadlineTimeMs = 1_000L),
                phase = Phase.POST_WAVE,
            )

        val result = RunReducer.reduce(offerState, RunIntent.RerollShop, config.unitDefs, config)

        assertTrue(result.isFailure)
        assertEquals("Finish upgrade selection first", result.exceptionOrNull()?.message)
    }

    @Test
    fun mergeFailureIncludesReason() {
        val cells = MutableList(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { null as UnitInstance? }
        cells[0] = UnitInstance("u-1", "SHOOTER", level = 1)
        cells[1] = UnitInstance("u-2", "SPLASH", level = 1)

        val result = RunReducer.reduce(baseState(board = Board(cells = cells)), RunIntent.Merge(0, 1), config.unitDefs)

        assertTrue(result.isFailure)
        assertEquals("Cannot merge: roles do not match", result.exceptionOrNull()?.message)
    }

    private fun baseState(board: Board = Board()): RunState =
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
            rng = RngState(42L),
        )
}
