/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
import android.util.AttributeSet;
import android.view.View;

import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import me.jfenn.androidutils.DimenUtils;
import me.jfenn.androidutils.anim.AnimatedColor;
import me.jfenn.androidutils.anim.AnimatedInteger;

public class StatusView extends View implements IconData.ReDrawListener {

    private int burnInOffsetX, burnInOffsetY;

    private AnimatedColor backgroundColor;
    private Paint paint;

    @Nullable
    private Bitmap backgroundImage;
    private boolean needsBackgroundImageDraw;

    /**
     * True if ".register()" has been called on all of the icons
     */
    private boolean isRegistered;

    private boolean isBurnInProtection;
    private boolean isBurnInProtectionStarted;

    private boolean isFullscreen;
    private boolean isSystemShowing;

    private boolean isTransparentHome;
    private AnimatedInteger sidePadding;
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

            if (burnInOffsetX == 1) {
                if (burnInOffsetY == 1) {
                    burnInOffsetY--;
                } else if (burnInOffsetY == -1) {
                    burnInOffsetX--;
                } else {
                    burnInOffsetY--;
                }
            } else if (burnInOffsetX == -1) {
                if (burnInOffsetY == 1) {
                    burnInOffsetX++;
                } else if (burnInOffsetY == -1) {
                    burnInOffsetY++;
                } else {
                    burnInOffsetY++;
                }
            } else {
                if (burnInOffsetY == 1) {
                    burnInOffsetX++;
                } else if (burnInOffsetY == -1) {
                    burnInOffsetX--;
                } else {
                    burnInOffsetX++;
                    burnInOffsetY++;
                }
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
        super(context, attrs, defStyleAttr);
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

        backgroundColor = new AnimatedColor((int) PreferenceData.STATUS_COLOR.getValue(getContext()));
        init();
    }

    @TargetApi(21)
    public StatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    public void init() {
        isAnimations = PreferenceData.STATUS_ICON_ANIMATIONS.getValue(getContext());
        backgroundColor.setDefault((int) PreferenceData.STATUS_COLOR.getValue(getContext()));
        isTransparentHome = PreferenceData.STATUS_HOME_TRANSPARENT.getValue(getContext());

        int sidePaddingInt = DimenUtils.dpToPx((int) PreferenceData.STATUS_SIDE_PADDING.getValue(getContext()));
        if (sidePadding == null)
            sidePadding = new AnimatedInteger(sidePaddingInt);
        else sidePadding.to(sidePaddingInt);

        isBurnInProtection = PreferenceData.STATUS_BURNIN_PROTECTION.getValue(getContext());

        for (IconData icon : icons)
            icon.init();

        if (backgroundImage != null)
            setTransparent();
        else setColor(backgroundColor.getTarget());

        if (isBurnInProtection && !isBurnInProtectionStarted) {
            handler.post(burnInRunnable);
            isBurnInProtectionStarted = true;
        }

        sortIcons();
        postInvalidate();
    }

    public void setIcons(List<IconData> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
        for (IconData icon : icons)
            icon.setReDrawListener(this);
        sortIcons();
    }

    /**
     * Vaguely hacky method of sending notifications to the NotificationIconData class.
     * This may be used for other things in the future.
     *
     * @param tClass  IconData class to send a message to
     * @param message the arguments of the message
     * @param <T>     type of IconData class to send the message to
     */
    public <T extends IconData> void sendMessage(Class<T> tClass, Object... message) {
        for (IconData icon : icons) {
            if (tClass.isAssignableFrom(icon.getClass()))
                icon.onMessage(message);
        }
    }

    private void sortIcons() {
        leftIcons.clear();
        centerIcons.clear();
        rightIcons.clear();

        for (IconData icon : icons) {
            int position = PreferenceData.ICON_POSITION.getSpecificOverriddenValue(getContext(), -1, icon.getIdentifierArgs());
            if (position < 0)
                PreferenceData.ICON_POSITION.setValue(getContext(), icons.indexOf(icon), icon.getIdentifierArgs());
        }

        Collections.sort(icons, new Comparator<IconData>() {
            @Override
            public int compare(IconData lhs, IconData rhs) {
                return lhs.getPosition() - rhs.getPosition();
            }
        });

        for (IconData icon : icons) {
            if (!icon.isVisible())
                continue;

            switch (icon.getGravity()) {
                case IconData.LEFT_GRAVITY:
                    leftIcons.add(icon);
                    break;
                case IconData.CENTER_GRAVITY:
                    centerIcons.add(icon);
                    break;
                case IconData.RIGHT_GRAVITY:
                    rightIcons.add(icon);
                    break;
            }
        }

        postInvalidate();
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
            animator.addUpdateListener(valueAnimator -> {
                setY((float) valueAnimator.getAnimatedValue());
                setAlpha(visible ? valueAnimator.getAnimatedFraction() : 1 - valueAnimator.getAnimatedFraction());
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (visible) setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!visible) setVisibility(View.GONE);
                    else setAlpha(1);
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
        backgroundColor.to(Color.argb(PreferenceData.STATUS_TRANSPARENT_MODE.getValue(getContext()) ? Color.alpha(color) : 255,
                Color.red(color), Color.green(color), Color.blue(color)));

        backgroundImage = null;
        needsBackgroundImageDraw = false;
        for (IconData icon : icons)
            icon.setBackgroundColor(color);

        postInvalidate();
    }

    @ColorInt
    public int getColor() {
        return backgroundColor.val();
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
        backgroundColor.to(color);
        postInvalidate();
    }

    public void setTransparent() {
        if (backgroundImage == null) {
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

            backgroundImage = ImageUtils.cropBitmapToBar(getContext(), me.jfenn.androidutils.ImageUtils.drawableToBitmap(backgroundDrawable));
        }

        if (backgroundImage != null) {
            int color = ColorUtils.getAverageColor(backgroundImage);

            setColor(color);
            if (isTransparentHome)
                needsBackgroundImageDraw = true;
            else backgroundImage = null;
        } else setColor(backgroundColor.getDefault());

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

        return needsBackgroundImageDraw
                || !backgroundColor.isTarget()
                || !sidePadding.isTarget();
    }

    private void updateAnimatedValues() {
        backgroundColor.next(isAnimations);
        sidePadding.next(isAnimations);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        updateAnimatedValues();

        if (needsBackgroundImageDraw)
            needsBackgroundImageDraw = false;

        if (backgroundImage != null)
            canvas.drawBitmap(backgroundImage, 0, 0, paint);
        else canvas.drawColor(backgroundColor.val());

        if (isBurnInProtection)
            canvas.translate(burnInOffsetX, burnInOffsetY);

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

        centerWidth = Math.min(centerWidth, canvas.getWidth() - (2 * sidePadding.val()));
        leftWidth = Math.min(leftWidth, (canvas.getWidth() / 2) - (centerWidth / 2) - sidePadding.val());
        rightWidth = Math.min(rightWidth, (canvas.getWidth() / 2) - (centerWidth / 2) - sidePadding.val());

        if (leftWidth < 0 || rightWidth < 0) {
            leftWidth = 0;
            rightWidth = 0;
            centerWidth = canvas.getWidth() - (2 * sidePadding.val());
        }

        for (int i = 0, x = sidePadding.val(); i < leftIcons.size(); i++) {
            IconData icon = leftIcons.get(i);
            int width = icon.getWidth(canvas.getHeight(), leftWidth - (x - sidePadding.val()));
            if (width > 0) {
                icon.draw(canvas, x, width);
                x += width;
            } else icon.updateAnimatedValues();
        }

        for (int i = 0, x = (canvas.getWidth() / 2) - (centerWidth / 2); i < centerIcons.size(); i++) {
            IconData icon = centerIcons.get(i);
            int width = icon.getWidth(canvas.getHeight(), centerWidth - x);
            if (width > 0) {
                icon.draw(canvas, x, width);
                x += width;
            } else icon.updateAnimatedValues();
        }

        for (int i = 0, x = canvas.getWidth() - sidePadding.val(); i < rightIcons.size(); i++) {
            IconData icon = rightIcons.get(i);
            int width = icon.getWidth(canvas.getHeight(), rightWidth - x);
            if (width > 0) {
                icon.draw(canvas, x - width, width);
                x -= width;
            } else icon.updateAnimatedValues();
        }

        if (needsDraw())
            postInvalidate();
    }

}
