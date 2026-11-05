package com.mahi.launcher

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.abs

class GestureHandler(
    private val context: Context,
    private val onAction: (GestureAction) -> Unit
) {

    enum class GestureAction {
        SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT,
        DOUBLE_TAP, LONG_PRESS, PINCH_IN, PINCH_OUT
    }

    private val swipeThreshold = 120
    private val swipeVelocity = 200

    private var lastPinchScale = 1f

    val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onAction(GestureAction.DOUBLE_TAP)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onAction(GestureAction.LONG_PRESS)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            val dx = e2.x - e1.x
            val dy = e2.y - e1.y

            if (abs(dx) > abs(dy)) {
                if (abs(dx) > swipeThreshold && abs(velocityX) > swipeVelocity) {
                    if (dx > 0) onAction(GestureAction.SWIPE_RIGHT)
                    else onAction(GestureAction.SWIPE_LEFT)
                    return true
                }
            } else {
                if (abs(dy) > swipeThreshold && abs(velocityY) > swipeVelocity) {
                    if (dy > 0) onAction(GestureAction.SWIPE_DOWN)
                    else onAction(GestureAction.SWIPE_UP)
                    return true
                }
            }
            return false
        }
    })

    val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            lastPinchScale = 1f
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            lastPinchScale *= detector.scaleFactor
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (lastPinchScale < 0.8f) onAction(GestureAction.PINCH_IN)
            else if (lastPinchScale > 1.2f) onAction(GestureAction.PINCH_OUT)
        }
    })

    fun resolveGestureAction(action: GestureAction) {
        val prefKey = when (action) {
            GestureAction.SWIPE_UP -> "gesture_swipe_up"
            GestureAction.SWIPE_DOWN -> "gesture_swipe_down"
            GestureAction.SWIPE_LEFT -> "gesture_swipe_left"
            GestureAction.SWIPE_RIGHT -> "gesture_swipe_right"
            GestureAction.DOUBLE_TAP -> "gesture_double_tap"
            GestureAction.LONG_PRESS -> "gesture_long_press"
            GestureAction.PINCH_IN -> "gesture_pinch_in"
            GestureAction.PINCH_OUT -> "gesture_pinch_out"
        }
        val target = MahiApp.prefs.getString(prefKey, "none") ?: "none"
        executeAction(target)
    }

    private fun executeAction(action: String) {
        when (action) {
            "app_drawer" -> {
                context.startActivity(
                    Intent(context, AppDrawerActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            "notifications" -> {
                try {
                    @Suppress("WrongConstant")
                    val sbService = context.getSystemService("statusbar")
                    sbService?.javaClass?.getMethod("expandNotificationsPanel")?.invoke(sbService)
                } catch (_: Exception) {}
            }
            "quick_settings" -> {
                try {
                    @Suppress("WrongConstant")
                    val sbService = context.getSystemService("statusbar")
                    sbService?.javaClass?.getMethod("expandSettingsPanel")?.invoke(sbService)
                } catch (_: Exception) {}
            }
            "settings" -> {
                context.startActivity(
                    Intent(context, SettingsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            "lock_screen" -> {
                try {
                    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    dpm.lockNow()
                } catch (_: Exception) {
                    // needs device admin permission
                }
            }
            "phone_settings" -> {
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            "recent_apps" -> {
                try {
                    @Suppress("WrongConstant")
                    val sbService = context.getSystemService("statusbar")
                    sbService?.javaClass?.getMethod("toggleRecentApps")?.invoke(sbService)
                } catch (_: Exception) {}
            }
            "none" -> { /* noop */ }
            else -> {
                // try launching app by package
                try {
                    val intent = context.packageManager.getLaunchIntentForPackage(action)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent?.let { context.startActivity(it) }
                } catch (_: Exception) {}
            }
        }
    }
}
