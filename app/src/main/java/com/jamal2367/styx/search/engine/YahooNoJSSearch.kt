package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Yahoo search engine.
 */
class YahooNoJSSearch : BaseSearchEngine(
    "file:///android_asset/yahoo.webp",
    "https://search.yahoo.com/mobile/s?nojs=1&p=",
    R.string.search_engine_yahoo_no_js
)
