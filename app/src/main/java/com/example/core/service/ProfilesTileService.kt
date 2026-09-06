package com.example.core.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.core.data.LauncherPreferences

/**
 * Native Android Quick Settings Tile for switching Purple Launcher personalities.
 */
@RequiresApi(Build.VERSION_CODES.N)
class ProfilesTileService : TileService() {

    private lateinit var preferences: LauncherPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = LauncherPreferences.getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        // Cycle to the next personality
        val nextProfile = preferences.cycleNextProfile()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val currentProfile = preferences.activeProfile.value
        tile.label = "Profile: ${currentProfile.title}"
        tile.contentDescription = "Purple Launcher ${currentProfile.title} personality"
        tile.state = Tile.STATE_ACTIVE
        tile.updateTile()
    }
}
