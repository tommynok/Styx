package com.jamal2367.styx.js

import com.anthonycr.mezzanine.FileStream

/**
 * Dark mode for the page.
 */
@FileStream("app/src/main/js/DarkMode.js")
interface DarkMode {

    fun provideJs(): String

}
