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

import com.james.status.R;
import com.james.status.data.NotificationData;
import com.james.status.data.icon.IconData;
import com.james.status.utils.ColorAnimator;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class StatusView extends FrameLayout {

    private LinearLayout status, notificationIconLayout, statusIconLayout, statusCenterIconLayout;

    @ColorInt
    private Integer color, iconColor;
    private boolean isSystemShowing, isFullscreen;
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
        statusIconLayout = (LinearLayout) v.findViewById(R.id.statusIcons);
        statusCenterIconLayout = (LinearLayout) v.findViewById(R.id.statusCenterIcons);

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
        for (int i = (statusIconLayout.getChildCount() - 1); i >= 0; i--) {
            View child = statusIconLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                statusIconLayout.removeViewAt(i);
            }
        }

        for (int i = (statusCenterIconLayout.getChildCount() - 1); i >= 0; i--) {
            View child = statusCenterIconLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                statusCenterIconLayout.removeViewAt(i);
            }
        }

        this.icons = icons;

        for (final IconData iconData : icons) {
            Boolean isVisible = iconData.getBooleanPreference(IconData.PreferenceIdentifier.VISIBILITY);
            if (isVisible != null && !isVisible) continue;

            final View item = getIconView(iconData.getIconPadding(), iconData.getIconScale());
            item.setTag(iconData);

            iconData.setDrawableListener(new IconData.DrawableListener() {
                @Override
                public void onUpdate(@Nullable Drawable drawable) {
                    CustomImageView iconView = (CustomImageView) item.findViewById(R.id.icon);
                    if (drawable != null) {
                        item.setVisibility(View.VISIBLE);
                        iconView.setVisibility(View.VISIBLE);

                        ImageUtils.tintDrawable(iconView, drawable, iconColor);
                    } else {
                        iconView.setVisibility(View.GONE);
                        if (iconData.getText() == null) item.setVisibility(View.GONE);
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

                        textView.setTextColor(iconColor);
                    } else {
                        textView.setVisibility(View.GONE);
                        if (iconData.getDrawable() == null) item.setVisibility(View.GONE);
                    }
                }
            });

            item.findViewById(R.id.icon).setVisibility(iconData.hasDrawable() ? View.VISIBLE : View.GONE);
            item.findViewById(R.id.text).setVisibility(iconData.hasText() ? View.VISIBLE : View.GONE);

            if (iconData.isCentered()) statusCenterIconLayout.addView(item, 0);
            else statusIconLayout.addView(item, 0);
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
                ImageUtils.tintDrawable((CustomImageView) v.findViewById(R.id.icon), drawable, iconColor);

                notificationIconLayout.addView(v);

                notifications.put(notification.getKey(), notification);
            }
        }
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
        color = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));

        Boolean isIconTint = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_TINTED_ICONS);

        if (isIconTint == null || !isIconTint) {
            new ColorAnimator(this.color, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                @Override
                public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                    if (status != null) {
                        status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                        if (animator.getAnimatedFraction() == 1)
                            setDarkMode(!ColorUtils.isColorDark(color));
                    }
                }
            }).start();
        } else if (status != null) {
            int backgroundColor = getDefaultColor();
            if (color == backgroundColor) {
                if (color == Color.BLACK) color = Color.WHITE;
                else if (color == Color.WHITE) color = Color.BLACK;
            }

            status.setBackgroundColor(backgroundColor);

            Boolean isDarkModeEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);
            if (isDarkModeEnabled == null || isDarkModeEnabled)
                color = ColorUtils.isColorDark(backgroundColor) ? ColorUtils.lightColor(color) : ColorUtils.darkColor(color);

            new ColorAnimator(this.color, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                @Override
                public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                    if (status != null) {
                        setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                    }
                }
            }).start();

            iconColor = color;
        }

        this.color = color;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    @ColorInt
    private int getDefaultColor() {
        Integer color = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (color == null) color = Color.BLACK;
        return color;
    }

    public void setHomeScreen() {
        if (status != null) {
            Bitmap background = ImageUtils.cropBitmapToBar(getContext(), ImageUtils.drawableToBitmap(WallpaperManager.getInstance(getContext()).getDrawable()));

            if (background != null) {
                int color = ColorUtils.getAverageColor(background);

                Boolean transparent = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_HOME_TRANSPARENT);
                if (transparent == null || transparent) {
                    status.setBackground(new BitmapDrawable(getResources(), background));
                    setDarkMode(ColorUtils.isColorDark(color));
                    StatusView.this.color = color;
                } else setColor(color);
            } else setColor(Color.BLACK);
        }
    }

    public void setDarkMode(boolean isDarkMode) {
        Boolean isDarkModeEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);
        if (isDarkModeEnabled == null || isDarkModeEnabled) {
            iconColor = isDarkMode ? Color.BLACK : Color.WHITE;
            setIconTint(status, iconColor);
        }
    }

    private void setIconTint(View view, int color) {
        if (view instanceof LinearLayout) {
            for (int i = 0; i < ((LinearLayout) view).getChildCount(); i++) {
                setIconTint(((LinearLayout) view).getChildAt(i), color);
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

    private View getIconView(int padding, final int scale) {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.item_icon, this, false);

        float iconPaddingDp = StaticUtils.getPixelsFromDp(getContext(), padding);
        v.setPadding((int) iconPaddingDp, 0, (int) iconPaddingDp, 0);

        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ValueAnimator animator = ValueAnimator.ofInt(0, (int) StaticUtils.getPixelsFromDp(getContext(), scale));
                animator.setDuration(150);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        v.setAlpha(valueAnimator.getAnimatedFraction());

                        View iconView = v.findViewById(R.id.icon);

                        ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                        if (layoutParams != null)
                            layoutParams.height = (int) valueAnimator.getAnimatedValue();
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
