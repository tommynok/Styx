package com.jamal2367.styx.settings.fragment

import com.jamal2367.styx.Capabilities
import com.jamal2367.styx.R
import com.jamal2367.styx.browser.SearchBoxDisplayChoice
import com.jamal2367.styx.constant.TEXT_ENCODINGS
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.extensions.resizeAndShow
import com.jamal2367.styx.extensions.withSingleChoiceItems
import com.jamal2367.styx.isSupported
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.settings.NewTabPosition
import com.jamal2367.styx.view.RenderingMode
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import javax.inject.Inject

/**
 * The advanced settings of the app.
 */
class AdvancedSettingsFragment : AbstractSettingsFragment() {

    @Inject internal lateinit var userPreferences: UserPreferences

    override fun providePreferencesXmlResource() = R.xml.preference_advanced

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        injector.inject(this)




    }


}
