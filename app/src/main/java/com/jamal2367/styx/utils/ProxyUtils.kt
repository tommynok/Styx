package com.jamal2367.styx.utils

import android.app.Dialog
import android.content.DialogInterface
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jamal2367.styx.BrowserApp
import com.jamal2367.styx.R
import com.jamal2367.styx.browser.ProxyChoice
import com.jamal2367.styx.dialog.BrowserDialog.setDialogSize
import com.jamal2367.styx.extensions.snackbar
import com.jamal2367.styx.extensions.withSingleChoiceItems
import com.jamal2367.styx.preference.DeveloperPreferences
import com.jamal2367.styx.preference.UserPreferences
import info.guardianproject.netcipher.proxy.OrbotHelper
import info.guardianproject.netcipher.webkit.WebkitProxy
import net.i2p.android.ui.I2PAndroidHelper
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProxyUtils @Inject constructor(
        private val userPreferences: UserPreferences,
        private val developerPreferences: DeveloperPreferences,
        private val i2PAndroidHelper: I2PAndroidHelper
) {
    /*
     * If Orbot/Tor or I2P is installed, prompt the user if they want to enable
     * proxying for this session
     */
    fun checkForProxy(activity: AppCompatActivity) {
        val currentProxyChoice = userPreferences.proxyChoice
        val orbotInstalled = OrbotHelper.isOrbotInstalled(activity)
        val orbotChecked = developerPreferences.checkedForTor
        val orbot = orbotInstalled && !orbotChecked
        val i2pInstalled = i2PAndroidHelper.isI2PAndroidInstalled
        val i2pChecked = developerPreferences.checkedForI2P
        val i2p = i2pInstalled && !i2pChecked

        // Do only once per install
        if (currentProxyChoice !== ProxyChoice.NONE && (orbot || i2p)) {
            if (orbot) {
                developerPreferences.checkedForTor = true
            }
            if (i2p) {
                developerPreferences.checkedForI2P = true
            }
            val builder = MaterialAlertDialogBuilder(activity)
            if (orbotInstalled && i2pInstalled) {
                val proxyChoices = activity.resources.getStringArray(R.array.proxy_choices_array)
                val values = Arrays.asList(ProxyChoice.NONE, ProxyChoice.ORBOT, ProxyChoice.I2P)
                val list: MutableList<Pair<ProxyChoice, String>> = ArrayList()
                for (proxyChoice in values) {
                    list.add(Pair(proxyChoice, proxyChoices[proxyChoice.value]))
                }
                builder.setTitle(activity.resources.getString(R.string.http_proxy))
                builder.withSingleChoiceItems(list, userPreferences.proxyChoice, { newProxyChoice: ProxyChoice? ->
                    userPreferences.proxyChoice = newProxyChoice!!
                    Unit
                })
                builder.setPositiveButton(activity.resources.getString(R.string.action_ok)
                ) { _: DialogInterface?, _: Int ->
                    if (userPreferences.proxyChoice !== ProxyChoice.NONE) {
                        initializeProxy(activity)
                    }
                }
            } else {
                val dialogClickListener = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            userPreferences.proxyChoice = if (orbotInstalled) ProxyChoice.ORBOT else ProxyChoice.I2P
                            initializeProxy(activity)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> userPreferences.proxyChoice = ProxyChoice.NONE
                    }
                }
                builder.setMessage(if (orbotInstalled) R.string.use_tor_prompt else R.string.use_i2p_prompt)
                        .setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener)
            }
            val dialog: Dialog = builder.show()
            setDialogSize(activity, dialog)
        }
    }

    /*
     * Initialize WebKit Proxying
     */
    private fun initializeProxy(activity: AppCompatActivity) {
        val host: String
        val port: Int
        when (userPreferences.proxyChoice) {
            ProxyChoice.NONE ->
                // We shouldn't be here
                return
            ProxyChoice.ORBOT -> {
                if (!OrbotHelper.isOrbotRunning(activity)) {
                    OrbotHelper.requestStartTor(activity)
                }
                host = "localhost"
                port = 8118
            }
            ProxyChoice.I2P -> {
                sI2PProxyInitialized = true
                if (sI2PHelperBound && !i2PAndroidHelper.isI2PAndroidRunning) {
                    i2PAndroidHelper.requestI2PAndroidStart(activity)
                }
                host = "localhost"
                port = 4444
            }
            ProxyChoice.MANUAL -> {
                host = userPreferences.proxyHost
                port = userPreferences.proxyPort
            }
            else -> {
                host = userPreferences.proxyHost
                port = userPreferences.proxyPort
            }
        }
        try {
            WebkitProxy.setProxy(BrowserApp::class.java.name, activity.applicationContext, null, host, port)
        } catch (e: Exception) {
            Log.d(TAG, "error enabling web proxying", e)
        }
    }

    fun isProxyReady(activity: AppCompatActivity): Boolean {
        if (userPreferences.proxyChoice === ProxyChoice.I2P) {
            if (!i2PAndroidHelper.isI2PAndroidRunning) {
                activity.snackbar(R.string.i2p_not_running, if (userPreferences.toolbarsBottom) Gravity.TOP else Gravity.BOTTOM)
                return false
            } else if (!i2PAndroidHelper.areTunnelsActive()) {
                activity.snackbar(R.string.i2p_tunnels_not_ready, if (userPreferences.toolbarsBottom) Gravity.TOP else Gravity.BOTTOM)
                return false
            }
        }
        return true
    }

    fun updateProxySettings(activity: AppCompatActivity) {
        if (userPreferences.proxyChoice !== ProxyChoice.NONE) {
            initializeProxy(activity)
        } else {
            try {
                WebkitProxy.resetProxy(BrowserApp::class.java.name, activity.applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to reset proxy", e)
            }
            sI2PProxyInitialized = false
        }
    }

    fun onStop() {
        i2PAndroidHelper.unbind()
        sI2PHelperBound = false
    }

    fun onStart(activity: AppCompatActivity?) {
        if (userPreferences.proxyChoice === ProxyChoice.I2P) {
            // Try to bind to I2P Android
            i2PAndroidHelper.bind {
                sI2PHelperBound = true
                if (sI2PProxyInitialized && !i2PAndroidHelper.isI2PAndroidRunning) i2PAndroidHelper.requestI2PAndroidStart(activity)
            }
        }
    }

    companion object {
        private const val TAG = "ProxyUtils"

        // Helper
        private var sI2PHelperBound = false
        private var sI2PProxyInitialized = false
        fun sanitizeProxyChoice(choice: ProxyChoice?, activity: AppCompatActivity): ProxyChoice? {
            var choice = choice
            when (choice) {
                ProxyChoice.ORBOT -> if (!OrbotHelper.isOrbotInstalled(activity)) {
                    choice = ProxyChoice.NONE
                    activity.snackbar(R.string.install_orbot, Gravity.BOTTOM)
                }
                ProxyChoice.I2P -> {
                    val ih = I2PAndroidHelper(activity.application)
                    if (!ih.isI2PAndroidInstalled) {
                        choice = ProxyChoice.NONE
                        ih.promptToInstall(activity)
                    }
                }
                ProxyChoice.MANUAL -> {
                }
            }
            return choice
        }
    }
}