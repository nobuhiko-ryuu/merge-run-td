package com.example.mergeruntd.domain.lane

import com.example.mergeruntd.domain.core.GameConstants

data class EnemyInstance(
    val id: String,
    val hp: Int,
    val tile: Int = 0,
    val progressMs: Long = 0L,
)

data class PendingSpawn(
    val enemyType: String,
    val dueMs: Long,
)

data class LaneState(
    val length: Int = GameConstants.LANE_TILES,
    val enemies: List<EnemyInstance> = emptyList(),
    val pendingSpawns: List<PendingSpawn> = emptyList(),
    val combatElapsedMs: Long = 0L,
)
