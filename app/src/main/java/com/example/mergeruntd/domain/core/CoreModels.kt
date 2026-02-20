package com.example.mergeruntd.domain.core

import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.shop.ShopState

enum class Phase {
    PREP,
    COMBAT,
    POST_WAVE,
    ENDED,
}

data class RunState(
    val stageIndex: Int,
    val waveIndex: Int,
    val phase: Phase,
    val baseHp: Int,
    val coins: Int,
    val freeRerollLeft: Int,
    val board: Board,
    val shop: ShopState,
    val lane: LaneState,
    val timeMs: Long,
    val rng: RngState,
)

data class RngState(val seed: Long)

object Rng {
    fun nextLong(state: RngState): Pair<Long, RngState> {
        val next = (state.seed * 6364136223846793005L) + 1442695040888963407L
        return next to RngState(next)
    }

    /** [0, 1) */
    fun nextDouble(state: RngState): Pair<Double, RngState> {
        val (value, nextState) = nextLong(state)
        val normalized = ((value ushr 11).toDouble() / (1L shl 53).toDouble())
        return normalized to nextState
    }
}

object GameConstants {
    const val BOARD_ROWS = 4
    const val BOARD_COLS = 4

    const val LANE_TILES = 12
    const val STAGE_WAVES = 5

    const val SHOP_SLOTS = 3
    const val BUY_COST = 3
    const val REROLL_COST = 2
    const val SELL_REFUND = 1
    const val SHOP_REFILL_MS = 2_500L
    const val WAVE_START_FREE_REROLL_BONUS = 1

    const val NORMAL_SPEED_MS_PER_TILE = 800L
    const val FAST_SPEED_MS_PER_TILE = 500L
    const val TANK_SPEED_MS_PER_TILE = 1_200L
    const val BOSS_SPEED_MS_PER_TILE = 900L

    const val SHOOTER_DMG = 3
    const val SPLASH_DMG = 2
    const val SLOW_DMG = 1

    const val SHOOTER_CD_MS = 500L
    const val SPLASH_CD_MS = 700L
    const val SLOW_CD_MS = 600L
}
