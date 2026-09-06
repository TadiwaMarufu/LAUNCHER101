package com.example.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.core.apps.AppManager

/**
 * Listens for system package events (app install, uninstall, update)
 * to keep the launcher's app drawer synchronized in real time.
 */
class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val packageName = intent.data?.schemeSpecificPart
        Log.d("PackageChangeReceiver", "Received action: $action for package: $packageName")

        when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_CHANGED -> {
                AppManager.getInstance(context).loadInstalledApps()
            }
        }
    }
}
