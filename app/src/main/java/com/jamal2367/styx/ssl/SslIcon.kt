package com.jamal2367.styx.ssl

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.jamal2367.styx.R

/**
 * Creates the proper [Drawable] to represent the [SslState].
 */
fun Context.createSslDrawableForState(sslState: SslState): Drawable? = when (sslState) {
    is SslState.None -> null
    is SslState.Valid -> {
        ContextCompat.getDrawable(this, R.drawable.ic_secure)
    }
    is SslState.Invalid -> {
        ContextCompat.getDrawable(this, R.drawable.ic_unsecured)
    }
}
