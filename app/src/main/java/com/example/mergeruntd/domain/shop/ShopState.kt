package com.example.mergeruntd.domain.shop

import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Rng
import com.example.mergeruntd.domain.core.RngState
import com.example.mergeruntd.domain.model.UnitDef

data class ShopSlot(
    val unitId: String? = null,
)

data class ShopState(
    val slots: List<ShopSlot> = List(GameConstants.SHOP_SLOTS) { ShopSlot() },
    val refillTimerMs: Long = 0,
)

object EconomyRules {
    const val BUY_COST = GameConstants.BUY_COST
    const val REROLL_COST = GameConstants.REROLL_COST
    const val SELL_REFUND = GameConstants.SELL_REFUND
    const val REFILL_MS = GameConstants.SHOP_REFILL_MS
    const val WAVE_START_FREE_REROLL_BONUS = GameConstants.WAVE_START_FREE_REROLL_BONUS
}

fun tickShop(
    shop: ShopState,
    deltaMs: Long,
    rng: RngState,
    unitDefs: List<UnitDef>,
): Pair<ShopState, RngState> {
    if (shop.slots.none { it.unitId == null }) {
        return shop.copy(refillTimerMs = 0) to rng
    }

    val timer = shop.refillTimerMs + deltaMs
    if (timer < EconomyRules.REFILL_MS) {
        return shop.copy(refillTimerMs = timer) to rng
    }

    val emptyIdx = shop.slots.indexOfFirst { it.unitId == null }
    if (emptyIdx == -1 || unitDefs.isEmpty()) {
        return shop.copy(refillTimerMs = timer - EconomyRules.REFILL_MS) to rng
    }

    val (roll, nextRng) = Rng.nextDouble(rng)
    val selectedIndex = (roll * unitDefs.size).toInt().coerceIn(0, unitDefs.lastIndex)
    val filled = shop.slots.toMutableList()
    filled[emptyIdx] = ShopSlot(unitId = unitDefs[selectedIndex].id)
    return shop.copy(slots = filled, refillTimerMs = timer - EconomyRules.REFILL_MS) to nextRng
}

fun buyUnit(shop: ShopState): Result<ShopState> {
    return Result.failure(UnsupportedOperationException("Buy not implemented in PR1"))
}

fun rerollShop(shop: ShopState): Result<ShopState> {
    return Result.failure(UnsupportedOperationException("Reroll not implemented in PR1"))
}

fun sellUnit(shop: ShopState): Result<ShopState> {
    return Result.failure(UnsupportedOperationException("Sell not implemented in PR1"))
}
