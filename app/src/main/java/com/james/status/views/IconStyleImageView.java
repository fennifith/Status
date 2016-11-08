package com.james.status.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;

import com.james.status.data.IconStyleData;

public class IconStyleImageView extends SquareImageView {

    private IconStyleData iconStyle;
    private int resource;

    private Handler handler;
    private Runnable runnable;

    public IconStyleImageView(Context context) {
        super(context);
        init();
    }

    public IconStyleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconStyleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (iconStyle != null) {
                    Drawable previous = VectorDrawableCompat.create(getContext().getResources(), iconStyle.resource[resource % iconStyle.resource.length], getContext().getTheme());
                    resource++;

                    TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{previous, VectorDrawableCompat.create(getContext().getResources(), iconStyle.resource[resource % iconStyle.resource.length], getContext().getTheme())});
                    setImageDrawable(drawable);
                    drawable.startTransition(150);
                }

                if (handler != null) handler.postDelayed(this, 1500);
            }
        }, 1500);
    }

    public void setIconStyle(IconStyleData iconStyle) {
        if (iconStyle != null && iconStyle.resource.length < 1) return;
        this.iconStyle = iconStyle;
        if (iconStyle != null)
            setImageDrawable(VectorDrawableCompat.create(getContext().getResources(), iconStyle.resource[resource % iconStyle.resource.length], getContext().getTheme()));
    }
}