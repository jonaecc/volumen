package com.example.limitevolumen

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Descarta la tecla "subir volumen" cuando ya se está en el tope.
 * Así el sistema no sube el volumen ni dibuja la barra, y no hay "rebote".
 * Por debajo del tope la tecla pasa normal.
 */
class VolumeKeyService : AccessibilityService() {

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) return false
        if (!Prefs.enabled(this)) return false

        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val limit = max * Prefs.maxPercent(this) / 100
        // true = tecla consumida (el sistema no la ve)
        return audio.getStreamVolume(AudioManager.STREAM_MUSIC) >= limit
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}
