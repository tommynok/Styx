/*
 * Copyright 2014 A.C.R. Development
 */
@file:JvmName("Constants")

package com.jamal2367.styx.constant

// Hardcoded user agents
const val WINDOWS_DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36"
const val LINUX_DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:85.0) Gecko/20100101 Firefox/85.0"
const val MACOS_DESKTOP_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.2 Safari/605.1.15"
const val ANDROID_MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 11; Pixel 5 Build/RQ1A.210205.004; wv) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.152 Mobile Safari/537.36"
const val IOS_MOBILE_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1"

// URL Schemes
const val HTTP = "http://"
const val HTTPS = "https://"
const val FILE = "file://"
const val FOLDER = "folder://"

object Schemes {
    const val Styx = "styx"
    const val About = "about"
}

object Hosts {
    const val Home = "home"
    const val Start = "start"
    const val Bookmarks = "bookmarks"
    const val History = "history"
    const val Downloads = "downloads"
    const val Noop = "noop"
    const val Blank = "blank"
}

object Uris {
    const val StyxHome = "${Schemes.Styx}://${Hosts.Home}"
    const val StyxStart = "${Schemes.Styx}://${Hosts.Start}"
    const val StyxBookmarks = "${Schemes.Styx}://${Hosts.Bookmarks}"
    const val StyxDownloads = "${Schemes.Styx}://${Hosts.Downloads}"
    const val StyxHistory = "${Schemes.Styx}://${Hosts.History}"
    const val StyxNoop = "${Schemes.Styx}://${Hosts.Noop}"
    // Custom local page schemes
    const val AboutHome = "${Schemes.About}:${Hosts.Home}"
    const val AboutBlank = "${Schemes.About}:${Hosts.Blank}"
    const val AboutBookmarks = "${Schemes.About}:${Hosts.Bookmarks}"
    const val AboutHistory = "${Schemes.About}:${Hosts.History}"
}

const val UTF8 = "UTF-8"

// Default text encoding we will use
const val DEFAULT_ENCODING = UTF8

// Allowable text encodings for the WebView
@JvmField
val TEXT_ENCODINGS = arrayOf("ISO-8859-1", UTF8, "GBK", "Big5", "ISO-2022-JP", "SHIFT_JS", "EUC-JP", "EUC-KR")

const val INTENT_ORIGIN = "URL_INTENT_ORIGIN"
