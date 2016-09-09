package com.james.status.utils;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.annotation.ColorInt;

public class ColorAnimator {

    private ValueAnimator animator;
    private ColorUpdateListener listener;

    @ColorInt
    private int startColor, endColor;

    private int startRed, startGreen, startBlue, endRed, endGreen, endBlue;

    public ColorAnimator(@ColorInt int startColor, @ColorInt int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;

        startRed = Color.red(startColor);
        startGreen = Color.green(startColor);
        startBlue = Color.blue(startColor);

        endRed = Color.red(endColor);
        endGreen = Color.green(endColor);
        endBlue = Color.blue(endColor);

        animator = ValueAnimator.ofFloat(0, 1);
    }

    public ColorAnimator setDuration(int duration) {
        if (duration < 1) throw new IllegalArgumentException();

        if (animator != null) animator.setDuration(duration);
        else throw new IllegalStateException();
        return this;
    }

    public ColorAnimator setInterpolator(TimeInterpolator interpolator) {
        if (animator != null) animator.setInterpolator(interpolator);
        else throw new IllegalStateException();
        return this;
    }

    public ColorAnimator setColorUpdateListener(ColorUpdateListener listener) {
        if (animator != null) {
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    int red = startRed + (int) ((endRed - startRed) * value);
                    int green = startGreen + (int) ((endGreen - startGreen) * value);
                    int blue = startBlue + (int) ((endBlue - startBlue) * value);

                    if (getColorUpdateListener() != null)
                        getColorUpdateListener().onColorUpdate(ColorAnimator.this, Color.argb(255, red, green, blue));
                }
            });

            this.listener = listener;
        } else throw new IllegalStateException();

        return this;
    }

    public ColorUpdateListener getColorUpdateListener() {
        return listener;
    }

    public float getAnimatedFraction() {
        if (animator != null) return animator.getAnimatedFraction();
        else throw new IllegalStateException();
    }

    public ColorAnimator setAnimatorListener(Animator.AnimatorListener listener) {
        if (animator != null) animator.addListener(listener);
        else throw new IllegalStateException();
        return this;
    }

    public void start() {
        if (animator != null) animator.start();
        else throw new IllegalStateException();
    }

    public void cancel() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    public boolean isCancelled() {
        return animator == null;
    }

    public interface ColorUpdateListener {
        void onColorUpdate(ColorAnimator animator, @ColorInt int color);
    }
}
