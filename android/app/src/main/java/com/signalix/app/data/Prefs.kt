package com.signalix.app.data

import android.content.Context

object Prefs {
    private const val PREFS = "signalix"
    private const val KEY_THEME = "theme"
    private const val KEY_PALETTE = "palette"
    private const val KEY_SERVER = "server"
    private const val KEY_REMEMBER = "remember"
    private const val KEY_USER = "user"
    private const val KEY_TOKEN = "token"
    private const val KEY_CURRENT = "current_user"
    private const val KEY_FULLSCREEN = "fullscreen"

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

    fun setServer(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_SERVER, url).apply()
    }

    fun getServer(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SERVER, "https://mhrcfdxelbmiuabvarrg.supabase.co")
            ?: "https://mhrcfdxelbmiuabvarrg.supabase.co"

    fun setRemember(context: Context, remember: Boolean, user: String, token: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REMEMBER, remember)
            .putString(KEY_USER, user)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getRemember(context: Context): Triple<Boolean, String, String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val remember = prefs.getBoolean(KEY_REMEMBER, false)
        val user = prefs.getString(KEY_USER, "") ?: ""
        val token = prefs.getString(KEY_TOKEN, "") ?: ""
        return Triple(remember, user, token)
    }

    fun setCurrentUser(context: Context, user: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_CURRENT, user).apply()
    }

    fun getCurrentUser(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CURRENT, "") ?: ""

    fun setFullscreen(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_FULLSCREEN, enabled).apply()
    }

    fun isFullscreen(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_FULLSCREEN, false)
}
