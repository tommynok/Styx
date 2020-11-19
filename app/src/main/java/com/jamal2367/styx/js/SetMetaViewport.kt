package com.jamal2367.styx.js

import com.anthonycr.mezzanine.FileStream

/**
 * Set HTML meta viewport thus enabling desktop mode or other zoom trick.
 */
@FileStream("app/src/main/js/SetMetaViewport.js")
interface SetMetaViewport {

    fun provideJs(): String

}