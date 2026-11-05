package com.mahi.launcher

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class MahiApp : Application() {

    companion object {
        lateinit var instance: MahiApp
            private set
        lateinit var prefs: SharedPreferences
            private set

        const val PRIME_ACTIVE = true

        fun isFirstLaunch(): Boolean {
            val first = prefs.getBoolean("first_launch", true)
            if (first) prefs.edit().putBoolean("first_launch", false).apply()
            return first
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (isFirstLaunch()) {
            prefs.edit().apply {
                putString("theme_mode", "dark")
                putInt("grid_columns", 4)
                putInt("grid_rows", 5)
                putBoolean("show_labels", true)
                putBoolean("show_dock", true)
                putBoolean("show_search_bar", true)
                putString("animation_style", "slide")
                putFloat("animation_speed", 1.0f)
                putInt("icon_size", 58)
                putString("gesture_double_tap", "lock_screen")
                putString("gesture_swipe_up", "app_drawer")
                putString("gesture_swipe_down", "notifications")
                putString("gesture_pinch_in", "settings")
                putString("gesture_swipe_left", "none")
                putString("gesture_swipe_right", "none")
                apply()
            }
        }
    }
}
