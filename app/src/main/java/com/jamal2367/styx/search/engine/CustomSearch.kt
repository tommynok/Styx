package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * A custom search engine.
 */
class CustomSearch(queryUrl: String) : BaseSearchEngine(
    "file:///android_asset/styx.webp",
    queryUrl,
    R.string.search_engine_custom
)
