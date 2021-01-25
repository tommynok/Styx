package com.jamal2367.styx

import com.jamal2367.styx.browser.activity.BrowserActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import io.reactivex.Completable

class IncognitoActivity : BrowserActivity() {

    override fun provideThemeOverride(): Int? = R.style.Theme_App_Dark

    @Suppress("DEPRECATION")
    public override fun updateCookiePreference(): Completable = Completable.fromAction {
        val cookieManager = CookieManager.getInstance()
        if (Capabilities.FULL_INCOGNITO.isSupported) {
            cookieManager.setAcceptCookie(userPreferences.cookiesEnabled)
        } else {
            cookieManager.setAcceptCookie(userPreferences.incognitoCookiesEnabled)
        }
    }

    @Suppress("RedundantOverride")
    override fun onNewIntent(intent: Intent) {
        handleNewIntent(intent)
        super.onNewIntent(intent)
    }

    @Suppress("RedundantOverride")
    override fun onPause() = super.onPause() // saveOpenTabs();

    override fun updateHistory(title: String?, url: String) = Unit // addItemToHistory(title, url)

    override fun isIncognito() = true

    override fun closeActivity() = closeDrawers(::closeBrowser)

    companion object {
        /**
         * Creates the intent with which to launch the activity. Adds the reorder to front flag.
         */
        fun createIntent(context: Context, uri: Uri? = null) = Intent(context, IncognitoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            data = uri
        }
    }
}
