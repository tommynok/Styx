package com.jamal2367.styx.settings.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import com.jamal2367.styx.R
import com.jamal2367.styx.adblock.BloomFilterAdBlocker
import com.jamal2367.styx.adblock.source.HostsSourceType
import com.jamal2367.styx.adblock.source.selectedHostsSource
import com.jamal2367.styx.adblock.source.toPreferenceIndex
import com.jamal2367.styx.di.DiskScheduler
import com.jamal2367.styx.di.MainScheduler
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.dialog.BrowserDialog
import com.jamal2367.styx.dialog.DialogItem
import com.jamal2367.styx.extensions.snackbar
import com.jamal2367.styx.preference.UserPreferences
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Settings for the ad block mechanic.
 */
class AdBlockSettingsFragment : AbstractSettingsFragment() {

    @Inject internal lateinit var userPreferences: UserPreferences
    @Inject @field:MainScheduler internal lateinit var mainScheduler: Scheduler
    @Inject @field:DiskScheduler internal lateinit var diskScheduler: Scheduler
    @Inject internal lateinit var bloomFilterAdBlocker: BloomFilterAdBlocker

    private var recentSummaryUpdater: SummaryUpdater? = null
    private val compositeDisposable = CompositeDisposable()
    private var forceRefreshHostsPreference: Preference? = null

    override fun providePreferencesXmlResource(): Int = R.xml.preference_ad_block

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        injector.inject(this)

        switchPreference(
                preference = SETTINGS_BLOCKMALWARE,
                isChecked = userPreferences.blockMalwareEnabled,
                onCheckChange = { userPreferences.blockMalwareEnabled = it }
        )

        switchPreference(
            preference = "cb_block_ads",
            isChecked = userPreferences.adBlockEnabled,
            onCheckChange = { userPreferences.adBlockEnabled = it }
        )

        clickableDynamicPreference(
            preference = "preference_hosts_source",
            summary = userPreferences.selectedHostsSource().toSummary(),
            onClick = ::showHostsSourceChooser
        )

        forceRefreshHostsPreference = clickableDynamicPreference(
            preference = "preference_hosts_refresh_force",
            isEnabled = isRefreshHostsEnabled(),
            onClick = {
                bloomFilterAdBlocker.populateAdBlockerFromDataSource(forceRefresh = true)
                (activity as AppCompatActivity).snackbar(R.string.block_ad_refresh_hosts)
            }
        )
    }

    private fun updateRefreshHostsEnabledStatus() {
        forceRefreshHostsPreference?.isEnabled = isRefreshHostsEnabled()
    }

    private fun isRefreshHostsEnabled() = userPreferences.selectedHostsSource() is HostsSourceType.Remote

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun HostsSourceType.toSummary(): String = when (this) {
        HostsSourceType.Default -> getString(R.string.block_source_default)
        is HostsSourceType.Local -> getString(R.string.block_source_local_description, file.path)
        is HostsSourceType.Remote -> getString(R.string.block_source_remote_description, httpUrl)
    }

    private fun showHostsSourceChooser(summaryUpdater: SummaryUpdater) {
        BrowserDialog.showListChoices(
                activity as AppCompatActivity,
            R.string.block_ad_source,
            DialogItem(
                title = R.string.block_source_default,
                isConditionMet = userPreferences.selectedHostsSource() == HostsSourceType.Default,
                onClick = {
                    userPreferences.hostsSource = HostsSourceType.Default.toPreferenceIndex()
                    summaryUpdater.updateSummary(userPreferences.selectedHostsSource().toSummary())
                    updateForNewHostsSource()
                }
            ),
            DialogItem(
                title = R.string.block_source_local,
                isConditionMet = userPreferences.selectedHostsSource() is HostsSourceType.Local,
                onClick = {
                    showFileChooser(summaryUpdater)
                }
            ),
            DialogItem(
                title = R.string.block_source_remote,
                isConditionMet = userPreferences.selectedHostsSource() is HostsSourceType.Remote,
                onClick = {
                    showUrlChooser(summaryUpdater)
                }
            )
        )
    }

    private fun showFileChooser(summaryUpdater: SummaryUpdater) {
        this.recentSummaryUpdater = summaryUpdater
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = TEXT_MIME_TYPE
        }

        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    private fun showUrlChooser(summaryUpdater: SummaryUpdater) {
        BrowserDialog.showEditText(
                activity as AppCompatActivity,
            title = R.string.block_source_remote,
            hint = R.string.hint_url,
            currentText = userPreferences.hostsRemoteFile,
            action = R.string.action_ok,
            textInputListener = {
                val url = it.toHttpUrlOrNull()
                    ?: return@showEditText run { (activity as AppCompatActivity).snackbar(R.string.problem_download) }
                userPreferences.hostsSource = HostsSourceType.Remote(url).toPreferenceIndex()
                userPreferences.hostsRemoteFile = it
                summaryUpdater.updateSummary(it)
                updateForNewHostsSource()
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                data?.data?.also { uri ->
                    compositeDisposable += readTextFromUri(uri)
                        .subscribeOn(diskScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onComplete = { (activity as AppCompatActivity).snackbar(R.string.action_message_canceled) },
                            onSuccess = { file ->
                                userPreferences.hostsSource = HostsSourceType.Local(file).toPreferenceIndex()
                                userPreferences.hostsLocalFile = file.path
                                recentSummaryUpdater?.updateSummary(userPreferences.selectedHostsSource().toSummary())
                                updateForNewHostsSource()
                            }
                        )
                }
            } else {
                (activity as AppCompatActivity).snackbar(R.string.action_message_canceled)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateForNewHostsSource() {
        bloomFilterAdBlocker.populateAdBlockerFromDataSource(forceRefresh = true)
        updateRefreshHostsEnabledStatus()
    }

    private fun readTextFromUri(uri: Uri): Maybe<File> = Maybe.create {
        val externalFilesDir = activity?.getExternalFilesDir("")
            ?: return@create it.onComplete()
        val inputStream = activity?.contentResolver?.openInputStream(uri)
            ?: return@create it.onComplete()

        try {
            val outputFile = File(externalFilesDir, AD_HOSTS_FILE)

            val input = inputStream.source()
            val output = outputFile.sink().buffer()
            output.writeAll(input)
            return@create it.onSuccess(outputFile)
        } catch (exception: IOException) {
            return@create it.onComplete()
        }
    }

    companion object {
        private const val FILE_REQUEST_CODE = 100
        private const val AD_HOSTS_FILE = "local_hosts.txt"
        private const val TEXT_MIME_TYPE = "text/*"
        private const val SETTINGS_BLOCKMALWARE = "block_malicious_sites"
    }
}
