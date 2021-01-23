package com.jamal2367.styx.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.jamal2367.styx.R
import com.jamal2367.styx.utils.ThemeUtils

/**
 * Create a new transition drawable with the specified list of layers. At least
 * 2 layers are required for this drawable to work properly.
 */
class BackgroundDrawable : TransitionDrawable
{

    constructor(
            context: Context,
            @AttrRes first: Int = R.attr.colorPrimaryDark,
            @AttrRes second: Int = R.attr.selectedBackground
    ) : super (
            arrayOf<Drawable>(
                    ColorDrawable(ThemeUtils.getColor(context, first)),
                    ColorDrawable(ThemeUtils.getColor(context, second))
            )
    )

    constructor(
            context: Context,
            aFirst: Drawable,
            aSecond: Drawable
    ) : super (
            arrayOf<Drawable>(
                    aFirst,
                    aSecond)
    )

    var isSelected: Boolean = false

    override fun startTransition(durationMillis: Int) {
        if (!isSelected) {
            super.startTransition(durationMillis)
        }
        isSelected = true
    }

    override fun reverseTransition(duration: Int) {
        if (isSelected) {
            super.reverseTransition(duration)
        }
        isSelected = false
    }

}
