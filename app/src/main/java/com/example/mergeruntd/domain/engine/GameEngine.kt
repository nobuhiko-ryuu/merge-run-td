package com.example.mergeruntd.domain.engine

import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.board.UnitInstance
import com.example.mergeruntd.domain.core.GameConstants
import com.example.mergeruntd.domain.core.Phase
import com.example.mergeruntd.domain.core.RngState
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.lane.EnemyInstance
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.lane.PendingSpawn
import com.example.mergeruntd.domain.model.GameConfig
import com.example.mergeruntd.domain.model.StageConfig
import com.example.mergeruntd.domain.model.WaveConfig
import com.example.mergeruntd.domain.shop.ShopState
import com.example.mergeruntd.domain.shop.tickShop

data class TickResult(
    val state: RunState,
    val events: List<DomainEvent>,
)

sealed interface DomainEvent {
    data class WaveStarted(val waveIndex: Int) : DomainEvent
    data class WaveEnded(val waveIndex: Int) : DomainEvent
    data class BaseDamaged(val amount: Int) : DomainEvent
    data class EnemySpawned(val enemyId: String) : DomainEvent
    data class EnemyKilled(val enemyId: String) : DomainEvent
}

class GameEngine(
    private val config: GameConfig,
) {
    fun newRun(stageIndex: Int, seed: Long): RunState {
        val guardianHp = config.unitDefs.firstOrNull { it.id == "guardian" }?.baseHp ?: 70
        return RunState(
            stageIndex = stageIndex,
            waveIndex = 0,
            phase = Phase.PREP,
            baseHp = guardianHp,
            coins = 0,
            freeRerollLeft = 0,
            board = Board(),
            shop = ShopState(),
            lane = LaneState(length = config.laneTiles),
            timeMs = 0L,
            rng = RngState(seed),
        )
    }

    fun tick(state: RunState, deltaMs: Long): TickResult {
        var nextState = state.copy(timeMs = state.timeMs + deltaMs)
        val events = mutableListOf<DomainEvent>()

        if (nextState.phase == Phase.ENDED) {
            return TickResult(nextState, events)
        }

        if (nextState.phase == Phase.PREP || nextState.phase == Phase.POST_WAVE) {
            val stage = stageConfig(nextState.stageIndex)

            if (nextState.waveIndex >= stage.waves.size || nextState.waveIndex >= GameConstants.STAGE_WAVES) {
                return TickResult(nextState.copy(phase = Phase.ENDED), events)
            }

            val spawns = buildWaveSpawns(stage.waves[nextState.waveIndex])

            nextState =
                nextState.copy(
                    phase = Phase.COMBAT,
                    freeRerollLeft = nextState.freeRerollLeft + GameConstants.WAVE_START_FREE_REROLL_BONUS,
                    lane =
                        nextState.lane.copy(
                            pendingSpawns = spawns,
                            combatElapsedMs = 0L,
                            enemies = emptyList(),
                        ),
                )
            events += DomainEvent.WaveStarted(nextState.waveIndex)
        }

        val combatTick = tickCombat(nextState, deltaMs)
        nextState = combatTick.state
        events += combatTick.events

        val (shop, nextRng) = tickShop(nextState.shop, deltaMs, nextState.rng, config.unitDefs)
        nextState = nextState.copy(shop = shop, rng = nextRng)

        if (nextState.phase == Phase.COMBAT &&
            nextState.lane.pendingSpawns.isEmpty() &&
            nextState.lane.enemies.isEmpty()
        ) {
            events += DomainEvent.WaveEnded(nextState.waveIndex)
            val nextWaveIndex = nextState.waveIndex + 1
            nextState =
                if (nextWaveIndex >= GameConstants.STAGE_WAVES) {
                    nextState.copy(phase = Phase.ENDED, waveIndex = nextWaveIndex)
                } else {
                    nextState.copy(phase = Phase.POST_WAVE, waveIndex = nextWaveIndex)
                }
        }

        if (nextState.baseHp <= 0) {
            nextState = nextState.copy(phase = Phase.ENDED)
        }

        return TickResult(nextState, events)
    }

    private fun tickCombat(state: RunState, deltaMs: Long): TickResult {
        if (state.phase != Phase.COMBAT) return TickResult(state, emptyList())

        val events = mutableListOf<DomainEvent>()
        val elapsed = state.lane.combatElapsedMs + deltaMs

        val (dueSpawns, remainingSpawns) =
            state.lane.pendingSpawns.partition { it.dueMs <= elapsed }

        val spawnedEnemies =
            dueSpawns.map { pending ->
                val def = config.enemyDefs.firstOrNull { it.id == pending.enemyType }
                EnemyInstance(id = pending.enemyType, hp = def?.hp ?: 1)
            }
        events += dueSpawns.map { DomainEvent.EnemySpawned(it.enemyType) }

        var baseDamageTaken = 0
        val moved =
            (state.lane.enemies + spawnedEnemies).flatMap { enemy ->
                val speed = enemySpeedMsPerTile(enemy.id)
                val totalProgress = enemy.progressMs + deltaMs
                val tileAdvance = (totalProgress / speed).toInt()
                val remainder = totalProgress % speed
                val nextTile = enemy.tile + tileAdvance

                if (nextTile >= state.lane.length) {
                    val dmg = config.enemyDefs.firstOrNull { it.id == enemy.id }?.baseDamage ?: 1
                    baseDamageTaken += dmg
                    events += DomainEvent.BaseDamaged(dmg)
                    emptyList()
                } else {
                    listOf(enemy.copy(tile = nextTile, progressMs = remainder))
                }
            }

        val attacked = applyUnitAttacks(state.board, moved, deltaMs, events)

        val nextLane =
            state.lane.copy(
                enemies = attacked.enemies,
                pendingSpawns = remainingSpawns,
                combatElapsedMs = elapsed,
            )

        return TickResult(
            state.copy(
                board = attacked.board,
                lane = nextLane,
                baseHp = state.baseHp - baseDamageTaken,
            ),
            events,
        )
    }

    private data class AttackOutcome(
        val board: Board,
        val enemies: List<EnemyInstance>,
    )

    private fun applyUnitAttacks(
        board: Board,
        enemies: List<EnemyInstance>,
        deltaMs: Long,
        events: MutableList<DomainEvent>,
    ): AttackOutcome {
        var mutableEnemies = enemies
        val updatedUnits = mutableListOf<UnitInstance>()

        for (unit in board.allUnits()) {
            val reducedCd = unit.cooldownMs - deltaMs

            if (unit.role == "WALL") {
                updatedUnits += unit.copy(cooldownMs = reducedCd.coerceAtLeast(0L))
                continue
            }

            val targetIndex = mutableEnemies.indices.maxByOrNull { mutableEnemies[it].tile }

            if (reducedCd <= 0 && targetIndex != null) {
                val target = mutableEnemies[targetIndex]
                val damage = unitDamage(unit.role)
                val nextHp = target.hp - damage

                mutableEnemies =
                    if (nextHp <= 0) {
                        events += DomainEvent.EnemyKilled(target.id)
                        mutableEnemies.toMutableList().also { it.removeAt(targetIndex) }
                    } else {
                        mutableEnemies.toMutableList().also { it[targetIndex] = target.copy(hp = nextHp) }
                    }

                updatedUnits += unit.copy(cooldownMs = unitCooldown(unit.role))
            } else {
                updatedUnits += unit.copy(cooldownMs = reducedCd.coerceAtLeast(0L))
            }
        }

        return AttackOutcome(
            board = board.withUpdatedUnits(updatedUnits),
            enemies = mutableEnemies,
        )
    }

    private fun buildWaveSpawns(wave: WaveConfig): List<PendingSpawn> {
        var due = 0L
        val pending = mutableListOf<PendingSpawn>()

        fun add(type: String, count: Int) {
            repeat(count) {
                pending += PendingSpawn(type, due)
                due += 600L
            }
        }

        add("normal", wave.normal)
        add("fast", wave.fast)
        add("tank", wave.tank)
        add("boss", wave.boss)

        return pending
    }

    private fun stageConfig(stageIndex: Int): StageConfig = config.stages[stageIndex]

    private fun enemySpeedMsPerTile(enemyId: String): Long =
        when (enemyId) {
            "normal" -> GameConstants.NORMAL_SPEED_MS_PER_TILE
            "fast" -> GameConstants.FAST_SPEED_MS_PER_TILE
            "tank" -> GameConstants.TANK_SPEED_MS_PER_TILE
            "boss" -> GameConstants.BOSS_SPEED_MS_PER_TILE
            else -> GameConstants.NORMAL_SPEED_MS_PER_TILE
        }

    private fun unitDamage(role: String): Int =
        when (role) {
            "SHOOTER" -> GameConstants.SHOOTER_DMG
            "SPLASH" -> GameConstants.SPLASH_DMG
            "SLOW" -> GameConstants.SLOW_DMG
            else -> 0
        }

    private fun unitCooldown(role: String): Long =
        when (role) {
            "SHOOTER" -> GameConstants.SHOOTER_CD_MS
            "SPLASH" -> GameConstants.SPLASH_CD_MS
            "SLOW" -> GameConstants.SLOW_CD_MS
            else -> Long.MAX_VALUE
        }
}
