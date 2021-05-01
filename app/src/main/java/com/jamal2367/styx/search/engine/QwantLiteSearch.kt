package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Qwant Lite search engine.
 */
class QwantLiteSearch : BaseSearchEngine(
    "file:///android_asset/qwant.webp",
    "https://lite.qwant.com/?q=",
    R.string.search_engine_qwant_lite
)
