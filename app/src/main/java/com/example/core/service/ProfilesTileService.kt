package com.example.core.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.core.LauncherDependencies
import com.example.core.data.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Native Android Quick Settings Tile for switching
 * Purple Launcher personalities.
 *
 * The tile is only a control surface.
 * Profile state remains owned by ProfileRepository.
 */
@RequiresApi(Build.VERSION_CODES.N)
class ProfilesTileService : TileService() {

    private lateinit var profileRepository: ProfileRepository

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        profileRepository =
            LauncherDependencies.get(applicationContext).profileRepository
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        serviceScope.launch {
            profileRepository.cycleProfile()
            updateTileState()
        }
    }

    private fun updateTileState() {
        serviceScope.launch {
            val profile = profileRepository.activeProfile.first()

            val tile = qsTile ?: return@launch

            tile.label = "Profile: ${profile.title}"
            tile.contentDescription =
                "Purple Launcher ${profile.title} personality"

            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
