package com.jamal2367.styx

import com.jamal2367.styx.preference.IntEnum

/**
 * The available app themes.
 */
enum class AppTheme(override val value: Int) : IntEnum {
    DEFAULT(0),
    LIGHT(1),
    DARK(2),
    BLACK(3)
}
