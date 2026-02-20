package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.engine.DomainEvent
import com.example.mergeruntd.domain.engine.GameEngine
import com.example.mergeruntd.domain.model.WaveConfig
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveLogicTest {
    @Test
    fun waveStartsAndEndsWithEvents() {
        val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
        val loaded = loader.loadAll()

        val config =
            loaded.copy(
                stages =
                    listOf(
                        loaded.stages.first().copy(
                            waves =
                                listOf(
                                    WaveConfig(normal = 1, fast = 0, tank = 0, boss = 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                ),
                        ),
                    ),
            )

        val engine = GameEngine(config)
        var state = engine.newRun(stageIndex = 0, seed = 7)

        var sawStart = false
        var sawEnd = false

        repeat(30) {
            val tick = engine.tick(state, 500)
            state = tick.state
            sawStart = sawStart || tick.events.any { it is DomainEvent.WaveStarted }
            sawEnd = sawEnd || tick.events.any { it is DomainEvent.WaveEnded }
        }

        assertTrue(sawStart)
        assertTrue(sawEnd)
    }

    @Test
    fun baseHpDecreasesWhenEnemyReachesEnd() {
        val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
        val loaded = loader.loadAll()

        val config =
            loaded.copy(
                stages =
                    listOf(
                        loaded.stages.first().copy(
                            waves =
                                listOf(
                                    WaveConfig(normal = 1, fast = 0, tank = 0, boss = 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                ),
                        ),
                    ),
            )

        val engine = GameEngine(config)
        var state = engine.newRun(stageIndex = 0, seed = 7)
        val startHp = state.baseHp

        repeat(30) {
            state = engine.tick(state, GameConstants.NORMAL_SPEED_MS_PER_TILE).state
        }

        assertTrue(state.baseHp < startHp)
    }
}
