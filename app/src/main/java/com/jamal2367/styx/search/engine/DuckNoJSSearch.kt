package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The DuckDuckGo search engine.
 */
class DuckNoJSSearch : BaseSearchEngine(
    "file:///android_asset/duckduckgo.png",
    "https://duckduckgo.com/html/?q=",
    R.string.search_engine_duckduckgo_no_js
)
