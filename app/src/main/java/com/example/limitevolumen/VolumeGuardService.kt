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
import android.os.SystemClock
import android.provider.Settings

/**
 * Servicio en primer plano que vigila el volumen. Si pasa del tope, deja que
 * suene GRACE_MS milisegundos y luego lo baja de a un nivel cada STEP_MS hasta
 * llegar al tope. Si alguien lo vuelve a subir, la espera empieza de nuevo.
 * Los cambios se hacen sin mostrar la barra de volumen del sistema.
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

    private var overSince = 0L   // desde cuándo está por encima del tope (0 = no lo está)
    private var lastStepAt = 0L   // última vez que se bajó un nivel
    private var lastVolume = -1   // último volumen visto, para detectar subidas

    private fun enforce() {
        if (!Prefs.enabled(this)) {
            overSince = 0L
            lastVolume = -1
            return
        }
        val now = SystemClock.elapsedRealtime()
        val streamMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val limit = streamMax * Prefs.maxPercent(this) / 100
        val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

        if (current <= limit) {
            overSince = 0L
            lastVolume = current
            return
        }

        // Por encima del tope: empieza la espera, o la reinicia si volvieron a subirlo.
        if (overSince == 0L || current > lastVolume) {
            overSince = now
            lastStepAt = 0L
        }
        lastVolume = current

        if (now - overSince < GRACE_MS) return

        // Terminó la espera: baja de a un nivel, sin mostrar la barra (flags = 0).
        if (now - lastStepAt >= STEP_MS) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, current - 1, 0)
            lastStepAt = now
            lastVolume = current - 1
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

        /** Tiempo que se tolera el volumen por encima del tope antes de empezar a bajarlo. */
        private const val GRACE_MS = 15_000L

        /** Cada cuánto baja un nivel una vez pasada la espera. */
        private const val STEP_MS = 1_500L
    }
}
