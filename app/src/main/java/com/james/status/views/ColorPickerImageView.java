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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import androidx.annotation.ColorInt;
import me.jfenn.androidutils.ColorUtils;

public class ColorPickerImageView extends CustomImageView {

    private OnColorChangedListener listener;
    private Bitmap bitmap;
    private float x, y;
    private Paint fillPaint, strokePaint;

    public ColorPickerImageView(Context context) {
        super(context);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5);
        strokePaint.setAntiAlias(true);
    }

    public ColorPickerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5);
        strokePaint.setAntiAlias(true);
    }

    public ColorPickerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5);
        strokePaint.setAntiAlias(true);
    }

    @Override
    public void setImageBitmap(final Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        this.bitmap = bitmap;

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                new Thread() {
                    @Override
                    public void run() {
                        if (bitmap != null) {
                            x = getWidth() / 3;
                            y = getHeight() / 3;

                            int color = bitmap.getPixel(bitmap.getWidth() / 3, bitmap.getHeight() / 3);

                            if (listener != null) listener.onColorChanged(color);

                            fillPaint.setColor(color);
                            strokePaint.setColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);

                            new Handler(Looper.getMainLooper()).post(() -> invalidate());
                        }
                    }
                }.start();

                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.x = event.getX();
        this.y = event.getY();

        new Thread() {
            @Override
            public void run() {
                int[] location = new int[2];
                getLocationOnScreen(location);

                if (bitmap != null) {
                    Rect rect = getDrawable().getBounds();
                    int color;

                    try {
                        color = bitmap.getPixel((int) ((x - rect.left) * bitmap.getWidth()) / getWidth(), (int) ((y - rect.top) * bitmap.getHeight()) / getHeight());
                    } catch (IllegalArgumentException e) {
                        return;
                    }

                    if (listener != null) listener.onColorChanged(color);

                    fillPaint.setColor(color);
                    strokePaint.setColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);

                    new Handler(Looper.getMainLooper()).post(() -> invalidate());
                }
            }
        }.start();

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(x, y, 30, strokePaint);
        canvas.drawCircle(x, y, 30, fillPaint);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    public interface OnColorChangedListener {
        void onColorChanged(@ColorInt int color);
    }
}
