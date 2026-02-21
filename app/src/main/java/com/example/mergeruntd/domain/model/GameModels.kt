package com.example.mergeruntd.domain.model

data class GameConfig(
    val stages: List<StageConfig>,
    val unitDefs: List<UnitDef>,
    val enemyDefs: List<EnemyDef>,
    val upgrades: List<UpgradeDef>,
    val upgradeRules: UpgradeRules,
    val laneTiles: Int,
    val shopConfig: ShopConfig,
)

data class StageConfig(
    val stage: Int,
    val hpMul: Double,
    val spdMul: Double,
    val countAdd: Int,
    val waves: List<WaveConfig>,
)

data class WaveConfig(
    val normal: Int,
    val fast: Int,
    val tank: Int,
    val boss: Int,
)

data class UnitDef(
    val id: String,
    val role: String,
    val baseAtk: Int,
    val atkSpd: Double,
    val range: Double,
    val baseHp: Int? = null,
)

data class EnemyDef(
    val id: String,
    val hp: Int,
    val speed: Double,
    val baseDamage: Int,
    val reward: Int,
)

data class UpgradeDef(
    val id: String,
    val type: String,
    val name: String,
    val atkMul: Double? = null,
    val aspdMul: Double? = null,
    val rerollCostDelta: Int? = null,
    val waveStartFreeRerollBonus: Int? = null,
    val maxApplications: Int? = null,
    val targetRole: String? = null,
)

data class UpgradeRules(
    val offerAfterWaves: List<Int>,
    val timeoutSec: Int,
    val maxTransformPerRun: Int,
    val autoPickTypeOnTimeout: String,
)

data class ShopConfig(
    val slots: Int,
    val buyCost: Int,
    val rerollCost: Int,
    val sellRefund: Int,
    val refillMs: Long,
    val spawnWeights: Map<String, Double>,
)
