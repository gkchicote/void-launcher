package com.mahi.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserManager

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val hiddenAppsManager = HiddenAppsManager(context)

    fun getAllApps(includeHidden: Boolean = false): List<AppModel> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val hiddenSet = hiddenAppsManager.getHiddenApps()

        return packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo != null }
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                val pkg = activityInfo.packageName
                val isHidden = hiddenSet.contains(pkg)

                AppModel(
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = pkg,
                    activityName = activityInfo.name,
                    icon = resolveInfo.loadIcon(packageManager),
                    isHidden = isHidden
                )
            }
            .filter { includeHidden || !it.isHidden }
            .sortedBy { it.label.lowercase() }
    }

    fun getDockApps(): List<AppModel> {
        val dockPackages = MahiApp.prefs.getStringSet("dock_apps", defaultDockApps()) ?: emptySet()
        val allApps = getAllApps(includeHidden = false)
        return dockPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
    }

    private fun defaultDockApps(): Set<String> {
        val defaults = mutableSetOf<String>()
        val phoneIntent = Intent(Intent.ACTION_DIAL)
        packageManager.resolveActivity(phoneIntent, 0)?.let {
            defaults.add(it.activityInfo.packageName)
        }
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("smsto:") }
        packageManager.resolveActivity(smsIntent, 0)?.let {
            defaults.add(it.activityInfo.packageName)
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://"))
        packageManager.resolveActivity(browserIntent, 0)?.let {
            defaults.add(it.activityInfo.packageName)
        }
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        packageManager.resolveActivity(cameraIntent, 0)?.let {
            defaults.add(it.activityInfo.packageName)
        }
        return defaults
    }
}
