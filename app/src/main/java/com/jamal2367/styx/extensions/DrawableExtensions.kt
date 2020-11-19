package com.jamal2367.styx.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat

/**
 * Tint a drawable with the provided [color], using [BlendModeCompat.SRC_IN].
 */
fun Drawable.tint(@ColorInt color: Int) {
    colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_IN)
}
