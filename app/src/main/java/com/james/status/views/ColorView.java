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
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import me.jfenn.androidutils.DimenUtils;

public class ColorView extends RenderableView {

    @ColorInt
    int color = Color.BLACK;
    float outlineSize;
    Paint tilePaint;

    public ColorView(final Context context) {
        super(context);
        setUp();
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public ColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    void setUp() {
        outlineSize = DimenUtils.dpToPx(2);

        tilePaint = new Paint();
        tilePaint.setAntiAlias(true);
        tilePaint.setStyle(Paint.Style.FILL);
        tilePaint.setColor(Color.LTGRAY);
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        startRender();
    }

    @Override
    public void render(Canvas canvas) {
        if (Color.alpha(color) < 255) {
            int outline = Math.round(outlineSize) * 4;
            for (int x = 0; x < canvas.getWidth(); x += outline) {
                for (int y = x % (outline * 2) == 0 ? 0 : outline; y < canvas.getWidth(); y += (outline * 2)) {
                    canvas.drawRect(x, y, x + outline, y + outline, tilePaint);
                }
            }
        }

        canvas.drawColor(color);
    }
}
