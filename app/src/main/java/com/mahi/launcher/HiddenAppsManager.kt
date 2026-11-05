package com.mahi.launcher

import android.content.Context

class HiddenAppsManager(context: Context) {

    private val prefs = context.getSharedPreferences("hidden_apps", Context.MODE_PRIVATE)
    private val key = "hidden_set"

    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    fun hideApp(packageName: String) {
        val current = getHiddenApps().toMutableSet()
        current.add(packageName)
        prefs.edit().putStringSet(key, current).apply()
    }

    fun unhideApp(packageName: String) {
        val current = getHiddenApps().toMutableSet()
        current.remove(packageName)
        prefs.edit().putStringSet(key, current).apply()
    }

    fun isHidden(packageName: String): Boolean {
        return getHiddenApps().contains(packageName)
    }

    fun toggleHidden(packageName: String) {
        if (isHidden(packageName)) unhideApp(packageName) else hideApp(packageName)
    }
}
