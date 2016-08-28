package com.james.status.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
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
import android.widget.Toast;

import com.james.status.R;
import com.james.status.data.NotificationData;
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
    private ArrayMap<String, NotificationData> notifications;

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

                        ImageUtils.tintDrawable(iconView, drawable, isDarkMode ? Color.BLACK : Color.WHITE);
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

    public void addNotification(NotificationData notification) {
        if (notifications == null) notifications = new ArrayMap<>();

        if (notificationIconLayout != null) {
            for (int i = 0; i < notificationIconLayout.getChildCount(); i++) {
                View child = notificationIconLayout.getChildAt(i);
                Object tag = child.getTag();

                if (tag != null && tag instanceof String && ((String) tag).matches(notification.getKey())) {
                    notificationIconLayout.removeView(child);
                    notifications.remove(notification.getKey());
                }
            }

            View v = getIconView();
            v.setTag(notification.getKey());

            Drawable drawable = notification.getIcon(getContext());

            if (drawable != null) {
                ImageUtils.tintDrawable((CustomImageView) v.findViewById(R.id.icon), drawable, isDarkMode ? Color.BLACK : Color.WHITE);

                notificationIconLayout.addView(v);

                notifications.put(notification.getKey(), notification);
            } else Toast.makeText(getContext(), "drawable null", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(getContext(), "layout null", Toast.LENGTH_SHORT).show();
    }

    public void removeNotification(NotificationData notification) {
        if (notificationIconLayout != null) {
            for (int i = 0; i < notificationIconLayout.getChildCount(); i++) {
                View child = notificationIconLayout.getChildAt(i);
                if (((String) child.getTag()).matches(notification.getKey()) && notifications.containsKey(notification.getKey())) {
                    removeIconView(notificationIconLayout.getChildAt(i), notificationIconLayout);
                    notifications.remove(notification.getKey());
                }
            }
        }
    }

    public boolean containsNotification(NotificationData notification) {
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

    @ColorInt
    public int getColor() {
        return color;
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
            CustomImageView imageView = (CustomImageView) view;
            if (imageView.getDrawable() != null)
                ImageUtils.tintDrawable(imageView, imageView.getDrawable(), color);
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

    private void removeIconView(final View child, final ViewGroup parent) {
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

                if (valueAnimator.getAnimatedFraction() == 1) parent.removeView(child);
            }
        });
        animator.start();
    }
}
