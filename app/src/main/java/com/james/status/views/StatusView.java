package com.james.status.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.icon.IconData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class StatusView extends FrameLayout {

    private LinearLayout status;
    private LinearLayout notificationIconLayout;

    @ColorInt
    private Integer color;
    private boolean isSystemShowing, isDarkMode, isFullscreen;
    private ArrayMap<String, Notification> notifications;

    private List<IconData> icons;

    public StatusView(Context context) {
        super(context);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setUp() {
        if (status != null && status.getParent() != null) removeView(status);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_status, null);
        status = (LinearLayout) v.findViewById(R.id.status);
        status.getLayoutParams().height = StaticUtils.getStatusBarHeight(getContext());

        notificationIconLayout = (LinearLayout) v.findViewById(R.id.notificationIcons);

        Boolean isNotifications = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.SHOW_NOTIFICATIONS);
        if (isNotifications != null && !isNotifications)
            notificationIconLayout.setVisibility(View.INVISIBLE);

        VectorDrawableCompat.create(getResources(), R.drawable.ic_battery_alert, getContext().getTheme());

        addView(v);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto != null && !isStatusColorAuto) {
            Integer statusBarColor = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (statusBarColor != null) setColor(statusBarColor);
        } else if (color != null) setColor(color);
        else setColor(Color.BLACK);
    }

    public void setIcons(List<IconData> icons) {
        for (int i = (status.getChildCount() - 1); i >= 0; i--) {
            View child = status.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                status.removeViewAt(i);
            }
        }

        this.icons = icons;

        for (final IconData iconData : icons) {
            final View item = getIconView();
            item.setTag(iconData);

            iconData.setDrawableListener(new IconData.DrawableListener() {
                @Override
                public void onUpdate(@Nullable Drawable drawable) {
                    CustomImageView iconView = (CustomImageView) item.findViewById(R.id.icon);
                    if (drawable != null) {
                        item.setVisibility(View.VISIBLE);
                        iconView.setVisibility(View.VISIBLE);

                        iconView.setImageDrawable(drawable);

                        if (isDarkMode) ImageUtils.setTint(iconView, Color.BLACK);
                        else ImageUtils.setTint(iconView, Color.WHITE);
                    } else {
                        iconView.setVisibility(View.GONE);
                        if (!iconData.hasText()) item.setVisibility(View.GONE);
                    }
                }
            });

            iconData.setTextListener(new IconData.TextListener() {
                @Override
                public void onUpdate(@Nullable String text) {
                    TextView textView = (TextView) item.findViewById(R.id.text);
                    if (text != null) {
                        item.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);

                        textView.setText(text);

                        if (isDarkMode) textView.setTextColor(Color.BLACK);
                        else textView.setTextColor(Color.WHITE);
                    } else {
                        textView.setVisibility(View.GONE);
                        if (!iconData.hasDrawable()) item.setVisibility(View.GONE);
                    }
                }
            });

            item.findViewById(R.id.icon).setVisibility(iconData.hasDrawable() ? View.VISIBLE : View.GONE);
            item.findViewById(R.id.text).setVisibility(iconData.hasText() ? View.VISIBLE : View.GONE);

            status.addView(item, 1);
        }
    }

    public List<IconData> getIcons() {
        if (icons == null) icons = new ArrayList<>();
        return icons;
    }

    public void register() {
        if (icons != null) {
            for (IconData icon : icons) {
                icon.register();
            }
        }
    }

    public void unregister() {
        if (icons != null) {
            for (IconData icon : icons) {
                icon.unregister();
            }
        }
    }

    public void addNotification(String key, Notification notification, @Nullable String packageName) {
        if (notifications == null) notifications = new ArrayMap<>();

        if (notificationIconLayout != null) {
            for (int i = 0; i < notificationIconLayout.getChildCount(); i++) {
                View child = notificationIconLayout.getChildAt(i);
                Object tag = child.getTag();

                if (tag != null && tag instanceof String && ((String) tag).matches(key)) {
                    notificationIconLayout.removeView(child);
                    notifications.remove(key);
                }
            }

            View v = getIconView();
            v.setTag(key);

            Drawable drawable = getNotificationIcon(notification, packageName);

            if (drawable != null) {
                CustomImageView icon = (CustomImageView) v.findViewById(R.id.icon);
                icon.setImageDrawable(drawable);

                if (isDarkMode) ImageUtils.setTint(icon, Color.BLACK);
                notificationIconLayout.addView(v);

                notifications.put(key, notification);
            }
        }
    }

    public void removeNotification(String key) {
        if (notificationIconLayout != null) {
            for (int i = 0; i < notificationIconLayout.getChildCount(); i++) {
                View child = notificationIconLayout.getChildAt(i);
                if (((String) child.getTag()).matches(key) && notifications.containsKey(key)) {
                    removeView(notificationIconLayout.getChildAt(i), notificationIconLayout);
                    notifications.remove(key);
                }
            }
        }
    }

    public ArrayMap<String, Notification> getNotifications() {
        if (notifications == null) notifications = new ArrayMap<>();
        return notifications;
    }

    public void setSystemShowing(boolean isSystemShowing) {
        if (this.isFullscreen != isSystemShowing || this.isSystemShowing != isSystemShowing) {
            if (isSystemShowing) {
                ValueAnimator animator = ValueAnimator.ofFloat(getY(), -StaticUtils.getStatusBarHeight(getContext()));
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
                        setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
                animator.start();
            }
        }

        this.isSystemShowing = isSystemShowing;
    }

    public boolean isSystemShowing() {
        return isSystemShowing;
    }

    public void setFullscreen(boolean isFullscreen) {
        if (((getVisibility() == View.GONE) != isFullscreen) && !isSystemShowing) {
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
        }

        this.isFullscreen = isFullscreen;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void setColor(@ColorInt int color) {
        if (this.color == null) this.color = Color.BLACK;

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

    public void setHomeScreen() {
        if (status != null) {
            Bitmap background = ImageUtils.cropBitmapToBar(getContext(), ImageUtils.drawableToBitmap(WallpaperManager.getInstance(getContext()).getDrawable()));

            if (background != null) {
                Palette.from(background).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int color = palette.getVibrantColor(palette.getDarkVibrantColor(Color.BLACK));

                        Boolean transparent = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_HOME_TRANSPARENT);
                        if (transparent == null || transparent) {
                            setDarkMode(ColorUtils.isColorDark(color));
                            StatusView.this.color = color;
                        } else setColor(color);
                    }
                });

                Boolean transparent = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_HOME_TRANSPARENT);
                if (transparent == null || transparent)
                    status.setBackground(new BitmapDrawable(getResources(), background));
            } else setColor(Color.BLACK);
        }
    }

    public void setDarkMode(boolean isDarkMode) {
        Boolean isDarkModeEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);

        if (this.isDarkMode != isDarkMode && (isDarkModeEnabled == null || isDarkModeEnabled)) {
            setDarkView(status, isDarkMode ? Color.BLACK : Color.WHITE);
            this.isDarkMode = isDarkMode;
        }
    }

    private void setDarkView(View view, int color) {
        if (view instanceof LinearLayout) {
            for (int i = 0; i < ((LinearLayout) view).getChildCount(); i++) {
                setDarkView(((LinearLayout) view).getChildAt(i), color);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        } else if (view instanceof CustomImageView) {
            ImageUtils.setTint((CustomImageView) view, color);
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

    private View getIconView() {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.item_icon, this, false);

        Integer iconPadding = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_PADDING);
        if (iconPadding == null) iconPadding = 2;

        float iconPaddingDp = StaticUtils.getPixelsFromDp(getContext(), iconPadding);

        v.setPadding((int) iconPaddingDp, 0, (int) iconPaddingDp, 0);

        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Integer iconScale = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_SCALE);
                if (iconScale == null) iconScale = 24;

                ValueAnimator animator = ValueAnimator.ofInt(0, (int) StaticUtils.getPixelsFromDp(getContext(), iconScale));
                animator.setDuration(150);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        v.setAlpha(valueAnimator.getAnimatedFraction());

                        View iconView = v.findViewById(R.id.icon);

                        ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                        if (layoutParams != null) layoutParams.height = (int) valueAnimator.getAnimatedValue();
                        else
                            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) valueAnimator.getAnimatedValue());
                        iconView.setLayoutParams(layoutParams);
                    }
                });
                animator.start();
            }
        });

        return v;
    }

    private void removeView(final View child, final ViewGroup parent) {
        ValueAnimator animator = ValueAnimator.ofInt(child.getHeight(), 0);
        animator.setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                child.setAlpha(valueAnimator.getAnimatedFraction());

                View iconView = child.findViewById(R.id.icon);

                if (iconView != null) {
                    ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                    if (layoutParams != null)
                        layoutParams.height = (int) valueAnimator.getAnimatedValue();
                    else
                        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) valueAnimator.getAnimatedValue());
                    iconView.setLayoutParams(layoutParams);
                }

                if ((int) valueAnimator.getAnimatedValue() == 0) parent.removeView(child);
            }
        });
        animator.start();
    }

    @Nullable
    private Drawable getNotificationIcon(Notification notification, @Nullable String packageName) {
        Drawable drawable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawable = notification.getSmallIcon().loadDrawable(getContext());
            if (drawable != null) return drawable;
        } else {
            if (packageName != null) {
                drawable = getDrawable(notification.icon, packageName);
                if (drawable != null) return drawable;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && notification.contentIntent != null) {
                drawable = getDrawable(notification.icon, notification.contentIntent.getCreatorPackage());
                if (drawable != null) return drawable;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && notification.deleteIntent != null) {
                drawable = getDrawable(notification.icon, notification.deleteIntent.getCreatorPackage());
                if (drawable != null) return drawable;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && notification.fullScreenIntent != null) {
                drawable = getDrawable(notification.icon, notification.fullScreenIntent.getCreatorPackage());
                if (drawable != null) return drawable;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && notification.actions != null && notification.actions.length > 0) {
                drawable = getDrawable(notification.icon, notification.actions[0].actionIntent.getCreatorPackage());
                if (drawable != null) return drawable;
            }
        }

        return null;
    }

    @Nullable
    private Drawable getDrawable(int resource, String packageName) {
        if (packageName == null) return null;

        Resources resources = null;
        PackageInfo packageInfo = null;

        try {
            resources = getContext().getPackageManager().getResourcesForApplication(packageName);
            packageInfo = getContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (resources == null || packageInfo == null) return null;

        Resources.Theme theme = resources.newTheme();
        theme.applyStyle(packageInfo.applicationInfo.theme, false);

        try {
            return ResourcesCompat.getDrawable(resources, resource, theme);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }
}
