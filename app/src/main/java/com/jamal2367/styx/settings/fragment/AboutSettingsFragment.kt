/*
 * Copyright 2014 A.C.R. Development
 */
package com.jamal2367.styx.settings.fragment

import com.jamal2367.styx.BuildConfig
import com.jamal2367.styx.R
import android.os.Bundle


class AboutSettingsFragment : AbstractSettingsFragment() {

    override fun providePreferencesXmlResource() = R.xml.preference_about

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        clickablePreference(
            preference = SETTINGS_VERSION,
            summary = BuildConfig.VERSION_NAME,
        )

    }

    companion object {
        private const val SETTINGS_VERSION = "pref_version"
        private const val TAG = "AboutSettingsFragment"
    }
}
