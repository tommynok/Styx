package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The DuckDuckGo Lite search engine.
 */
class DuckLiteSearch : BaseSearchEngine(
    "file:///android_asset/duckduckgo.png",
    "https://duckduckgo.com/lite/?t=styx&q=",
    R.string.search_engine_duckduckgo_lite
)
