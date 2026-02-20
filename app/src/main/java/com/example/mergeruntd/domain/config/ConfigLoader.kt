package com.example.mergeruntd.domain.config

import android.content.res.AssetManager
import com.example.mergeruntd.domain.model.GameConfig
import java.io.InputStream
import kotlinx.serialization.json.Json

interface InputStreamProvider {
    fun open(path: String): InputStream
}

class AndroidAssetProvider(
    private val assetManager: AssetManager,
) : InputStreamProvider {
    override fun open(path: String): InputStream = assetManager.open(path.removePrefix("assets/"))
}

class TestResourceProvider(
    private val classLoader: ClassLoader,
) : InputStreamProvider {
    override fun open(path: String): InputStream {
        return checkNotNull(classLoader.getResourceAsStream(path)) { "Missing test resource: $path" }
    }
}

class AssetConfigLoader(
    private val provider: InputStreamProvider,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun loadAll(): GameConfig {
        val stages = provider.open(STAGE_CONFIG_PATH).bufferedReader().use { input ->
            json.decodeFromString<List<StageDto>>(input.readText())
        }
        val enemyDefs = provider.open(ENEMY_DEFS_PATH).bufferedReader().use { input ->
            json.decodeFromString<EnemyDefsDto>(input.readText())
        }
        val unitDefs = provider.open(UNIT_DEFS_PATH).bufferedReader().use { input ->
            json.decodeFromString<UnitDefsDto>(input.readText())
        }
        val upgrades = provider.open(UPGRADES_PATH).bufferedReader().use { input ->
            json.decodeFromString<UpgradesDto>(input.readText())
        }
        return toGameConfig(stages = stages, enemyDefs = enemyDefs, unitDefs = unitDefs, upgrades = upgrades)
    }

    companion object {
        private const val STAGE_CONFIG_PATH = "assets/stage_config.json"
        private const val ENEMY_DEFS_PATH = "assets/enemy_defs.json"
        private const val UNIT_DEFS_PATH = "assets/unit_defs.json"
        private const val UPGRADES_PATH = "assets/upgrades.json"
    }
}
