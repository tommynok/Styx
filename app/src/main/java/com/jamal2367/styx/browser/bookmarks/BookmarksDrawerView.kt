package com.jamal2367.styx.browser.bookmarks

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.webkit.CookieManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ahmadaghazadeh.editor.widget.CodeEditor
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jamal2367.styx.R
import com.jamal2367.styx.adblock.allowlist.AllowListModel
import com.jamal2367.styx.animation.AnimationUtils
import com.jamal2367.styx.browser.BookmarksView
import com.jamal2367.styx.browser.JavaScriptChoice
import com.jamal2367.styx.browser.TabsManager
import com.jamal2367.styx.controller.UIController
import com.jamal2367.styx.database.Bookmark
import com.jamal2367.styx.database.bookmark.BookmarkRepository
import com.jamal2367.styx.databinding.BookmarkDrawerViewBinding
import com.jamal2367.styx.di.DatabaseScheduler
import com.jamal2367.styx.di.MainScheduler
import com.jamal2367.styx.di.NetworkScheduler
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.dialog.BrowserDialog
import com.jamal2367.styx.dialog.DialogItem
import com.jamal2367.styx.dialog.StyxDialogBuilder
import com.jamal2367.styx.extensions.color
import com.jamal2367.styx.extensions.drawable
import com.jamal2367.styx.extensions.inflater
import com.jamal2367.styx.favicon.FaviconModel
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.utils.*
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.net.URL
import javax.inject.Inject

/**
 * The view that displays bookmarks in a list and some controls.
 */
class BookmarksDrawerView @JvmOverloads constructor(
        context: Context,
        private val activity: Activity,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        userPreferences: UserPreferences
) : LinearLayout(context, attrs, defStyleAttr), BookmarksView {

    @Inject internal lateinit var bookmarkModel: BookmarkRepository
    @Inject internal lateinit var allowListModel: AllowListModel
    @Inject internal lateinit var bookmarksDialogBuilder: StyxDialogBuilder
    @Inject internal lateinit var faviconModel: FaviconModel
    @Inject lateinit var userPreferences: UserPreferences
    @Inject @field:DatabaseScheduler internal lateinit var databaseScheduler: Scheduler
    @Inject @field:NetworkScheduler internal lateinit var networkScheduler: Scheduler
    @Inject @field:MainScheduler internal lateinit var mainScheduler: Scheduler

    private val uiController: UIController

    // Adapter
    private var iAdapter: BookmarksAdapter
    // Drag & drop support
    private var iItemTouchHelper: ItemTouchHelper? = null

    // Colors
    private var scrollIndex: Int = 0

    private var bookmarksSubscription: Disposable? = null
    private var bookmarkUpdateSubscription: Disposable? = null

    private val uiModel = BookmarkUiModel()

    var iBinding: BookmarkDrawerViewBinding

    private var addBookmarkView: ImageView? = null

    init {

        context.injector.inject(this)

        uiController = context as UIController

        iBinding = BookmarkDrawerViewBinding.inflate(context.inflater,this, true)

        iBinding.uiController = uiController


        iBinding.bookmarkBackButton.setOnClickListener {
            if (!uiModel.isCurrentFolderRoot()) {
                setBookmarksShown(null, true)
                iBinding.listBookmarks.layoutManager?.scrollToPosition(scrollIndex)
            }
        }

        addBookmarkView = findViewById(R.id.menuItemAddBookmark)
        addBookmarkView?.setOnClickListener { uiController.bookmarkButtonClicked() }

        iAdapter = BookmarksAdapter(
                context,
                uiController,
                faviconModel,
                networkScheduler,
                mainScheduler,
                ::showBookmarkMenu,
                ::openBookmark
        )

        iBinding.listBookmarks.apply {
            // Reverse layout if using bottom tool bars
            // LinearLayoutManager.setReverseLayout is also adjusted from BrowserActivity.setupToolBar
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, userPreferences.toolbarsBottom)
            adapter = iAdapter
        }

        // Enable drag & drop but not swipe
        val callback: ItemTouchHelper.Callback = ItemDragDropSwipeHelper(iAdapter, true, false)
        iItemTouchHelper = ItemTouchHelper(callback)
        iItemTouchHelper?.attachToRecyclerView(iBinding.listBookmarks)

        setBookmarksShown(null, true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        bookmarksSubscription?.dispose()
        bookmarkUpdateSubscription?.dispose()

        iAdapter.cleanupSubscriptions()
    }

    private fun getTabsManager(): TabsManager = uiController.getTabModel()

    // TODO: apply that logic to the add bookmark menu item from main pop-up menu
    // SL: I guess this is of no use here anymore since we removed the add bookmark button
    private fun updateBookmarkIndicator(url: String) {
        bookmarkUpdateSubscription?.dispose()
        bookmarkUpdateSubscription = bookmarkModel.isBookmark(url)
            .subscribeOn(databaseScheduler)
            .observeOn(mainScheduler)
            .subscribe { isBookmark ->
                bookmarkUpdateSubscription = null
                addBookmarkView?.isSelected = isBookmark
                addBookmarkView?.isEnabled = !url.isSpecialUrl() && !url.isHomeUri() && !url.isBookmarkUri() && !url.isHistoryUri()
            }
    }

    override fun handleBookmarkDeleted(bookmark: Bookmark) = when (bookmark) {
        is Bookmark.Folder -> setBookmarksShown(null, false)
        is Bookmark.Entry -> iAdapter.deleteItem(BookmarksViewModel(bookmark))
    }

    /**
     *
     */
    private fun setBookmarksShown(folder: String?, animate: Boolean) {
        bookmarksSubscription?.dispose()
        bookmarksSubscription = bookmarkModel.getBookmarksFromFolderSorted(folder)
            .concatWith(Single.defer {
                if (folder == null) {
                    bookmarkModel.getFoldersSorted()
                } else {
                    Single.just(emptyList())
                }
            })
            .toList()
            .map { it.flatten() }
            .subscribeOn(databaseScheduler)
            .observeOn(mainScheduler)
            .subscribe { bookmarksAndFolders ->
                uiModel.currentFolder = folder
                setBookmarkDataSet(bookmarksAndFolders, animate)
                iBinding.textTitle.text = if (folder.isNullOrBlank()) resources.getString(R.string.action_bookmarks) else folder
            }
    }

    /**
     *
     */
    private fun setBookmarkDataSet(items: List<Bookmark>, animate: Boolean) {
        iAdapter.updateItems(items.map { BookmarksViewModel(it) })
        val resource = if (uiModel.isCurrentFolderRoot()) {
            R.drawable.ic_bookmark_border
        } else {
            R.drawable.ic_action_back
        }

        if (animate) {
            iBinding.bookmarkBackButton.let {
                val transition = AnimationUtils.createRotationTransitionAnimation(it, resource)
                it.startAnimation(transition)
            }
        } else {
            iBinding.bookmarkBackButton.setImageResource(resource)
        }
    }

    /**
     *
     */
    private fun showBookmarkMenu(bookmark: Bookmark): Boolean {
        (context as AppCompatActivity?)?.let {
            when (bookmark) {
                is Bookmark.Folder -> bookmarksDialogBuilder.showBookmarkFolderLongPressedDialog(it, uiController, bookmark)
                is Bookmark.Entry -> bookmarksDialogBuilder.showLongPressedDialogForBookmarkUrl(it, uiController, bookmark)
            }
        }
        return true
    }

    /**
     *
     */
    private fun openBookmark(bookmark: Bookmark) = when (bookmark) {
        is Bookmark.Folder -> {
            scrollIndex = (iBinding.listBookmarks.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            setBookmarksShown(bookmark.title, true)
        }
        is Bookmark.Entry -> uiController.bookmarkItemClicked(bookmark)
    }

    fun stringContainsItemFromList(inputStr: String, items: Array<String>): Boolean {
        for (i in items.indices) {
            if (inputStr.contains(items[i])) {
                return true
            }
        }
        return false
    }

    /**
     * Show the page tools dialog.
     */
    fun showPageToolsDialog(context: Context, userPreferences: UserPreferences) {
        val currentTab = getTabsManager().currentTab ?: return
        val isAllowedAds = allowListModel.isUrlAllowedAds(currentTab.url)
        val whitelistString = if (isAllowedAds) {
            R.string.dialog_adblock_enable_for_site
        } else {
            R.string.dialog_adblock_disable_for_site
        }
        val arrayOfURLs = userPreferences.javaScriptBlocked
        val strgs: Array<String>
        if (arrayOfURLs.contains(", ")) {
            strgs = arrayOfURLs.split(", ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        } else {
            strgs = arrayOfURLs.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        }
        var jsEnabledString = if (userPreferences.javaScriptChoice == JavaScriptChoice.BLACKLIST && !stringContainsItemFromList(currentTab.url, strgs) || userPreferences.javaScriptChoice == JavaScriptChoice.WHITELIST && stringContainsItemFromList(currentTab.url, strgs)) {
            R.string.allow_javascript
        } else{
            R.string.blocked_javascript
        }

        BrowserDialog.showWithIcons(context, context.getString(R.string.dialog_tools_title),
                DialogItem(
                        icon = context.drawable(R.drawable.ic_block),
                        colorTint = context.color(R.color.error_red).takeIf { isAllowedAds },
                        title = whitelistString
                ) {
                    if (isAllowedAds) {
                        allowListModel.removeUrlFromAllowList(currentTab.url)
                    } else {
                        allowListModel.addUrlToAllowList(currentTab.url)
                    }
                    getTabsManager().currentTab?.reload()
                },
                DialogItem(
                        icon = context.drawable(R.drawable.ic_baseline_code_24),
                        title = R.string.page_source
                ) {
                    currentTab.webView?.evaluateJavascript("""(function() {
                        return "<html>" + document.getElementsByTagName('html')[0].innerHTML + "</html>";
                     })()""".trimMargin()) {
                        // Hacky workaround for weird WebView encoding bug
                        var name = it?.replace("\\u003C", "<")
                        name = name?.replace("\\n", System.getProperty("line.separator").toString())
                        name = name?.replace("\\t", "")
                        name = name?.replace("\\\"", "\"")
                        name = name?.substring(1, name.length - 1)

                        val builder = MaterialAlertDialogBuilder(context)
                        val inflater = activity.layoutInflater
                        builder.setTitle(R.string.page_source)
                        val dialogLayout = inflater.inflate(R.layout.dialog_view_source, null)
                        val editText = dialogLayout.findViewById<CodeEditor>(R.id.dialog_multi_line)
                        editText.setText(name, 1)
                        builder.setView(dialogLayout)
                        builder.setNegativeButton(R.string.action_cancel) { _, _ -> }
                        builder.setPositiveButton(R.string.action_ok) { _, _ ->
                            editText.setText(editText.text?.toString()?.replace("\'", "\\\'"), 1)
                            currentTab.loadUrl("javascript:(function() { document.documentElement.innerHTML = '" + editText.text.toString() + "'; })()")
                        }
                        builder.show()
                    }
                },
                DialogItem(
                        icon= context.drawable(R.drawable.ic_script_add),
                        title = R.string.inspect
                ){
                    val builder = MaterialAlertDialogBuilder(context)
                    val inflater = activity.layoutInflater
                    builder.setTitle(R.string.inspect)
                    val dialogLayout = inflater.inflate(R.layout.dialog_view_source, null)
                    val editText = dialogLayout.findViewById<CodeEditor>(R.id.dialog_multi_line)
                    editText.setText(editText.text.toString(),1)
                    builder.setView(dialogLayout)
                    builder.setNegativeButton(R.string.action_cancel) { _, _ -> }
                    builder.setPositiveButton(R.string.action_ok) { _, _ -> currentTab.loadUrl("javascript:(function() {" + editText.text.toString() + "})()") }
                    builder.show()
                },
                DialogItem(
                        icon = context.drawable(R.drawable.ic_script_key),
                        colorTint = context.color(R.color.error_red).takeIf { userPreferences.javaScriptChoice == JavaScriptChoice.BLACKLIST && !stringContainsItemFromList(currentTab.url, strgs) || userPreferences.javaScriptChoice == JavaScriptChoice.WHITELIST && stringContainsItemFromList(currentTab.url, strgs) },
                        title = jsEnabledString
                ) {
                    val url = URL(currentTab.url)
                    if (userPreferences.javaScriptChoice != JavaScriptChoice.NONE) {
                        if (!stringContainsItemFromList(currentTab.url, strgs)) {
                            if (userPreferences.javaScriptBlocked.equals("")) {
                                userPreferences.javaScriptBlocked = url.host
                            } else {
                                userPreferences.javaScriptBlocked = userPreferences.javaScriptBlocked + ", " + url.host
                            }
                        } else {
                            if (!userPreferences.javaScriptBlocked.contains(", " + url.host)) {
                                userPreferences.javaScriptBlocked = userPreferences.javaScriptBlocked.replace(url.host, "")
                            } else {
                                userPreferences.javaScriptBlocked = userPreferences.javaScriptBlocked.replace(", " + url.host, "")
                            }
                        }
                    } else {
                        userPreferences.javaScriptChoice = JavaScriptChoice.WHITELIST
                    }
                    getTabsManager().currentTab?.reload()
                    Handler().postDelayed({
                        getTabsManager().currentTab?.reload()
                    }, 250)
                },
                DialogItem(
                        icon = context.drawable(R.drawable.ic_cookie),
                        title = R.string.edit_cookies
                ) {

                    val cookieManager = CookieManager.getInstance()
                    if (cookieManager.getCookie(currentTab.url) != null) {
                        val builder = MaterialAlertDialogBuilder(context)
                        val inflater = activity.layoutInflater
                        builder.setTitle(R.string.site_cookies)
                        val dialogLayout = inflater.inflate(R.layout.dialog_view_source, null)
                        val editText = dialogLayout.findViewById<CodeEditor>(R.id.dialog_multi_line)
                        editText.setText(cookieManager.getCookie(currentTab.url), 1)
                        builder.setView(dialogLayout)
                        builder.setNegativeButton(R.string.action_cancel) { _, _ -> }
                        builder.setPositiveButton(R.string.action_ok) { _, _ ->
                            val cookiesList = editText.text.toString().split(";")
                            cookiesList.forEach { item ->
                                CookieManager.getInstance().setCookie(currentTab.url, item)
                            }
                        }
                        builder.show()
                    }

                }
        )

    }

    override fun navigateBack() {
        if (uiModel.isCurrentFolderRoot()) {
            uiController.onBackButtonPressed()
        } else {
            setBookmarksShown(null, true)
            iBinding.listBookmarks.layoutManager?.scrollToPosition(scrollIndex)
        }
    }

    override fun handleUpdatedUrl(url: String) {
        updateBookmarkIndicator(url)
        val folder = uiModel.currentFolder
        setBookmarksShown(folder, false)
    }

}
