package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.board.UnitInstance
import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Phase
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

    @Test
    fun stageOneEarlyWavesShooterEarnsCoinsFromKills() {
        val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
        val loaded = loader.loadAll()
        val engine = GameEngine(loaded)

        val cells = MutableList(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { null as UnitInstance? }
        cells[0] = UnitInstance(id = "u-1", role = "SHOOTER", level = 1)
        var state = engine.newRun(stageIndex = 0, seed = 11L).copy(board = Board(cells = cells))
        val initialCoins = state.coins

        repeat(60) {
            state = engine.tick(state, 500L).state
            if (state.waveIndex >= 2 || state.phase == Phase.ENDED) return@repeat
        }

        assertTrue("Expected coins to increase from early kills", state.coins > initialCoins)
        assertTrue("Expected to clear at least wave 1", state.waveIndex >= 1)
        assertTrue("Base HP should remain positive in early game", state.baseHp > 0)
    }


    @Test
    fun twoShootersInStageOneEarnCoinsReliably() {
        val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
        val loaded = loader.loadAll()
        val engine = GameEngine(loaded)

        val cells = MutableList(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { null as UnitInstance? }
        cells[0] = UnitInstance(id = "u-1", role = "SHOOTER", unitDefId = "archer", level = 1)
        cells[1] = UnitInstance(id = "u-2", role = "SHOOTER", unitDefId = "archer", level = 1)
        var state = engine.newRun(stageIndex = 0, seed = 15L).copy(board = Board(cells = cells))
        val initialCoins = state.coins

        repeat(40) {
            state = engine.tick(state, 500L).state
        }

        assertTrue("Expected two shooters to kill enemies and gain coins", state.coins > initialCoins)
    }


    @Test
    fun shooterByUnitIdFallbackDealsDamageAndEarnsCoins() {
        val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
        val loaded = loader.loadAll()

        val config =
            loaded.copy(
                stages =
                    listOf(
                        loaded.stages.first().copy(
                            waves =
                                listOf(
                                    WaveConfig(normal = 2, fast = 0, tank = 0, boss = 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                    WaveConfig(0, 0, 0, 0),
                                ),
                        ),
                    ),
            )

        val engine = GameEngine(config)
        val cells = MutableList(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { null as UnitInstance? }
        cells[0] = UnitInstance(id = "u-archer", role = "archer", unitDefId = "archer", level = 1)
        var state = engine.newRun(stageIndex = 0, seed = 99L).copy(board = Board(cells = cells))
        val initialCoins = state.coins

        repeat(30) {
            state = engine.tick(state, 500L).state
        }

        assertTrue("Expected fallback role mapping to allow kills", state.coins > initialCoins)
    }

}
