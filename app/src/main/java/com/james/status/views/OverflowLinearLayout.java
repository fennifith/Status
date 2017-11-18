package com.james.status.views;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

public class OverflowLinearLayout extends LinearLayout {

    private boolean isOverflow;
    private ImageView overflow;

    public OverflowLinearLayout(Context context) {
        this(context, null);
    }

    public OverflowLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverflowLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        overflow = new ImageView(context);
        overflow.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_more, context.getTheme()));
        int size = getHeight();
        if (size > 0) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size / 2, size);
            overflow.setLayoutParams(layoutParams);
            overflow.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            overflow.setPadding(0, size / 8, 0, size / 8);
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int size = getHeight();
                    if (size > 0) {
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size / 2, size);
                        overflow.setLayoutParams(layoutParams);
                        overflow.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        overflow.setPadding(0, size / 8, 0, size / 8);

                        Boolean preventOverlap = PreferenceUtils.getBooleanPreference(OverflowLinearLayout.this.getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_PREVENT_ICON_OVERLAP);
                        if (preventOverlap != null && preventOverlap)
                            onViewsChanged();
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    public void setColor(@ColorInt int color) {
        overflow.setColorFilter(color);
    }

    @Override
    public View getChildAt(int index) {
        if (isOverflow)
            return super.getChildAt(index + 1);
        else return super.getChildAt(index);
    }

    @Override
    public int getChildCount() {
        return super.getChildCount() - (isOverflow ? 1 : 0);
    }

    @Override
    public void removeViewAt(int index) {
        if (isOverflow)
            super.removeViewAt(index + 1);
        else super.removeViewAt(index);
    }

    @Override
    public void removeAllViewsInLayout() {
        super.removeAllViewsInLayout();
        isOverflow = false;
    }

    public void onViewsChanged() {
        int totalWidth = getWidth();
        if (totalWidth > 0 && overflow.getLayoutParams() != null) {
            int width = changeViews(this, getPaddingLeft() + getPaddingRight(), totalWidth);

            boolean isOverflow = width >= totalWidth;
            if (isOverflow != this.isOverflow) {
                this.isOverflow = isOverflow;
                if (isOverflow)
                    super.addView(overflow, 0, overflow.getLayoutParams());
                else super.removeView(overflow);
            }
        }
    }

    public int changeViews(ViewGroup viewGroup, int width, int totalWidth) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                width += child.getPaddingLeft() + child.getPaddingRight();
                if (width >= totalWidth) {
                    child.setAlpha(0);
                } else {
                    child.setAlpha(1);
                    width = changeViews((ViewGroup) child, width, totalWidth);
                }
            } else {
                if (width >= totalWidth) {
                    child.setAlpha(0);
                } else {
                    width += child.getMeasuredWidth();
                    child.setAlpha(width >= totalWidth ? 0 : 1);
                }
            }
        }

        return width;
    }
}
