package com.james.status.utils;

public class AnimatedInteger {

    private int targetValue;
    private int drawnValue;
    private Integer defaultValue;

    public AnimatedInteger(int value) {
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
        int difference = targetValue - drawnValue;
        if (Math.abs(difference) > 1)
            return drawnValue + (targetValue < drawnValue ? Math.min(difference / 8, -1) : Math.max(difference / 8, 1));
        else return targetValue;
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

}
