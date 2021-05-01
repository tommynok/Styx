package com.jamal2367.styx.search

import android.app.Application
import com.jamal2367.styx.di.SuggestionsClient
import com.jamal2367.styx.log.Logger
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.search.engine.*
import com.jamal2367.styx.search.suggestions.*
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
            1 -> GoogleSuggestionsModel(okHttpClient, requestFactory, application, logger, userPreferences)
            2 -> DuckSuggestionsModel(okHttpClient, requestFactory, application, logger, userPreferences)
            3 -> BaiduSuggestionsModel(okHttpClient, requestFactory, application, logger, userPreferences)
            4 -> NaverSuggestionsModel(okHttpClient, requestFactory, application, logger, userPreferences)
            else -> GoogleSuggestionsModel(okHttpClient, requestFactory, application, logger, userPreferences)
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
			9 -> EcosiaSearch()
			10 -> EkoruSearch()
            11 -> MojeekSearch()
            12 -> NaverSearch()
            13 -> QwantSearch()
            14 -> QwantLiteSearch()
            15 -> SearxSearch()
            16 -> StartPageSearch()
            17 -> StartPageMobileSearch()
            18 -> YahooSearch()
            19 -> YahooNoJSSearch()
            20 -> YandexSearch()
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
			is EcosiaSearch -> 9
			is EkoruSearch -> 10
            is MojeekSearch -> 11
            is NaverSearch -> 12
            is QwantSearch -> 13
            is QwantLiteSearch -> 14
            is SearxSearch -> 15
            is StartPageSearch -> 16
            is StartPageMobileSearch -> 17
            is YahooSearch -> 18
            is YahooNoJSSearch -> 19
            is YandexSearch -> 20
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
		EcosiaSearch(),
		EkoruSearch(),
        MojeekSearch(),
        NaverSearch(),
        QwantSearch(),
        QwantLiteSearch(),
        SearxSearch(),
        StartPageSearch(),
        StartPageMobileSearch(),
        YahooSearch(),
        YahooNoJSSearch(),
        YandexSearch()
    )

}
