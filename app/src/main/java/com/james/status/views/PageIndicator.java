/*
 * Copyright 2015 Alexandre Piveteau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.james.status.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.james.status.R;
import com.james.status.utils.StaticUtils;

public class PageIndicator extends View implements ViewPager.OnPageChangeListener {

    private int actualPosition;
    private float offset;
    private int size;
    private ViewPager viewPager;

    private IndicatorEngine engine;

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        engine = new IndicatorEngine();

        engine.onInitEngine(this, context);
        size = 2;
    }

    public int getTotalPages() {
        return size;
    }

    public int getActualPosition() {
        return actualPosition;
    }

    public float getPositionOffset() {
        return offset;
    }

    public void notifyNumberPagesChanged() {
        size = viewPager.getAdapter().getCount();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        engine.onDrawIndicator(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(engine.getMeasuredWidth(widthMeasureSpec, heightMeasureSpec), engine.getMeasuredHeight(widthMeasureSpec, heightMeasureSpec));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        actualPosition = position;
        offset = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * You must call this AFTER setting the Adapter for the ViewPager, or it won't display the right amount of points.
     *
     * @param viewPager
     */
    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
        size = viewPager.getAdapter().getCount();
        invalidate();
    }

    private static class IndicatorEngine {

        private Context context;

        private PageIndicator indicator;

        private Paint selectedPaint;
        private Paint unselectedPaint;

        public int getMeasuredHeight(int widthMeasuredSpec, int heightMeasuredSpec) {
            return (int) StaticUtils.getPixelsFromDp(8);
        }

        public int getMeasuredWidth(int widthMeasuredSpec, int heightMeasuredSpec) {
            return (int) StaticUtils.getPixelsFromDp(8 * (indicator.getTotalPages() * 2 - 1));
        }

        public void onInitEngine(PageIndicator indicator, Context context) {
            this.indicator = indicator;
            this.context = context;

            selectedPaint = new Paint();
            unselectedPaint = new Paint();

            selectedPaint.setColor(ContextCompat.getColor(context, R.color.textColorPrimary));
            unselectedPaint.setColor(ContextCompat.getColor(context, R.color.textColorSecondary));
            selectedPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            unselectedPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        public void onDrawIndicator(Canvas canvas) {
            int height = indicator.getHeight();

            for (int i = 0; i < indicator.getTotalPages(); i++) {
                float radius;
                if (i == indicator.getActualPosition() + 1) {
                    radius = StaticUtils.getPixelsFromDp((int) (4 * (1 - indicator.getPositionOffset())));
                } else if (i == indicator.getActualPosition()) {
                    radius = StaticUtils.getPixelsFromDp((int) (4 * (indicator.getPositionOffset())));
                } else {
                    radius = StaticUtils.getPixelsFromDp(4);
                }
                float x = StaticUtils.getPixelsFromDp(4) + StaticUtils.getPixelsFromDp(16 * i);
                canvas.drawCircle(x, height / 2, radius, unselectedPaint);
            }

            float firstX;
            float secondX;

            firstX = StaticUtils.getPixelsFromDp(4 + indicator.getActualPosition() * 16);

            if (indicator.getPositionOffset() > .5f) {
                firstX += StaticUtils.getPixelsFromDp((int) (16 * (indicator.getPositionOffset() - .5f) * 2));
            }

            secondX = StaticUtils.getPixelsFromDp(4 + indicator.getActualPosition() * 16);

            if (indicator.getPositionOffset() < .5f) {
                secondX += StaticUtils.getPixelsFromDp((int) (16 * indicator.getPositionOffset() * 2));
            } else {
                secondX += StaticUtils.getPixelsFromDp(16);
            }

            canvas.drawCircle(firstX, StaticUtils.getPixelsFromDp(4), StaticUtils.getPixelsFromDp(4), selectedPaint);
            canvas.drawCircle(secondX, StaticUtils.getPixelsFromDp(4), StaticUtils.getPixelsFromDp(4), selectedPaint);
            canvas.drawRect(firstX, 0, secondX, StaticUtils.getPixelsFromDp(8), selectedPaint);
        }
    }
}
