package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import com.example.core.model.IconShape
import com.example.core.model.QualityTier
import com.example.core.model.WallpaperPreset
import kotlinx.coroutines.flow.Flow

class AppearanceRepository(
    private val dataStore: LauncherDataStore
) {

    val qualityTier: Flow<QualityTier> = dataStore.qualityTier

    val wallpaperPreset: Flow<WallpaperPreset> =
        dataStore.wallpaperPreset

    val iconShape: Flow<IconShape> =
        dataStore.iconShape

    val showIconLabels: Flow<Boolean> =
        dataStore.showIconLabels

    suspend fun setQualityTier(tier: QualityTier) =
        dataStore.setQualityTier(tier)

    suspend fun setWallpaperPreset(preset: WallpaperPreset) =
        dataStore.setWallpaperPreset(preset)

    suspend fun setIconShape(shape: IconShape) =
        dataStore.setIconShape(shape)

    suspend fun setShowIconLabels(show: Boolean) =
        dataStore.setShowIconLabels(show)
}
