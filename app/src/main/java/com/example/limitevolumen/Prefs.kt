package com.example.limitevolumen

import android.content.Context

/** Guarda el tope de volumen (en %) y si el limitador está activo. */
object Prefs {
    private const val FILE = "limite_volumen"
    private const val KEY_MAX = "max_percent"
    private const val KEY_ENABLED = "enabled"

    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun maxPercent(c: Context): Int = sp(c).getInt(KEY_MAX, 40)

    fun setMaxPercent(c: Context, value: Int) {
        sp(c).edit().putInt(KEY_MAX, value.coerceIn(0, 100)).apply()
    }

    fun enabled(c: Context): Boolean = sp(c).getBoolean(KEY_ENABLED, true)

    fun setEnabled(c: Context, value: Boolean) {
        sp(c).edit().putBoolean(KEY_ENABLED, value).apply()
    }
}
