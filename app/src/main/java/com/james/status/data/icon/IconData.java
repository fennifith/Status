package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.james.status.utils.PreferenceUtils;

public class IconData<T extends BroadcastReceiver> {

    private Context context;
    private DrawableListener drawableListener;
    private TextListener textListener;
    private int[] resource;
    private T receiver;

    private Drawable drawable;
    private String text;

    public IconData(Context context, PreferenceUtils.PreferenceIdentifier identifier) {
        this.context = context;

        if (identifier != null)
            resource = PreferenceUtils.getIntegerArrayPreference(context, identifier);
        if (resource == null) resource = getDefaultIconResource();
    }

    public Context getContext() {
        return context;
    }

    public void setDrawableListener(DrawableListener drawableListener) {
        this.drawableListener = drawableListener;
    }

    public boolean hasDrawableListener() {
        return drawableListener != null;
    }

    public DrawableListener getDrawableListener() {
        return drawableListener;
    }

    public void setTextListener(TextListener textListener) {
        this.textListener = textListener;
    }

    public boolean hasTextListener() {
        return textListener != null;
    }

    public TextListener getTextListener() {
        return textListener;
    }

    public void onDrawableUpdate(@Nullable Drawable drawable) {
        if (hasDrawable()) {
            if (hasDrawableListener()) getDrawableListener().onUpdate(drawable);
            this.drawable = drawable;
        }
    }

    public void onTextUpdate(@Nullable String text) {
        if (hasText()) {
            if (hasTextListener()) getTextListener().onUpdate(text);
            this.text = text;
        }
    }

    public boolean hasDrawable() {
        return true;
    }

    public boolean hasText() {
        return false;
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
        onDrawableUpdate(null);
    }

    public void unregister() {
        if (receiver != null) getContext().unregisterReceiver(receiver);
    }

    public int[] getDefaultIconResource() {
        return null;
    }

    public int getIconResource() {
        return resource[0];
    }

    public int getIconResource(int level) {
        return resource[level % resource.length];
    }

    @Nullable
    public Drawable getDrawable() {
        if (hasDrawable()) return drawable;
        else return null;
    }

    @Nullable
    public String getText() {
        if (hasText()) return text;
        else return null;
    }

    public interface DrawableListener {
        void onUpdate(@Nullable Drawable drawable);
    }

    public interface TextListener {
        void onUpdate(@Nullable String text);
    }
}
