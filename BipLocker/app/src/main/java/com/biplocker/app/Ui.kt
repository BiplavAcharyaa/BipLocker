package com.biplocker.app

import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.View

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    var enabled: Boolean
)

object AppLookup {

    @Suppress("DEPRECATION")
    fun find(pm: PackageManager, packageName: String, enabled: Boolean): AppEntry? {
        return try {
            val info = pm.getApplicationInfo(packageName, 0)
            AppEntry(
                packageName,
                pm.getApplicationLabel(info).toString(),
                pm.getApplicationIcon(info),
                enabled
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}

fun View.fadeIn(delayMillis: Long = 0L) {
    alpha = 0f
    translationY = 14f * resources.displayMetrics.density
    animate()
        .alpha(1f)
        .translationY(0f)
        .setStartDelay(delayMillis)
        .setDuration(300L)
        .start()
}

fun View.shake() {
    ObjectAnimator.ofFloat(
        this,
        View.TRANSLATION_X,
        0f, -16f, 16f, -12f, 12f, -6f, 6f, 0f
    ).setDuration(340L).start()
}
