package com.example.mergeruntd.domain.config

import com.example.mergeruntd.domain.model.GameConfig
import kotlinx.serialization.json.Json
import java.io.InputStream

interface InputStreamProvider {
    fun open(path: String): InputStream
}

class TestResourceProvider(
    private val classLoader: ClassLoader,
) : InputStreamProvider {
    override fun open(path: String): InputStream {
        return requireNotNull(classLoader.getResourceAsStream(path)) {
            "Missing test resource: $path"
        }
    }
}

class AssetConfigLoader(
    private val provider: InputStreamProvider,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun loadAll(): GameConfig {
        val stagesText = provider.open(STAGE_CONFIG_PATH).bufferedReader().use { it.readText() }
        val enemyText = provider.open(ENEMY_DEFS_PATH).bufferedReader().use { it.readText() }
        val unitText = provider.open(UNIT_DEFS_PATH).bufferedReader().use { it.readText() }
        val upgradeText = provider.open(UPGRADES_PATH).bufferedReader().use { it.readText() }

        val stages = json.decodeFromString<List<StageDto>>(stagesText)
        val enemyDefs = json.decodeFromString<EnemyDefsDto>(enemyText)
        val unitDefs = json.decodeFromString<UnitDefsDto>(unitText)
        val upgrades = json.decodeFromString<UpgradesDto>(upgradeText)

        return toGameConfig(
            stages = stages,
            enemyDefs = enemyDefs,
            unitDefs = unitDefs,
            upgrades = upgrades,
        )
    }

    companion object {
        private const val STAGE_CONFIG_PATH = "assets/stage_config.json"
        private const val ENEMY_DEFS_PATH = "assets/enemy_defs.json"
        private const val UNIT_DEFS_PATH = "assets/unit_defs.json"
        private const val UPGRADES_PATH = "assets/upgrades.json"
    }
}
