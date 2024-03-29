package com.jamal2367.styx.html.homepage

import android.app.Application
import com.jamal2367.styx.BrowserApp
import com.jamal2367.styx.R
import com.jamal2367.styx.constant.FILE
import com.jamal2367.styx.constant.UTF8
import com.jamal2367.styx.html.HtmlPageFactory
import com.jamal2367.styx.html.jsoup.*
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.search.SearchEngineProvider
import com.jamal2367.styx.utils.ThemeUtils
import com.jamal2367.styx.utils.htmlColor
import dagger.Reusable
import io.reactivex.Single
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/**
 * A factory for the home page.
 */
@Reusable
class HomePageFactory @Inject constructor(
        private val application: Application,
        private val searchEngineProvider: SearchEngineProvider,
        private val homePageReader: HomePageReader,
        private var userPreferences: UserPreferences
) : HtmlPageFactory {

    override fun buildPage(): Single<String> = Single
            .just(searchEngineProvider.provideSearchEngine())
            .map { (iconUrl, queryUrl, _) ->
                parse(homePageReader.provideHtml()
                        .replace("\${TITLE}", application.getString(R.string.home))
                        .replace("\${backgroundColor}", htmlColor(ThemeUtils.getSurfaceColor(BrowserApp.currentContext())))
                        .replace("\${searchBarColor}", htmlColor(ThemeUtils.getSearchBarColor(ThemeUtils.getSurfaceColor(BrowserApp.currentContext()))))
                        .replace("\${searchBarTextColor}", htmlColor(ThemeUtils.getColor(BrowserApp.currentContext(),R.attr.colorOnPrimary)))
                        .replace("\${search}", application.getString(R.string.search_homepage))
                ) andBuild {
                    charset { UTF8 }
                    body {
                        if (userPreferences.imageUrlString != "") {
                            tag("body") {
                                attr("style", "background: url('" + userPreferences.imageUrlString + "') no-repeat scroll; background-size: 100%; ") }

                            tag("img") {
                                attr("style", "display: none") }

                            tag("form") {
                                attr("style", "opacity: 0.7;") }
                        }

                        id("image_url") {
                            attr("src", iconUrl)
                        }
                        tag("script") {
                            html(
                                html()
                                    .replace("\${BASE_URL}", queryUrl)
                                    .replace("&", "\\u0026")
                            )
                        }
                    }
                }
            }
            .map { content -> Pair(createHomePage(), content) }
            .doOnSuccess { (page, content) ->
                FileWriter(page, false).use {
                    it.write(content)
                }
            }
            .map { (page, _) -> "$FILE$page" }

    /**
     * Create the home page file.
     */
    fun createHomePage() = File(application.filesDir, FILENAME)

    companion object {

        const val FILENAME = "homepage.html"

    }

}
