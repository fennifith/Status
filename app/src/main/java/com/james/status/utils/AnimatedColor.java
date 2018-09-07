package com.james.status.utils;

import android.graphics.Color;

public class AnimatedColor {

    private AnimatedInteger redValue, blueValue, greenValue, alphaValue;

    public AnimatedColor(int value) {
        redValue = new AnimatedInteger(Color.red(value));
        greenValue = new AnimatedInteger(Color.green(value));
        blueValue = new AnimatedInteger(Color.blue(value));
        alphaValue = new AnimatedInteger(Color.alpha(value));
    }

    public void setDefault(Integer defaultValue) {
        redValue.setDefault(Color.red(defaultValue));
        greenValue.setDefault(Color.green(defaultValue));
        blueValue.setDefault(Color.blue(defaultValue));
        alphaValue.setDefault(Color.alpha(defaultValue));
    }

    public void setCurrent(int value) {
        redValue.setCurrent(Color.red(value));
        greenValue.setCurrent(Color.green(value));
        blueValue.setCurrent(Color.blue(value));
        alphaValue.setCurrent(Color.alpha(value));
    }

    public int val() {
        return Color.argb(
                alphaValue.val(),
                redValue.val(),
                greenValue.val(),
                blueValue.val()
        );
    }

    public int nextVal() {
        return nextVal(AnimatedValue.DEFAULT_ANIMATION_DURATION);
    }

    public int nextVal(long duration) {
        return Color.argb(
                alphaValue.nextVal(duration),
                redValue.nextVal(duration),
                greenValue.nextVal(duration),
                blueValue.nextVal(duration)
        );
    }

    public int getTarget() {
        return Color.argb(
                alphaValue.getTarget(),
                redValue.getTarget(),
                greenValue.getTarget(),
                blueValue.getTarget()
        );
    }

    public Integer getDefault() {
        return Color.argb(
                alphaValue.getDefault(),
                redValue.getDefault(),
                greenValue.getDefault(),
                blueValue.getDefault()
        );
    }

    public boolean isTarget() {
        return alphaValue.isTarget() && redValue.isTarget() && greenValue.isTarget() && blueValue.isTarget();
    }

    public boolean isDefault() {
        return alphaValue.isDefault() && redValue.isDefault() && greenValue.isDefault() && blueValue.isDefault();
    }

    public boolean isTargetDefault() {
        return alphaValue.isTargetDefault() && redValue.isTargetDefault() && greenValue.isTargetDefault() && blueValue.isTargetDefault();
    }

    public void toDefault() {
        alphaValue.toDefault();
        redValue.toDefault();
        greenValue.toDefault();
        blueValue.toDefault();
    }

    public void to(int value) {
        alphaValue.to(Color.alpha(value));
        redValue.to(Color.red(value));
        greenValue.to(Color.green(value));
        blueValue.to(Color.blue(value));
    }

    public void next(boolean animate) {
        next(animate, AnimatedValue.DEFAULT_ANIMATION_DURATION);
    }

    public void next(boolean animate, long duration) {
        alphaValue.next(animate, duration);
        redValue.next(animate, duration);
        greenValue.next(animate, duration);
        blueValue.next(animate, duration);
    }

}
