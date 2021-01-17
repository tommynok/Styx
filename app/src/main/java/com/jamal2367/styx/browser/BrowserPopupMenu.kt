package com.jamal2367.styx.browser

import com.jamal2367.styx.R
import com.jamal2367.styx.browser.activity.BrowserActivity
import com.jamal2367.styx.database.bookmark.BookmarkRepository
import com.jamal2367.styx.databinding.PopupMenuBrowserBinding
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.utils.Utils
import com.jamal2367.styx.utils.isSpecialUrl
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.popup_menu_browser.view.*
import javax.inject.Inject

class BrowserPopupMenu : PopupWindow {

    @Inject
    internal lateinit var bookmarkModel: BookmarkRepository

    constructor(layoutInflater: LayoutInflater, view: View = BrowserPopupMenu.inflate(layoutInflater))
            : super(view, WRAP_CONTENT, WRAP_CONTENT, true) {

        view.context.injector.inject(this)

        animationStyle = R.style.AnimationMenu
        //animationStyle = android.R.style.Animation_Dialog

        // Needed on Android 5 to make sure our pop-up can be dismissed by tapping outside and back button
        // See: https://stackoverflow.com/questions/46872634/close-popupwindow-upon-tapping-outside-or-back-button
        setBackgroundDrawable(ColorDrawable())

        // Hide incognito menu item if we are already incognito
        if ((view.context as BrowserActivity).isIncognito()) {
            view.menuItemIncognito.visibility = View.GONE
            // No sessions in incognito mode
            view.menuItemSessions.visibility = View.GONE
        }

    }


    fun onMenuItemClicked(menuView: View, onClick: () -> Unit) {
        menuView.setOnClickListener {
            onClick()
            dismiss()
        }
    }

    fun show(rootView: View) {

        (contentView.context as BrowserActivity).tabsManager.let {
            // Set desktop mode checkbox according to current tab
            contentView.menuItemDesktopMode.isChecked = it.currentTab?.toggleDesktop ?: false

            it.currentTab?.let { tab ->
                // Let user add multiple times the same URL I guess, for now anyway
                // Blocking it is not nice and subscription is more involved I guess
                // See BookmarksDrawerView.updateBookmarkIndicator
                //contentView.menuItemAddBookmark.visibility = if (bookmarkModel.isBookmark(tab.url).blockingGet() || tab.url.isSpecialUrl()) View.GONE else View.VISIBLE
                contentView.menuItemAddBookmark.visibility = if (tab.url.isSpecialUrl()) View.GONE else View.VISIBLE
            }


        }
        // Assuming top right for now
        //val anchorLocation = IntArray(2)
        //anchorView.getLocationOnScreen(anchorLocation)
        val x = Utils.dpToPx(5f) //anchorLocation[0] margin
        val y =  Utils.dpToPx(5f) //anchorLocation[1] //+ margin
        showAtLocation(rootView, Gravity.TOP or Gravity.RIGHT, x, y)
    }

    companion object {

        private const val margin = 15

        fun inflate(layoutInflater: LayoutInflater): View {
            return PopupMenuBrowserBinding.inflate(layoutInflater).root
        }

    }
}

