/*
 * Copyright 2014 A.C.R. Development
 */
package com.jamal2367.styx.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jamal2367.styx.R
import com.jamal2367.styx.database.HistoryEntry
import com.jamal2367.styx.dialog.BrowserDialog.setDialogSize
import com.jamal2367.styx.extensions.snackbar
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    private const val TAG = "Utils"

    /**
     * Creates a new intent that can launch the email
     * app with a subject, address, body, and cc. It
     * is used to handle mail:to links.
     *
     * @param address the address to send the email to.
     * @param subject the subject of the email.
     * @param body    the body of the email.
     * @param cc      extra addresses to CC.
     * @return a valid intent.
     */
    fun newEmailIntent(address: String, subject: String?,
                       body: String?, cc: String?): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_CC, cc)
        intent.type = "message/rfc822"
        return intent
    }

    /**
     * Creates a dialog with only a title, message, and okay button.
     *
     * @param activity the activity needed to create a dialog.
     * @param title    the title of the dialog.
     * @param message  the message of the dialog.
     */
    fun createInformativeDialog(activity: AppCompatActivity, @StringRes title: Int, @StringRes message: Int) {
        val builder = MaterialAlertDialogBuilder(activity)
        builder.setTitle(title)
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(activity.resources.getString(R.string.action_ok)
                ) { dialog: DialogInterface?, id: Int -> }
        val alert = builder.create()
        alert.show()
        setDialogSize(activity, alert)
    }

    /**
     * Converts Density Pixels (DP) to Pixels (PX).
     *
     * @param dp the number of density pixels to convert.
     * @return the number of pixels that the conversion generates.
     */
    @JvmStatic
    fun dpToPx(dp: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        return (dp * metrics.density + 0.5f).toInt()
    }

    /**
     * Extracts the domain name from a URL.
     * NOTE: Should be used for display only.
     *
     * @param url the URL to extract the domain from.
     * @return the domain name, or the URL if the domain
     * could not be extracted. The domain name may include
     * HTTPS if the URL is an SSL supported URL.
     */
    fun getDisplayDomainName(url: String?): String {
        var url = url
        if (url == null || url.isEmpty()) return ""
        val index = url.indexOf('/', 8)
        if (index != -1) {
            url = url.substring(0, index)
        }
        val uri: URI
        var domain: String?
        try {
            uri = URI(url)
            domain = uri.host
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Unable to parse URI", e)
            domain = null
        }
        if (domain == null || domain.isEmpty()) {
            return url
        }
        return if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    @JvmStatic
    fun trimCache(context: Context) {
        try {
            val dir = context.cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (ignored: Exception) {
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (aChildren in children) {
                val success = deleteDir(File(dir, aChildren))
                if (!success) {
                    return false
                }
            }
        }
        // The directory is now empty so delete it
        return dir != null && dir.delete()
    }

    fun isColorTooDark(color: Int): Boolean {
        val RED_CHANNEL: Byte = 16
        val GREEN_CHANNEL: Byte = 8
        //final byte BLUE_CHANNEL = 0;
        val r = ((color shr RED_CHANNEL.toInt() and 0xff).toFloat() * 0.3f).toInt() and 0xff
        val g = ((color shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * 0.59).toInt() and 0xff
        val b = ((color /* >> BLUE_CHANNEL */ and 0xff).toFloat() * 0.11).toInt() and 0xff
        val gr = r + g + b and 0xff
        val gray = gr /* << BLUE_CHANNEL */ + (gr shl GREEN_CHANNEL.toInt()) + (gr shl RED_CHANNEL.toInt())
        return gray < 0x727272
    }

    fun mixTwoColors(color1: Int, color2: Int, amount: Float): Int {
        val ALPHA_CHANNEL: Byte = 24
        val RED_CHANNEL: Byte = 16
        val GREEN_CHANNEL: Byte = 8
        //final byte BLUE_CHANNEL = 0;
        val inverseAmount = 1.0f - amount
        val r = ((color1 shr RED_CHANNEL.toInt() and 0xff).toFloat() * amount + (color2 shr RED_CHANNEL.toInt() and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        val g = ((color1 shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * amount + (color2 shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        val b = ((color1 and 0xff).toFloat() * amount + (color2 and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        return 0xff shl ALPHA_CHANNEL.toInt() or (r shl RED_CHANNEL.toInt()) or (g shl GREEN_CHANNEL.toInt()) or b
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + '_'
        val storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
        )
    }

    /**
     * Quietly closes a closeable object like an InputStream or OutputStream without
     * throwing any errors or requiring you do do any checks.
     *
     * @param closeable the object to close
     */
    @JvmStatic
    fun close(closeable: Closeable?) {
        if (closeable == null) {
            return
        }
        try {
            closeable.close()
        } catch (e: IOException) {
            Log.e(TAG, "Unable to close closeable", e)
        }
    }

    /**
     * Creates a shortcut on the homescreen using the
     * [HistoryEntry] information that opens the
     * browser. The icon, URL, and title are used in
     * the creation of the shortcut.
     *
     * @param activity the activity needed to create
     * the intent and show a snackbar message
     * @param historyEntry     the HistoryEntity to create the shortcut from
     */
    fun createShortcut(activity: AppCompatActivity,
                       historyEntry: HistoryEntry,
                       favicon: Bitmap) {
        val shortcutIntent = Intent(Intent.ACTION_VIEW)
        shortcutIntent.data = Uri.parse(historyEntry.url)
        val title = if (TextUtils.isEmpty(historyEntry.title)) activity.getString(R.string.untitled) else historyEntry.title
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val addIntent = Intent()
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, favicon)
            addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            activity.sendBroadcast(addIntent)
            activity.snackbar(R.string.message_added_to_homescreen)
        } else {
            val shortcutManager = activity.getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(activity, "browser-shortcut-" + historyEntry.url.hashCode())
                        .setIntent(shortcutIntent)
                        .setIcon(Icon.createWithBitmap(favicon))
                        .setShortLabel(title)
                        .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                activity.snackbar(R.string.message_added_to_homescreen)
            } else {
                activity.snackbar(R.string.shortcut_message_failed_to_add)
            }
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options,
                              reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight
                    && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    @JvmStatic
    fun guessFileExtension(filename: String): String? {
        val lastIndex = filename.lastIndexOf('.') + 1
        return if (lastIndex > 0 && filename.length > lastIndex) {
            filename.substring(lastIndex)
        } else null
    }

    /**
     * Construct an intent to display downloads folder either by using a file browser application
     * or using system download manager.
     *
     * @param aContext
     * @param aDownloadFolder
     * @return
     */
    @JvmStatic
    fun getIntentForDownloads(aContext: Context, aDownloadFolder: String?): Intent {
        // This is the solution from there: https://stackoverflow.com/a/26651827/3969362
        // Build an intent to open our download folder in a file explorer app
        val intent = Intent(Intent.ACTION_VIEW).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.setDataAndType(Uri.parse(aDownloadFolder), "resource/folder")
        // Check that there is an app activity handling that intent on our system
        return if (intent.resolveActivityInfo(aContext.packageManager, 0) != null) {
            // Yes there is one use it
            intent
        } else {
            // Just launch system download manager activity if no custom file explorer found
            Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}