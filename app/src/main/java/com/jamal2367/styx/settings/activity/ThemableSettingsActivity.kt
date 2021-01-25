package com.jamal2367.styx.settings.activity

import com.jamal2367.styx.AppTheme
import com.jamal2367.styx.R
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.extensions.setStatusBarIconsColor
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.utils.ThemeUtils
import com.jamal2367.styx.utils.foregroundColorFromBackgroundColor
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

abstract class ThemableSettingsActivity : AppCompatActivity() {

    protected var themeId: AppTheme = AppTheme.LIGHT

    @Inject internal lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        themeId = userPreferences.useTheme

        // set the theme
        applyTheme(themeId)

        super.onCreate(savedInstanceState)

        resetPreferences()
    }

    protected fun applyTheme(themeId: AppTheme) {
        when (themeId) {
            AppTheme.LIGHT -> {
                setTheme(R.style.Theme_App_Light_Settings)
            }
            AppTheme.DARK -> {
                setTheme(R.style.Theme_App_Dark_Settings)
            }
            AppTheme.BLACK -> {
                setTheme(R.style.Theme_App_Black_Settings)
            }
        }
    }

    private fun resetPreferences() {
        if (userPreferences.useBlackStatusBar) {
            window.statusBarColor = Color.BLACK
        } else {
            window.statusBarColor = ThemeUtils.getStatusBarColor(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // Make sure icons have the right color
        setStatusBarIconsColor(foregroundColorFromBackgroundColor(ThemeUtils.getPrimaryColor(this))==Color.BLACK && !userPreferences.useBlackStatusBar)
        resetPreferences()
        if (userPreferences.useTheme != themeId) {
            recreate()
        }
    }

}
