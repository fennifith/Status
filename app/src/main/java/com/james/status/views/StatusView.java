package com.james.status.views;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.BatteryManager;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

    private boolean isWifiConnected;

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

        battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_alert));
        signal.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_0));
        wifi.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_0));
        airplane.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_airplane));
        alarm.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_alarm));

        status.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        status.animate().alpha(0f).setDuration(150).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        status.animate().alpha(1f).setDuration(150).start();
                        break;
                }
                return false;
            }
        });

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
                    setColor(palette.getDarkVibrantColor(palette.getVibrantColor(Color.BLACK)));
                }
            });
        }
    }

    public void setAirplaneMode(boolean isAirplaneMode) {
        if (isAirplaneMode) {
            signal.transition((Bitmap) null);
            airplane.transition(ContextCompat.getDrawable(getContext(), R.drawable.ic_airplane));
        } else {
            airplane.transition((Bitmap) null);
        }
    }

    public void setAlarm(boolean isAlarm) {
        if (alarm != null) alarm.transition(isAlarm ? ContextCompat.getDrawable(getContext(), R.drawable.ic_alarm) : null);
    }

    public void setWifiConnected(boolean isWifiConnected) {
        if (this.isWifiConnected != isWifiConnected) {
            wifi.transition(isWifiConnected ? ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_0) : null);
            this.isWifiConnected = isWifiConnected;
        }
    }

    public void setWifiStrength(int wifiStrength) {
        switch (wifiStrength) {
            case 1:
                wifi.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_1));
                break;
            case 2:
                wifi.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_2));
                break;
            case 3:
                wifi.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_3));
                break;
            case 4:
                wifi.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_4));
                break;
            default:
                wifi.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_0));
                break;
        }
    }

    public void setSignalStrength(int signalStrength) {
        if (signal == null) return;

        switch (signalStrength) {
            case 1:
                signal.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_1));
                break;
            case 2:
                signal.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_2));
                break;
            case 3:
                signal.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_3));
                break;
            case 4:
                signal.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_4));
                break;
            default:
                signal.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_0));
                break;
        }
    }

    public void setBattery(int level, int status) {
        if (battery == null) return;

        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
            if (level < 20)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_charging_20));
            else if (level < 35)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_charging_30));
            else if (level < 50)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_charging_50));
            else if (level < 65)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_charging_60));
            else if (level < 80)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_charging_80));
            else if (level < 95)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_charging_90));
            else
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_charging_full));
        } else {
            if (level < 20)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_20));
            else if (level < 35)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_30));
            else if (level < 50)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_50));
            else if (level < 65)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_60));
            else if (level < 80)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_80));
            else if (level < 95)
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_90));
            else
                battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_full));
        }
    }
}
