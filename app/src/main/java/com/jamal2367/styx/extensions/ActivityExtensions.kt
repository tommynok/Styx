@file:JvmName("ActivityExtensions")

package com.jamal2367.styx.extensions

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

/**
 * Displays a snackbar to the user with a [StringRes] message.
 *
 * NOTE: If there is an accessibility manager enabled on
 * the device, such as LastPass, then the snackbar animations
 * will not work.
 *
 * @param resource the string resource to display to the user.
 */
fun AppCompatActivity.snackbar(@StringRes resource: Int) {
    makeSnackbar(getString(resource)).show()
}

/**
 * Display a snackbar to the user with a [String] message.
 *
 * @param message the message to display to the user.
 * @see snackbar
 */
fun AppCompatActivity.snackbar(message: String) {
    makeSnackbar(message).show()
}

// Define our snackbar popup duration
const val KDuration: Int = 4000; // Snackbar.LENGTH_LONG

/**
 *
 */
@SuppressLint("WrongConstant")
fun AppCompatActivity.makeSnackbar(message: String): Snackbar {
    val view = findViewById<View>(android.R.id.content)
    return Snackbar.make(view, message, KDuration)
}

/**
 *
 */

fun AppCompatActivity.setStatusBarIconsColor(dark: Boolean)
{
        if (dark) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
}
