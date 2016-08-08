package com.james.status.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;

import com.james.status.data.IconStyleData;

public class IconStyleImageView extends SquareImageView {

    Thread thread;
    IconStyleData iconStyle;

    public IconStyleImageView(Context context) {
        super(context);
    }

    public IconStyleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconStyleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIconStyle(final IconStyleData iconStyle) {
        if (iconStyle == null) return;

        this.iconStyle = iconStyle;
        if (thread != null && thread.isAlive()) thread.interrupt();

        thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    for (final int resource : iconStyle.resource) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                setImageDrawable(VectorDrawableCompat.create(getContext().getResources(), resource, getContext().getTheme()));
                            }
                        });

                        try {
                            sleep(600);
                        } catch (InterruptedException e) {
                            return;
                        }

                    }
                }
            }
        };
        thread.start();
    }
}