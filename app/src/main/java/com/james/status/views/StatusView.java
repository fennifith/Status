package com.james.status.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
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
import com.james.status.utils.ColorUtils;
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
    private boolean isSystemShowing, isDarkMode, isAirplaneMode, isSignalConnected, isWifiConnected, isFullscreen;
    private ArrayList<StatusBarNotification> notifications;

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

        battery.setImageDrawable(getDrawable(R.drawable.ic_battery_alert));
        signal.setImageDrawable(getDrawable(R.drawable.ic_signal_0));
        wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_0));
        gps.setImageDrawable(getDrawable(R.drawable.ic_gps_fixed));
        bluetooth.setImageDrawable(getDrawable(R.drawable.ic_bluetooth));
        airplane.setImageDrawable(getDrawable(R.drawable.ic_airplane));
        alarm.setImageDrawable(getDrawable(R.drawable.ic_alarm));

        VectorDrawableCompat.create(getResources(), R.drawable.ic_battery_alert, getContext().getTheme());

        Boolean isAmPmEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM);
        String format = isAmPmEnabled == null || isAmPmEnabled ? "h:mm a" : "h:mm";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
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
        this.notifications = notifications;

        if (notificationIconLayout != null) {
            notificationIconLayout.removeAllViewsInLayout();
            for (StatusBarNotification notification : notifications) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.item_icon, null);
                Drawable drawable = getNotificationIcon(notification);
                if (drawable == null) continue;

                CustomImageView icon = (CustomImageView) v.findViewById(R.id.icon);
                icon.setImageDrawable(drawable);
                if (isDarkMode) ImageUtils.setTint(icon, Color.BLACK);
                notificationIconLayout.addView(v);
            }
        }
    }

    public void addNotification(StatusBarNotification notification) {
        if (notifications == null) notifications = new ArrayList<>();
        else {
            for (StatusBarNotification notification2 : notifications) {
                if (notification.getId() == notification2.getId()) return;
            }
        }
        notifications.add(notification);
        setNotifications(notifications);
    }

    public void removeNotification(StatusBarNotification notification) {
        ArrayList<StatusBarNotification> notifications = new ArrayList<>();
        if (this.notifications != null) {
            for (StatusBarNotification notification2 : this.notifications) {
                if (notification.getId() != notification2.getId()) notifications.add(notification2);
            }
        }

        setNotifications(notifications);
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

    public void setSystemShowing(boolean isSystemShowing) {
        if (this.isFullscreen != isSystemShowing || this.isSystemShowing != isSystemShowing) {
            ValueAnimator animator = ValueAnimator.ofFloat(getY(), isSystemShowing ? -StaticUtils.getStatusBarHeight(getContext()) : 0f);
            animator.setDuration(150);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float y = (float) valueAnimator.getAnimatedValue();
                    setY(y);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (StatusView.this.isSystemShowing) setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            animator.start();

            if (!isSystemShowing) setVisibility(View.VISIBLE);
        }

        this.isSystemShowing = isSystemShowing;
    }

    public void setFullscreen(boolean isFullscreen) {
        if (this.isFullscreen != isFullscreen && !isSystemShowing) {
            ValueAnimator animator = ValueAnimator.ofFloat(getY(), isFullscreen ? -StaticUtils.getStatusBarHeight(getContext()) : 0f);
            animator.setDuration(150);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float y = (float) valueAnimator.getAnimatedValue();
                    setY(y);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (StatusView.this.isFullscreen) setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            animator.start();

            if (!isFullscreen) setVisibility(View.VISIBLE);

            this.isFullscreen = isFullscreen;
        }
    }

    public void setColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator animator = ValueAnimator.ofArgb(this.color, color);
            animator.setDuration(150);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int color = (int) valueAnimator.getAnimatedValue();
                    if (status != null)
                        status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                    setDarkMode(!ColorUtils.isColorDark(color));
                }
            });
            animator.start();
        } else {
            if (status != null)
                status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
            setDarkMode(!ColorUtils.isColorDark(color));
        }

        this.color = color;
    }

    public void setDarkMode(boolean isDarkMode) {
        Boolean isDarkModeEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);

        if (this.isDarkMode != isDarkMode && (isDarkModeEnabled == null || isDarkModeEnabled)) {
            int color = isDarkMode ? Color.BLACK : Color.WHITE;

            if (notificationIconLayout != null) {
                for (int i = 0; i < notificationIconLayout.getChildCount(); i++) {
                    View icon = notificationIconLayout.getChildAt(i).findViewById(R.id.icon);
                    if (icon != null && icon instanceof CustomImageView) {
                        ImageUtils.setTint((CustomImageView) icon, color);
                    }
                }
            }

            if (alarm != null)
                ImageUtils.setTint(alarm, color);
            if (airplane != null)
                ImageUtils.setTint(airplane, color);
            if (wifi != null)
                ImageUtils.setTint(wifi, color);
            if (signal != null)
                ImageUtils.setTint(signal, color);
            if (batteryPercent != null)
                batteryPercent.setTextColor(color);
            if (battery != null)
                ImageUtils.setTint(battery, color);
            if (clock != null)
                clock.setTextColor(color);

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
                    setColor(palette.getDarkVibrantColor(ColorUtils.darkColor(palette.getVibrantColor(Color.BLACK))));
                }
            });
        }
    }

    public void setAlarm(boolean isAlarm) {
        if (alarm != null)
            alarm.setVisibility(isAlarm ? View.VISIBLE : View.GONE);
    }

    public void setAirplaneMode(boolean isAirplaneMode) {
        if (this.isAirplaneMode != isAirplaneMode) {
            if (isAirplaneMode) {
                airplane.setVisibility(View.VISIBLE);
                setSignalConnected(false);
            } else {
                airplane.setVisibility(View.GONE);
                setSignalConnected(isSignalConnected);
            }
            this.isAirplaneMode = isAirplaneMode;
        }
    }

    public void setBluetooth(boolean isEnabled, boolean isConnected) {
        bluetooth.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        bluetooth.setImageDrawable(getDrawable(isConnected ? R.drawable.ic_bluetooth_connected : R.drawable.ic_bluetooth));
    }

    public void setGpsEnabled(boolean isEnabled) {
        gps.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
    }

    public void setWifiConnected(boolean isWifiConnected) {
        if (wifi != null && this.isWifiConnected != isWifiConnected) {
            wifi.setVisibility(isWifiConnected ? View.VISIBLE : View.GONE);
            this.isWifiConnected = isWifiConnected;
        }
    }

    public void setWifiStrength(int wifiStrength) {
        if (wifi == null) return;

        switch (wifiStrength) {
            case 1:
                wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_1));
                break;
            case 2:
                wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_2));
                break;
            case 3:
                wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_3));
                break;
            case 4:
                wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_4));
                break;
            default:
                wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_0));
                break;
        }
    }

    public void setSignalConnected(boolean isSignalConnected) {
        if (signal != null && this.isSignalConnected != isSignalConnected) {
            if (!this.isAirplaneMode)
                signal.setVisibility(isSignalConnected ? View.VISIBLE : View.GONE);
            this.isSignalConnected = isSignalConnected;
        }
    }

    public void setSignalStrength(int signalStrength) {
        if (signal == null) return;

        switch (signalStrength) {
            case 1:
                signal.setImageDrawable(getDrawable(R.drawable.ic_signal_1));
                break;
            case 2:
                signal.setImageDrawable(getDrawable(R.drawable.ic_signal_2));
                break;
            case 3:
                signal.setImageDrawable(getDrawable(R.drawable.ic_signal_3));
                break;
            case 4:
                signal.setImageDrawable(getDrawable(R.drawable.ic_signal_4));
                break;
            default:
                signal.setImageDrawable(getDrawable(R.drawable.ic_signal_0));
                break;
        }
    }

    public void setBattery(int level, int status) {
        if (battery == null) return;

        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
            if (level < 20)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_20));
            else if (level < 35)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_30));
            else if (level < 50)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_50));
            else if (level < 65)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_60));
            else if (level < 80)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_80));
            else if (level < 95)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_90));
            else
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_full));
        } else {
            if (level < 20)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_20));
            else if (level < 35)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_30));
            else if (level < 50)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_50));
            else if (level < 65)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_60));
            else if (level < 80)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_80));
            else if (level < 95)
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_90));
            else
                battery.setImageDrawable(getDrawable(R.drawable.ic_battery_full));
        }

        if (batteryPercent != null)
            batteryPercent.setText(String.valueOf(level) + "%");
    }

    private Drawable getDrawable(@DrawableRes int res) {
        return VectorDrawableCompat.create(getResources(), res, getContext().getTheme());
    }
}
