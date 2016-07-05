package com.james.status.views;

import android.content.Context;
import android.util.AttributeSet;

public class RatioImageView extends CustomImageView {

    public RatioImageView(Context context) {
        super(context);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = (int) (getMeasuredWidth() * 0.5625);
        setMeasuredDimension(getMeasuredWidth(), height);
    }
}