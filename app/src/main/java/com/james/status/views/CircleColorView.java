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

package com.james.status.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import com.james.status.data.PreferenceData;

import androidx.annotation.ColorInt;
import me.jfenn.androidutils.ColorUtils;

public class CircleColorView extends ColorView {

    Paint outlinePaint;

    public CircleColorView(final Context context) {
        super(context);
    }

    public CircleColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void setUp() {
        super.setUp();

        outlinePaint = new Paint();
        outlinePaint.setAntiAlias(true);
        outlinePaint.setDither(true);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(outlineSize);
        outlinePaint.setColor(Color.BLACK);
    }

    @Override
    public void setColor(@ColorInt int color) {
        if (PreferenceData.PREF_DARK_THEME.getValue(getContext()))
            outlinePaint.setColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.TRANSPARENT);
        else outlinePaint.setColor(ColorUtils.isColorDark(color) ? Color.TRANSPARENT : Color.BLACK);

        super.setColor(color);
    }

    @Override
    public void render(Canvas canvas) {
        int size = Math.min(canvas.getWidth(), canvas.getHeight());

        Path path = new Path();
        path.addCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, size / 2, Path.Direction.CW);
        canvas.clipPath(path);

        super.render(canvas);

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (size / 2) - (outlineSize / 2), outlinePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredWidth();
        setMeasuredDimension(size, size);
    }

}
