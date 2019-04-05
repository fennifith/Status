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

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

import com.james.status.data.IconStyleData;

public class IconStyleImageView extends SquareImageView {

    private IconStyleData iconStyle;
    private int resource;
    private Drawable drawable;

    private Handler handler;

    public IconStyleImageView(Context context) {
        super(context);
    }

    public IconStyleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconStyleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        if (iconStyle != null && iconStyle.getSize() > 1) {
            handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (iconStyle != null) {
                        resource++;
                        drawable = iconStyle.getDrawable(getContext(), resource % iconStyle.getSize());

                        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f, 1f);
                        animator.setDuration(500);
                        animator.addUpdateListener(animator1 -> {
                            float alpha = (float) animator1.getAnimatedValue();
                            setAlpha(alpha);
                            if (alpha == 0)
                                setImageDrawable(drawable, Color.BLACK);
                        });
                        animator.start();
                    }

                    if (handler != null) handler.postDelayed(this, 1500);
                }
            }, 1500);
        }
    }

    public void setIconStyle(IconStyleData iconStyle) {
        if (iconStyle != null && iconStyle.getSize() < 1) return;
        this.iconStyle = iconStyle;
        if (iconStyle != null) {
            Drawable drawable = iconStyle.getDrawable(getContext(), resource % iconStyle.getSize());
            setImageDrawable(drawable, Color.BLACK);
            init();
        }
    }
}