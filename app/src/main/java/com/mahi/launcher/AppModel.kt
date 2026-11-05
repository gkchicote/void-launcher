package com.mahi.launcher

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class AppModel(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable,
    val isHidden: Boolean = false,
    val customIconUri: String? = null
) {
    val componentName: ComponentName
        get() = ComponentName(packageName, activityName)

    fun matchesQuery(query: String): Boolean {
        return label.contains(query, ignoreCase = true) ||
               packageName.contains(query, ignoreCase = true)
    }
}
