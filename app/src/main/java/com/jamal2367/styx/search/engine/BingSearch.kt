package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Bing search engine.
 */
class BingSearch : BaseSearchEngine(
    "file:///android_asset/bing.png",
    "https://www.bing.com/search?q=",
    R.string.search_engine_bing
)
