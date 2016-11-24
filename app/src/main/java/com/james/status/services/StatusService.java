package com.james.status.services;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
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
import com.james.status.data.icon.GpsIconData;
import com.james.status.data.icon.HeadphoneIconData;
import com.james.status.data.icon.IconData;
import com.james.status.data.icon.NetworkIconData;
import com.james.status.data.icon.NfcIconData;
import com.james.status.data.icon.NotificationsIconData;
import com.james.status.data.icon.RingerIconData;
import com.james.status.data.icon.TimeIconData;
import com.james.status.data.icon.WifiIconData;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CustomImageView;
import com.james.status.views.StatusView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatusService extends Service {

    public static final String ACTION_START = "com.james.status.ACTION_START";
    public static final String ACTION_STOP = "com.james.status.ACTION_STOP";
    public static final String ACTION_UPDATE = "com.james.status.ACTION_UPDATE";
    public static final String EXTRA_COLOR = "com.james.status.EXTRA_COLOR";
    public static final String EXTRA_IS_SYSTEM_FULLSCREEN = "com.james.status.EXTRA_IS_SYSTEM_FULLSCREEN";
    public static final String EXTRA_IS_FULLSCREEN = "com.james.status.EXTRA_IS_FULLSCREEN";
    public static final String EXTRA_IS_HOME_SCREEN = "com.james.status.EXTRA_IS_HOME_SCREEN";

    public static final int HEADSUP_LAYOUT_PLAIN = 0;
    public static final int HEADSUP_LAYOUT_CARD = 1;
    public static final int HEADSUP_LAYOUT_CONDENSED = 2;
    public static final int HEADSUP_LAYOUT_TRANSPARENT = 3;

    private StatusView statusView;
    private View fullscreenView;
    private View headsUpView;

    private KeyguardManager keyguardManager;
    private WindowManager windowManager;

    private NotificationReceiver notificationReceiver;
    private ArrayMap<String, NotificationData> notifications;

    private Handler headsUpHandler;
    private Runnable headsUpRunnable, headsUpDisabledRunnable;
    private NotificationData headsUpNotification;

    private boolean shouldFireClickEvent = true;
    private int headsUpDuration = 10000;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        notificationReceiver = new NotificationReceiver();

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

        Integer duration = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_HEADS_UP_DURATION);
        if (duration != null) headsUpDuration = duration * 1000;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled == null || !enabled || !StaticUtils.isPermissionsGranted(this)) {
            if (statusView != null) {
                if (statusView.getParent() != null) windowManager.removeView(statusView);
                statusView = null;
            }
            stopSelf();
            return START_NOT_STICKY;
        }

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
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(ACTION_UPDATE);
            intent.setClass(this, StatusService.class);

            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pendingIntent);
        } else super.onTaskRemoved(rootIntent);
    }

    public void setUp() {
        if (statusView == null || statusView.getParent() == null) {
            if (statusView != null) windowManager.removeView(statusView);
            statusView = new StatusView(this);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP;

            windowManager.addView(statusView, params);
        } else statusView.unregister();

        statusView.setUp();

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

        statusView.setIcons(getIcons(this));
        statusView.register();

        IntentFilter notificationFilter = new IntentFilter();
        notificationFilter.addAction(NotificationsIconData.ACTION_NOTIFICATION_ADDED);
        notificationFilter.addAction(NotificationsIconData.ACTION_NOTIFICATION_REMOVED);

        registerReceiver(notificationReceiver, notificationFilter);

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
        unregisterReceiver(notificationReceiver);

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

    private boolean containsNotification(NotificationData notification) {
        if (notifications == null) notifications = new ArrayMap<>();
        for (NotificationData data : notifications.values()) {
            if (data.equals(notification)) return true;
        }
        return false;
    }

    public ArrayMap<String, NotificationData> getNotifications() {
        if (notifications == null) notifications = new ArrayMap<>();
        return notifications;
    }

    public void showHeadsUp(NotificationData notification) {
        Integer headsUpLayout = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_HEADS_UP_LAYOUT);
        if (headsUpLayout == null) headsUpLayout = HEADSUP_LAYOUT_PLAIN;

        switch (headsUpLayout) {
            case HEADSUP_LAYOUT_CARD:
                headsUpLayout = R.layout.layout_notification_card;
                break;
            case HEADSUP_LAYOUT_CONDENSED:
                headsUpLayout = R.layout.layout_notification_condensed;
                break;
            case HEADSUP_LAYOUT_TRANSPARENT:
                headsUpLayout = R.layout.layout_notification_transparent;
                break;
            default:
                headsUpLayout = R.layout.layout_notification;
                break;
        }

        headsUpView = LayoutInflater.from(this).inflate(headsUpLayout, null);

        ViewCompat.setElevation(headsUpView, StaticUtils.getPixelsFromDp(this, 2));

        headsUpView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (headsUpView == null) return;
                headsUpView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ValueAnimator animator = ValueAnimator.ofInt(-headsUpView.getHeight(), 0);
                animator.setDuration(250);
                animator.setInterpolator(new OvershootInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        if (headsUpView != null)
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
                        if (headsUpView != null) headsUpView.setX(event.getX() - offsetX);
                        shouldFireClickEvent = Math.abs(event.getX() - offsetX) < StaticUtils.getPixelsFromDp(StatusService.this, 8);
                }

                return false;
            }
        });

        Integer duration = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_HEADS_UP_DURATION);
        if (duration != null) headsUpDuration = duration * 1000;

        headsUpHandler.postDelayed(headsUpRunnable, headsUpDuration);

        CustomImageView icon = (CustomImageView) headsUpView.findViewById(R.id.icon);
        Drawable drawable = notification.getIcon(this);
        if (drawable != null) ImageUtils.tintDrawable(icon, drawable, notification.color);

        CustomImageView largeIcon = (CustomImageView) headsUpView.findViewById(R.id.largeIcon);
        Drawable largeDrawable = notification.getLargeIcon(this);
        if (drawable != null) largeIcon.setImageDrawable(largeDrawable);

        TextView name = (TextView) headsUpView.findViewById(R.id.name);
        name.setText(notification.getName(this));
        name.setTextColor(notification.color);

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
                    ImageUtils.tintDrawable((CustomImageView) button.findViewById(R.id.icon), actionIcon, notification.color);
                else button.findViewById(R.id.icon).setVisibility(View.GONE);

                TextView title = (TextView) button.findViewById(R.id.title);
                title.setText(action.getTitle());
                title.setTextColor(notification.color);

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
            params.type = WindowManager.LayoutParams.TYPE_PHONE;

        params.gravity = Gravity.TOP;

        windowManager.addView(headsUpView, params);
    }

    private void removeHeadsUpView() {
        headsUpNotification = null;
        headsUpHandler.removeCallbacks(headsUpRunnable);

        ValueAnimator animator = ValueAnimator.ofInt((int) headsUpView.getY(), -headsUpView.getHeight());
        animator.setDuration(250);
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
        animator.setDuration(250);
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

    public static List<IconData> getIcons(Context context) {
        List<IconData> icons = new ArrayList<>();
        icons.add(new NotificationsIconData(context));
        icons.add(new TimeIconData(context));
        icons.add(new BatteryIconData(context));
        icons.add(new NetworkIconData(context));
        icons.add(new DataIconData(context));
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
            icons.add(new WifiIconData(context));
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            icons.add(new BluetoothIconData(context));
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
            icons.add(new GpsIconData(context));
        icons.add(new AirplaneModeIconData(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
            icons.add(new NfcIconData(context));
        icons.add(new AlarmIconData(context));
        icons.add(new RingerIconData(context));
        icons.add(new HeadphoneIconData(context));

        for (IconData icon : icons) {
            if (icon.getIntegerPreference(IconData.PreferenceIdentifier.POSITION) == null)
                icon.putPreference(IconData.PreferenceIdentifier.POSITION, icons.indexOf(icon));
        }

        Collections.sort(icons, new Comparator<IconData>() {
            @Override
            public int compare(IconData lhs, IconData rhs) {
                return lhs.getPosition() - rhs.getPosition();
            }
        });

        return icons;
    }

    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case NotificationsIconData.ACTION_NOTIFICATION_ADDED:
                    NotificationData notification = intent.getParcelableExtra(NotificationsIconData.EXTRA_NOTIFICATION);

                    if (!containsNotification(notification) && notification.shouldShowHeadsUp(StatusService.this) && headsUpNotification == null) {
                        showHeadsUp(notification);
                        headsUpNotification = notification;
                    } else if (notification.shouldHideStatusBar()) {
                        statusView.setSystemShowing(true);
                        headsUpNotification = notification;

                        Integer duration = PreferenceUtils.getIntegerPreference(StatusService.this, PreferenceUtils.PreferenceIdentifier.STATUS_HEADS_UP_DURATION);
                        if (duration != null) headsUpDuration = duration * 1000;

                        headsUpHandler.postDelayed(headsUpDisabledRunnable, headsUpDuration);
                    }

                    getNotifications().put(notification.getKey(), notification);
                    break;
                case NotificationsIconData.ACTION_NOTIFICATION_REMOVED:
                    notification = intent.getParcelableExtra(NotificationsIconData.EXTRA_NOTIFICATION);
                    getNotifications().remove(notification.getKey());

                    if (headsUpNotification != null && headsUpNotification.equals(notification)) {
                        if (headsUpView != null && headsUpView.getParent() != null)
                            removeHeadsUpView();
                        else {
                            statusView.setSystemShowing(false);
                            statusView.setFullscreen(isFullscreen());
                            headsUpHandler.removeCallbacks(headsUpDisabledRunnable);
                            headsUpNotification = null;
                        }
                    }
                    break;
            }
        }
    }
}
