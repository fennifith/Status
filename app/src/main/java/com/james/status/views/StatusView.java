package com.james.status.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class StatusView extends View implements IconData.ReDrawListener {

    private int burnInOffsetX, burnInOffsetY;

    private int drawnBackgroundColor;
    private int targetBackgroundColor;
    private Paint paint;

    @Nullable
    private Bitmap backgroundImage;
    private boolean needsBackgroundImageDraw;
    private int defaultBackgroundColor;

    /**
     * True if ".register()" has been called on all of the icons
     */
    private boolean isRegistered;

    private boolean isBurnInProtection;
    private boolean isBurnInProtectionStarted;

    private boolean isFullscreen;
    private boolean isSystemShowing;

    private boolean isTransparentHome;
    private int sidePadding;
    private boolean isAnimations;

    private List<IconData> icons, leftIcons, centerIcons, rightIcons;
    private WallpaperManager wallpaperManager;

    private Handler handler;
    private Runnable burnInRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBurnInProtection)
                handler.postDelayed(this, 2000);
            else isBurnInProtectionStarted = false;

            switch (burnInOffsetX) {
                case 0:
                    burnInOffsetX++;
                    break;
                case 2:
                case 3:
                case 4:
                    burnInOffsetX++;
                    break;
                case 6:
                    burnInOffsetX++;
                    break;
                case 7:
                    burnInOffsetX = 0;
                    break;
                default:
                    burnInOffsetX++;
            }

            switch (burnInOffsetY) {
                case 0:
                case 1:
                case 2:
                    burnInOffsetY++;
                    break;
                case 4:
                case 5:
                case 6:
                    burnInOffsetY++;
                    break;
                case 7:
                    burnInOffsetY = 0;
                    break;
                default:
                    burnInOffsetY++;
            }

            postInvalidate();
        }
    };

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public StatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        icons = new ArrayList<>();
        leftIcons = new ArrayList<>();
        centerIcons = new ArrayList<>();
        rightIcons = new ArrayList<>();

        handler = new Handler();
        wallpaperManager = WallpaperManager.getInstance(getContext());

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        defaultBackgroundColor = drawnBackgroundColor = targetBackgroundColor = PreferenceData.STATUS_COLOR.getValue(getContext());
        init();
    }

    public void init() {
        isAnimations = PreferenceData.STATUS_ICON_ANIMATIONS.getValue(getContext());
        defaultBackgroundColor = PreferenceData.STATUS_COLOR.getValue(getContext());
        isTransparentHome = PreferenceData.STATUS_HOME_TRANSPARENT.getValue(getContext());
        sidePadding = (int) StaticUtils.getPixelsFromDp((int) PreferenceData.STATUS_SIDE_PADDING.getValue(getContext()));
        isBurnInProtection = PreferenceData.STATUS_BURNIN_PROTECTION.getValue(getContext());

        for (IconData icon : icons)
            icon.init();

        if (isBurnInProtection && !isBurnInProtectionStarted) {
            handler.post(burnInRunnable);
            isBurnInProtectionStarted = true;
        }

        sortIcons();
    }

    public void setIcons(List<IconData> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
        for (IconData icon : icons)
            icon.setReDrawListener(this);
        sortIcons();
    }

    private void sortIcons() {
        leftIcons.clear();
        centerIcons.clear();
        rightIcons.clear();

        for (IconData icon : icons) {
            if (!icon.isVisible())
                continue;

            switch (icon.getGravity()) {
                case IconData.LEFT_GRAVITY:
                    Log.d("StatusView", "left " + icon.getClass().getName());
                    leftIcons.add(icon);
                    break;
                case IconData.CENTER_GRAVITY:
                    Log.d("StatusView", "center " + icon.getClass().getName());
                    centerIcons.add(icon);
                    break;
                case IconData.RIGHT_GRAVITY:
                    Log.d("StatusView", "right " + icon.getClass().getName());
                    rightIcons.add(icon);
                    break;
            }
        }

        postInvalidate();
        Log.d("StatusView", "sorted icons");
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
        if ((isFullscreen != isSystemShowing || this.isSystemShowing != isSystemShowing) && isSystemShowing)
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
        targetBackgroundColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
        backgroundImage = null;
        needsBackgroundImageDraw = false;
        for (IconData icon : icons)
            icon.setBackgroundColor(targetBackgroundColor);

        postInvalidate();
    }

    @ColorInt
    public int getColor() {
        return drawnBackgroundColor;
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
        targetBackgroundColor = color;
        postInvalidate();
    }

    public void setTransparent() {
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

            setColor(color);
            if (isTransparentHome) {
                backgroundImage = background;
                needsBackgroundImageDraw = true;
            }
        } else setColor(defaultBackgroundColor);

        postInvalidate();
    }

    @Override
    public void onRequestReDraw() {
        postInvalidate();
    }

    private boolean needsDraw() {
        for (IconData icon : icons) {
            if (icon.isVisible() && icon.needsDraw())
                return true;
        }

        return Color.red(drawnBackgroundColor) != Color.red(targetBackgroundColor) ||
                Color.green(drawnBackgroundColor) != Color.green(targetBackgroundColor) ||
                Color.blue(drawnBackgroundColor) != Color.blue(targetBackgroundColor) ||
                needsBackgroundImageDraw;
    }

    private void updateAnimatedValues() {
        if (isAnimations) {
            drawnBackgroundColor = Color.rgb(
                    StaticUtils.getAnimatedValue(Color.red(drawnBackgroundColor), Color.red(targetBackgroundColor)),
                    StaticUtils.getAnimatedValue(Color.green(drawnBackgroundColor), Color.green(targetBackgroundColor)),
                    StaticUtils.getAnimatedValue(Color.blue(drawnBackgroundColor), Color.blue(targetBackgroundColor))
            );
        } else drawnBackgroundColor = targetBackgroundColor;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        updateAnimatedValues();

        if (needsBackgroundImageDraw)
            needsBackgroundImageDraw = false;

        if (backgroundImage != null)
            canvas.drawBitmap(backgroundImage, 0, 0, paint);
        else canvas.drawColor(drawnBackgroundColor);

        int leftWidth = 0, centerWidth = 0, rightWidth = 0;

        for (IconData icon : leftIcons) {
            int width = icon.getWidth(canvas.getHeight(), -1);
            leftWidth += width > 0 ? width : 0;
        }

        for (IconData icon : centerIcons) {
            int width = icon.getWidth(canvas.getHeight(), -1);
            centerWidth += width > 0 ? width : 0;
        }

        for (IconData icon : rightIcons) {
            int width = icon.getWidth(canvas.getHeight(), -1);
            rightWidth += width > 0 ? width : 0;
        }

        centerWidth = Math.min(centerWidth, canvas.getWidth() - (2 * sidePadding));
        leftWidth = Math.min(leftWidth, (canvas.getWidth() / 2) - (centerWidth / 2) - sidePadding);
        rightWidth = Math.min(rightWidth, (canvas.getWidth() / 2) - (centerWidth / 2) - sidePadding);

        if (leftWidth < 0 || rightWidth < 0) {
            leftWidth = 0;
            rightWidth = 0;
            centerWidth = canvas.getWidth() - (2 * sidePadding);
        }

        Log.d("StatusView", "widths " + leftWidth + " " + centerWidth + " " + rightWidth);

        for (int i = 0, x = sidePadding; i < leftIcons.size(); i++) {
            IconData icon = leftIcons.get(i);
            int width = icon.getWidth(canvas.getHeight(), leftWidth - x);
            if (width > 0) {
                icon.draw(canvas, x, width);
                x += width;
            }
        }

        for (int i = 0, x = (canvas.getWidth() / 2) - (centerWidth / 2); i < centerIcons.size(); i++) {
            IconData icon = centerIcons.get(i);
            int width = icon.getWidth(canvas.getHeight(), centerWidth - x);
            if (width > 0) {
                icon.draw(canvas, x, width);
                x += width;
            }
        }

        for (int i = 0, x = canvas.getWidth() - sidePadding; i < rightIcons.size(); i++) {
            IconData icon = rightIcons.get(i);
            int width = icon.getWidth(canvas.getHeight(), rightWidth - x);
            if (width > 0) {
                icon.draw(canvas, x - width, width);
                x -= width;
            } else
                Log.d("StatusView", "not enough room by " + width + ", " + icon.getClass().getName());
        }

        if (isBurnInProtection)
            canvas.translate(burnInOffsetX, burnInOffsetY);

        if (needsDraw())
            postInvalidate();
    }

}
