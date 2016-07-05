package com.james.status.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.james.status.utils.ImageUtils;

public class CustomImageView extends AppCompatImageView {

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void transition(final Bitmap second) {
        if (second == null || second.getWidth() < 1 || second.getHeight() < 1) {
            Animation exitAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
            exitAnim.setDuration(150);
            exitAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
                    setVisibility(GONE);
                }
            });
            startAnimation(exitAnim);
            return;
        }

        final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        new Thread() {
            @Override
            public void run() {
                final Bitmap image;
                try {
                    image = ThumbnailUtils.extractThumbnail(second, size, size);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            setImageBitmap(second);
                        }
                    });
                    return;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Animation exitAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
                        exitAnim.setDuration(150);
                        exitAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                setImageBitmap(image);
                                setVisibility(VISIBLE);
                                Animation enterAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                                enterAnim.setDuration(150);
                                startAnimation(enterAnim);
                            }
                        });
                        startAnimation(exitAnim);
                    }
                });
            }
        }.start();
    }

    public void transition(Drawable second) {
        transition(ImageUtils.drawableToBitmap(second));
    }
}
