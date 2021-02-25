package com.jamal2367.styx.browser

import com.jamal2367.styx.preference.IntEnum

/**
 * The available Block JavaScript choices.
 */
enum class JavaScriptChoice(override val value: Int) : IntEnum {
    NONE(0),
    WHITELIST(1),
    BLACKLIST(2)
}
