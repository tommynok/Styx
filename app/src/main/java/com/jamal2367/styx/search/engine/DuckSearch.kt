package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The DuckDuckGo search engine.
 */
class DuckSearch : BaseSearchEngine(
    "file:///android_asset/duckduckgo.webp",
    "https://duckduckgo.com/?t=styx&q=",
    R.string.search_engine_duckduckgo
)
