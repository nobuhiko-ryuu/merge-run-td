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
import com.example.mergeruntd.domain.shop.ShopSlot
import com.example.mergeruntd.domain.shop.ShopState
import org.junit.Assert.assertTrue
import org.junit.Test

class BuyTest {
    private val config =
        AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!)).loadAll()

    @Test
    fun buyFailsWhenBoardIsFull() {
        val fullBoard =
            Board(
                cells =
                    List(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { index ->
                        UnitInstance(id = "u-$index", role = "SHOOTER")
                    },
            )
        val state =
            baseState(
                coins = 10,
                board = fullBoard,
                shop = ShopState(slots = listOf(ShopSlot("shooter"), ShopSlot(), ShopSlot())),
            )

        val result = RunReducer.reduce(state, RunIntent.BuyFromShop(0), config.unitDefs)

        assertTrue(result.isFailure)
    }

    @Test
    fun buyFailsWhenNotEnoughCoins() {
        val state =
            baseState(
                coins = 2,
                shop = ShopState(slots = listOf(ShopSlot("shooter"), ShopSlot(), ShopSlot())),
            )

        val result = RunReducer.reduce(state, RunIntent.BuyFromShop(0), config.unitDefs)

        assertTrue(result.isFailure)
    }

    private fun baseState(
        coins: Int,
        board: Board = Board(),
        shop: ShopState,
    ): RunState =
        RunState(
            stageIndex = 0,
            waveIndex = 0,
            phase = Phase.PREP,
            baseHp = 40,
            coins = coins,
            freeRerollLeft = 0,
            board = board,
            shop = shop,
            lane = LaneState(),
            timeMs = 0,
            rng = RngState(11L),
        )
}
