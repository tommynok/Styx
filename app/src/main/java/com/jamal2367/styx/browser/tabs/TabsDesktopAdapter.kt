package com.jamal2367.styx.browser.tabs

import com.jamal2367.styx.R
import com.jamal2367.styx.browser.activity.BrowserActivity
import com.jamal2367.styx.controller.UIController
import com.jamal2367.styx.extensions.*
import com.jamal2367.styx.utils.ThemeUtils
import com.jamal2367.styx.utils.Utils
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * The adapter for horizontal desktop style browser tabs.
 */
class TabsDesktopAdapter(
    context: Context,
    private val resources: Resources,
    private val uiController: UIController
) : RecyclerView.Adapter<TabViewHolder>() {

    private val backgroundTabDrawable: Drawable?
    private val foregroundTabBitmap: Bitmap?
    private var tabList: List<TabViewState> = emptyList()
    private var textColor = Color.TRANSPARENT

    init {
        val backgroundColor = Utils.mixTwoColors(ThemeUtils.getPrimaryColor(context), Color.BLACK, 0.75f)
        val backgroundTabBitmap = Bitmap.createBitmap(
            context.dimen(R.dimen.desktop_tab_width),
            context.dimen(R.dimen.desktop_tab_height),
            Bitmap.Config.ARGB_8888
        ).also {
            Canvas(it).drawTrapezoid(backgroundColor, true)
        }
        backgroundTabDrawable = BitmapDrawable(resources, backgroundTabBitmap)

        val foregroundColor = ThemeUtils.getPrimaryColor(context)
        foregroundTabBitmap = Bitmap.createBitmap(
            context.dimen(R.dimen.desktop_tab_width),
            context.dimen(R.dimen.desktop_tab_height),
            Bitmap.Config.ARGB_8888
        ).also {
            Canvas(it).drawTrapezoid(foregroundColor, false)
        }
    }

    fun showTabs(tabs: List<TabViewState>) {
        val oldList = tabList
        tabList = tabs

        DiffUtil.calculateDiff(TabViewStateDiffCallback(oldList, tabList)).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TabViewHolder {
        val view = viewGroup.context.inflater.inflate(R.layout.tab_list_item_horizontal, viewGroup, false)
        return TabViewHolder(view, uiController)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.exitButton.tag = position

        val web = tabList[position]

        holder.txtTitle.text = web.title
        updateViewHolderAppearance(holder, web)
        updateViewHolderFavicon(holder, web.favicon)
    }

    private fun updateViewHolderFavicon(viewHolder: TabViewHolder, favicon: Bitmap?) {
        favicon?.let {
                viewHolder.favicon.setImageBitmap(it)
            }
        ?: viewHolder.favicon.setImageResource(R.drawable.ic_webpage)
    }

    private fun updateViewHolderAppearance(viewHolder: TabViewHolder, tab: TabViewState) {

        // Just to init our default text color
        if (textColor == Color.TRANSPARENT) {
            textColor = viewHolder.txtTitle.currentTextColor
        }

        if (tab.isForeground) {
            val foregroundDrawable = BitmapDrawable(resources, foregroundTabBitmap)
            TextViewCompat.setTextAppearance(viewHolder.txtTitle, R.style.boldText)
            val newTextColor = (uiController as BrowserActivity).currentToolBarTextColor
            viewHolder.txtTitle.setTextColor(newTextColor)
            viewHolder.exitButton.findViewById<ImageView>(R.id.deleteButton).setColorFilter(newTextColor)
            uiController.changeToolbarBackground(tab.favicon, tab.themeColor, foregroundDrawable)
            if (uiController.isColorMode()) {
                foregroundDrawable.tint(uiController.getUiColor())
            }
            viewHolder.layout.background = foregroundDrawable
        } else {
            TextViewCompat.setTextAppearance(viewHolder.txtTitle, R.style.italicText)
            viewHolder.layout.background = backgroundTabDrawable
            // Put back the color we stashed
            viewHolder.txtTitle.setTextColor(textColor)
            viewHolder.exitButton.findViewById<ImageView>(R.id.deleteButton).setColorFilter(textColor)
        }
    }

    override fun getItemCount() = tabList.size

}
