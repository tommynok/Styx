package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Qwant search engine.
 */
class QwantSearch : BaseSearchEngine(
    "file:///android_asset/qwant.webp",
    "https://www.qwant.com/?q=",
    R.string.search_engine_qwant
)
