package com.jamal2367.styx.browser.tabs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jamal2367.styx.browser.TabsView
import com.jamal2367.styx.browser.activity.BrowserActivity
import com.jamal2367.styx.controller.UIController
import com.jamal2367.styx.databinding.TabDrawerViewBinding
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.extensions.inflater
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.utils.ItemDragDropSwipeHelper
import com.jamal2367.styx.view.StyxView
import javax.inject.Inject

/**
 * A view which displays tabs in a vertical [RecyclerView].
 */
class TabsDrawerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TabsView {

    @Inject lateinit var userPreferences: UserPreferences

    private val uiController = context as UIController
    private val tabsAdapter: TabsDrawerAdapter

    private var mItemTouchHelper: ItemTouchHelper? = null

    var iBinding: TabDrawerViewBinding

    init {

        context.injector.inject(this)

        orientation = VERTICAL
        isClickable = true
        isFocusable = true

        // Inflate our layout with binding support, provide UI controller
        iBinding = TabDrawerViewBinding.inflate(context.inflater,this, true)
        iBinding.uiController = uiController

        tabsAdapter = TabsDrawerAdapter(uiController)

        iBinding.tabsList.apply {
            //setLayerType(View.LAYER_TYPE_NONE, null)
            // We don't want that morphing animation for now
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            // Reverse layout if using bottom tool bars
            // LinearLayoutManager.setReverseLayout is also adjusted from BrowserActivity.setupToolBar
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, userPreferences.toolbarsBottom)
            adapter = tabsAdapter
            setHasFixedSize(false)
        }

        val callback: ItemTouchHelper.Callback = ItemDragDropSwipeHelper(tabsAdapter)

        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(iBinding.tabsList)

    }

    /**
     * Enable tool bar buttons according to current state of things
     * * TODO: Find a way to share that code with TabsDesktopView
     */
    private fun updateTabActionButtons() {
        // If more than one tab, enable close all tabs button
        iBinding.actionCloseAllTabs.isEnabled = uiController.getTabModel().allTabs.count()>1
        // If we have more than one tab in our closed tabs list enable restore all pages button
        iBinding.actionRestoreAllPages.isEnabled = (uiController as BrowserActivity).presenter?.closedTabs?.bundleStack?.count()?:0>1
        // If we have at least one tab in our closed tabs list enable restore page button
        iBinding.actionRestorePage.isEnabled = uiController.presenter?.closedTabs?.bundleStack?.count()?:0>0
        // No sessions in incognito mode
        if (uiController.isIncognito()) {
            iBinding.actionSessions.visibility = View.GONE
        }

    }

    override fun tabAdded() {
        displayTabs()
        updateTabActionButtons()
    }

    override fun tabRemoved(position: Int) {
        displayTabs()
        //tabsAdapter.notifyItemRemoved(position)
        updateTabActionButtons()
    }

    override fun tabChanged(position: Int) {
        displayTabs()
        //tabsAdapter.notifyItemChanged(position)
    }

    private fun displayTabs() {
        tabsAdapter.showTabs(uiController.getTabModel().allTabs.map(StyxView::asTabViewState))
    }

    override fun tabsInitialized() {
        tabsAdapter.notifyDataSetChanged()
        updateTabActionButtons()
    }

    override fun setGoBackEnabled(isEnabled: Boolean) {
        //actionBack.isEnabled = isEnabled
    }

    override fun setGoForwardEnabled(isEnabled: Boolean) {
        //actionForward.isEnabled = isEnabled
    }

}
