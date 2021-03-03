package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The StartPage search engine.
 */
class StartPageSearch : BaseSearchEngine(
    "file:///android_asset/startpage.webp",
    "https://startpage.com/do/search?language=english&query=",
    R.string.search_engine_startpage
)
