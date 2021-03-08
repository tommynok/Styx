package com.jamal2367.styx.browser.activity

import com.jamal2367.styx.AppTheme
import com.jamal2367.styx.R
import com.jamal2367.styx.ThemedActivity
import com.jamal2367.styx.di.injector
import android.content.Intent
import android.os.Bundle


abstract class ThemedBrowserActivity : ThemedActivity() {

    private var shouldRunOnResumeActions = false


    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && shouldRunOnResumeActions) {
            shouldRunOnResumeActions = false
            onWindowVisibleToUserAfterResume()
        }
    }


    override fun onResume() {
        super.onResume()
        resetPreferences()
        shouldRunOnResumeActions = true
        if (themeId != userPreferences.useTheme) {
            restart()
        }
    }

    /**
     * Using this instead of recreate() because it does not work when handling resource changes I guess.
     */
    protected fun restart() {
        finish()
        startActivity(Intent(this, javaClass))
    }

    /**
     * From ThemedActivity
     */
    override fun themeStyle(aTheme: AppTheme): Int {
        return when (aTheme) {
            AppTheme.DEFAULT -> R.style.Theme_App_DayNight
            AppTheme.LIGHT -> R.style.Theme_App_Light
            AppTheme.DARK ->  R.style.Theme_App_Dark
            AppTheme.BLACK -> R.style.Theme_App_Black
        }
    }

}
