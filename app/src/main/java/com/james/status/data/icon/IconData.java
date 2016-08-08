package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.james.status.data.IconStyleData;
import com.james.status.utils.PreferenceUtils;

public class IconData<T extends BroadcastReceiver> {

    private Context context;
    private DrawableListener drawableListener;
    private TextListener textListener;
    private IconStyleData iconStyle;
    private T receiver;

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

    public boolean hasDrawableListener() {
        return drawableListener != null;
    }

    public DrawableListener getDrawableListener() {
        return drawableListener;
    }

    public boolean hasTextListener() {
        return textListener != null;
    }

    public TextListener getTextListener() {
        return textListener;
    }

    public void onDrawableUpdate(@Nullable Drawable drawable) {
        if (hasDrawableListener()) getDrawableListener().onUpdate(drawable);
    }

    public void onTextUpdate(@Nullable String text) {
        if (hasTextListener()) getTextListener().onUpdate(text);
    }

    public T getReceiver() {
        return null;
    }

    public IntentFilter getIntentFilter() {
        return new IntentFilter();
    }

    public void register() {
        if (receiver == null) receiver = getReceiver();
        if (receiver != null) getContext().registerReceiver(receiver, getIntentFilter());
    }

    public void unregister() {
        if (receiver != null) getContext().unregisterReceiver(receiver);
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
        void onUpdate(@Nullable Drawable drawable);
    }

    public interface TextListener {
        void onUpdate(@Nullable String text);
    }
}
