package com.jamal2367.styx.js

import com.anthonycr.mezzanine.FileStream

/**
 * Cookie Dialog Blocker for pages.
 */
@FileStream("app/src/main/js/CookieBlock.js")
interface CookieBlock {

    fun provideJs(): String

}
