package com.mahi.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // send broadcast to refresh launcher when apps are installed/removed
        context?.sendBroadcast(Intent("com.mahi.launcher.REFRESH_APPS"))
    }
}
