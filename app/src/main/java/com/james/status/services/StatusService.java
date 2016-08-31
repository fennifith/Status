package com.james.status.services;

import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.ActionData;
import com.james.status.data.NotificationData;
import com.james.status.data.icon.AirplaneModeIconData;
import com.james.status.data.icon.AlarmIconData;
import com.james.status.data.icon.BatteryIconData;
import com.james.status.data.icon.BluetoothIconData;
import com.james.status.data.icon.DataIconData;
import com.james.status.data.icon.DualNetworkIconData;
import com.james.status.data.icon.IconData;
import com.james.status.data.icon.NetworkIconData;
import com.james.status.data.icon.RingerIconData;
import com.james.status.data.icon.TimeIconData;
import com.james.status.data.icon.WifiIconData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CustomImageView;
import com.james.status.views.StatusView;

import java.util.ArrayList;
import java.util.List;

public class StatusService extends Service {

    public static final String
            ACTION_START = "com.james.status.ACTION_START",
            ACTION_STOP = "com.james.status.ACTION_STOP",
            ACTION_UPDATE = "com.james.status.ACTION_UPDATE",
            ACTION_NOTIFICATION_ADDED = "com.james.status.ACTION_NOTIFICATION_ADDED",
            ACTION_NOTIFICATION_REMOVED = "com.james.status.ACTION_NOTIFICATION_REMOVED",
            EXTRA_NOTIFICATION = "com.james.status.EXTRA_NOTIFICATION",
            EXTRA_COLOR = "com.james.status.EXTRA_COLOR",
            EXTRA_IS_SYSTEM_FULLSCREEN = "com.james.status.EXTRA_IS_SYSTEM_FULLSCREEN",
            EXTRA_IS_FULLSCREEN = "com.james.status.EXTRA_IS_FULLSCREEN",
            EXTRA_IS_HOME_SCREEN = "com.james.status.EXTRA_IS_HOME_SCREEN";

    private static final int HEADS_UP_DURATION = 11000;

    private StatusView statusView;
    private View fullscreenView;
    private View headsUpView;

    private KeyguardManager keyguardManager;
    private WindowManager windowManager;

    private Handler headsUpHandler;
    private Runnable headsUpRunnable, headsUpDisabledRunnable;
    private NotificationData headsUpNotification;

    private boolean shouldFireClickEvent = true;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        headsUpHandler = new Handler();
        headsUpRunnable = new Runnable() {
            @Override
            public void run() {
                if (headsUpView != null && headsUpView.getParent() != null) removeHeadsUpView();
            }
        };

        headsUpDisabledRunnable = new Runnable() {
            @Override
            public void run() {
                if (statusView != null) {
                    statusView.setSystemShowing(false);
                    statusView.setFullscreen(isFullscreen());
                    headsUpNotification = null;
                }
            }
        };

        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled == null || !enabled) stopSelf();
        else setUp();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        String action = intent.getAction();
        if (action == null) return START_STICKY;
        switch (action) {
            case ACTION_START:
                setUp();
                break;
            case ACTION_STOP:
                windowManager.removeView(statusView);
                statusView = null;
                stopSelf();
                break;
            case ACTION_UPDATE:
                if (statusView != null) {
                    statusView.setLockscreen(keyguardManager.isKeyguardLocked());

                    if (intent.hasExtra(EXTRA_IS_HOME_SCREEN) && intent.getBooleanExtra(EXTRA_IS_HOME_SCREEN, false))
                        statusView.setHomeScreen();
                    else if (intent.hasExtra(EXTRA_COLOR) && headsUpView == null)
                        statusView.setColor(intent.getIntExtra(EXTRA_COLOR, Color.BLACK));

                    statusView.setSystemShowing(intent.getBooleanExtra(EXTRA_IS_SYSTEM_FULLSCREEN, statusView.isSystemShowing()));
                    statusView.setFullscreen(intent.getBooleanExtra(EXTRA_IS_FULLSCREEN, isFullscreen()));
                }
                break;
            case ACTION_NOTIFICATION_ADDED:
                NotificationData notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);

                if (!statusView.containsNotification(notification) && notification.shouldShowHeadsUp(this) && headsUpNotification == null) {
                    showHeadsUp(notification);
                    headsUpNotification = notification;
                } else if (notification.shouldHideStatusBar()) {
                    statusView.setSystemShowing(true);
                    headsUpNotification = notification;

                    headsUpHandler.postDelayed(headsUpDisabledRunnable, HEADS_UP_DURATION);
                }

                statusView.addNotification(notification);
                break;
            case ACTION_NOTIFICATION_REMOVED:
                notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
                statusView.removeNotification(notification);

                if (headsUpNotification != null && headsUpNotification.equals(notification)) {
                    if (headsUpView != null && headsUpView.getParent() != null) removeHeadsUpView();
                    else {
                        statusView.setSystemShowing(false);
                        statusView.setFullscreen(isFullscreen());
                        headsUpHandler.removeCallbacks(headsUpDisabledRunnable);
                        headsUpNotification = null;
                    }
                }
                break;
        }
        return START_STICKY;
    }


    public void setUp() {
        if (statusView == null || statusView.getParent() == null) {
            if (statusView != null) windowManager.removeView(statusView);
            statusView = new StatusView(this);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP;

            windowManager.addView(statusView, params);
        }

        statusView.setUp();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent intent = new Intent(NotificationService.ACTION_GET_NOTIFICATIONS);
            intent.setClass(this, NotificationService.class);
            startService(intent);
        }

        if (fullscreenView == null || fullscreenView.getParent() == null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSPARENT);
            params.gravity = Gravity.START | Gravity.TOP;
            fullscreenView = new View(this);

            windowManager.addView(fullscreenView, params);

            fullscreenView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (statusView != null && fullscreenView != null) {
                        Point size = new Point();
                        windowManager.getDefaultDisplay().getSize(size);
                        statusView.setFullscreen(fullscreenView.getMeasuredHeight() == size.y);
                    }
                }
            });
        }

        List<IconData> icons = new ArrayList<>();

        Boolean showClock = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_CLOCK);
        if (showClock == null || showClock) icons.add(new TimeIconData(this));

        Boolean battery = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_BATTERY_ICON);
        if (battery == null || battery) icons.add(new BatteryIconData(this));

        Boolean network = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_NETWORK_ICON);
        if (network == null || network) icons.add(new NetworkIconData(this));

        Boolean dualSim = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_NETWORK_DUAL_SIM);
        if (dualSim != null && dualSim) icons.add(new DualNetworkIconData(this));

        Boolean data = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_DATA);
        if (data == null || data) icons.add(new DataIconData(this));

        Boolean wifi = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_WIFI_ICON);
        if (wifi == null || wifi) icons.add(new WifiIconData(this));

        Boolean bluetooth = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_BLUETOOTH_ICON);
        if (bluetooth == null || bluetooth) icons.add(new BluetoothIconData(this));

        Boolean airplane = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_AIRPLANE_MODE_ICON);
        if (airplane == null || airplane) icons.add(new AirplaneModeIconData(this));

        Boolean alarm = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_ALARM_ICON);
        if ((alarm == null || alarm) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            icons.add(new AlarmIconData(this));

        Boolean ringer = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_RINGER_ICON);
        if (ringer == null || ringer) icons.add(new RingerIconData(this));

        statusView.setIcons(icons);
        statusView.register();

        if (StaticUtils.isAccessibilityServiceRunning(this)) {
            Intent intent = new Intent(AccessibilityService.ACTION_GET_COLOR);
            intent.setClass(this, AccessibilityService.class);
            startService(intent);
        }
    }

    public boolean isFullscreen() {
        if (statusView != null && fullscreenView != null) {
            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            return fullscreenView.getMeasuredHeight() == size.y;
        } else return false;
    }

    @Override
    public void onDestroy() {
        if (fullscreenView != null) {
            windowManager.removeView(fullscreenView);
            fullscreenView = null;
        }

        if (statusView != null) {
            statusView.unregister();
            windowManager.removeView(statusView);
            statusView = null;
        }

        super.onDestroy();
    }

    public void showHeadsUp(NotificationData notification) {
        headsUpView = LayoutInflater.from(this).inflate(R.layout.layout_notification, null);

        ViewCompat.setElevation(headsUpView, StaticUtils.getPixelsFromDp(this, 2));

        headsUpView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                headsUpView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ValueAnimator animator = ValueAnimator.ofInt(-headsUpView.getHeight(), 0);
                animator.setDuration(250);
                animator.setInterpolator(new OvershootInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        headsUpView.setY((int) valueAnimator.getAnimatedValue());
                    }
                });
                animator.start();
            }
        });

        headsUpView.setFilterTouchesWhenObscured(false);
        headsUpView.setOnTouchListener(new View.OnTouchListener() {
            float offsetX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_OUTSIDE:
                        if (headsUpView != null && headsUpView.getParent() != null && event.getX() < v.getWidth() && event.getY() < v.getHeight())
                            offsetX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (Math.abs(event.getX() - offsetX) > StaticUtils.getPixelsFromDp(StatusService.this, 72) && headsUpView != null && headsUpView.getParent() != null)
                            dismissHeadsUpView();
                        else if (headsUpView != null)
                            headsUpView.animate().x(0).setDuration(150).start();
                        offsetX = 0;
                        break;
                    default:
                        headsUpView.setX(event.getX() - offsetX);
                        shouldFireClickEvent = Math.abs(event.getX() - offsetX) < StaticUtils.getPixelsFromDp(StatusService.this, 8);
                }

                return false;
            }
        });

        headsUpHandler.postDelayed(headsUpRunnable, HEADS_UP_DURATION);

        Integer color = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (color == null) color = Color.BLACK;
        if (!ColorUtils.isColorDark(color)) color = ColorUtils.darkColor(color);

        CustomImageView icon = (CustomImageView) headsUpView.findViewById(R.id.icon);
        Drawable drawable = notification.getIcon(this);
        if (drawable != null) ImageUtils.tintDrawable(icon, drawable, color);

        CustomImageView largeIcon = (CustomImageView) headsUpView.findViewById(R.id.largeIcon);
        Drawable largeDrawable = notification.getLargeIcon(this);
        if (drawable != null) largeIcon.setImageDrawable(largeDrawable);

        TextView name = (TextView) headsUpView.findViewById(R.id.name);
        name.setText(notification.getName(this));
        name.setTextColor(color);

        notification.getColor(this, new NotificationData.OnColorListener() {
            @Override
            public void onColor(String key, @ColorInt int color) {
                if (headsUpView != null && headsUpNotification != null && headsUpNotification.getKey().matches(key)) {
                    CustomImageView icon = (CustomImageView) headsUpView.findViewById(R.id.icon);
                    if (icon.getDrawable() != null)
                        ImageUtils.tintDrawable(icon, icon.getDrawable(), color);

                    ((TextView) headsUpView.findViewById(R.id.name)).setTextColor(color);
                }
            }
        });

        ((TextView) headsUpView.findViewById(R.id.title)).setText(notification.title);
        ((TextView) headsUpView.findViewById(R.id.subtitle)).setText(notification.subtitle);

        if (notification.intent != null) {
            headsUpView.setTag(notification.intent);
            headsUpView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    if (tag != null && tag instanceof PendingIntent && shouldFireClickEvent) {
                        try {
                            ((PendingIntent) tag).send();
                        } catch (PendingIntent.CanceledException ignored) {
                        }

                        if (headsUpView != null && headsUpView.getParent() != null)
                            removeHeadsUpView();
                    }
                }
            });
        }

        LinearLayout actionsLayout = (LinearLayout) headsUpView.findViewById(R.id.actions);
        ActionData[] actions = notification.getActions();

        if (actions.length > 0) {
            actionsLayout.setVisibility(View.VISIBLE);

            for (ActionData action : actions) {
                View button = LayoutInflater.from(this).inflate(R.layout.item_action, null);

                Drawable actionIcon = action.getIcon(this);
                if (actionIcon != null)
                    ((CustomImageView) button.findViewById(R.id.icon)).setImageDrawable(actionIcon);
                else button.findViewById(R.id.icon).setVisibility(View.GONE);

                ((TextView) button.findViewById(R.id.title)).setText(action.getTitle());

                PendingIntent intent = action.getActionIntent();
                if (intent != null) {
                    button.setTag(intent);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object tag = v.getTag();
                            if (tag != null && tag instanceof PendingIntent && shouldFireClickEvent) {
                                try {
                                    ((PendingIntent) tag).send();
                                } catch (PendingIntent.CanceledException ignored) {
                                }

                                if (headsUpView != null && headsUpView.getParent() != null)
                                    removeHeadsUpView();
                            }
                        }
                    });
                }

                actionsLayout.addView(button);
            }
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        params.gravity = Gravity.TOP;

        windowManager.addView(headsUpView, params);
    }

    private void removeHeadsUpView() {
        headsUpNotification = null;
        headsUpHandler.removeCallbacks(headsUpRunnable);

        ValueAnimator animator = ValueAnimator.ofInt((int) headsUpView.getY(), -headsUpView.getHeight());
        animator.setDuration(150);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (headsUpView != null) {
                    headsUpView.setY((int) valueAnimator.getAnimatedValue());
                    headsUpView.setAlpha(1 - valueAnimator.getAnimatedFraction());

                    if (valueAnimator.getAnimatedFraction() == 1 && headsUpView.getParent() != null) {
                        windowManager.removeView(headsUpView);
                        headsUpView = null;
                    }
                }
            }
        });
        animator.start();
    }

    private void dismissHeadsUpView() {
        if (!StaticUtils.shouldUseCompatNotifications(this)) {
            Intent intent = new Intent(NotificationService.ACTION_CANCEL_NOTIFICATION);
            intent.setClass(this, NotificationService.class);
            intent.putExtra(NotificationService.EXTRA_NOTIFICATION, headsUpNotification);
            startService(intent);
        }

        headsUpNotification = null;
        headsUpHandler.removeCallbacks(headsUpRunnable);

        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);

        ValueAnimator animator = ValueAnimator.ofInt((int) headsUpView.getX(), headsUpView.getX() > 0 ? size.x : -size.x);
        animator.setDuration(150);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (headsUpView != null) {
                    headsUpView.setX((int) valueAnimator.getAnimatedValue());
                    headsUpView.setAlpha(1 - valueAnimator.getAnimatedFraction());

                    if (valueAnimator.getAnimatedFraction() == 1 && headsUpView.getParent() != null) {
                        windowManager.removeView(headsUpView);
                        headsUpView = null;
                    }
                }
            }
        });
        animator.start();
    }
}
