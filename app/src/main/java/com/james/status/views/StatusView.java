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
import android.support.v4.util.ArrayMap;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private Integer color, iconColor = Color.WHITE;
    private boolean isSystemShowing, isFullscreen, isAnimations, isIconAnimations, isTintedIcons, isContrastIcons;
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

        Boolean isAnimations = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_BACKGROUND_ANIMATIONS);
        this.isAnimations = isAnimations != null ? isAnimations : true;

        Boolean isIconAnimations = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_ANIMATIONS);
        this.isIconAnimations = isIconAnimations != null ? isIconAnimations : true;

        Boolean isTintedIcons = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_TINTED_ICONS);
        this.isTintedIcons = isTintedIcons != null ? isTintedIcons : false;

        Boolean isContrastIcons = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);
        this.isContrastIcons = isContrastIcons != null ? isContrastIcons : true;

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
            if (!iconData.isVisible()) continue;

            final View item = getIconView(iconData.getIconPadding());
            item.setTag(iconData);

            iconData.setDrawableListener(new IconData.DrawableListener() {
                @Override
                public void onUpdate(@Nullable Drawable drawable) {
                    CustomImageView iconView = (CustomImageView) item.findViewById(R.id.icon);

                    if (drawable != null) {
                        setIconVisibility(item, null, (int) StaticUtils.getPixelsFromDp(getContext(), iconData.getIconScale()), true);

                        iconView.setVisibility(View.VISIBLE);
                        ImageUtils.tintDrawable(iconView, drawable, iconColor);
                    } else {
                        iconView.setVisibility(View.GONE);
                        if (iconData.getText() == null)
                            setIconVisibility(item, null, null, false);
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
                        if (iconData.getDrawable() == null)
                            item.setVisibility(View.GONE);
                    }
                }
            });

            ((TextView) item.findViewById(R.id.text)).setTextSize(TypedValue.COMPLEX_UNIT_SP, iconData.getTextSize());

            if (!iconData.hasDrawable()) item.findViewById(R.id.icon).setVisibility(View.GONE);
            if (!iconData.hasText()) item.findViewById(R.id.icon).setVisibility(View.GONE);
            item.setVisibility(View.GONE);

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

                Integer iconScale = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_SCALE);
                if (iconScale == null) iconScale = 24;

                Toast.makeText(getContext(), String.valueOf(iconScale), Toast.LENGTH_SHORT).show();

                setIconVisibility(v, null, (int) StaticUtils.getPixelsFromDp(getContext(), iconScale), true);

                notifications.put(notification.getKey(), notification);
            }
        }
    }

    public void removeNotification(NotificationData notification) {
        if (notificationIconLayout != null) {
            for (int i = 0; i < notificationIconLayout.getChildCount(); i++) {
                View child = notificationIconLayout.getChildAt(i);
                if (((String) child.getTag()).matches(notification.getKey()) && notifications.containsKey(notification.getKey())) {
                    setIconVisibility(notificationIconLayout.getChildAt(i), notificationIconLayout, null, false);
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
        if ((this.isFullscreen != isSystemShowing || this.isSystemShowing != isSystemShowing) && isSystemShowing)
            setStatusBarVisibility(false);
        this.isSystemShowing = isSystemShowing;
    }

    public boolean isSystemShowing() {
        return isSystemShowing;
    }

    public void setFullscreen(boolean isFullscreen) {
        if (((getVisibility() == View.GONE) != isFullscreen) && !isSystemShowing) {
            setStatusBarVisibility(!isFullscreen);
        }

        this.isFullscreen = isFullscreen;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    private void setStatusBarVisibility(final boolean visible) {
        if (isAnimations) {
            ValueAnimator animator = ValueAnimator.ofFloat(getY(), visible ? 0 : -StaticUtils.getStatusBarHeight(getContext()));
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
                    if (visible) setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!visible) setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            animator.start();
        } else {
            if (visible) setVisibility(View.VISIBLE);
            else setVisibility(View.GONE);
        }
    }

    public void setColor(@ColorInt int color) {
        if (this.color == null) this.color = Color.BLACK;
        color = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));

        if (!isTintedIcons) {
            if (isAnimations) {
                new ColorAnimator(this.color, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                    @Override
                    public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                        if (status != null) {
                            status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                        }
                    }
                }).start();
            } else
                status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

            setDarkMode(!ColorUtils.isColorDark(color));
        } else if (status != null) {
            int backgroundColor = getDefaultColor();
            if (color == backgroundColor) {
                if (color == Color.BLACK) color = Color.WHITE;
                else if (color == Color.WHITE) color = Color.BLACK;
            }

            status.setBackgroundColor(backgroundColor);

            if (isContrastIcons)
                color = ColorUtils.isColorDark(backgroundColor) ? ColorUtils.lightColor(color) : ColorUtils.darkColor(color);

            if (isIconAnimations) {
                new ColorAnimator(this.color, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                    @Override
                    public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                        if (status != null) {
                            setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                        }
                    }
                }).start();
            } else
                setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

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
                    setDarkMode(!ColorUtils.isColorDark(color));
                    StatusView.this.color = color;
                } else setColor(color);
            } else setColor(Color.BLACK);
        }
    }

    public void setDarkMode(boolean isDarkMode) {
        if (isContrastIcons) {
            int color = isDarkMode ? Color.BLACK : Color.WHITE;

            if (isIconAnimations) {
                new ColorAnimator(iconColor, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                    @Override
                    public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                        if (status != null) {
                            setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                        }
                    }
                }).start();
            } else
                setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

            iconColor = color;
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

        return v;
    }

    private View getIconView(int padding) {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.item_icon, this, false);

        float iconPaddingDp = StaticUtils.getPixelsFromDp(getContext(), padding);
        v.setPadding((int) iconPaddingDp, 0, (int) iconPaddingDp, 0);

        return v;
    }

    private void setIconVisibility(final View child, @Nullable final ViewGroup parent, @Nullable Integer scale, final boolean visible) {
        if (scale == null) scale = (int) StaticUtils.getPixelsFromDp(getContext(), 24);

        if (visible && child.getVisibility() == View.VISIBLE) return;
        else if (!visible && child.getVisibility() == View.GONE) return;

        if (isIconAnimations) {
            ValueAnimator animator = ValueAnimator.ofInt(visible ? 0 : child.getHeight(), visible ? scale : 0);
            animator.setDuration(250);
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
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (visible) child.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!visible) {
                        child.setVisibility(View.GONE);
                        if (parent != null) parent.removeView(child);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        } else {
            View iconView = child.findViewById(R.id.icon);

            if (iconView != null) {
                ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                if (layoutParams != null)
                    layoutParams.height = visible ? scale : 0;
                else
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, visible ? scale : 0);

                iconView.setLayoutParams(layoutParams);
            }

            if (visible) {
                child.setVisibility(View.VISIBLE);
            } else {
                child.setVisibility(View.GONE);
                if (parent != null) parent.removeView(child);
            }
        }
    }
}
