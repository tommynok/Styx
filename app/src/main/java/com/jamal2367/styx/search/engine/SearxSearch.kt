package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Searx search engine.
 */
class SearxSearch : BaseSearchEngine(
    "file:///android_asset/searx.png",
    "https://searx.prvcy.eu/search?q=",
    R.string.search_engine_searx
)
