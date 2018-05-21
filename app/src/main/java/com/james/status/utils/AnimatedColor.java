package com.james.status.utils;

import android.graphics.Color;

public class AnimatedColor {

    private int targetValue;
    private int drawnValue;
    private Integer defaultValue;

    private long start;

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
        return nextVal(250);
    }

    public int nextVal(long duration) {
        return Color.argb(
                next(Color.alpha(drawnValue), Color.alpha(targetValue), duration),
                next(Color.red(drawnValue), Color.red(targetValue), duration),
                next(Color.green(drawnValue), Color.green(targetValue), duration),
                next(Color.blue(drawnValue), Color.blue(targetValue), duration)
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
        start = System.currentTimeMillis();
    }

    public void next(boolean animate) {
        next(animate, 250);
    }

    public void next(boolean animate, long duration) {
        drawnValue = animate ? nextVal(duration) : targetValue;
    }

    private int next(int drawn, int target, long duration) {
        int difference = (int) ((target - drawn) * Math.sqrt((float) (System.currentTimeMillis() - start) / (duration)));
        if (Math.abs(target - drawn) > 1)
            return drawn + (target < drawn ? Math.min(difference, -1) : Math.max(difference, 1));
        else return target;
    }

}
