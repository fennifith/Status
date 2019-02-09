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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import com.james.status.utils.tasks.CanvasRenderTask;

import androidx.annotation.Nullable;

public abstract class RenderableView extends View implements CanvasRenderTask.Renderable {

    private Paint paint;
    private Bitmap render;
    private AsyncTask task;

    private int width, height;

    public RenderableView(Context context) {
        super(context);
        init();
    }

    public RenderableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RenderableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public RenderableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
    }

    public void startRender() {
        if (task != null)
            task.cancel(true);

        task = new CanvasRenderTask(this).execute(width, height);
    }

    @Override
    public void onRendered(@Nullable Bitmap bitmap) {
        if (render != null && render != bitmap)
            render.recycle();

        task = null;
        render = bitmap;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        width = canvas.getWidth();
        height = canvas.getHeight();

        if (render != null)
            canvas.drawBitmap(render, 0, 0, paint);
        else if (task == null)
            startRender();
    }
}
