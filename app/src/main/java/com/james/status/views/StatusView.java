package com.james.status.views;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.BatteryManager;
import android.support.annotation.ColorInt;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextClock;

import com.james.status.R;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.StaticUtils;

public class StatusView extends FrameLayout {

    private View status;
    private TextClock clock;
    private CustomImageView battery, signal, wifi, airplane, alarm;
    private ListView notifications;

    @ColorInt
    private int color = 0;

    public StatusView(Context context) {
        super(context);
        setUp();
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp();
    }

    @TargetApi(21)
    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp();
    }

    public void setUp() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_status, null);
        status = v.findViewById(R.id.status);
        status.getLayoutParams().height = StaticUtils.getStatusBarMargin(getContext());

        clock = (TextClock) status.findViewById(R.id.clock);
        battery = (CustomImageView) status.findViewById(R.id.battery);
        signal = (CustomImageView) status.findViewById(R.id.signal);
        wifi = (CustomImageView) status.findViewById(R.id.wifi);
        airplane = (CustomImageView) status.findViewById(R.id.airplane);
        alarm = (CustomImageView) status.findViewById(R.id.alarm);

        battery.setImageDrawable(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_alert));
        signal.setImageDrawable(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_signal_0));
        wifi.setImageDrawable(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_wifi_0));
        airplane.setImageDrawable(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_airplane));
        alarm.setImageDrawable(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_alarm));

        addView(v);

        if (color > 0) setColor(color);
    }

    public void setColor(@ColorInt int color) {
        ValueAnimator animator = ValueAnimator.ofArgb(this.color, color);
        animator.setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (int) valueAnimator.getAnimatedValue();
                if (status != null) status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
            }
        });
        animator.start();

        this.color = color;
    }

    public void setLockscreen(boolean lockscreen) {
        status.getLayoutParams().height = StaticUtils.getStatusBarMargin(getContext()) * (lockscreen ? 3 : 1);

        if (lockscreen) {
            Palette.from(ImageUtils.drawableToBitmap(WallpaperManager.getInstance(getContext()).getFastDrawable())).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    setColor(palette.getDarkVibrantColor(Color.BLACK));
                }
            });
        }
    }

    public void setAirplaneMode(boolean isAirplaneMode) {
        if (isAirplaneMode) {
            signal.transition((Bitmap) null);
            airplane.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_airplane));
        } else {
            airplane.transition((Bitmap) null);
        }
    }

    public void setAlarm(boolean isAlarm) {
        if (alarm != null) alarm.transition(isAlarm ? ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_alarm) : null);
    }

    public void setWifiStrength(int wifiStrength) {
        switch (wifiStrength) {
            case 1:
                wifi.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_wifi_1));
                break;
            case 2:
                wifi.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_wifi_2));
                break;
            case 3:
                wifi.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_wifi_3));
                break;
            case 4:
                wifi.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_wifi_4));
                break;
            default:
                wifi.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_wifi_0));
                break;
        }
    }

    public void setSignalStrength(int signalStrength) {
        if (signal == null) return;

        switch (signalStrength) {
            case 1:
                signal.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_signal_1));
                break;
            case 2:
                signal.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_signal_2));
                break;
            case 3:
                signal.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_signal_3));
                break;
            case 4:
                signal.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_signal_4));
                break;
            default:
                signal.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_signal_0));
                break;
        }
    }

    public void setBattery(int level, int scale, int status) {
        if (battery == null) return;

        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
            if (level / scale < 0.2)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_charging_20));
            else if (level / scale < 0.35)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_charging_30));
            else if (level / scale < 0.5)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_charging_50));
            else if (level / scale < 0.65)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_charging_60));
            else if (level / scale < 0.8)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_charging_80));
            else if (level / scale < 0.95)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_charging_90));
            else
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_charging_full));
        } else {
            if (level / scale < 0.2)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_20));
            else if (level / scale < 0.35)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_30));
            else if (level / scale < 0.5)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_50));
            else if (level / scale < 0.65)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_60));
            else if (level / scale < 0.8)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_80));
            else if (level / scale < 0.95)
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_90));
            else
                battery.transition(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_battery_full));
        }
    }
}
