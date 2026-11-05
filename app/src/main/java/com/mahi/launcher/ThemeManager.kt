package com.mahi.launcher

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val context: Context) {

    companion object {
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_AMOLED = "amoled"
        const val THEME_SYSTEM = "system"
    }

    data class ThemeColors(
        val background: Int,
        val surface: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accent: Int,
        val drawerBackground: Int,
        val searchBarBackground: Int,
        val searchBarText: Int,
        val dockBackground: Int
    )

    fun getCurrentTheme(): String {
        return MahiApp.prefs.getString("theme_mode", THEME_DARK) ?: THEME_DARK
    }

    fun setTheme(theme: String) {
        MahiApp.prefs.edit().putString("theme_mode", theme).apply()
        applySystemTheme(theme)
    }

    fun applySystemTheme(theme: String = getCurrentTheme()) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK, THEME_AMOLED -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun getAccentColor(): Int {
        val hex = MahiApp.prefs.getString("accent_color", "#6C5CE7") ?: "#6C5CE7"
        return try { Color.parseColor(hex) } catch (e: Exception) { Color.parseColor("#6C5CE7") }
    }

    fun setAccentColor(hex: String) {
        MahiApp.prefs.edit().putString("accent_color", hex).apply()
    }

    fun getColors(): ThemeColors {
        val accent = getAccentColor()
        return when (getCurrentTheme()) {
            THEME_LIGHT -> ThemeColors(
                background = Color.TRANSPARENT,
                surface = Color.parseColor("#F5F5F5"),
                textPrimary = Color.parseColor("#212121"),
                textSecondary = Color.parseColor("#757575"),
                accent = accent,
                drawerBackground = Color.parseColor("#F8F8F8"),
                searchBarBackground = Color.parseColor("#EEEEEE"),
                searchBarText = Color.parseColor("#424242"),
                dockBackground = Color.parseColor("#E0E0E0")
            )
            THEME_AMOLED -> ThemeColors(
                background = Color.BLACK,
                surface = Color.parseColor("#0A0A0A"),
                textPrimary = Color.WHITE,
                textSecondary = Color.parseColor("#AAAAAA"),
                accent = accent,
                drawerBackground = Color.BLACK,
                searchBarBackground = Color.parseColor("#1A1A1A"),
                searchBarText = Color.parseColor("#CCCCCC"),
                dockBackground = Color.parseColor("#0D0D0D")
            )
            else -> ThemeColors(
                background = Color.TRANSPARENT,
                surface = Color.parseColor("#1E1E2E"),
                textPrimary = Color.WHITE,
                textSecondary = Color.parseColor("#B0B0B0"),
                accent = accent,
                drawerBackground = Color.parseColor("#1A1A2E"),
                searchBarBackground = Color.parseColor("#2A2A3E"),
                searchBarText = Color.parseColor("#CCCCCC"),
                dockBackground = Color.parseColor("#16162A")
            )
        }
    }
}
