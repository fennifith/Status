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

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.IconPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public boolean isVisible() {
        Boolean isVisible = getBooleanPreference(PreferenceIdentifier.VISIBILITY);
        return (isVisible == null || isVisible) && (hasText() || hasDrawable());
    }

    public boolean canHazDrawable() {
        //i can haz drawable resource
        return true;
    }

    public boolean hasDrawable() {
        Boolean hasDrawable = getBooleanPreference(PreferenceIdentifier.ICON_VISIBILITY);
        return hasDrawable == null || hasDrawable;
    }

    public boolean canHazText() {
        //u can not haz text tho
        return false;
    }

    public boolean hasText() {
        Boolean hasText = getBooleanPreference(PreferenceIdentifier.TEXT_VISIBILITY);
        return hasText != null && hasText;
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
        List<IconStyleData> iconStyles = getIconStyles();
        if (iconStyles.size() > 0) return iconStyles.get(0).resource;
        else return null;
    }

    public int getIconResource() {
        return resource[0];
    }

    public int getIconResource(int level) {
        return resource[Math.abs(level % resource.length)];
    }

    public int getIconPadding() {
        Integer padding = getIntegerPreference(PreferenceIdentifier.ICON_PADDING);
        if (padding == null) padding = 2;
        return padding;
    }

    public int getIconScale() {
        Integer scale = getIntegerPreference(PreferenceIdentifier.ICON_SCALE);
        if (scale == null) scale = 24;
        return scale;
    }

    public float getTextSize() {
        Integer size = getIntegerPreference(PreferenceIdentifier.TEXT_SIZE);
        if (size == null) size = 14;
        return size;
    }

    public int getPosition() {
        Integer position = getIntegerPreference(PreferenceIdentifier.POSITION);
        if (position == null) position = 0;
        return position;
    }

    public boolean isCentered() {
        Boolean isCenter = getBooleanPreference(PreferenceIdentifier.CENTER_GRAVITY);
        if (isCenter == null) isCenter = false;
        return isCenter;
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

    public String getTitle() {
        return getClass().getSimpleName();
    }

    public List<PreferenceData> getPreferences() {
        List<PreferenceData> preferences = new ArrayList<>();

        if (canHazDrawable() && (hasText() || !hasDrawable())) {
            preferences.add(new BooleanPreferenceData(
                    getContext(),
                    new PreferenceData.Identifier(
                            getContext().getString(R.string.preference_show_drawable)
                    ),
                    hasDrawable(),
                    new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                        @Override
                        public void onPreferenceChange(Boolean preference) {
                            putPreference(PreferenceIdentifier.ICON_VISIBILITY, preference);
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        if (canHazText() && (hasDrawable() || !hasText())) {
            preferences.add(new BooleanPreferenceData(
                    getContext(),
                    new PreferenceData.Identifier(
                            getContext().getString(R.string.preference_show_text)
                    ),
                    hasText(),
                    new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                        @Override
                        public void onPreferenceChange(Boolean preference) {
                            putPreference(PreferenceIdentifier.TEXT_VISIBILITY, preference);
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        preferences.addAll(Arrays.asList(
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                getContext().getString(R.string.preference_center_gravity),
                                getContext().getString(R.string.preference_center_gravity_desc)
                        ),
                        isCentered(),
                        new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                            @Override
                            public void onPreferenceChange(Boolean preference) {
                                putPreference(PreferenceIdentifier.CENTER_GRAVITY, preference);
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                ),
                new IntegerPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                getContext().getString(R.string.preference_icon_padding)
                        ),
                        getIconPadding(),
                        getContext().getString(R.string.unit_dp),
                        null,
                        null,
                        new PreferenceData.OnPreferenceChangeListener<Integer>() {
                            @Override
                            public void onPreferenceChange(Integer preference) {
                                putPreference(PreferenceIdentifier.ICON_PADDING, preference);
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                )
        ));

        if (hasDrawable()) {
            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new PreferenceData.Identifier(
                            getContext().getString(R.string.preference_icon_scale)
                    ),
                    getIconScale(),
                    getContext().getString(R.string.unit_dp),
                    0,
                    null,
                    new PreferenceData.OnPreferenceChangeListener<Integer>() {
                        @Override
                        public void onPreferenceChange(Integer preference) {
                            putPreference(PreferenceIdentifier.ICON_SCALE, preference);
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        if (hasText()) {
            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new PreferenceData.Identifier(
                            getContext().getString(R.string.preference_text_size)
                    ),
                    (int) getTextSize(),
                    getContext().getString(R.string.unit_sp),
                    0,
                    null,
                    new PreferenceData.OnPreferenceChangeListener<Integer>() {
                        @Override
                        public void onPreferenceChange(Integer preference) {
                            putPreference(PreferenceIdentifier.TEXT_SIZE, preference);
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        if (hasDrawable() && getIconStyles().size() > 1) {
            preferences.add(new IconPreferenceData(
                    getContext(),
                    new PreferenceData.Identifier(
                            getContext().getString(R.string.preference_icon_style)
                    ),
                    getResourceIntPreference(PreferenceIdentifier.ICON_STYLE, "drawable"),
                    getIconStyles(),
                    new PreferenceData.OnPreferenceChangeListener<IconStyleData>() {
                        @Override
                        public void onPreferenceChange(IconStyleData preference) {
                            putPreference(PreferenceIdentifier.ICON_STYLE, preference.resource);
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        return preferences;
    }

    public List<IconStyleData> getIconStyles() {
        return new ArrayList<>();
    }

    @Nullable
    public Boolean getBooleanPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier))) {
            try {
                return prefs.getBoolean(getIdentifierString(identifier), false);
            } catch (ClassCastException e) {
                return null;
            }
        }
        else
            return null;
    }

    @Nullable
    public Integer getIntegerPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier))) {
            try {
                return prefs.getInt(getIdentifierString(identifier), 0);
            } catch (ClassCastException e) {
                return null;
            }
        }
        else
            return null;
    }

    @Nullable
    public String getStringPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier))) {
            try {
                return prefs.getString(getIdentifierString(identifier), null);
            } catch (ClassCastException e) {
                return null;
            }
        }
        else
            return null;
    }

    @Nullable
    public int[] getResourceIntPreference(PreferenceIdentifier identifier, String resourceType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();

        if (prefs.contains(getIdentifierString(identifier) + "-length")) {
            int length = 0;
            try {
                length = prefs.getInt(getIdentifierString(identifier) + "-length", 0);
            } catch (ClassCastException ignored) {
            }
            
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
        POSITION,
        CENTER_GRAVITY,
        TEXT_VISIBILITY,
        TEXT_FORMAT,
        TEXT_SIZE,
        ICON_VISIBILITY,
        ICON_STYLE,
        ICON_PADDING,
        ICON_SCALE
    }
}
