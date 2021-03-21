package com.jamal2367.styx.settings.activity

import android.graphics.Color
import com.jamal2367.styx.AppTheme
import com.jamal2367.styx.R
import com.jamal2367.styx.ThemedActivity
import com.jamal2367.styx.extensions.setStatusBarIconsColor
import com.jamal2367.styx.utils.ThemeUtils
import com.jamal2367.styx.utils.foregroundColorFromBackgroundColor


abstract class ThemedSettingsActivity : ThemedActivity() {

    override fun onResume() {
        super.onResume()
        // Make sure icons have the right color
        setStatusBarIconsColor(foregroundColorFromBackgroundColor(ThemeUtils.getPrimaryColor(this)) == Color.BLACK && !userPreferences.useBlackStatusBar)
        resetPreferences()
        if (userPreferences.useTheme != themeId) {
            recreate()
        }
    }

    /**
     * From ThemedActivity
     */
    override fun themeStyle(aTheme: AppTheme): Int {
        return when (aTheme) {
		    AppTheme.DEFAULT -> R.style.Theme_App_DayNight_Settings
            AppTheme.LIGHT -> R.style.Theme_App_Light_Settings
            AppTheme.DARK ->  R.style.Theme_App_Dark_Settings
            AppTheme.BLACK -> R.style.Theme_App_Black_Settings
        }
    }
}
