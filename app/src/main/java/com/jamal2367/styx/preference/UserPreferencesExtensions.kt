package com.jamal2367.styx.preference

import com.jamal2367.styx.constant.WINDOWS_DESKTOP_USER_AGENT
import com.jamal2367.styx.constant.LINUX_DESKTOP_USER_AGENT
import com.jamal2367.styx.constant.MACOS_DESKTOP_USER_AGENT
import com.jamal2367.styx.constant.ANDROID_MOBILE_USER_AGENT
import com.jamal2367.styx.constant.IOS_MOBILE_USER_AGENT
import android.app.Application
import android.webkit.WebSettings

/**
 * Return the user agent chosen by the user or the custom user agent entered by the user.
 */
fun UserPreferences.userAgent(application: Application): String =
    when (val choice = userAgentChoice) {
        // WebSettings default identifies us as WebView and as WebView Google is preventing use to login to its services.
        // Clearly we don't want that so we just modify default user agent by removing the WebView specific parts.
        // That should make us look like Chrome, which we are really.
        1 -> Regex(" Build/.+; wv").replace(WebSettings.getDefaultUserAgent(application),"")
        2 -> WINDOWS_DESKTOP_USER_AGENT
        3 -> LINUX_DESKTOP_USER_AGENT
        4 -> MACOS_DESKTOP_USER_AGENT
        5 -> ANDROID_MOBILE_USER_AGENT
        6 -> IOS_MOBILE_USER_AGENT
        7 -> System.getProperty("http.agent") ?: " "
        8 -> WebSettings.getDefaultUserAgent(application)
        9 -> userAgentString.takeIf(String::isNotEmpty) ?: " "
        else -> throw UnsupportedOperationException("Unknown userAgentChoice: $choice")
    }
