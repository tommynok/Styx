package com.jamal2367.styx.settings.fragment

import android.app.Activity
import com.jamal2367.styx.Capabilities
import com.jamal2367.styx.R
import com.jamal2367.styx.browser.JavaScriptChoice
import com.jamal2367.styx.browser.ProxyChoice
import com.jamal2367.styx.constant.SCHEME_BLANK
import com.jamal2367.styx.constant.SCHEME_BOOKMARKS
import com.jamal2367.styx.constant.SCHEME_HOMEPAGE
import com.jamal2367.styx.constant.TEXT_ENCODINGS
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.dialog.BrowserDialog
import com.jamal2367.styx.extensions.resizeAndShow
import com.jamal2367.styx.extensions.withSingleChoiceItems
import com.jamal2367.styx.isSupported
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.preference.userAgent
import com.jamal2367.styx.search.SearchEngineProvider
import com.jamal2367.styx.search.Suggestions
import com.jamal2367.styx.search.engine.BaseSearchEngine
import com.jamal2367.styx.search.engine.CustomSearch
import com.jamal2367.styx.utils.FileUtils
import com.jamal2367.styx.utils.ProxyUtils
import com.jamal2367.styx.utils.ThemeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.jamal2367.styx.browser.SuggestionNumChoice
import javax.inject.Inject

/**
 * The general settings of the app.
 */
class GeneralSettingsFragment : AbstractSettingsFragment() {

    @Inject lateinit var searchEngineProvider: SearchEngineProvider
    @Inject lateinit var userPreferences: UserPreferences

    private lateinit var proxyChoices: Array<String>

    override fun providePreferencesXmlResource() = R.xml.preference_general

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        injector.inject(this)

        proxyChoices = resources.getStringArray(R.array.proxy_choices_array)

        clickableDynamicPreference(
            preference = SETTINGS_PROXY,
            summary = userPreferences.proxyChoice.toSummary(),
            onClick = ::showProxyPicker
        )

        clickableDynamicPreference(
            preference = SETTINGS_USER_AGENT,
            summary = userAgentSummary(),
            onClick = ::showUserAgentChooserDialog
        )

        clickableDynamicPreference(
            preference = SETTINGS_DOWNLOAD,
            summary = userPreferences.downloadDirectory,
            onClick = ::showDownloadLocationDialog
        )

        clickableDynamicPreference(
            preference = SETTINGS_HOME,
            summary = homePageUrlToDisplayTitle(userPreferences.homepage),
            onClick = ::showHomePageDialog
        )

        switchPreference(
            preference = SETTINGS_SHOW_SSL,
            isChecked = userPreferences.ssl,
            onCheckChange = { userPreferences.ssl = it }
        )

        clickableDynamicPreference(
            preference = SETTINGS_SEARCH_ENGINE,
            summary = getSearchEngineSummary(searchEngineProvider.provideSearchEngine()),
            onClick = ::showSearchProviderDialog
        )

        clickableDynamicPreference(
            preference = SETTINGS_SUGGESTIONS,
            summary = searchSuggestionChoiceToTitle(Suggestions.from(userPreferences.searchSuggestionChoice)),
            onClick = ::showSearchSuggestionsDialog
        )

        val stringArray = resources.getStringArray(R.array.suggestion_name_array)

        clickableDynamicPreference(
                preference = SETTINGS_SUGGESTIONS_NUM,
                summary = stringArray[userPreferences.suggestionChoice.value],
                onClick = ::showSuggestionNumPicker
        )

        clickableDynamicPreference(
                preference = getString(R.string.pref_key_default_text_encoding),
                summary = userPreferences.textEncoding,
                onClick = this::showTextEncodingDialogPicker
        )

        val incognitoCheckboxPreference = switchPreference(
                preference = getString(R.string.pref_key_cookies_incognito),
                isEnabled = !Capabilities.FULL_INCOGNITO.isSupported,
                isVisible = !Capabilities.FULL_INCOGNITO.isSupported,
                isChecked = if (Capabilities.FULL_INCOGNITO.isSupported) {
                    userPreferences.cookiesEnabled
                } else {
                    userPreferences.incognitoCookiesEnabled
                },
                summary = if (Capabilities.FULL_INCOGNITO.isSupported) {
                    getString(R.string.incognito_cookies_pie)
                } else {
                    null
                },
                onCheckChange = { userPreferences.incognitoCookiesEnabled = it }
        )

        switchPreference(
                preference = getString(R.string.pref_key_cookies),
                isChecked = userPreferences.cookiesEnabled,
                onCheckChange = {
                    userPreferences.cookiesEnabled = it
                    if (Capabilities.FULL_INCOGNITO.isSupported) {
                        incognitoCheckboxPreference.isChecked = it
                    }
                }
        )

        clickableDynamicPreference(
                preference = SETTINGS_BLOCK_JAVASCRIPT,
                summary = userPreferences.javaScriptChoice.toSummary(),
                onClick = ::showJavaScriptPicker
        )

        switchPreference(
                preference = SETTINGS_FORCE_ZOOM,
                isChecked = userPreferences.forceZoom,
                onCheckChange = { userPreferences.forceZoom = it }
        )

        switchPreference(
                preference = SETTINGS_LAST_TAB,
                isChecked = userPreferences.closeOnLastTab,
                onCheckChange = { userPreferences.closeOnLastTab = it }
        )
    }

    /**
     * Shows the dialog which allows the user to choose the browser's text encoding.
     *
     * @param summaryUpdater the command which allows the summary to be updated.
     */
    private fun showTextEncodingDialogPicker(summaryUpdater: SummaryUpdater) {
        activity?.let {
            MaterialAlertDialogBuilder(it).apply {
                setTitle(resources.getString(R.string.text_encoding))

                val currentChoice = TEXT_ENCODINGS.indexOf(userPreferences.textEncoding)

                setSingleChoiceItems(TEXT_ENCODINGS, currentChoice) { _, which ->
                    userPreferences.textEncoding = TEXT_ENCODINGS[which]
                    summaryUpdater.updateSummary(TEXT_ENCODINGS[which])
                }
                setPositiveButton(resources.getString(R.string.action_ok), null)
            }.resizeAndShow()
        }
    }


    private fun showSuggestionNumPicker(summaryUpdater: SummaryUpdater) {
        BrowserDialog.showCustomDialog(activity as AppCompatActivity) {
            setTitle(R.string.suggest)
            val stringArray = resources.getStringArray(R.array.suggestion_name_array)
            val values = SuggestionNumChoice.values().map {
                Pair(it, when (it) {
                    SuggestionNumChoice.THREE -> stringArray[0]
                    SuggestionNumChoice.FOUR -> stringArray[1]
                    SuggestionNumChoice.FIVE -> stringArray[2]
                    SuggestionNumChoice.SIX -> stringArray[3]
                    SuggestionNumChoice.SEVEN -> stringArray[4]
                    SuggestionNumChoice.EIGHT -> stringArray[5]
                    else -> stringArray[2]
                })
            }
            withSingleChoiceItems(values, userPreferences.suggestionChoice) {
                updateSearchNum(it, activity as AppCompatActivity, summaryUpdater)
            }
            setPositiveButton(R.string.action_ok, null)
        }
    }

    private fun updateSearchNum(choice: SuggestionNumChoice, activity: Activity, summaryUpdater: SummaryUpdater) {
        val stringArray = resources.getStringArray(R.array.suggestion_name_array)

        userPreferences.suggestionChoice = choice
        summaryUpdater.updateSummary(stringArray[choice.value])
    }


    private fun ProxyChoice.toSummary(): String {
        val stringArray = resources.getStringArray(R.array.proxy_choices_array)
        return when (this) {
            ProxyChoice.NONE -> stringArray[0]
            ProxyChoice.ORBOT -> stringArray[1]
            ProxyChoice.I2P -> stringArray[2]
            ProxyChoice.MANUAL -> "${userPreferences.proxyHost}:${userPreferences.proxyPort}"
        }
    }

    private fun showProxyPicker(summaryUpdater: SummaryUpdater) {
        BrowserDialog.showCustomDialog(activity as AppCompatActivity) {
            setTitle(R.string.http_proxy)
            val stringArray = resources.getStringArray(R.array.proxy_choices_array)
            val values = ProxyChoice.values().map {
                Pair(it, when (it) {
                    ProxyChoice.NONE -> stringArray[0]
                    ProxyChoice.ORBOT -> stringArray[1]
                    ProxyChoice.I2P -> stringArray[2]
                    ProxyChoice.MANUAL -> stringArray[3]
                })
            }
            withSingleChoiceItems(values, userPreferences.proxyChoice) {
                updateProxyChoice(it, activity as AppCompatActivity, summaryUpdater)
            }
            setPositiveButton(R.string.action_ok, null)
        }
    }

    private fun updateProxyChoice(choice: ProxyChoice, activity: AppCompatActivity, summaryUpdater: SummaryUpdater) {
        val sanitizedChoice = ProxyUtils.sanitizeProxyChoice(choice, activity)
        if (sanitizedChoice == ProxyChoice.MANUAL) {
            showManualProxyPicker(activity, summaryUpdater)
        }

        userPreferences.proxyChoice = sanitizedChoice
        summaryUpdater.updateSummary(sanitizedChoice.toSummary())
    }

    private fun showManualProxyPicker(activity: AppCompatActivity, summaryUpdater: SummaryUpdater) {
        val v = activity.layoutInflater.inflate(R.layout.dialog_manual_proxy, null)
        val eProxyHost = v.findViewById<TextView>(R.id.proxyHost)
        val eProxyPort = v.findViewById<TextView>(R.id.proxyPort)

        // Limit the number of characters since the port needs to be of type int
        // Use input filters to limit the EditText length and determine the max
        // length by using length of integer MAX_VALUE
        val maxCharacters = Integer.MAX_VALUE.toString().length
        eProxyPort.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxCharacters - 1))

        eProxyHost.text = userPreferences.proxyHost
        eProxyPort.text = userPreferences.proxyPort.toString()

        BrowserDialog.showCustomDialog(activity) {
            setTitle(R.string.manual_proxy)
            setView(v)
            setPositiveButton(R.string.action_ok) { _, _ ->
                val proxyHost = eProxyHost.text.toString()
                val proxyPort = try {
                    // Try/Catch in case the user types an empty string or a number
                    // larger than max integer
                    Integer.parseInt(eProxyPort.text.toString())
                } catch (ignored: NumberFormatException) {
                    userPreferences.proxyPort
                }

                userPreferences.proxyHost = proxyHost
                userPreferences.proxyPort = proxyPort
                summaryUpdater.updateSummary("$proxyHost:$proxyPort")
            }
        }
    }


    private fun userAgentSummary() =
            choiceToUserAgent(userPreferences.userAgentChoice) + activity?.application?.let { ":\n" + userPreferences.userAgent(it) }



    private fun choiceToUserAgent(index: Int) = when (index) {
        1 -> resources.getString(R.string.agent_default)
        2 -> resources.getString(R.string.agent_windows_desktop)
        3 -> resources.getString(R.string.agent_linux_desktop)
        4 -> resources.getString(R.string.agent_macos_desktop)
        5 -> resources.getString(R.string.agent_android_mobile)
        6 -> resources.getString(R.string.agent_ios_mobile)
        7 -> resources.getString(R.string.agent_system)
        8 -> resources.getString(R.string.agent_web_view)
        9 -> resources.getString(R.string.agent_custom)
        else -> resources.getString(R.string.agent_default)
    }

    private fun showUserAgentChooserDialog(summaryUpdater: SummaryUpdater) {
        activity?.let {
            BrowserDialog.showCustomDialog(it as AppCompatActivity) {
                setTitle(resources.getString(R.string.title_user_agent))
                setSingleChoiceItems(R.array.user_agent, userPreferences.userAgentChoice - 1) { _, which ->
                    userPreferences.userAgentChoice = which + 1
                    when (which) {
                        in 0..7 -> Unit
                        8 -> {
                            showCustomUserAgentPicker()
                        }
                    }

                    summaryUpdater.updateSummary(userAgentSummary())
                }
                setPositiveButton(resources.getString(R.string.action_ok), null)
            }
        }
    }

    private fun showCustomUserAgentPicker() {
        activity?.let {
            BrowserDialog.showEditText(it as AppCompatActivity,
                R.string.title_user_agent,
                R.string.title_user_agent,
                userPreferences.userAgentString,
                R.string.action_ok) { s ->
                userPreferences.userAgentString = s
            }
        }
    }

    private fun showDownloadLocationDialog(summaryUpdater: SummaryUpdater) {
        activity?.let {
            BrowserDialog.showCustomDialog(it as AppCompatActivity) {
            setTitle(resources.getString(R.string.title_download_location))
            val n: Int = if (userPreferences.downloadDirectory.contains(Environment.DIRECTORY_DOWNLOADS)) {
                0
            } else {
                1
            }

            setSingleChoiceItems(R.array.download_folder, n) { _, which ->
                when (which) {
                    0 -> {
                        userPreferences.downloadDirectory = FileUtils.DEFAULT_DOWNLOAD_PATH
                        summaryUpdater.updateSummary(FileUtils.DEFAULT_DOWNLOAD_PATH)
                    }
                    1 -> {
                        showCustomDownloadLocationPicker(summaryUpdater)
                    }
                }
            }
            setPositiveButton(resources.getString(R.string.action_ok), null)
        }
        }
    }


    private fun showCustomDownloadLocationPicker(summaryUpdater: SummaryUpdater) {
        activity?.let { activity ->
            val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_text, null)
            val getDownload = dialogView.findViewById<EditText>(R.id.dialog_edit_text)

            val errorColor = ContextCompat.getColor(activity, R.color.error_red)

            val regularColor = ThemeUtils.getTextColor(activity)
            getDownload.setTextColor(regularColor)
            getDownload.addTextChangedListener(DownloadLocationTextWatcher(getDownload, errorColor, regularColor))
            getDownload.setText(userPreferences.downloadDirectory)

            BrowserDialog.showCustomDialog(activity as AppCompatActivity) {
                setTitle(R.string.title_download_location)
                setView(dialogView)
                setPositiveButton(R.string.action_ok) { _, _ ->
                    var text = getDownload.text.toString()
                    text = FileUtils.addNecessarySlashes(text)
                    userPreferences.downloadDirectory = text
                    summaryUpdater.updateSummary(text)
                }
            }
        }
    }

    private class DownloadLocationTextWatcher(
        private val getDownload: EditText,
        private val errorColor: Int,
        private val regularColor: Int
    ) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            if (!FileUtils.isWriteAccessAvailable(s.toString())) {
                this.getDownload.setTextColor(this.errorColor)
            } else {
                this.getDownload.setTextColor(this.regularColor)
            }
        }
    }

    private fun homePageUrlToDisplayTitle(url: String): String = when (url) {
        SCHEME_HOMEPAGE -> resources.getString(R.string.action_homepage)
        SCHEME_BLANK -> resources.getString(R.string.action_blank)
        SCHEME_BOOKMARKS -> resources.getString(R.string.action_bookmarks)
        else -> url
    }

    private fun showHomePageDialog(summaryUpdater: SummaryUpdater) {
        activity?.let {
            BrowserDialog.showCustomDialog(it as AppCompatActivity) {
            setTitle(R.string.home)
            val n = when (userPreferences.homepage) {
                SCHEME_HOMEPAGE -> 0
                SCHEME_BLANK -> 1
                SCHEME_BOOKMARKS -> 2
                else -> 3
            }

            setSingleChoiceItems(R.array.homepage, n) { _, which ->
                when (which) {
                    0 -> {
                        userPreferences.homepage = SCHEME_HOMEPAGE
                        summaryUpdater.updateSummary(resources.getString(R.string.action_homepage))
                    }
                    1 -> {
                        userPreferences.homepage = SCHEME_BLANK
                        summaryUpdater.updateSummary(resources.getString(R.string.action_blank))
                    }
                    2 -> {
                        userPreferences.homepage = SCHEME_BOOKMARKS
                        summaryUpdater.updateSummary(resources.getString(R.string.action_bookmarks))
                    }
                    3 -> {
                        showCustomHomePagePicker(summaryUpdater)
                    }
                }
            }
            setPositiveButton(resources.getString(R.string.action_ok), null)
        }
        }
    }

    private fun showCustomHomePagePicker(summaryUpdater: SummaryUpdater) {
        val currentHomepage: String = if (!URLUtil.isAboutUrl(userPreferences.homepage)) {
            userPreferences.homepage
        } else {
            "https://www.google.com"
        }

        activity?.let {
            BrowserDialog.showEditText(it as AppCompatActivity,
                R.string.title_custom_homepage,
                R.string.title_custom_homepage,
                currentHomepage,
                R.string.action_ok) { url ->
                userPreferences.homepage = url
                summaryUpdater.updateSummary(url)
            }
        }
    }

    private fun getSearchEngineSummary(baseSearchEngine: BaseSearchEngine): String {
        return if (baseSearchEngine is CustomSearch) {
            baseSearchEngine.queryUrl
        } else {
            getString(baseSearchEngine.titleRes)
        }
    }

    private fun convertSearchEngineToString(searchEngines: List<BaseSearchEngine>): Array<CharSequence> =
        searchEngines.map { getString(it.titleRes) }.toTypedArray()

    private fun showSearchProviderDialog(summaryUpdater: SummaryUpdater) {
        activity?.let {
            BrowserDialog.showCustomDialog(it as AppCompatActivity) {
            setTitle(resources.getString(R.string.title_search_engine))

            val searchEngineList = searchEngineProvider.provideAllSearchEngines()

            val chars = convertSearchEngineToString(searchEngineList)

            val n = userPreferences.searchChoice

            setSingleChoiceItems(chars, n) { _, which ->
                val searchEngine = searchEngineList[which]

                // Store the search engine preference
                val preferencesIndex = searchEngineProvider.mapSearchEngineToPreferenceIndex(searchEngine)
                userPreferences.searchChoice = preferencesIndex

                if (searchEngine is CustomSearch) {
                    // Show the URL picker
                    showCustomSearchDialog(searchEngine, summaryUpdater)
                } else {
                    // Set the new search engine summary
                    summaryUpdater.updateSummary(getSearchEngineSummary(searchEngine))
                }
            }
            setPositiveButton(R.string.action_ok, null)
        }
        }
    }

    private fun showCustomSearchDialog(customSearch: CustomSearch, summaryUpdater: SummaryUpdater) {
        activity?.let {
            BrowserDialog.showEditText(
                    it as AppCompatActivity,
                R.string.search_engine_custom,
                R.string.search_engine_custom,
                userPreferences.searchUrl,
                R.string.action_ok
            ) { searchUrl ->
                userPreferences.searchUrl = searchUrl
                summaryUpdater.updateSummary(getSearchEngineSummary(customSearch))
            }

        }
    }

    private fun JavaScriptChoice.toSummary(): String {
        val stringArray = resources.getStringArray(R.array.block_javascript)
        return when (this) {
            JavaScriptChoice.NONE -> stringArray[0]
            JavaScriptChoice.WHITELIST -> userPreferences.siteBlockNames
            JavaScriptChoice.BLACKLIST -> userPreferences.siteBlockNames
        }
    }

    private fun showJavaScriptPicker(summaryUpdater: SummaryUpdater) {
        BrowserDialog.showCustomDialog(activity as AppCompatActivity) {
            setTitle(R.string.block_javascript)
            val stringArray = resources.getStringArray(R.array.block_javascript)
            val values = JavaScriptChoice.values().map {
                Pair(it, when (it) {
                    JavaScriptChoice.NONE -> stringArray[0]
                    JavaScriptChoice.WHITELIST -> stringArray[1]
                    JavaScriptChoice.BLACKLIST -> stringArray[2]
                })
            }
            withSingleChoiceItems(values, userPreferences.javaScriptChoice) {
                updateJavaScriptChoice(it, activity as Activity, summaryUpdater)
            }
            setPositiveButton(R.string.action_ok, null)
        }
    }

    private fun updateJavaScriptChoice(choice: JavaScriptChoice, activity: Activity, summaryUpdater: SummaryUpdater) {
        if (choice == JavaScriptChoice.WHITELIST || choice == JavaScriptChoice.BLACKLIST) {
            showManualJavaScriptPicker(activity, summaryUpdater, choice)
        }

        userPreferences.javaScriptChoice = choice
        summaryUpdater.updateSummary(choice.toSummary())
    }

    private fun showManualJavaScriptPicker(activity: Activity, summaryUpdater: SummaryUpdater, choice: JavaScriptChoice) {
        val v = activity.layoutInflater.inflate(R.layout.site_block, null)
        val blockedSites = v.findViewById<TextView>(R.id.siteBlock)
        // Limit the number of characters since the port needs to be of type int
        // Use input filters to limit the EditText length and determine the max
        // length by using length of integer MAX_VALUE
        val maxCharacters = Integer.MAX_VALUE.toString().length

        blockedSites.text = userPreferences.javaScriptBlocked

        BrowserDialog.showCustomDialog(activity as AppCompatActivity) {
            setTitle(R.string.block_sites_title)
            setView(v)
            setPositiveButton(R.string.action_ok) { _, _ ->
                val proxyHost = blockedSites.text.toString()
                userPreferences.javaScriptBlocked = proxyHost
                if(choice.toString() == "BLACKLIST"){
                    summaryUpdater.updateSummary(getText(R.string.listed_javascript).toString())
                }
                else{
                    summaryUpdater.updateSummary(getText(R.string.unlisted_javascript).toString())
                }

            }
        }
    }

    private fun searchSuggestionChoiceToTitle(choice: Suggestions): String =
        when (choice) {
            Suggestions.NONE -> getString(R.string.search_suggestions_off)
            Suggestions.GOOGLE -> getString(R.string.powered_by_google)
            Suggestions.DUCK -> getString(R.string.powered_by_duck)
            Suggestions.NAVER -> getString(R.string.powered_by_naver)
            Suggestions.BAIDU -> getString(R.string.powered_by_baidu)
        }

    private fun showSearchSuggestionsDialog(summaryUpdater: SummaryUpdater) {
        activity?.let {
            BrowserDialog.showCustomDialog(it as AppCompatActivity) {
            setTitle(resources.getString(R.string.search_suggestions))

            val currentChoice = when (Suggestions.from(userPreferences.searchSuggestionChoice)) {
                Suggestions.GOOGLE -> 0
                Suggestions.DUCK -> 1
                Suggestions.NAVER -> 2
                Suggestions.BAIDU -> 3
                Suggestions.NONE -> 4
            }

            setSingleChoiceItems(R.array.suggestions, currentChoice) { _, which ->
                val suggestionsProvider = when (which) {
                    0 -> Suggestions.GOOGLE
                    1 -> Suggestions.DUCK
                    2 -> Suggestions.NAVER
                    3 -> Suggestions.BAIDU
                    4 -> Suggestions.NONE
                    else -> Suggestions.GOOGLE
                }
                userPreferences.searchSuggestionChoice = suggestionsProvider.index
                summaryUpdater.updateSummary(searchSuggestionChoiceToTitle(suggestionsProvider))
            }
            setPositiveButton(resources.getString(R.string.action_ok), null)
        }
        }
    }

    companion object {
        private const val SETTINGS_PROXY = "proxy"
        private const val SETTINGS_SUGGESTIONS_NUM = "suggestions_number"
        private const val SETTINGS_USER_AGENT = "agent"
        private const val SETTINGS_DOWNLOAD = "download"
        private const val SETTINGS_HOME = "home"
        private const val SETTINGS_SEARCH_ENGINE = "search"
        private const val SETTINGS_SUGGESTIONS = "suggestions_choice"
        private const val SETTINGS_BLOCK_JAVASCRIPT = "block_javascript"
        private const val SETTINGS_FORCE_ZOOM = "force_zoom"
        private const val SETTINGS_SHOW_SSL = "show_ssl"
        private const val SETTINGS_LAST_TAB = "last_tab"
    }
}
