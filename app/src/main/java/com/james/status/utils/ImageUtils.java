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

package com.james.status.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class ImageUtils {

    public static Drawable getVectorDrawable(Context context, int resId) {
        VectorDrawableCompat drawable;
        try {
            drawable = VectorDrawableCompat.create(context.getResources(), resId, context.getTheme());
        } catch (Exception e) {
            e.printStackTrace();
            return new ColorDrawable(Color.TRANSPARENT);
        }

        if (drawable != null) {
            Drawable icon = drawable.getCurrent();
            DrawableCompat.setTint(icon, Color.WHITE);
            return icon;
        } else {
            Log.wtf(context.getClass().getName(), "Can't get a vector drawable.");
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    public static Bitmap blurBitmap(Bitmap bitmap) {
        Paint paint = new Paint();
        paint.setAlpha(180);

        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        int blurRadius = Math.max(bitmap.getWidth(), bitmap.getHeight()) / 10;
        for (int row = -blurRadius; row < blurRadius; row += 2) {
            for (int column = -blurRadius; column < blurRadius; column += 2) {
                if (column * column + row * row <= blurRadius * blurRadius) {
                    paint.setAlpha((blurRadius * blurRadius) / ((column * column + row * row) + 1) * 2);
                    canvas.drawBitmap(bitmap, row, column, paint);
                }
            }
        }

        return resultBitmap;
    }

    @Nullable
    public static Bitmap cropBitmapToBar(Context context, Bitmap source) {
        if (context == null || source == null) return null;

        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);

        try {
            return Bitmap.createBitmap(source, (source.getWidth() - size.x) / 2, 0, size.x, StaticUtils.getStatusBarHeight(context));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap tintBitmap(Bitmap source, @ColorInt int color) {
        Bitmap bitmap = Bitmap.createBitmap(source);

        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        new Canvas(bitmap).drawBitmap(source, new Matrix(), paint);

        return bitmap;
    }
}
