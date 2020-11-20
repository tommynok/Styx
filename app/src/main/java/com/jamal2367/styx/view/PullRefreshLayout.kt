package com.jamal2367.styx.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import android.view.ViewConfiguration

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlin.math.abs

class PullRefreshLayout(context: Context, attrs: AttributeSet?) : SwipeRefreshLayout(context, attrs) {
    private val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var mTouchDownX = 0f
    //private var mTouchDownY = 0f
    private var mIntercept = true

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            // Mark X position of our touchdown
            MotionEvent.ACTION_DOWN -> {
                mTouchDownX = event.x
                //mTouchDownY = event.y
                mIntercept = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIntercept) {
                    return false;
                }
                // Check if we think user is scrolling vertically
                val eventX = event.x
                val xDiff = abs(eventX - mTouchDownX)
                if (xDiff > mTouchSlop) {
                    // User is scrolling vertically do not intercept inputs
                    // Thus preventing pull-to-refresh to trigger while we scroll vertically
                    mIntercept = false
                }
            }
        }

        if (mIntercept) {
            return super.onInterceptTouchEvent(event)
        }

        return false;
    }

}
