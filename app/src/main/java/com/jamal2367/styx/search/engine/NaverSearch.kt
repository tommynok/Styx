package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Naver search engine.
 */
class NaverSearch : BaseSearchEngine(
    "file:///android_asset/naver.webp",
    "https://search.naver.com/search.naver?ie=utf8&query=",
    R.string.search_engine_naver
)
