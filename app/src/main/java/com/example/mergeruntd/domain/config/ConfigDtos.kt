package com.example.mergeruntd.domain.config

import com.example.mergeruntd.domain.model.EnemyDef
import com.example.mergeruntd.domain.model.GameConfig
import com.example.mergeruntd.domain.model.ShopConfig
import com.example.mergeruntd.domain.model.UpgradeRules
import com.example.mergeruntd.domain.model.StageConfig
import com.example.mergeruntd.domain.model.UnitDef
import com.example.mergeruntd.domain.model.UpgradeDef
import com.example.mergeruntd.domain.model.WaveConfig
import kotlinx.serialization.Serializable

@Serializable
data class StageDto(
    val stage: Int,
    val hpMul: Double,
    val spdMul: Double,
    val countAdd: Int,
    val waves: List<WaveDto>,
)

@Serializable
data class WaveDto(
    val normal: Int = 0,
    val fast: Int = 0,
    val tank: Int = 0,
    val boss: Int = 0,
)

@Serializable
data class EnemyDefsDto(
    val enemies: List<EnemyDto>,
    val lane: LaneDto,
)

@Serializable
data class EnemyDto(
    val id: String,
    val hp: Int,
    val speed: Double,
    val baseDamage: Int,
    val reward: Int,
)

@Serializable
data class LaneDto(val tiles: Int)

@Serializable
data class UnitDefsDto(
    val units: List<UnitDto>,
    val shop: ShopDto,
)

@Serializable
data class UnitDto(
    val id: String,
    val role: String,
    val baseAtk: Int,
    val atkSpd: Double,
    val range: Double,
    val baseHp: Int? = null,
)

@Serializable
data class ShopDto(
    val slots: Int,
    val buyCost: Int,
    val rerollCost: Int,
    val sellRefund: Int,
    val refillSec: Double,
    val spawnWeights: Map<String, Double>,
)

@Serializable
data class UpgradesDto(
    val upgrades: List<UpgradeDto>,
    val rules: UpgradeRulesDto,
)

@Serializable
data class UpgradeRulesDto(
    val offerAfterWaves: List<Int>,
    val timeoutSec: Int,
    val maxTransformPerRun: Int,
    val autoPickTypeOnTimeout: String,
)

@Serializable
data class UpgradeDto(
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

fun toGameConfig(
    stages: List<StageDto>,
    enemyDefs: EnemyDefsDto,
    unitDefs: UnitDefsDto,
    upgrades: UpgradesDto,
): GameConfig {
    return GameConfig(
        stages =
            stages.map { stage ->
                StageConfig(
                    stage = stage.stage,
                    hpMul = stage.hpMul,
                    spdMul = stage.spdMul,
                    countAdd = stage.countAdd,
                    waves =
                        stage.waves.map { wave ->
                            WaveConfig(
                                normal = wave.normal,
                                fast = wave.fast,
                                tank = wave.tank,
                                boss = wave.boss,
                            )
                        },
                )
            },
        unitDefs =
            unitDefs.units.map { unit ->
                UnitDef(
                    id = unit.id,
                    role = unit.role,
                    baseAtk = unit.baseAtk,
                    atkSpd = unit.atkSpd,
                    range = unit.range,
                    baseHp = unit.baseHp,
                )
            },
        enemyDefs =
            enemyDefs.enemies.map { enemy ->
                EnemyDef(
                    id = enemy.id,
                    hp = enemy.hp,
                    speed = enemy.speed,
                    baseDamage = enemy.baseDamage,
                    reward = enemy.reward,
                )
            },
        upgrades =
            upgrades.upgrades.map { upgrade ->
                UpgradeDef(
                    id = upgrade.id,
                    type = upgrade.type,
                    name = upgrade.name,
                    atkMul = upgrade.atkMul,
                    aspdMul = upgrade.aspdMul,
                    rerollCostDelta = upgrade.rerollCostDelta,
                    waveStartFreeRerollBonus = upgrade.waveStartFreeRerollBonus,
                    maxApplications = upgrade.maxApplications,
                    targetRole = upgrade.targetRole,
                )
            },
        upgradeRules =
            UpgradeRules(
                offerAfterWaves = upgrades.rules.offerAfterWaves,
                timeoutSec = upgrades.rules.timeoutSec,
                maxTransformPerRun = upgrades.rules.maxTransformPerRun,
                autoPickTypeOnTimeout = upgrades.rules.autoPickTypeOnTimeout,
            ),
        laneTiles = enemyDefs.lane.tiles,
        shopConfig =
            ShopConfig(
                slots = unitDefs.shop.slots,
                buyCost = unitDefs.shop.buyCost,
                rerollCost = unitDefs.shop.rerollCost,
                sellRefund = unitDefs.shop.sellRefund,
                refillMs = (unitDefs.shop.refillSec * 1000).toLong(),
                spawnWeights = unitDefs.shop.spawnWeights,
            ),
    )
}
