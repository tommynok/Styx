package com.jamal2367.styx.search.engine

import com.jamal2367.styx.R

/**
 * The Yandex search engine.
 */
class YandexSearch : BaseSearchEngine(
    "file:///android_asset/yandex.webp",
    "https://yandex.ru/yandsearch?lr=21411&text=",
    R.string.search_engine_yandex
)
