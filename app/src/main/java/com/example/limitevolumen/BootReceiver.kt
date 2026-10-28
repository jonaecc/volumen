package com.example.limitevolumen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/** Arranca el limitador automáticamente cuando se enciende la TV. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && Prefs.enabled(context)) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, VolumeGuardService::class.java)
            )
        }
    }
}
