package com.jamal2367.styx.browser

import com.jamal2367.styx.R
import com.jamal2367.styx.browser.activity.BrowserActivity
import com.jamal2367.styx.database.bookmark.BookmarkRepository
import com.jamal2367.styx.databinding.PopupMenuBrowserBinding
import com.jamal2367.styx.di.injector
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.core.view.isVisible
import com.jamal2367.styx.utils.*
import javax.inject.Inject

class BrowserPopupMenu

(layoutInflater: LayoutInflater, aBinding: PopupMenuBrowserBinding = inflate(layoutInflater)) : PopupWindow(aBinding.root, WRAP_CONTENT, WRAP_CONTENT, true) {

    @Inject internal lateinit var bookmarkModel: BookmarkRepository

    var iBinding: PopupMenuBrowserBinding = aBinding

    init {
        aBinding.root.context.injector.inject(this)

        elevation = 100F

        animationStyle = R.style.AnimationMenu

        aBinding.menuItemCloseIncognito.visibility = View.GONE

        setBackgroundDrawable(ColorDrawable())

        if ((aBinding.root.context as BrowserActivity).isIncognito()) {
            aBinding.menuItemIncognito.visibility = View.GONE
            // No sessions in incognito mode
            aBinding.menuItemSessions.visibility = View.GONE
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

            it.currentTab?.let { tab ->
                iBinding.menuItemAddToHome.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.menuItemShare.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.menuItemPrint.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.menuItemPageTools.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.menuItemFind.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.menuItemTranslate.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.menuItemReaderMode.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.menuItemDesktopMode.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.divider.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
                iBinding.divider1.visibility = if (tab.url.isSpecialUrl() or tab.url.isHomeUri() or tab.url.isBookmarkUri() or tab.url.isHistoryUri()) View.GONE else View.VISIBLE
            }
        }

        // Get our anchor location
        val anchorLoc = IntArray(2)
        aAnchor.getLocationInWindow(anchorLoc)

        // Show our popup menu from the right side of the screen below our anchor
        showAtLocation(aAnchor, Gravity.TOP or Gravity.END,

        // Offset from the right screen edge
        Utils.dpToPx(10F),

        // Above our anchor
        anchorLoc[1])
    }

    companion object {

        fun inflate(layoutInflater: LayoutInflater): PopupMenuBrowserBinding {
            return PopupMenuBrowserBinding.inflate(layoutInflater)
        }

    }
}
