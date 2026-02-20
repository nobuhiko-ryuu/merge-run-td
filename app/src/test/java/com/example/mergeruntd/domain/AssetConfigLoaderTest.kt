package com.example.mergeruntd.domain

import com.example.mergeruntd.domain.config.AssetConfigLoader
import com.example.mergeruntd.domain.config.TestResourceProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetConfigLoaderTest {
  @Test
  fun loadAll() {
    val loader = AssetConfigLoader(TestResourceProvider(javaClass.classLoader!!))
    val config = loader.loadAll()

    assertEquals(20, config.stages.size)
    assertTrue(config.stages.all { it.waves.size == 5 })
    assertEquals(12, config.laneTiles)
    assertEquals(70, config.unitDefs.first { it.id == "guardian" }.baseHp)
  }
}
