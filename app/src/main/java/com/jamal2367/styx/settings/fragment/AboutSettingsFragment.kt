/*
 * Copyright 2014 A.C.R. Development
 */
package com.jamal2367.styx.settings.fragment

import android.os.Bundle
import androidx.webkit.WebViewCompat
import com.jamal2367.styx.BuildConfig
import com.jamal2367.styx.R


class AboutSettingsFragment : AbstractSettingsFragment() {

    override fun providePreferencesXmlResource() = R.xml.preference_about

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        var webview = resources.getString(R.string.unknown)

        context?.let {
            WebViewCompat.getCurrentWebViewPackage(it)?.versionName?.let {
                webview = it
            }
        }

        clickablePreference(
            preference = SETTINGS_VERSION,
            summary = "${getString(R.string.pref_app_version_summary)} ${BuildConfig.VERSION_NAME} (${getString(R.string.app_version_name)})"
        )

        clickablePreference(
            preference = WEBVIEW_VERSION,
            summary = webview,
            onClick = { }
        )

    }

    companion object {
        private const val SETTINGS_VERSION = "pref_version"
        private const val WEBVIEW_VERSION = "pref_webview"
        private const val TAG = "AboutSettingsFragment"
    }
}