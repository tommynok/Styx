package com.jamal2367.styx.view

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.util.AttributeSet
import android.view.KeyEvent
import android.webkit.WebView

/**
 * Specialising  WebView could be useful at some point.
 * We may want to get rid of StyxView.
 *
 * We used that to try debug our issue with ALT + TAB scrolling back to the top of the page.
 * We could not figure out that issue though.
 */
class WebViewEx : WebView {
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {

        /*
        if (event?.keyCode == KeyEvent.KEYCODE_TAB) {
            Log.v("WebViewEx","Tab: " + event.action.toString())
        }
        */

        return super.dispatchKeyEvent(event)
    }

    /**
     * Start a print job, thus notably enabling saving a web page as PDF.
     */
    fun print() : PrintJob {
        val printManager: PrintManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter: PrintDocumentAdapter = createPrintDocumentAdapter(title)
        val jobName = title
        val builder: PrintAttributes.Builder = PrintAttributes.Builder()
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        return printManager.print(jobName, printAdapter, builder.build())
    }
}
