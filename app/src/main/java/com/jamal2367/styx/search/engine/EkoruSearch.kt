package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Ekoru search engine.
 */
class EkoruSearch : BaseSearchEngine(
    "file:///android_asset/ekoru.png",
    "https://www.ekoru.org/?ext=styx&q=",
    R.string.search_engine_ekoru
)
