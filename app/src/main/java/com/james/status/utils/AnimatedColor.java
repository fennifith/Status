package com.james.status.utils;

import android.graphics.Color;

public class AnimatedColor {

    private AnimatedInteger redValue, blueValue, greenValue;

    public AnimatedColor(int value) {
        redValue = new AnimatedInteger(Color.red(value));
        greenValue = new AnimatedInteger(Color.green(value));
        blueValue = new AnimatedInteger(Color.blue(value));
    }

    public void setDefault(Integer defaultValue) {
        redValue.setDefault(Color.red(defaultValue));
        greenValue.setDefault(Color.green(defaultValue));
        blueValue.setDefault(Color.blue(defaultValue));
    }

    public void setCurrent(int value) {
        redValue.setCurrent(Color.red(value));
        greenValue.setCurrent(Color.green(value));
        blueValue.setCurrent(Color.blue(value));
    }

    public int val() {
        return Color.rgb(
                redValue.val(),
                greenValue.val(),
                blueValue.val()
        );
    }

    public int nextVal() {
        return nextVal(AnimatedValue.DEFAULT_ANIMATION_DURATION);
    }

    public int nextVal(long duration) {
        return Color.rgb(
                redValue.nextVal(duration),
                greenValue.nextVal(duration),
                blueValue.nextVal(duration)
        );
    }

    public int getTarget() {
        return Color.rgb(
                redValue.getTarget(),
                greenValue.getTarget(),
                blueValue.getTarget()
        );
    }

    public Integer getDefault() {
        return Color.rgb(
                redValue.getDefault(),
                greenValue.getDefault(),
                blueValue.getDefault()
        );
    }

    public boolean isTarget() {
        return redValue.isTarget() && greenValue.isTarget() && blueValue.isTarget();
    }

    public boolean isDefault() {
        return redValue.isDefault() && greenValue.isDefault() && blueValue.isDefault();
    }

    public boolean isTargetDefault() {
        return redValue.isTargetDefault() && greenValue.isTargetDefault() && blueValue.isTargetDefault();
    }

    public void toDefault() {
        redValue.toDefault();
        greenValue.toDefault();
        blueValue.toDefault();
    }

    public void to(int value) {
        redValue.to(Color.red(value));
        greenValue.to(Color.green(value));
        blueValue.to(Color.blue(value));
    }

    public void next(boolean animate) {
        next(animate, AnimatedValue.DEFAULT_ANIMATION_DURATION);
    }

    public void next(boolean animate, long duration) {
        redValue.next(animate, duration);
        greenValue.next(animate, duration);
        blueValue.next(animate, duration);
    }

}
