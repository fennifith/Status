package com.james.status.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;

import com.james.status.utils.ColorUtils;
import com.james.status.utils.StaticUtils;

public class ColorImageView extends CustomImageView {

    @ColorInt
    int color = Color.BLACK;
    Paint paint, outlinePaint;

    public ColorImageView(final Context context) {
        super(context);
        setUp();
    }

    public ColorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public ColorImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    private void setUp() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        outlinePaint = new Paint();
        outlinePaint.setAntiAlias(true);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(StaticUtils.getPixelsFromDp(2));
        outlinePaint.setColor(Color.BLACK);
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        paint.setColor(color);
        outlinePaint.setColor(ColorUtils.isColorDark(color) ? Color.TRANSPARENT : Color.BLACK);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        int size = Math.min(getWidth(), getHeight());
        int outline = (int) StaticUtils.getPixelsFromDp(2);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (size / 2) - outline, paint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (size / 2) - outline, outlinePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredWidth();
        setMeasuredDimension(size, size);
    }

}
