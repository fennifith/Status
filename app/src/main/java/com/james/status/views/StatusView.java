package com.james.status.views;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;

public class StatusView extends FrameLayout {

    private View status;
    private TextClock clock;
    private TextView batteryPercent;
    private CustomImageView alarm, airplane, bluetooth, gps, wifi, signal, battery;
    private LinearLayout notificationIconLayout;

    @ColorInt
    private int color = 0;
    private boolean isDarkMode, isWifiConnected;

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
        status.getLayoutParams().height = StaticUtils.getStatusBarHeight(getContext());

        clock = (TextClock) v.findViewById(R.id.clock);
        battery = (CustomImageView) v.findViewById(R.id.battery);
        batteryPercent = (TextView) v.findViewById(R.id.batteryPercent);
        signal = (CustomImageView) v.findViewById(R.id.signal);
        wifi = (CustomImageView) v.findViewById(R.id.wifi);
        gps = (CustomImageView) v.findViewById(R.id.gps);
        bluetooth = (CustomImageView) v.findViewById(R.id.bluetooth);
        airplane = (CustomImageView) v.findViewById(R.id.airplane);
        alarm = (CustomImageView) v.findViewById(R.id.alarm);

        notificationIconLayout = (LinearLayout) v.findViewById(R.id.notificationIcons);

        battery.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_battery_alert));
        signal.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_signal_0));
        wifi.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_wifi_0));
        gps.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_gps_fixed));
        bluetooth.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_bluetooth));
        airplane.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_airplane));
        alarm.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_alarm));

        Boolean isAmPmEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM);
        String format = isAmPmEnabled == null || isAmPmEnabled ? "h:mm a" : "h:mm";
        clock.setFormat12Hour(format);

        Boolean isBatteryPercent = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_BATTERY_PERCENT);
        if (isBatteryPercent != null && isBatteryPercent)
            batteryPercent.setVisibility(View.VISIBLE);

        addView(v);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto != null && !isStatusColorAuto) {
            Integer statusBarColor = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (statusBarColor != null) setColor(statusBarColor);
        } else if (color > 0) setColor(color);
    }

    public void setNotifications(ArrayList<StatusBarNotification> notifications) {
        if (notificationIconLayout != null) {
            notificationIconLayout.removeAllViewsInLayout();
            for (StatusBarNotification notification : notifications) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.item_icon, null);
                Drawable drawable = getNotificationIcon(notification);
                if (drawable == null) continue;

                CustomImageView icon = (CustomImageView) v.findViewById(R.id.icon);
                icon.setImageDrawable(drawable);
                if (isDarkMode) icon.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                notificationIconLayout.addView(v);
            }
        }
    }

    @Nullable
    private Drawable getNotificationIcon(StatusBarNotification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Resources resources = null;
            PackageInfo packageInfo = null;

            try {
                resources = getContext().getPackageManager().getResourcesForApplication(notification.getPackageName());
                packageInfo = getContext().getPackageManager().getPackageInfo(notification.getPackageName(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            if (resources != null && packageInfo != null) {
                Resources.Theme theme = resources.newTheme();
                theme.applyStyle(packageInfo.applicationInfo.theme, false);

                Drawable drawable = null;
                try {
                    drawable = ResourcesCompat.getDrawable(resources, notification.getNotification().icon, theme);
                } catch (Resources.NotFoundException ignored) {
                }

                return drawable;
            }

        } else
            return notification.getNotification().getSmallIcon().loadDrawable(getContext());

        return null;
    }

    public void setFullscreen(boolean isFullscreen) {
        setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
    }

    public void setColor(@ColorInt int color) {
        ValueAnimator animator = ValueAnimator.ofArgb(this.color, color);
        animator.setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (int) valueAnimator.getAnimatedValue();
                if (status != null) status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                setDarkMode(!ImageUtils.isColorDark(color));
            }
        });
        animator.start();

        this.color = color;
    }

    public void setDarkMode(boolean isDarkMode) {
        Boolean isDarkModeEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);

        if (this.isDarkMode != isDarkMode && (isDarkModeEnabled == null || isDarkModeEnabled)) {
            if (notificationIconLayout != null) {
                for (int i = 0; i < notificationIconLayout.getChildCount(); i++) {
                    View icon = notificationIconLayout.getChildAt(i).findViewById(R.id.icon);
                    if (icon != null && icon instanceof CustomImageView) {
                        ((CustomImageView) icon).setImageTintList(ColorStateList.valueOf(isDarkMode ? Color.BLACK : Color.WHITE));
                    }
                }
            }

            if (alarm != null)
                alarm.setImageTintList(ColorStateList.valueOf(isDarkMode ? Color.BLACK : Color.WHITE));
            if (airplane != null)
                airplane.setImageTintList(ColorStateList.valueOf(isDarkMode ? Color.BLACK : Color.WHITE));
            if (wifi != null)
                wifi.setImageTintList(ColorStateList.valueOf(isDarkMode ? Color.BLACK : Color.WHITE));
            if (signal != null)
                signal.setImageTintList(ColorStateList.valueOf(isDarkMode ? Color.BLACK : Color.WHITE));
            if (batteryPercent != null)
                batteryPercent.setTextColor(isDarkMode ? Color.BLACK : Color.WHITE);
            if (battery != null)
                battery.setImageTintList(ColorStateList.valueOf(isDarkMode ? Color.BLACK : Color.WHITE));
            if (clock != null)
                clock.setTextColor(isDarkMode ? Color.BLACK : Color.WHITE);

            this.isDarkMode = isDarkMode;
        }
    }

    public void setLockscreen(boolean lockscreen) {
        Boolean expand = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_LOCKSCREEN_EXPAND);
        if (expand != null && expand)
            status.getLayoutParams().height = StaticUtils.getStatusBarHeight(getContext()) * (lockscreen ? 3 : 1);

        if (lockscreen) {
            Palette.from(ImageUtils.drawableToBitmap(WallpaperManager.getInstance(getContext()).getFastDrawable())).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    setColor(palette.getDarkVibrantColor(ImageUtils.darkColor(palette.getVibrantColor(Color.BLACK))));
                }
            });
        }
    }

    public void setAlarm(boolean isAlarm) {
        if (alarm != null)
            alarm.setVisibility(isAlarm ? View.VISIBLE : View.GONE);
    }

    public void setAirplaneMode(boolean isAirplaneMode) {
        if (isAirplaneMode) {
            signal.setVisibility(View.GONE);
            wifi.setVisibility(View.GONE);
            airplane.setVisibility(View.VISIBLE);
        } else {
            signal.setVisibility(View.VISIBLE);
            wifi.setVisibility(View.VISIBLE);
            airplane.setVisibility(View.GONE);
        }
    }

    public void setBluetooth(boolean isEnabled, boolean isConnected) {
        bluetooth.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        bluetooth.setImageDrawable(ContextCompat.getDrawable(getContext(), isConnected ? R.drawable.ic_bluetooth_connected : R.drawable.ic_bluetooth));
    }

    public void setGpsEnabled(boolean isEnabled) {
        gps.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
    }

    public void setWifiConnected(boolean isWifiConnected) {
        if (this.isWifiConnected != isWifiConnected) {
            if (!isWifiConnected) wifi.setVisibility(View.GONE);
            else wifi.setVisibility(View.VISIBLE);
            this.isWifiConnected = isWifiConnected;
        }
    }

    public void setWifiStrength(int wifiStrength) {
        if (wifi == null) return;

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

        if (batteryPercent != null)
            batteryPercent.setText(String.valueOf(level) + "%");
    }
}
