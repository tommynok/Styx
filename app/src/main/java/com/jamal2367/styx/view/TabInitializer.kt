package com.jamal2367.styx.view

import com.jamal2367.styx.R
import com.jamal2367.styx.browser.TabModel
import com.jamal2367.styx.constant.SCHEME_BOOKMARKS
import com.jamal2367.styx.constant.SCHEME_HOMEPAGE
import com.jamal2367.styx.di.DiskScheduler
import com.jamal2367.styx.di.MainScheduler
import com.jamal2367.styx.extensions.resizeAndShow
import com.jamal2367.styx.html.HtmlPageFactory
import com.jamal2367.styx.html.bookmark.BookmarkPageFactory
import com.jamal2367.styx.html.download.DownloadPageFactory
import com.jamal2367.styx.html.history.HistoryPageFactory
import com.jamal2367.styx.html.homepage.HomePageFactory
import com.jamal2367.styx.preference.UserPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import dagger.Reusable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * An initializer that is run on a [StyxView] after it is created.
 */
interface TabInitializer {

    /**
     * Initialize the [WebView] instance held by the [StyxView]. If a url is loaded, the
     * provided [headers] should be used to load the url.
     */
    fun initialize(webView: WebView, headers: Map<String, String>)

}

/**
 * An initializer that loads a [url].
 */
class UrlInitializer(private val url: String) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        webView.loadUrl(url, headers)
    }

}

/**
 * An initializer that displays the page set as the user's homepage preference.
 */
@Reusable
class HomePageInitializer @Inject constructor(
    private val userPreferences: UserPreferences,
    private val startPageInitializer: StartPageInitializer,
    private val bookmarkPageInitializer: BookmarkPageInitializer
) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        val homepage = userPreferences.homepage

        when (homepage) {
            SCHEME_HOMEPAGE -> startPageInitializer
            SCHEME_BOOKMARKS -> bookmarkPageInitializer
            else -> UrlInitializer(homepage)
        }.initialize(webView, headers)
    }

}

/**
 * An initializer that displays the start page.
 */
@Reusable
class StartPageInitializer @Inject constructor(
    homePageFactory: HomePageFactory,
    @DiskScheduler diskScheduler: Scheduler,
    @MainScheduler foregroundScheduler: Scheduler
) : HtmlPageFactoryInitializer(homePageFactory, diskScheduler, foregroundScheduler)

/**
 * An initializer that displays the bookmark page.
 */
@Reusable
class BookmarkPageInitializer @Inject constructor(
    bookmarkPageFactory: BookmarkPageFactory,
    @DiskScheduler diskScheduler: Scheduler,
    @MainScheduler foregroundScheduler: Scheduler
) : HtmlPageFactoryInitializer(bookmarkPageFactory, diskScheduler, foregroundScheduler)

/**
 * An initializer that displays the download page.
 */
@Reusable
class DownloadPageInitializer @Inject constructor(
    downloadPageFactory: DownloadPageFactory,
    @DiskScheduler diskScheduler: Scheduler,
    @MainScheduler foregroundScheduler: Scheduler
) : HtmlPageFactoryInitializer(downloadPageFactory, diskScheduler, foregroundScheduler)

/**
 * An initializer that displays the history page.
 */
@Reusable
class HistoryPageInitializer @Inject constructor(
    historyPageFactory: HistoryPageFactory,
    @DiskScheduler diskScheduler: Scheduler,
    @MainScheduler foregroundScheduler: Scheduler
) : HtmlPageFactoryInitializer(historyPageFactory, diskScheduler, foregroundScheduler)

/**
 * An initializer that loads the url built by the [HtmlPageFactory].
 */
abstract class HtmlPageFactoryInitializer(
    private val htmlPageFactory: HtmlPageFactory,
    @DiskScheduler private val diskScheduler: Scheduler,
    @MainScheduler private val foregroundScheduler: Scheduler
) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        htmlPageFactory
            .buildPage()
            .subscribeOn(diskScheduler)
            .observeOn(foregroundScheduler)
            .subscribeBy(onSuccess = { webView.loadUrl(it, headers) })
    }

}

/**
 * An initializer that sets the [WebView] as the target of the [resultMessage]. Used for
 * `target="_blank"` links.
 */
class ResultMessageInitializer(private val resultMessage: Message) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        resultMessage.apply {
            (obj as WebView.WebViewTransport).webView = webView
        }.sendToTarget()
    }

}

/**
 * An initializer that restores the [WebView] state using the [bundle].
 */
open class BundleInitializer(private val bundle: Bundle?) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        bundle?.let { webView.restoreState(it) }
    }

}

/**
 * An initializer that can be delayed until the view is attached. [initialTitle] is the title that
 * should be initially set on the tab.
 */
class FreezableBundleInitializer(
        val tabModel: TabModel
) : BundleInitializer(tabModel.webView)

/**
 * An initializer that does not load anything into the [WebView].
 */
class NoOpInitializer : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) = Unit

}

/**
 * Ask the user's permission before loading the [url] and load the homepage instead if they deny
 * permission. Useful for scenarios where another app may attempt to open a malicious URL in the
 * browser via an intent.
 */
class PermissionInitializer(
    private val url: String,
    private val activity: Activity,
    private val homePageInitializer: HomePageInitializer
) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.title_warning)
            setMessage(R.string.message_blocked_local)
            setCancelable(false)
            setOnDismissListener {
                homePageInitializer.initialize(webView, headers)
            }
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(R.string.action_open) { _, _ ->
                UrlInitializer(url).initialize(webView, headers)
            }
        }.resizeAndShow()
    }

}
