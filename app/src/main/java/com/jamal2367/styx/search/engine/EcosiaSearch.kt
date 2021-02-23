package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Ecosia search engine.
 */
class EcosiaSearch : BaseSearchEngine(
    "file:///android_asset/ecosia.png",
    "https://www.ecosia.org/search?q=",
    R.string.search_engine_ecosia
)
