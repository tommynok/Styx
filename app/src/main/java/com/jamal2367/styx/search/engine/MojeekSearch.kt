package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Mojeek search engine.
 */
class MojeekSearch : BaseSearchEngine(
    "file:///android_asset/mojeek.png",
    "https://www.mojeek.com/search?q=",
    R.string.search_engine_mojeek
)
