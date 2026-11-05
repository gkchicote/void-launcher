package com.mahi.launcher

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator

object AnimationHelper {

    private fun getSpeed(): Long {
        val factor = MahiApp.prefs.getFloat("animation_speed", 1.0f)
        return (300 * factor).toLong()
    }

    fun getStyle(): String {
        return MahiApp.prefs.getString("animation_style", "slide") ?: "slide"
    }

    fun animateAppLaunch(view: View, onEnd: () -> Unit) {
        val duration = getSpeed()
        when (getStyle()) {
            "scale" -> {
                val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 0f)
                val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 0f)
                val alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.8f, 0f)
                AnimatorSet().apply {
                    playTogether(scaleX, scaleY, alpha)
                    this.duration = duration
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
                view.postDelayed({ resetView(view); onEnd() }, duration)
            }
            "fade" -> {
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                    this.duration = duration
                    start()
                }
                view.postDelayed({ resetView(view); onEnd() }, duration)
            }
            "slide" -> {
                ObjectAnimator.ofFloat(view, "translationY", 0f, -view.height.toFloat()).apply {
                    this.duration = duration
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
                view.postDelayed({ resetView(view); onEnd() }, duration)
            }
            "bounce" -> {
                val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f, 1.1f, 0f)
                val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f, 1.1f, 0f)
                AnimatorSet().apply {
                    playTogether(scaleX, scaleY)
                    this.duration = duration
                    interpolator = AnticipateOvershootInterpolator()
                    start()
                }
                view.postDelayed({ resetView(view); onEnd() }, duration)
            }
            else -> onEnd()
        }
    }

    fun animateGridItemEntry(view: View, position: Int) {
        view.alpha = 0f
        view.translationY = 60f
        view.scaleX = 0.9f
        view.scaleY = 0.9f

        val delay = (position * 30L).coerceAtMost(500L)
        val duration = getSpeed()

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(OvershootInterpolator(0.8f))
            .start()
    }

    fun animateDrawerOpen(view: View) {
        view.translationY = view.height.toFloat()
        view.alpha = 0f
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(getSpeed())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun resetView(view: View) {
        view.scaleX = 1f
        view.scaleY = 1f
        view.alpha = 1f
        view.translationX = 0f
        view.translationY = 0f
    }
}
