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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet

import androidx.annotation.ColorInt
import me.jfenn.androidutils.DimenUtils

/**
 * A view that displays a solid color in the background, with
 * support for transparency.
 */
open class ColorView : RenderableView {

    @ColorInt
    open var color : Int = Color.BLACK
        set(color) {
            field = color
            startRender()
        }

    var outlineSize: Float = DimenUtils.dpToPx(2f).toFloat()
        set(outlineSize) {
            field = outlineSize
            startRender()
        }

    private val tilePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.LTGRAY
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun render(canvas: Canvas) {
        if (Color.alpha(color) < 255) {
            val outline = Math.round(outlineSize) * 4
            var x = 0
            while (x < canvas.width) {
                var y = if (x % (outline * 2) == 0) 0 else outline
                while (y < canvas.width) {
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + outline).toFloat(), (y + outline).toFloat(), tilePaint)
                    y += outline * 2
                }
                x += outline
            }
        }

        canvas.drawColor(color)
    }
}
