package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.board.UnitInstance
import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Phase
import com.example.mergeruntd.domain.core.RngState
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.engine.GameEngine
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.run.RunIntent
import com.example.mergeruntd.domain.run.RunReducer
import com.example.mergeruntd.domain.shop.EconomyRules
import com.example.mergeruntd.domain.shop.ShopSlot
import com.example.mergeruntd.domain.shop.ShopState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EconomyRulesTest {
    private val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
    private val config = loader.loadAll()

    @Test
    fun economyConstantsMatchSpec() {
        assertEquals(3, EconomyRules.BUY_COST)
        assertEquals(2, EconomyRules.REROLL_COST)
        assertEquals(1, EconomyRules.SELL_REFUND)
        assertEquals(2500L, EconomyRules.REFILL_MS)
    }

    @Test
    fun waveStartAddsFreeReroll() {
        val engine = GameEngine(config)
        val start = engine.newRun(stageIndex = 0, seed = 1234L)

        val tick = engine.tick(start, 100)
        assertEquals(GameConstants.WAVE_START_FREE_REROLL_BONUS, tick.state.freeRerollLeft)
    }

    @Test
    fun buyConsumesCoinsAndPlacesUnit() {
        val state =
            testState(
                coins = 5,
                shop = ShopState(slots = listOf(ShopSlot("shooter"), ShopSlot(), ShopSlot())),
            )

        val result = RunReducer.reduce(state, RunIntent.BuyFromShop(slotIndex = 0), config.unitDefs)
        assertTrue(result.isSuccess)

        val next = result.getOrThrow()
        assertEquals(2, next.coins)
        assertEquals("SHOOTER", next.board.cells.first()?.role)
        assertEquals(1, next.board.cells.first()?.level)
        assertNull(next.shop.slots[0].unitId)
    }

    @Test
    fun freeRerollConsumesChargeBeforeCoins() {
        val state =
            testState(
                coins = 4,
                freeRerollLeft = 1,
                shop = ShopState(slots = listOf(ShopSlot("wall"), ShopSlot("shooter"), ShopSlot("splash"))),
            )

        val result = RunReducer.reduce(state, RunIntent.RerollShop, config.unitDefs).getOrThrow()

        assertEquals(4, result.coins)
        assertEquals(0, result.freeRerollLeft)
        assertTrue(result.shop.slots.all { it.unitId != null })
    }

    @Test
    fun sellAddsRefundCoin() {
        val board =
            Board(
                cells =
                    List(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { index ->
                        if (index == 2) UnitInstance("u-1", "SHOOTER") else null
                    },
            )
        val state = testState(coins = 0, board = board)

        val result = RunReducer.reduce(state, RunIntent.SellAt(cellIndex = 2), config.unitDefs).getOrThrow()

        assertEquals(1, result.coins)
        assertNull(result.board.cells[2])
    }

    private fun testState(
        coins: Int = 0,
        freeRerollLeft: Int = 0,
        board: Board = Board(),
        shop: ShopState = ShopState(),
    ): RunState =
        RunState(
            stageIndex = 0,
            waveIndex = 0,
            phase = Phase.PREP,
            baseHp = 50,
            coins = coins,
            freeRerollLeft = freeRerollLeft,
            board = board,
            shop = shop,
            lane = LaneState(),
            timeMs = 0,
            rng = RngState(7L),
        )
}
