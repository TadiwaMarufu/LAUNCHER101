package com.example.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.core.LauncherDependencies

/**
 * Keeps the installed-app model synchronized with Android.
 *
 * Package add/remove/replace/change events are public Android intents.
 * Enabled/disabled package broadcasts are represented by their Android
 * action strings because the SDK does not expose public constants for them.
 */
class PackageChangeReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        val action =
            intent?.action
                ?: return

        when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_CHANGED,
            ACTION_PACKAGE_ENABLED,
            ACTION_PACKAGE_DISABLED -> {

                val packageName =
                    intent.data
                        ?.schemeSpecificPart

                Log.d(
                    "PackageChangeReceiver",
                    "Package change: action=$action package=$packageName"
                )

                LauncherDependencies
                    .get(context.applicationContext)
                    .appManager
                    .loadInstalledApps()
            }
        }
    }

    companion object {
        private const val ACTION_PACKAGE_ENABLED =
            "android.intent.action.PACKAGE_ENABLED"

        private const val ACTION_PACKAGE_DISABLED =
            "android.intent.action.PACKAGE_DISABLED"
    }
}
