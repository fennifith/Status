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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.WindowManager;

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

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) drawable = new ColorDrawable(Color.TRANSPARENT);
        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();
        if (drawable instanceof VectorDrawableCompat)
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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
