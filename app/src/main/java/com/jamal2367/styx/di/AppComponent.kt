package com.jamal2367.styx.di

import com.jamal2367.styx.BrowserApp
import com.jamal2367.styx.adblock.BloomFilterAdBlocker
import com.jamal2367.styx.adblock.NoOpAdBlocker
import com.jamal2367.styx.browser.BrowserPopupMenu
import com.jamal2367.styx.browser.SearchBoxModel
import com.jamal2367.styx.browser.activity.BrowserActivity
import com.jamal2367.styx.browser.activity.ThemableBrowserActivity
import com.jamal2367.styx.browser.bookmarks.BookmarksDrawerView
import com.jamal2367.styx.device.BuildInfo
import com.jamal2367.styx.dialog.LightningDialogBuilder
import com.jamal2367.styx.download.LightningDownloadListener
import com.jamal2367.styx.reading.activity.ReadingActivity
import com.jamal2367.styx.search.SuggestionsAdapter
import com.jamal2367.styx.settings.activity.SettingsActivity
import com.jamal2367.styx.settings.activity.ThemableSettingsActivity
import com.jamal2367.styx.settings.fragment.*
import com.jamal2367.styx.view.LightningChromeClient
import com.jamal2367.styx.view.LightningView
import com.jamal2367.styx.view.LightningWebClient
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (AppBindsModule::class)])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun buildInfo(buildInfo: BuildInfo): Builder

        fun build(): AppComponent
    }

    fun inject(activity: BrowserActivity)

    fun inject(fragment: BookmarkSettingsFragment)

    fun inject(builder: LightningDialogBuilder)

    fun inject(lightningView: LightningView)

    fun inject(activity: ThemableBrowserActivity)

    fun inject(advancedSettingsFragment: AdvancedSettingsFragment)

    fun inject(app: BrowserApp)

    fun inject(activity: ReadingActivity)

    fun inject(webClient: LightningWebClient)

    fun inject(activity: SettingsActivity)

    fun inject(activity: ThemableSettingsActivity)

    fun inject(listener: LightningDownloadListener)

    fun inject(fragment: PrivacySettingsFragment)

    fun inject(fragment: DebugSettingsFragment)

    fun inject(suggestionsAdapter: SuggestionsAdapter)

    fun inject(chromeClient: LightningChromeClient)

    fun inject(searchBoxModel: SearchBoxModel)

    fun inject(generalSettingsFragment: GeneralSettingsFragment)

    fun inject(displaySettingsFragment: DisplaySettingsFragment)

    fun inject(adBlockSettingsFragment: AdBlockSettingsFragment)

    fun inject(bookmarksView: BookmarksDrawerView)

    fun inject(popupMenu: BrowserPopupMenu)

    fun inject(appsSettingsFragment: AppsSettingsFragment)

    fun provideBloomFilterAdBlocker(): BloomFilterAdBlocker

    fun provideNoOpAdBlocker(): NoOpAdBlocker

}
