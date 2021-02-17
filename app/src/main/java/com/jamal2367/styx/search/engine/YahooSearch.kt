package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Yahoo search engine.
 */
class YahooSearch : BaseSearchEngine(
    "file:///android_asset/yahoo.png",
    "https://search.yahoo.com/search?p=",
    R.string.search_engine_yahoo
)
