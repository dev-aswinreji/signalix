package com.signalix.app.data

import android.content.Context

object Prefs {
    private const val PREFS = "signalix"
    private const val KEY_THEME = "theme"
    private const val KEY_PALETTE = "palette"

    fun setTheme(context: Context, dark: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_THEME, dark).apply()
    }

    fun isDark(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_THEME, true)

    fun setPalette(context: Context, palette: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PALETTE, palette).apply()
    }

    fun getPalette(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PALETTE, "indigo") ?: "indigo"
}
