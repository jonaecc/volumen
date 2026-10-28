package com.example.limitevolumen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat

/** Pantalla sencilla, pensada para usarse con el control remoto (flechas + OK). */
class MainActivity : Activity() {

    private lateinit var audio: AudioManager
    private lateinit var limitText: TextView
    private lateinit var currentText: TextView
    private lateinit var toggle: Button
    private lateinit var block: Button

    private val handler = Handler(Looper.getMainLooper())
    private val refresher = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        limitText = findViewById(R.id.limitText)
        currentText = findViewById(R.id.currentText)
        toggle = findViewById(R.id.toggle)
        block = findViewById(R.id.block)
        block.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {
                // Algunas TV no tienen esta pantalla; se activa por ADB (ver README).
            }
        }

        findViewById<Button>(R.id.minus).setOnClickListener { changeLimit(-5) }
        findViewById<Button>(R.id.plus).setOnClickListener { changeLimit(5) }
        toggle.setOnClickListener {
            Prefs.setEnabled(this, !Prefs.enabled(this))
            applyService()
            refresh()
        }

        applyService()
    }

    override fun onResume() {
        super.onResume()
        handler.post(refresher)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refresher)
    }

    private fun changeLimit(delta: Int) {
        Prefs.setMaxPercent(this, Prefs.maxPercent(this) + delta)
        refresh()
    }

    private fun applyService() {
        val intent = Intent(this, VolumeGuardService::class.java)
        if (Prefs.enabled(this)) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            stopService(intent)
        }
    }

    private fun refresh() {
        val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val pct = Prefs.maxPercent(this)
        val level = max * pct / 100

        currentText.text = getString(R.string.current_volume, current, max)
        limitText.text = getString(R.string.limit_value, pct, level)
        toggle.text = getString(
            if (Prefs.enabled(this)) R.string.toggle_on else R.string.toggle_off
        )
        block.text = getString(
            if (blockServiceEnabled()) R.string.block_on else R.string.block_off
        )
    }

    private fun blockServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains("$packageName/")
    }
}
