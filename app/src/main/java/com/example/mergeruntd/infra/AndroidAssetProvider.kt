package com.example.mergeruntd.infra

import android.content.res.AssetManager
import com.example.mergeruntd.domain.config.InputStreamProvider
import java.io.InputStream

class AndroidAssetProvider(
  private val assetManager: AssetManager,
) : InputStreamProvider {
  override fun open(path: String): InputStream =
    assetManager.open(path.removePrefix("assets/"))
}
