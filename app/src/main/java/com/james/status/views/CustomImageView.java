package com.james.status.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
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

    public void setImageDrawable(Drawable drawable, @ColorInt int color) {
        super.setImageDrawable(drawable);
        setColorFilter(color);
    }

    public void transition(final Bitmap second) {
        if (second == null || second.getWidth() < 1 || second.getHeight() < 1) return;

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
                setImageBitmap(second);
                setVisibility(VISIBLE);
                Animation enterAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                enterAnim.setDuration(150);
                startAnimation(enterAnim);
            }
        });
        startAnimation(exitAnim);
    }

    public void transition(@Nullable Drawable second) {
        if (second != null) transition(ImageUtils.drawableToBitmap(second));
        else transition((Bitmap) null);
    }
}
