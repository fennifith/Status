/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewTreeObserver

import androidx.annotation.ColorInt
import me.jfenn.androidutils.ColorUtils

/**
 * An ImageView that allows the user to pick
 * a color from the bitmap that is drawn on
 * it.
 */
class ColorPickerImageView : CustomImageView {

    private val fillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private var bitmap: Bitmap? = null
    private var colorX: Float = 0f
    private var colorY: Float = 0f
    private var color: Int = Color.BLACK
        set(color) {
            field = color
            onColorChangedListener?.onColorChanged(color)

            fillPaint.color = color
            strokePaint.color = if (ColorUtils.isColorDark(color)) Color.WHITE else Color.BLACK

            Handler(Looper.getMainLooper()).post { postInvalidate() }
        }

    var onColorChangedListener: OnColorChangedListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun setImageBitmap(bitmap: Bitmap?) {
        super.setImageBitmap(bitmap)
        this.bitmap = bitmap

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                object : Thread() {
                    override fun run() {
                        bitmap?.let { bitmapSafe ->
                            colorX = width / 3f
                            colorY = height / 3f
                            color = bitmapSafe.getPixel(bitmapSafe.width / 3, bitmapSafe.height / 3)
                        }
                    }
                }.start()

                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.x = event.x
        this.y = event.y

        object : Thread() {
            override fun run() {
                val location = IntArray(2)
                getLocationOnScreen(location)

                bitmap?.let { bitmapSafe ->
                    val rect = drawable.bounds
                    val x : Int = ((x - rect.left) * bitmapSafe.width).toInt() / width
                    val y : Int = ((y - rect.top) * bitmapSafe.height).toInt() / height

                    if (x in (0 until bitmapSafe.width) && y in (0 until bitmapSafe.height)) {
                        color = bitmapSafe.getPixel(x, y)
                    }
                }
            }
        }.start()

        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(x, y, 30f, strokePaint)
        canvas.drawCircle(x, y, 30f, fillPaint)
    }

    interface OnColorChangedListener {
        fun onColorChanged(@ColorInt color: Int)
    }
}
