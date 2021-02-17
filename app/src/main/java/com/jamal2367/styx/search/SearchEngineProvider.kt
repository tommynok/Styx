package com.jamal2367.styx.search

import com.jamal2367.styx.di.SuggestionsClient
import com.jamal2367.styx.log.Logger
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.search.engine.*
import com.jamal2367.styx.search.suggestions.*
import android.app.Application
import dagger.Reusable
import io.reactivex.Single
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * The model that provides the search engine based
 * on the user's preference.
 */
@Reusable
class SearchEngineProvider @Inject constructor(
    private val userPreferences: UserPreferences,
    @SuggestionsClient private val okHttpClient: Single<OkHttpClient>,
    private val requestFactory: RequestFactory,
    private val application: Application,
    private val logger: Logger
) {

    /**
     * Provide the [SuggestionsRepository] that maps to the user's current preference.
     */
    fun provideSearchSuggestions(): SuggestionsRepository =
        when (userPreferences.searchSuggestionChoice) {
            0 -> NoOpSuggestionsRepository()
            1 -> GoogleSuggestionsModel(okHttpClient, requestFactory, application, logger)
            2 -> DuckSuggestionsModel(okHttpClient, requestFactory, application, logger)
            3 -> BaiduSuggestionsModel(okHttpClient, requestFactory, application, logger)
            4 -> NaverSuggestionsModel(okHttpClient, requestFactory, application, logger)
            else -> GoogleSuggestionsModel(okHttpClient, requestFactory, application, logger)
        }

    /**
     * Provide the [BaseSearchEngine] that maps to the user's current preference.
     */
    fun provideSearchEngine(): BaseSearchEngine =
        when (userPreferences.searchChoice) {
            0 -> CustomSearch(userPreferences.searchUrl)
            1 -> GoogleSearch()
            2 -> AskSearch()
            3 -> BaiduSearch()
            4 -> BingSearch()
            5 -> DuckSearch()
            6 -> DuckNoJSSearch()
            7 -> DuckLiteSearch()
            8 -> DuckLiteNoJSSearch()
            9 -> MojeekSearch()
            10 -> NaverSearch()
            11 -> SearxSearch()
            12 -> StartPageSearch()
            13 -> StartPageMobileSearch()
            14 -> YahooSearch()
            15 -> YahooNoJSSearch()
            16 -> YandexSearch()
            else -> GoogleSearch()
        }

    /**
     * Return the serializable index of of the provided [BaseSearchEngine].
     */
    fun mapSearchEngineToPreferenceIndex(searchEngine: BaseSearchEngine): Int =
        when (searchEngine) {
            is CustomSearch -> 0
            is GoogleSearch -> 1
            is AskSearch -> 2
            is BaiduSearch -> 3
            is BingSearch -> 4
            is DuckSearch -> 5
            is DuckNoJSSearch -> 6
            is DuckLiteSearch -> 7
            is DuckLiteNoJSSearch -> 8
            is MojeekSearch -> 9
            is NaverSearch -> 10
            is SearxSearch -> 11
            is StartPageSearch -> 12
            is StartPageMobileSearch -> 13
            is YahooSearch -> 14
            is YahooNoJSSearch -> 15
            is YandexSearch -> 16
            else -> throw UnsupportedOperationException("Unknown search engine provided: " + searchEngine.javaClass)
        }

    /**
     * Provide a list of all supported search engines.
     */
    fun provideAllSearchEngines(): List<BaseSearchEngine> = listOf(
        CustomSearch(userPreferences.searchUrl),
        GoogleSearch(),
        AskSearch(),
        BaiduSearch(),
        BingSearch(),
        DuckSearch(),
        DuckNoJSSearch(),
        DuckLiteSearch(),
        DuckLiteNoJSSearch(),
        MojeekSearch(),
        NaverSearch(),
        SearxSearch(),
        StartPageSearch(),
        StartPageMobileSearch(),
        YahooSearch(),
        YahooNoJSSearch(),
        YandexSearch()
    )

}
