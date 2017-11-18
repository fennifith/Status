package com.james.status.views;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
    private OverflowLinearLayout leftLayout;
    private float leftX, leftY;
    private OverflowLinearLayout rightLayout;
    private float rightX, rightY;
    private LinearLayout centerLayout;
    private float centerX, centerY;
    private int burnInOffsetX, burnInOffsetY;

    @ColorInt
    private Integer color, iconColor = Color.WHITE;
    private boolean isSystemShowing;
    private boolean isFullscreen;
    private boolean isAnimations;
    private boolean isIconAnimations;
    private boolean isTintedIcons;
    private boolean isContrastIcons;
    private boolean isRegistered;
    private boolean isBumpMode;
    private boolean isTransparentMode;
    private boolean isBurnInProtection, isBurnInProtectionStarted;
    private boolean isIconOverlapPrevention;

    private List<IconData> icons;
    private WallpaperManager wallpaperManager;

    private Handler handler;
    private Runnable burnInRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBurnInProtection)
                handler.postDelayed(this, 2000);
            else isBurnInProtectionStarted = false;

            if (status != null && status.getParent() != null) {
                switch (burnInOffsetX) {
                    case 0:
                        setLayoutX(-1);
                        burnInOffsetX++;
                        break;
                    case 2:
                    case 3:
                    case 4:
                        setLayoutX(1);
                        burnInOffsetX++;
                        break;
                    case 6:
                        setLayoutX(-1);
                        burnInOffsetX++;
                        break;
                    case 7:
                        setLayoutX(-1);
                        burnInOffsetX = 0;
                        break;
                    default:
                        setLayoutX(0);
                        burnInOffsetX++;
                }

                switch (burnInOffsetY) {
                    case 0:
                    case 1:
                    case 2:
                        setLayoutY(1);
                        burnInOffsetY++;
                        break;
                    case 4:
                    case 5:
                    case 6:
                        setLayoutY(-1);
                        burnInOffsetY++;
                        break;
                    case 7:
                        setLayoutY(0);
                        burnInOffsetY = 0;
                        break;
                    default:
                        setLayoutY(0);
                        burnInOffsetY++;
                }
            }
        }

        private void setLayoutX(float x) {
            leftLayout.setX(leftX + x);
            rightLayout.setX(rightX + x);
            centerLayout.setX(centerX + x);
        }

        private void setLayoutY(float y) {
            leftLayout.setY(leftY + y);
            rightLayout.setY(rightY + y);
            centerLayout.setY(centerY + y);
        }
    };

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handler = new Handler();
    }

    @TargetApi(21)
    public StatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        handler = new Handler();
    }

    public void setUp() {
        removeAllViews();

        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_status, this, false);
        status = (LinearLayout) v.findViewById(R.id.status);
        status.getLayoutParams().height = StaticUtils.getStatusBarHeight(getContext());

        leftLayout = (OverflowLinearLayout) v.findViewById(R.id.notificationIcons);
        rightLayout = (OverflowLinearLayout) v.findViewById(R.id.statusIcons);
        centerLayout = (LinearLayout) v.findViewById(R.id.statusCenterIcons);

        Boolean isAnimations = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_BACKGROUND_ANIMATIONS);
        this.isAnimations = isAnimations != null ? isAnimations : true;

        Boolean isIconAnimations = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_ANIMATIONS);
        this.isIconAnimations = isIconAnimations != null ? isIconAnimations : true;

        if (this.isIconAnimations) {
            leftLayout.setLayoutTransition(new LayoutTransition());
            rightLayout.setLayoutTransition(new LayoutTransition());
            centerLayout.setLayoutTransition(new LayoutTransition());
        } else {
            leftLayout.setLayoutTransition(null);
            rightLayout.setLayoutTransition(null);
            centerLayout.setLayoutTransition(null);
        }

        Boolean isTintedIcons = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_TINTED_ICONS);
        this.isTintedIcons = isTintedIcons != null ? isTintedIcons : false;

        Boolean isContrastIcons = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);
        this.isContrastIcons = isContrastIcons != null ? isContrastIcons : true;

        Boolean isTransparentMode = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_TRANSPARENT_MODE);
        this.isTransparentMode = isTransparentMode != null ? isTransparentMode : false;

        Boolean isIconOverlapPrevention = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_PREVENT_ICON_OVERLAP);
        this.isIconOverlapPrevention = isIconOverlapPrevention != null && isIconOverlapPrevention;

        addView(v);
        Boolean isBurnInProtection = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_BURNIN_PROTECTION);
        this.isBurnInProtection = isBurnInProtection != null && isBurnInProtection;
        status.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                leftX = leftLayout.getX();
                leftY = leftLayout.getY();
                rightX = rightLayout.getX();
                rightY = rightLayout.getY();
                centerX = centerLayout.getX();
                centerY = centerLayout.getY();

                if (StatusView.this.isBurnInProtection && !isBurnInProtectionStarted) {
                    handler.post(burnInRunnable);
                    isBurnInProtectionStarted = false;
                }

                status.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto != null && !isStatusColorAuto) {
            Integer statusBarColor = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (statusBarColor != null) setColor(statusBarColor);
        } else if (color != null) setColor(color);
        else setColor(Color.BLACK);

        Integer defaultIconColor = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_COLOR);
        if (defaultIconColor != null) iconColor = defaultIconColor;

        Integer sidePadding = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_SIDE_PADDING);
        if (sidePadding != null) {
            sidePadding = (int) StaticUtils.getPixelsFromDp(sidePadding);
            status.setPadding(sidePadding, 0, sidePadding, 0);
        }

        Boolean bumpMode = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_BUMP_MODE);
        isBumpMode = bumpMode != null && bumpMode;
        if (isBumpMode) {
            int padding = (int) StaticUtils.getPixelsFromDp(16);
            centerLayout.setPadding(padding, 0, padding, 0);
            centerLayout.setBackgroundResource(R.drawable.bump_inner);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                leftLayout.setBackgroundResource(R.drawable.bump_outer_left);
                rightLayout.setBackgroundResource(R.drawable.bump_outer_right);
            }
        }

        if (wallpaperManager == null) wallpaperManager = WallpaperManager.getInstance(getContext());
    }

    public void setIcons(List<IconData> icons) {
        for (int i = (leftLayout.getChildCount() - 1); i >= 0; i--) {
            View child = leftLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                leftLayout.removeViewAt(i);
            }
        }

        for (int i = (centerLayout.getChildCount() - 1); i >= 0; i--) {
            View child = centerLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                centerLayout.removeViewAt(i);
            }
        }

        for (int i = (rightLayout.getChildCount() - 1); i >= 0; i--) {
            View child = rightLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                rightLayout.removeViewAt(i);
            }
        }

        this.icons = icons;

        for (final IconData iconData : icons) {
            if (!iconData.isVisible()) continue;

            final View item = iconData.getIconView();

            iconData.setDrawableListener(new IconData.DrawableListener() {
                @Override
                public void onUpdate(@Nullable Drawable drawable) {
                    CustomImageView iconView = (CustomImageView) item.findViewById(R.id.icon);
                    int color = iconData.getGravity() == IconData.CENTER_GRAVITY && isBumpMode ? Color.WHITE : iconColor;

                    if (drawable != null && iconView != null)
                        iconView.setImageDrawable(drawable, color);
                    else if (iconView == null || !iconView.getParent().equals(item))
                        setIconTint(item, color);
                }
            });

            switch (iconData.getGravity()) {
                case IconData.LEFT_GRAVITY:
                    leftLayout.addView(item, 0);
                    break;
                case IconData.CENTER_GRAVITY:
                    centerLayout.addView(item, 0);
                    break;
                case IconData.RIGHT_GRAVITY:
                    rightLayout.addView(item, 0);
                    break;
            }
        }

        if (isIconOverlapPrevention) {
            leftLayout.onViewsChanged();
            rightLayout.onViewsChanged();
        }
    }

    public List<IconData> getIcons() {
        if (icons == null) icons = new ArrayList<>();
        return icons;
    }

    public void register() {
        if (icons != null && !isRegistered()) {
            for (IconData icon : icons) {
                icon.register();
            }
            isRegistered = true;
        }
    }

    public void unregister() {
        if (icons != null && isRegistered()) {
            for (IconData icon : icons) {
                icon.unregister();
            }
            isRegistered = false;
        }
    }

    public boolean isRegistered() {
        return isRegistered;
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
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), this.color, color);
                animator.setDuration(150);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (int) animation.getAnimatedValue();
                        if (status != null)
                            setStatusBackgroundColor(color);
                    }
                });
                animator.start();
            } else
                setStatusBackgroundColor(color);

            setDarkMode(!ColorUtils.isColorDark(color));
        } else if (status != null) {
            int backgroundColor = getDefaultColor();
            if (color == backgroundColor) {
                if (color == Color.BLACK) color = getDefaultIconColor();
                else if (color == Color.WHITE) color = Color.BLACK;
            }

            setStatusBackgroundColor(backgroundColor);

            if (isContrastIcons)
                color = ColorUtils.isColorDark(backgroundColor) ? ColorUtils.lightColor(color) : ColorUtils.darkColor(color);

            if (isIconAnimations) {
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), this.color, color);
                animator.setDuration(150);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (int) animation.getAnimatedValue();
                        if (status != null)
                            setIconTint(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                    }
                });
                animator.start();
            } else
                setIconTint(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

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

    @ColorInt
    private int getDefaultIconColor() {
        Integer color = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_COLOR);
        if (color == null) color = Color.WHITE;
        return color;
    }

    private void setStatusBackgroundColor(@ColorInt int color) {
        status.setBackgroundColor(isTransparentMode ? Color.TRANSPARENT : Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
    }

    public void setTransparent() {
        if (status != null && wallpaperManager != null) {
            Drawable backgroundDrawable;
            WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
            if (wallpaperInfo != null)
                backgroundDrawable = wallpaperInfo.loadThumbnail(getContext().getPackageManager());
            else {
                try {
                    backgroundDrawable = wallpaperManager.getDrawable();
                } catch (SecurityException e) {
                    setColor(getDefaultColor());
                    return;
                }
            }

            Bitmap background = ImageUtils.cropBitmapToBar(getContext(), ImageUtils.drawableToBitmap(backgroundDrawable));

            if (background != null) {
                int color = ColorUtils.getAverageColor(background);

                Boolean transparent = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_HOME_TRANSPARENT);
                if (transparent == null || transparent) {
                    status.setBackground(new BitmapDrawable(getResources(), background));
                    setDarkMode(!ColorUtils.isColorDark(color));
                    StatusView.this.color = color;
                } else setColor(color);
            } else setColor(getDefaultColor());
        }
    }

    public void setDarkMode(boolean isDarkMode) {
        if (isContrastIcons) {
            int color = isDarkMode ? Color.BLACK : getDefaultIconColor();

            if (isIconAnimations) {
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), iconColor, color);
                animator.setDuration(150);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (int) animation.getAnimatedValue();
                        if (status != null)
                            setIconTint(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                    }
                });
                animator.start();
            } else
                setIconTint(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

            iconColor = color;
        }
    }

    private void setIconTint(@ColorInt int color) {
        for (IconData icon : getIcons()) {
            icon.setColor(color);
        }

        leftLayout.setColor(color);
        rightLayout.setColor(color);
        setIconTint(status, color);
    }

    private void setIconTint(View view, @ColorInt int color) {
        if (view instanceof LinearLayout) {
            for (int i = 0; i < ((LinearLayout) view).getChildCount(); i++) {
                setIconTint(((LinearLayout) view).getChildAt(i), view.equals(centerLayout) && isBumpMode ? Color.WHITE : color);
            }
        } else if (view instanceof TextView) {
            if (view.getTag() == null)
                ((TextView) view).setTextColor(color);
        } else if (view instanceof CustomImageView) {
            CustomImageView imageView = (CustomImageView) view;
            if (imageView.getDrawable() != null)
                imageView.setColorFilter(color);
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
}
