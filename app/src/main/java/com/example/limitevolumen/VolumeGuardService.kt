package com.example.limitevolumen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings

/**
 * Servicio en primer plano que vigila el volumen y lo baja al tope
 * configurado cada vez que alguien lo sube de más.
 */
class VolumeGuardService : Service() {

    private lateinit var audio: AudioManager
    private val handler = Handler(Looper.getMainLooper())

    // Respaldo: revisa cada medio segundo por si el sistema no avisa del cambio.
    private val poller = object : Runnable {
        override fun run() {
            enforce()
            handler.postDelayed(this, 500)
        }
    }

    // Reacciona de inmediato cuando cambian los ajustes de volumen.
    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            enforce()
        }
    }

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        handler.post(poller)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(poller)
        contentResolver.unregisterContentObserver(observer)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun enforce() {
        if (!Prefs.enabled(this)) return
        val streamMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val limit = streamMax * Prefs.maxPercent(this) / 100
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) > limit) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, limit, 0)
        }
    }

    private fun startInForeground() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_speaker)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "limite_volumen"
        private const val NOTIFICATION_ID = 1
    }
}
