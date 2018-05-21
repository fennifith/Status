package com.james.status.utils;

public class AnimatedInteger {

    private int targetValue;
    private int drawnValue;
    private Integer defaultValue;

    private long start;

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
        return nextVal(250);
    }

    public int nextVal(long duration) {
        int difference = (int) ((targetValue - drawnValue) * Math.sqrt((double) (System.currentTimeMillis() - start) / (duration)));
        if (Math.abs(targetValue - drawnValue) > 1 && System.currentTimeMillis() - start < duration)
            return drawnValue + (targetValue < drawnValue ? Math.min(difference, -1) : Math.max(difference, 1));
        else return targetValue;
    }

    public int getTarget() {
        return targetValue;
    }

    public Integer getDefault() {
        return defaultValue != null ? defaultValue : targetValue;
    }

    public boolean isTarget() {
        return drawnValue == targetValue;
    }

    public boolean isDefault() {
        return defaultValue != null && drawnValue == defaultValue;
    }

    public boolean isTargetDefault() {
        return defaultValue != null && targetValue == defaultValue;
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

}
