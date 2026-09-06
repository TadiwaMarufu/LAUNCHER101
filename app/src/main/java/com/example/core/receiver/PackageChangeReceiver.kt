package com.example.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.core.LauncherDependencies

/**
 * Keeps the launcher's installed-app model synchronized with Android.
 *
 * The receiver does not own AppManager or persistence. It resolves the
 * shared launcher dependency graph so package events update the same
 * AppManager instance used by the UI.
 */
class PackageChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_CHANGED,
            Intent.ACTION_PACKAGE_ENABLED,
            Intent.ACTION_PACKAGE_DISABLED -> {
                val packageName =
                    intent.data?.schemeSpecificPart

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
}
