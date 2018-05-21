package com.james.status.utils;

import android.graphics.Color;

public class AnimatedColor {

    private int targetValue;
    private int drawnValue;
    private Integer defaultValue;

    public AnimatedColor(int value) {
        targetValue = value;
        drawnValue = value;
    }

    public void setDefault(Integer defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setCurrent(int value) {
        drawnValue = targetValue = value;
    }

    public int val() {
        return drawnValue;
    }

    public int nextVal() {
        return Color.argb(
                next(Color.alpha(drawnValue), Color.alpha(targetValue)),
                next(Color.red(drawnValue), Color.red(targetValue)),
                next(Color.green(drawnValue), Color.green(targetValue)),
                next(Color.blue(drawnValue), Color.blue(targetValue))
        );
    }

    public int getTarget() {
        return targetValue;
    }

    public Integer getDefault() {
        return defaultValue;
    }

    public boolean isTarget() {
        return drawnValue == targetValue;
    }

    public boolean isDefault() {
        return drawnValue == defaultValue;
    }

    public boolean isTargetDefault() {
        return targetValue == defaultValue;
    }

    public void toDefault() {
        if (defaultValue != null)
            to(defaultValue);
    }

    public void to(int value) {
        targetValue = value;
    }

    public void next() {
        next(true);
    }

    public void next(boolean animate) {
        drawnValue = animate ? nextVal() : targetValue;
    }

    private static int next(int drawn, int target) {
        int difference = target - drawn;
        if (Math.abs(difference) > 1)
            return drawn + (target < drawn ? Math.min(difference / 8, -1) : Math.max(difference / 8, 1));
        else return target;
    }

}
