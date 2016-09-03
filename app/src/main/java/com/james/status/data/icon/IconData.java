package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;

public class IconData<T extends BroadcastReceiver> {

    private Context context;
    private DrawableListener drawableListener;
    private TextListener textListener;
    private int[] resource;
    private T receiver;

    private Drawable drawable;
    private String text;

    public IconData(Context context) {
        this.context = context;

        resource = getResourceIntPreference(PreferenceIdentifier.ICON_STYLE, "drawable");

        if (resource == null)
            resource = getDefaultIconResource();
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
        return resource[Math.abs(level % resource.length)];
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

    public Drawable getFakeDrawable() {
        if (hasDrawable())
            return VectorDrawableCompat.create(getContext().getResources(), resource[resource.length / 2], getContext().getTheme());
        else return new ColorDrawable(Color.TRANSPARENT);
    }

    public String getFakeText() {
        return "";
    }

    @Nullable
    public Boolean getBooleanPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier)))
            return prefs.getBoolean(getIdentifierString(identifier), false);
        else
            return null;
    }

    @Nullable
    public Integer getIntegerPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier)))
            return prefs.getInt(getIdentifierString(identifier), 0);
        else
            return null;
    }

    @Nullable
    public String getStringPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier)))
            return prefs.getString(getIdentifierString(identifier), null);
        else
            return null;
    }

    @Nullable
    public int[] getResourceIntPreference(PreferenceIdentifier identifier, String resourceType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();

        if (prefs.contains(getIdentifierString(identifier) + "-length")) {
            int length = prefs.getInt(getIdentifierString(identifier) + "-length", 0);
            int[] value = new int[length];

            for (int i = 0; i < length; i++) {
                if (prefs.contains(getIdentifierString(identifier) + "-" + i))
                    value[i] = resources.getIdentifier(prefs.getString(getIdentifierString(identifier) + "-" + i, null), resourceType, context.getPackageName());
                else return null;
            }

            return value;
        } else return null;
    }

    public void putPreference(PreferenceIdentifier identifier, boolean object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(getIdentifierString(identifier), object).apply();
    }

    public void putPreference(PreferenceIdentifier identifier, int object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(getIdentifierString(identifier), object).apply();
    }

    public void putPreference(PreferenceIdentifier identifier, String object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(getIdentifierString(identifier), object).apply();
    }

    public void putPreference(PreferenceIdentifier identifier, int[] object) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();

        prefs.edit().putInt(getIdentifierString(identifier) + "-length", object.length).apply();

        for (int i = 0; i < object.length; i++) {
            prefs.edit().putString(getIdentifierString(identifier) + "-" + i, resources.getResourceEntryName(object[i])).apply();
        }
    }

    private String getIdentifierString(PreferenceIdentifier identifier) {
        return getClass().getName() + "/" + identifier.toString();
    }

    public interface DrawableListener {
        void onUpdate(@Nullable Drawable drawable);
    }

    public interface TextListener {
        void onUpdate(@Nullable String text);
    }

    public enum PreferenceIdentifier {
        VISIBILITY,
        CENTER_GRAVITY,
        TEXT_VISIBILITY,
        TEXT_FORMAT,
        ICON_STYLE
    }
}
