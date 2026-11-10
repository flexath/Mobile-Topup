package com.aungthiha.topup.core.designsystem.utils

import android.content.res.Resources

object PlatformConfiguration {
    private val resources = Resources.getSystem()

    val screenWidthDp: Int
        get() = (resources.displayMetrics.widthPixels / resources.displayMetrics.density).toInt()

    val fontScale: Float
        get() = resources.configuration.fontScale
}
