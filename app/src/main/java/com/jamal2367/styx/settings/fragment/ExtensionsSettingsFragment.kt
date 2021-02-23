package com.jamal2367.styx.settings.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jamal2367.styx.R
import com.jamal2367.styx.di.DatabaseScheduler
import com.jamal2367.styx.di.MainScheduler
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.extensions.snackbar
import javax.inject.Inject

/**
 * The extension settings of the app.
 */
class ExtensionsSettingsFragment : AbstractSettingsFragment() {

    @Inject lateinit var userPreferences: UserPreferences

    @Inject @field:DatabaseScheduler internal lateinit var databaseScheduler: io.reactivex.Scheduler
    @Inject @field:MainScheduler internal lateinit var mainScheduler: io.reactivex.Scheduler

    override fun providePreferencesXmlResource() = R.xml.preference_extensions

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_extensions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injector.inject(this)

        switchPreference(
                preference = DARK_MODE,
                isChecked = userPreferences.darkModeExtension,
                onCheckChange = { userPreferences.darkModeExtension = it; (activity as AppCompatActivity).snackbar(R.string.app_restart)}
        )
        switchPreference(
                preference = AMP,
                isChecked = userPreferences.noAmp,
                onCheckChange = { userPreferences.noAmp = it }
        )

    }

    companion object {
        private const val DARK_MODE = "dark_mode"
        private const val AMP = "amp"
    }
}
