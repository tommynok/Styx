package com.jamal2367.styx.browser

import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import com.jamal2367.styx.R
import com.jamal2367.styx.browser.activity.BrowserActivity
import com.jamal2367.styx.database.bookmark.BookmarkRepository
import com.jamal2367.styx.databinding.PopupMenuBrowserBinding
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.utils.*
import javax.inject.Inject

class BrowserPopupMenu

(layoutInflater: LayoutInflater, aBinding: PopupMenuBrowserBinding = inflate(layoutInflater)) : PopupWindow(aBinding.root, WRAP_CONTENT, WRAP_CONTENT, true) {

    @Inject internal lateinit var bookmarkModel: BookmarkRepository
    @Inject lateinit var userPreferences: UserPreferences

    var iBinding: PopupMenuBrowserBinding = aBinding

    init {
        aBinding.root.context.injector.inject(this)

        elevation = 100F

        animationStyle = R.style.AnimationMenu

        aBinding.menuItemCloseIncognito.visibility = GONE

        setBackgroundDrawable(ColorDrawable())

        if ((aBinding.root.context as BrowserActivity).isIncognito()) {
            aBinding.menuItemIncognito.visibility = GONE
            // No sessions in incognito mode
            aBinding.menuItemSessions.visibility = GONE
            // Show close incognito mode button
            aBinding.menuItemCloseIncognito.visibility = View.VISIBLE
        }
    }

    fun onMenuItemClicked(menuView: View, onClick: () -> Unit) {
        menuView.setOnClickListener {
            onClick()
            dismiss()
        }
    }

    fun show(aAnchor: View) {

        (contentView.context as BrowserActivity).tabsManager.let {
            // Set desktop mode checkbox according to current tab
            iBinding.menuItemDesktopMode.isChecked = it.currentTab?.desktopMode ?: false

            // Same with dark mode
            iBinding.menuItemDarkMode.isChecked = it.currentTab?.darkMode ?: false

            it.currentTab?.let { tab ->
                iBinding.menuItemAddToHome.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemShare.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemPrint.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemPageTools.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemFind.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemTranslate.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemReaderMode.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemDesktopMode.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.menuItemDarkMode.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.divider2.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.divider3.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
                iBinding.divider4.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) GONE else View.VISIBLE
            }

            if (userPreferences.navbar) {
                iBinding.header.visibility = GONE
                iBinding.divider1.visibility = GONE
                iBinding.menuShortcutRefresh.visibility = GONE
                iBinding.menuShortcutHome.visibility = GONE
                iBinding.menuShortcutForward.visibility = GONE
                iBinding.menuShortcutBack.visibility = GONE
                iBinding.menuShortcutBookmarks.visibility = GONE
            }
        }

        // Get our anchor location
        val anchorLoc = IntArray(2)
        aAnchor.getLocationInWindow(anchorLoc)

        // Show our popup menu from the right side of the screen below our anchor
        val gravity = if (userPreferences.toolbarsBottom) Gravity.BOTTOM or Gravity.RIGHT else Gravity.TOP or Gravity.RIGHT
        val yOffset = if (userPreferences.toolbarsBottom) (contentView.context as BrowserActivity).iBinding.root.height - anchorLoc[1] - aAnchor.height else anchorLoc[1]
        showAtLocation(aAnchor, gravity,
        // Offset from the right screen edge
        Utils.dpToPx(10F),
        // Above our anchor
        yOffset)
    }

    companion object {

        fun inflate(layoutInflater: LayoutInflater): PopupMenuBrowserBinding {
            return PopupMenuBrowserBinding.inflate(layoutInflater)
        }

    }
}
