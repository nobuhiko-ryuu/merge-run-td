package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Phase
import com.example.mergeruntd.domain.core.RunEnd
import com.example.mergeruntd.domain.core.UpgradeOffer
import com.example.mergeruntd.domain.engine.GameEngine
import com.example.mergeruntd.domain.model.WaveConfig
import com.example.mergeruntd.domain.run.RunIntent
import com.example.mergeruntd.domain.run.RunReducer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpgradeFlowTest {
    private val loaded = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!)).loadAll()

    @Test
    fun offerAppearsAfterWave2AndWave4() {
        val config = loaded.copy(stages = listOf(loaded.stages.first().copy(waves = List(5) { WaveConfig(0, 0, 0, 0) })))
        val engine = GameEngine(config)
        var state = engine.newRun(0, 99L)

        var seenWave2 = false
        var seenWave4 = false
        repeat(20) {
            state = engine.tick(state, 100L).state
            val offer = state.offeredUpgrade
            if (offer?.waveIndex == 2) seenWave2 = true
            if (offer?.waveIndex == 4) seenWave4 = true
            if (offer != null) {
                state = RunReducer.reduce(state, RunIntent.SelectUpgrade(0), config.unitDefs, config).getOrThrow()
            }
        }

        assertTrue(seenWave2)
        assertTrue(seenWave4)
    }

    @Test
    fun timeoutAutoAppliesInstantUpgrade() {
        val config = loaded.copy(stages = listOf(loaded.stages.first().copy(waves = List(5) { WaveConfig(0, 0, 0, 0) })))
        val engine = GameEngine(config)
        var state = engine.newRun(0, 7L)

        while (state.offeredUpgrade == null) {
            state = engine.tick(state, 100L).state
        }
        val offer = state.offeredUpgrade
        assertNotNull(offer)

        state = engine.tick(state, (config.upgradeRules.timeoutSec * 1000L) + 100L).state

        assertNull(state.offeredUpgrade)
        assertTrue(state.atkMul > 1.0 || state.aspdMul > 1.0)
    }

    @Test
    fun transformCannotBeSelectedTwice() {
        val transform = loaded.upgrades.first { it.type == "TRANSFORM" }
        val state =
            GameEngine(loaded).newRun(0, 1L).copy(
                offeredUpgrade = UpgradeOffer(2, listOf(transform), 1_000L),
                phase = Phase.POST_WAVE,
            )
        val once = RunReducer.reduce(state, RunIntent.SelectUpgrade(0), loaded.unitDefs, loaded).getOrThrow()
        assertTrue(once.transformUsed)

        val secondState = once.copy(offeredUpgrade = UpgradeOffer(4, listOf(transform), 2_000L))
        val secondResult = RunReducer.reduce(secondState, RunIntent.SelectUpgrade(0), loaded.unitDefs, loaded)
        assertTrue(secondResult.isFailure)
    }

    @Test
    fun runEndsWithVictoryOrDefeat() {
        val victoryConfig = loaded.copy(stages = listOf(loaded.stages.first().copy(waves = List(5) { WaveConfig(0, 0, 0, 0) })))
        val victoryEngine = GameEngine(victoryConfig)
        var victory = victoryEngine.newRun(0, 5L)
        repeat(30) {
            victory = victoryEngine.tick(victory, 100L).state
            if (victory.offeredUpgrade != null) {
                victory = RunReducer.reduce(victory, RunIntent.SelectUpgrade(0), victoryConfig.unitDefs, victoryConfig).getOrThrow()
            }
        }
        assertEquals(RunEnd.Victory, victory.end)

        val defeatConfig =
            loaded.copy(
                stages = listOf(loaded.stages.first().copy(waves = listOf(WaveConfig(normal = 0, fast = 0, tank = 0, boss = 1)) + List(4) { WaveConfig(0, 0, 0, 0) })),
            )
        val defeatEngine = GameEngine(defeatConfig)
        var defeat = defeatEngine.newRun(0, 6L).copy(baseHp = 1)
        repeat(20) {
            defeat = defeatEngine.tick(defeat, GameConstants.BOSS_SPEED_MS_PER_TILE).state
        }
        assertEquals(RunEnd.Defeat, defeat.end)
    }
}
