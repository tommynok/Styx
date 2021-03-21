package com.jamal2367.styx.browser.tabs

import android.graphics.Bitmap
import android.graphics.Color
import com.jamal2367.styx.view.StyxView

/**
 * @param id The unique id of the tab.
 * @param title The title of the tab.
 * @param favicon The favicon of the tab, may be null.
 * @param isForegroundTab True if the tab is in the foreground, false otherwise.
 */
data class TabViewState(
        val id: Int = 0,
        val title: String = "",
        val favicon: Bitmap? = null,
        val isForeground: Boolean = false,
        val themeColor: Int = Color.TRANSPARENT,
        val isFrozen: Boolean = true
)

/**
 * Converts a [StyxView] to a [TabViewState].
 */
fun StyxView.asTabViewState() = TabViewState(
    id = id,
    title = title,
    favicon = favicon,
    isForeground = isForeground,
    themeColor = htmlMetaThemeColor
)
