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
import android.graphics.Path
import android.util.AttributeSet

import com.james.status.data.PreferenceData

import androidx.annotation.ColorInt
import me.jfenn.androidutils.ColorUtils

/**
 * A ColorView... but it's a circle.
 */
class CircleColorView : ColorView {

    private val outlinePaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeWidth = outlineSize
        color = Color.BLACK
    }

    override var color : Int
        get() = super.color
        set(color) {
            if (PreferenceData.PREF_DARK_THEME.getValue(context))
                outlinePaint.color = if (ColorUtils.isColorDark(color)) Color.WHITE else Color.TRANSPARENT
            else
                outlinePaint.color = if (ColorUtils.isColorDark(color)) Color.TRANSPARENT else Color.BLACK

            super.color = color
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun render(canvas: Canvas) {
        val size = Math.min(canvas.width, canvas.height)

        val path = Path()
        path.addCircle(canvas.width / 2f, canvas.height / 2f, size / 2f, Path.Direction.CW)
        canvas.clipPath(path)

        super.render(canvas)

        canvas.drawCircle(width / 2f, height / 2f, (size / 2f) - (outlineSize / 2f), outlinePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val size = measuredWidth
        setMeasuredDimension(size, size)
    }

}
