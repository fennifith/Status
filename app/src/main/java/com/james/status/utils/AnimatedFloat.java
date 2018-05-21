package com.james.status.utils;

public class AnimatedFloat {

    private float targetValue;
    private float drawnValue;
    private Float defaultValue;

    private long start;

    public AnimatedFloat(float value) {
        targetValue = value;
        drawnValue = value;
    }

    public void setDefault(Float defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setCurrent(int value) {
        drawnValue = targetValue = value;
    }

    public float val() {
        return drawnValue;
    }

    public float nextVal() {
        float difference = targetValue - drawnValue;
        if (Math.abs(difference) > .1f)
            return drawnValue + (targetValue < drawnValue ? Math.min(difference / 8, -.1f) : Math.max(difference / 8, .1f));
        else return targetValue;
    }

    public float nextVal(long duration) {
        float difference = (targetValue - drawnValue) * (float) Math.sqrt((double) (System.currentTimeMillis() - start) / (duration));
        if (Math.abs(targetValue - drawnValue) > .1f)
            return drawnValue + (targetValue < drawnValue ? Math.min(difference, -.1f) : Math.max(difference, .1f));
        else return targetValue;
    }

    public float getTarget() {
        return targetValue;
    }

    public Float getDefault() {
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

    public void to(float value) {
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
