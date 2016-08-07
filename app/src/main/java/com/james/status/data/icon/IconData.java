package com.james.status.data.icon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.james.status.data.IconStyleData;
import com.james.status.utils.PreferenceUtils;

public class IconData {

    private Context context;
    private DrawableListener drawableListener;
    private TextListener textListener;
    private IconStyleData iconStyle;

    public IconData(Context context, PreferenceUtils.PreferenceIdentifier identifier, DrawableListener drawableListener) {
        this.context = context;

        iconStyle = PreferenceUtils.getObjectPreference(context, identifier, IconStyleData.class);
        if (iconStyle == null) iconStyle = getDefaultIconStyle();

        this.drawableListener = drawableListener;
    }

    public IconData(Context context, PreferenceUtils.PreferenceIdentifier identifier, DrawableListener drawableListener, TextListener textListener) {
        this.context = context;

        iconStyle = PreferenceUtils.getObjectPreference(context, identifier, IconStyleData.class);
        if (iconStyle == null) iconStyle = getDefaultIconStyle();

        this.drawableListener = drawableListener;
        this.textListener = textListener;
    }

    public Context getContext() {
        return context;
    }

    public DrawableListener getDrawableListener() {
        return drawableListener;
    }

    public TextListener getTextListener() {
        return textListener;
    }

    public void register() {
    }

    public void unregister() {
    }

    public IconStyleData getDefaultIconStyle() {
        return null;
    }

    public int getIconResource() {
        return iconStyle.resource[0];
    }

    public int getIconResource(int level) {
        return iconStyle.resource[level];
    }

    @Nullable
    public Drawable getDrawable() {
        return null;
    }

    public interface DrawableListener {
        void onUpdate(Drawable drawable);
    }

    public interface TextListener {
        void onUpdate(String text);
    }
}
