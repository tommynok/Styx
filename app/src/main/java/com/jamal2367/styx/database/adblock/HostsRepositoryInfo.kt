package com.jamal2367.styx.database.adblock

import android.content.SharedPreferences
import com.jamal2367.styx.R
import com.jamal2367.styx.di.AdBlockPrefs
import com.jamal2367.styx.preference.delegates.nullableStringPreference
import javax.inject.Inject

/**
 * Information about the contents of the hosts repository.
 */
class HostsRepositoryInfo @Inject constructor(@AdBlockPrefs preferences: SharedPreferences) {

    /**
     * The identity of the contents of the hosts repository as a [String] or `null`.
     */
    var identity: String? by preferences.nullableStringPreference(R.string.pref_key_identity)

}

