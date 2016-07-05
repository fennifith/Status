package com.james.status.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.BatteryManager;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.Toast;

import com.james.status.R;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.StaticUtils;

public class StatusView extends FrameLayout {

    TextClock clock;
    CustomImageView battery, signal, wifi, airplane, alarm;
    ListView notifications;

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
        View status = LayoutInflater.from(getContext()).inflate(R.layout.layout_status, null);

        clock = (TextClock) status.findViewById(R.id.clock);
        battery = (CustomImageView) status.findViewById(R.id.battery);
        signal = (CustomImageView) status.findViewById(R.id.signal);
        wifi = (CustomImageView) status.findViewById(R.id.wifi);
        airplane = (CustomImageView) status.findViewById(R.id.airplane);
        alarm = (CustomImageView) status.findViewById(R.id.alarm);

        addView(status);
    }

    public void setAlarm(boolean isAlarm) {
        alarm.transition(isAlarm ? ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_alarm) : null);
    }

    public void setBattery(int level, int scale, int status) {
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
