package com.mahi.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser

class IconPackManager(private val context: Context) {

    data class IconPack(
        val label: String,
        val packageName: String,
        val icon: Drawable
    )

    fun getInstalledIconPacks(): List<IconPack> {
        val packs = mutableListOf<IconPack>()
        val pm = context.packageManager

        // standard icon pack intents
        val intents = listOf(
            Intent("com.novalauncher.THEME"),
            Intent("org.adw.launcher.THEMES"),
            Intent("com.teslacoilsw.launcher.THEME"),
            Intent("com.gau.go.launcherex.theme"),
            Intent("org.adw.launcher.icons.ACTION_PICK_ICON")
        )

        val seen = mutableSetOf<String>()
        for (intent in intents) {
            val resolveInfos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
            for (ri in resolveInfos) {
                val pkg = ri.activityInfo.packageName
                if (seen.add(pkg)) {
                    packs.add(
                        IconPack(
                            label = ri.loadLabel(pm).toString(),
                            packageName = pkg,
                            icon = ri.loadIcon(pm)
                        )
                    )
                }
            }
        }
        return packs
    }

    fun getIconForApp(iconPackPackage: String, appPackage: String): Drawable? {
        return try {
            val res = context.packageManager.getResourcesForApplication(iconPackPackage)
            val drawableName = appPackage.replace(".", "_")
            val resId = res.getIdentifier(drawableName, "drawable", iconPackPackage)
            if (resId != 0) res.getDrawable(resId, null) else null
        } catch (e: Exception) {
            null
        }
    }

    fun getActiveIconPack(): String? {
        return MahiApp.prefs.getString("icon_pack", null)
    }

    fun setActiveIconPack(packageName: String?) {
        MahiApp.prefs.edit().putString("icon_pack", packageName).apply()
    }
}
