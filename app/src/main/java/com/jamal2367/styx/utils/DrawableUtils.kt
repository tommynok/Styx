package com.jamal2367.styx.utils

import android.app.Application
import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.jamal2367.styx.R
import com.jamal2367.styx.utils.ThemeUtils.getBitmapFromVectorDrawable
import com.jamal2367.styx.utils.Utils.dpToPx
import kotlin.math.abs

object DrawableUtils {
    /**
     * Creates a white rounded drawable with an inset image of a different color.
     *
     * @param context     the context needed to work with resources.
     * @param drawableRes the drawable to inset on the rounded drawable.
     * @return a bitmap with the desired content.
     */
    fun createImageInsetInRoundedSquare(
            context: Context?,
            @DrawableRes drawableRes: Int
    ): Bitmap {
        val icon = getBitmapFromVectorDrawable(context!!, drawableRes)
        val image = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val paint = Paint()
        paint.color = Color.TRANSPARENT
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        val radius = dpToPx(2f)
        val outer = RectF(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat())
        canvas.drawRoundRect(outer, radius.toFloat(), radius.toFloat(), paint)
        val dest = Rect(Math.round(outer.left + radius), Math.round(outer.top + radius), Math.round(outer.right - radius), Math.round(outer.bottom - radius))
        paint.color = Color.WHITE
        canvas.drawBitmap(icon, null, dest, paint)
        return image
    }

    /**
     * Creates a rounded square of a certain color with
     * a character imprinted in white on it.
     *
     * @param character the character to write on the image.
     * @param width     the width of the final image.
     * @param height    the height of the final image.
     * @param color     the background color of the rounded square.
     * @return a valid bitmap of a rounded square with a character on it.
     */
    fun createRoundedLetterImage(
            character: Char,
            width: Int,
            height: Int,
            color: Int
    ): Bitmap {
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val paint = Paint()
        paint.color = color
        val boldText = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.typeface = boldText
        paint.textSize = dpToPx(14f).toFloat()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        val radius = dpToPx(2f)
        val outer = RectF(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat())
        canvas.drawRoundRect(outer, radius.toFloat(), radius.toFloat(), paint)
        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()
        paint.color = Color.WHITE
        canvas.drawText(character.toString(), xPos.toFloat(), yPos.toFloat(), paint)
        return image
    }

    /**
     * Hashes a character to one of four colors:
     * blue, green, red, or orange.
     *
     * @param character the character to hash.
     * @param app       the application needed to get the color.
     * @return one of the above colors, or black something goes wrong.
     */
    @ColorInt
    fun characterToColorHash(character: Char, app: Application): Int {
        val smallHash = Character.getNumericValue(character) % 2
        return when (abs(smallHash)) {
            0 -> ContextCompat.getColor(app, R.color.bookmark_default_blue)
            1 -> ContextCompat.getColor(app, R.color.bookmark_default_red)
            else -> Color.BLACK
        }
    }

    fun mixColor(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff
        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff
        return startA + (fraction * (endA - startA)).toInt() shl 24 or (
                startR + (fraction * (endR - startR)).toInt() shl 16) or (
                startG + (fraction * (endG - startG)).toInt() shl 8) or
                startB + (fraction * (endB - startB)).toInt()
    }
}