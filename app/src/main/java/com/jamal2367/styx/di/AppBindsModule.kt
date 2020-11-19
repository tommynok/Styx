package com.jamal2367.styx.di

import com.jamal2367.styx.adblock.allowlist.AllowListModel
import com.jamal2367.styx.adblock.allowlist.SessionAllowListModel
import com.jamal2367.styx.adblock.source.AssetsHostsDataSource
import com.jamal2367.styx.adblock.source.HostsDataSource
import com.jamal2367.styx.adblock.source.HostsDataSourceProvider
import com.jamal2367.styx.adblock.source.PreferencesHostsDataSourceProvider
import com.jamal2367.styx.browser.cleanup.DelegatingExitCleanup
import com.jamal2367.styx.browser.cleanup.ExitCleanup
import com.jamal2367.styx.database.adblock.HostsDatabase
import com.jamal2367.styx.database.adblock.HostsRepository
import com.jamal2367.styx.database.allowlist.AdBlockAllowListDatabase
import com.jamal2367.styx.database.allowlist.AdBlockAllowListRepository
import com.jamal2367.styx.database.bookmark.BookmarkDatabase
import com.jamal2367.styx.database.bookmark.BookmarkRepository
import com.jamal2367.styx.database.downloads.DownloadsDatabase
import com.jamal2367.styx.database.downloads.DownloadsRepository
import com.jamal2367.styx.database.history.HistoryDatabase
import com.jamal2367.styx.database.history.HistoryRepository
import com.jamal2367.styx.ssl.SessionSslWarningPreferences
import com.jamal2367.styx.ssl.SslWarningPreferences
import dagger.Binds
import dagger.Module

/**
 * Dependency injection module used to bind implementations to interfaces.
 */
@Module
interface AppBindsModule {

    @Binds
    fun bindsExitCleanup(delegatingExitCleanup: DelegatingExitCleanup): ExitCleanup

    @Binds
    fun bindsBookmarkModel(bookmarkDatabase: BookmarkDatabase): BookmarkRepository

    @Binds
    fun bindsDownloadsModel(downloadsDatabase: DownloadsDatabase): DownloadsRepository

    @Binds
    fun bindsHistoryModel(historyDatabase: HistoryDatabase): HistoryRepository

    @Binds
    fun bindsAdBlockAllowListModel(adBlockAllowListDatabase: AdBlockAllowListDatabase): AdBlockAllowListRepository

    @Binds
    fun bindsAllowListModel(sessionAllowListModel: SessionAllowListModel): AllowListModel

    @Binds
    fun bindsSslWarningPreferences(sessionSslWarningPreferences: SessionSslWarningPreferences): SslWarningPreferences

    @Binds
    fun bindsHostsDataSource(assetsHostsDataSource: AssetsHostsDataSource): HostsDataSource

    @Binds
    fun bindsHostsRepository(hostsDatabase: HostsDatabase): HostsRepository

    @Binds
    fun bindsHostsDataSourceProvider(preferencesHostsDataSourceProvider: PreferencesHostsDataSourceProvider): HostsDataSourceProvider
}
