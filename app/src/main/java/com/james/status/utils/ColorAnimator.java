package com.james.status.utils;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.annotation.ColorInt;

public class ColorAnimator {

    private ValueAnimator animator;
    private ColorUpdateListener listener;

    @ColorInt
    private int startColor, endColor;

    private int red, blue, green;

    public ColorAnimator(@ColorInt int startColor, @ColorInt int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;

        red = Color.red(startColor);
        blue = Color.blue(startColor);
        green = Color.green(startColor);

        animator = ValueAnimator.ofFloat(0, 1);
    }

    public ColorAnimator setDuration(int duration) {
        if (duration < 1) throw new IllegalArgumentException();

        if (animator != null) animator.setDuration(duration);
        else throw new IllegalStateException();
        return this;
    }

    public ColorAnimator setColorUpdateListener(ColorUpdateListener listener) {
        this.listener = listener;
        return this;
    }

    public void start() {
        if (animator != null) animator.start();
        else throw new IllegalStateException();
    }

    public void cancel() {
        if (animator != null) animator.cancel();
    }

    public interface ColorUpdateListener {
        void onColorUpdate(ColorAnimator animator, @ColorInt int color);
    }
}
