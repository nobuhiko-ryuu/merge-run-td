package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.engine.GameEngine
import com.example.mergeruntd.domain.shop.EconomyRules
import org.junit.Assert.assertEquals
import org.junit.Test

class EconomyRulesTest {
  @Test
  fun economyConstantsMatchSpec() {
    assertEquals(3, EconomyRules.BUY_COST)
    assertEquals(2, EconomyRules.REROLL_COST)
    assertEquals(1, EconomyRules.SELL_REFUND)
    assertEquals(2500L, EconomyRules.REFILL_MS)
  }

  @Test
  fun waveStartAddsFreeReroll() {
    val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
    val engine = GameEngine(loader.loadAll())
    val start = engine.newRun(stageIndex = 0, seed = 1234L)

    val tick = engine.tick(start, 100)
    assertEquals(GameConstants.WAVE_START_FREE_REROLL_BONUS, tick.state.freeRerollLeft)
  }
}
