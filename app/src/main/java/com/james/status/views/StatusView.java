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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class StatusView extends FrameLayout {

    public static final int OPTION_SYSTEM_SHOWING = 0;
    public static final int OPTION_FULLSCREEN = 1;
    public static final int OPTION_ANIMATIONS = 2;
    public static final int OPTION_ICON_ANIMATIONS = 3;
    public static final int OPTION_TINTED_ICONS = 4;
    public static final int OPTION_CONTRAST_ICONS = 5;
    public static final int OPTION_BUMP_MODE = 6;
    public static final int OPTION_TRANSPARENT_MODE = 7;
    public static final int OPTION_BURN_IN_PROTECTION = 8;
    public static final int OPTION_ICON_OVERLAP_PREVENTION = 9;
    public static final int OPTION_COLOR = 10;
    public static final int OPTION_ICON_COLOR = 11;
    public static final int OPTION_AUTO_COLOR = 12;
    public static final int OPTION_SIDE_PADDING = 13;
    public static final int OPTION_TRANSPARENT_HOME = 14;
    public static final PreferenceData[] OPTIONS = new PreferenceData[]{
            null,
            null,
            PreferenceData.STATUS_BACKGROUND_ANIMATIONS,
            PreferenceData.STATUS_ICON_ANIMATIONS,
            PreferenceData.STATUS_TINTED_ICONS,
            PreferenceData.STATUS_DARK_ICONS,
            PreferenceData.STATUS_BUMP_MODE,
            PreferenceData.STATUS_TRANSPARENT_MODE,
            PreferenceData.STATUS_BURNIN_PROTECTION,
            PreferenceData.STATUS_PREVENT_ICON_OVERLAP,
            PreferenceData.STATUS_COLOR,
            PreferenceData.STATUS_ICON_COLOR,
            PreferenceData.STATUS_COLOR_AUTO,
            PreferenceData.STATUS_SIDE_PADDING,
            PreferenceData.STATUS_HOME_TRANSPARENT
    };

    private boolean[] booleanOptions = new boolean[OPTIONS.length];
    private int[] intOptions = new int[OPTIONS.length];

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

    private boolean isRegistered;
    private boolean isBurnInProtectionStarted;

    private List<IconData> icons;
    private WallpaperManager wallpaperManager;

    private Handler handler;
    private Runnable burnInRunnable = new Runnable() {
        @Override
        public void run() {
            if (booleanOptions[OPTION_BURN_IN_PROTECTION])
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

        leftLayout = (OverflowLinearLayout) v.findViewById(R.id.leftLayout);
        rightLayout = (OverflowLinearLayout) v.findViewById(R.id.rightLayout);
        centerLayout = (LinearLayout) v.findViewById(R.id.centerLayout);

        getOptions();

        if (booleanOptions[OPTION_ICON_ANIMATIONS]) {
            leftLayout.setLayoutTransition(new LayoutTransition());
            rightLayout.setLayoutTransition(new LayoutTransition());
            centerLayout.setLayoutTransition(new LayoutTransition());
        } else {
            leftLayout.setLayoutTransition(null);
            rightLayout.setLayoutTransition(null);
            centerLayout.setLayoutTransition(null);
        }

        addView(v);
        status.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                leftX = leftLayout.getX();
                leftY = leftLayout.getY();
                rightX = rightLayout.getX();
                rightY = rightLayout.getY();
                centerX = centerLayout.getX();
                centerY = centerLayout.getY();

                if (booleanOptions[OPTION_BURN_IN_PROTECTION] && !isBurnInProtectionStarted) {
                    handler.post(burnInRunnable);
                    isBurnInProtectionStarted = false;
                }

                status.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        if (booleanOptions[OPTION_AUTO_COLOR])
            setColor(intOptions[OPTION_COLOR]);
        else if (color != null) setColor(color);
        else setColor(Color.BLACK);

        iconColor = intOptions[OPTION_ICON_COLOR];

        status.setPadding(intOptions[OPTION_SIDE_PADDING], 0, intOptions[OPTION_SIDE_PADDING], 0);

        if (booleanOptions[OPTION_BUMP_MODE]) {
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

    private void getOptions() {
        for (int i = 0; i < OPTIONS.length; i++) {
            PreferenceData data = OPTIONS[i];
            if (data != null) {
                Object defaultValue = data.getDefaultValue();
                if (defaultValue instanceof Boolean)
                    booleanOptions[i] = data.getValue(getContext());
                else if (defaultValue instanceof Integer)
                    intOptions[i] = data.getValue(getContext());
            }
        }
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
                    int color = iconData.getGravity() == IconData.CENTER_GRAVITY && booleanOptions[OPTION_BUMP_MODE] ? Color.WHITE : iconColor;

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

        if (booleanOptions[OPTION_ICON_OVERLAP_PREVENTION]) {
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
        if ((booleanOptions[OPTION_FULLSCREEN] != isSystemShowing || booleanOptions[OPTION_SYSTEM_SHOWING] != isSystemShowing) && isSystemShowing)
            setStatusBarVisibility(false);
        booleanOptions[OPTION_SYSTEM_SHOWING] = isSystemShowing;
    }

    public boolean isSystemShowing() {
        return booleanOptions[OPTION_SYSTEM_SHOWING];
    }

    public void setFullscreen(boolean isFullscreen) {
        if (((getVisibility() == View.GONE) != isFullscreen) && !booleanOptions[OPTION_SYSTEM_SHOWING]) {
            setStatusBarVisibility(!isFullscreen);
        }

        booleanOptions[OPTION_FULLSCREEN] = isFullscreen;
    }

    public boolean isFullscreen() {
        return booleanOptions[OPTION_FULLSCREEN];
    }

    private void setStatusBarVisibility(final boolean visible) {
        if (booleanOptions[OPTION_ANIMATIONS]) {
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

        if (!booleanOptions[OPTION_TINTED_ICONS]) {
            if (booleanOptions[OPTION_ANIMATIONS]) {
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

            if (booleanOptions[OPTION_CONTRAST_ICONS])
                color = ColorUtils.isColorDark(backgroundColor) ? ColorUtils.lightColor(color) : ColorUtils.darkColor(color);

            if (booleanOptions[OPTION_ICON_ANIMATIONS]) {
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
        return PreferenceData.STATUS_COLOR.getValue(getContext());
    }

    @ColorInt
    private int getDefaultIconColor() {
        return PreferenceData.STATUS_ICON_COLOR.getValue(getContext());
    }

    private void setStatusBackgroundColor(@ColorInt int color) {
        status.setBackgroundColor(booleanOptions[OPTION_TRANSPARENT_MODE] ? Color.TRANSPARENT : Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
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

                if (booleanOptions[OPTION_TRANSPARENT_HOME]) {
                    status.setBackground(new BitmapDrawable(getResources(), background));
                    setDarkMode(!ColorUtils.isColorDark(color));
                    StatusView.this.color = color;
                } else setColor(color);
            } else setColor(getDefaultColor());
        }
    }

    public void setDarkMode(boolean isDarkMode) {
        if (booleanOptions[OPTION_CONTRAST_ICONS]) {
            int color = isDarkMode ? Color.BLACK : getDefaultIconColor();

            if (booleanOptions[OPTION_ICON_ANIMATIONS]) {
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

        setIconTint(status, color);
    }

    private void setIconTint(View view, @ColorInt int color) {
        if (view instanceof LinearLayout) {
            for (int i = 0; i < ((LinearLayout) view).getChildCount(); i++) {
                setIconTint(((LinearLayout) view).getChildAt(i), view.equals(centerLayout) && booleanOptions[OPTION_BUMP_MODE] ? Color.WHITE : color);
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
}
