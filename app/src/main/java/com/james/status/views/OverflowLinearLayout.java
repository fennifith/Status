package com.james.status.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.james.status.activities.MainActivity;
import com.james.status.data.PreferenceData;

public class OverflowLinearLayout extends LinearLayout {

    public OverflowLinearLayout(Context context) {
        this(context, null);
    }

    public OverflowLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverflowLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int width = getWidth();
        if (width > 0) {
            setUpOverflow(width);
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = getWidth();
                    if (width > 0) {
                        setUpOverflow(width);
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    private void setUpOverflow(int width) {
        if (PreferenceData.STATUS_PREVENT_ICON_OVERLAP.getBooleanValue(OverflowLinearLayout.this.getContext()))
            onViewsChanged();
        else {
            Intent intent = new Intent(MainActivity.ACTION_TOO_MANY_ICONS);
            intent.setPackage(getContext().getPackageName());
            intent.putExtra(MainActivity.EXTRA_MANY_ICONS, changeViews(this, getPaddingLeft() + getPaddingRight(), width, true) >= width);
            getContext().sendBroadcast(intent);
        }
    }

    public void onViewsChanged() {
        int totalWidth = getWidth();
        if (totalWidth > 0)
            changeViews(this, getPaddingLeft() + getPaddingRight(), totalWidth, false);
    }

    public int changeViews(ViewGroup viewGroup, int width, int totalWidth, boolean measureOnly) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                width += child.getPaddingLeft() + child.getPaddingRight();
                if (width >= totalWidth) {
                    if (!measureOnly)
                        child.setAlpha(0);
                } else {
                    if (!measureOnly)
                        child.setAlpha(1);
                    width = changeViews((ViewGroup) child, width, totalWidth, measureOnly);
                }
            } else {
                if (width >= totalWidth) {
                    if (!measureOnly)
                        child.setAlpha(0);
                } else {
                    width += child.getMeasuredWidth();
                    if (!measureOnly)
                        child.setAlpha(width >= totalWidth ? 0 : 1);
                }
            }
        }

        return width;
    }
}
