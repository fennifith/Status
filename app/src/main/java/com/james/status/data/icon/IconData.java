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
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.IconPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class IconData<T extends BroadcastReceiver> {

    private Context context;
    private DrawableListener drawableListener;
    private TextListener textListener;
    private int[] resource;
    private T receiver;

    private Drawable drawable;
    private String text;

    private View v;

    public IconData(Context context) {
        this.context = context;

        resource = getResourceIntPreference(PreferenceIdentifier.ICON_STYLE, "drawable");

        if (resource == null)
            resource = getDefaultIconResource();
    }

    public final Context getContext() {
        return context;
    }

    public final boolean hasDrawableListener() {
        return drawableListener != null;
    }

    public final DrawableListener getDrawableListener() {
        return drawableListener;
    }

    public final void setDrawableListener(DrawableListener drawableListener) {
        this.drawableListener = drawableListener;
    }

    public final boolean hasTextListener() {
        return textListener != null;
    }

    public final TextListener getTextListener() {
        return textListener;
    }

    public final void setTextListener(TextListener textListener) {
        this.textListener = textListener;
    }

    public final void onDrawableUpdate(@Nullable Drawable drawable) {
        if (hasDrawable()) {
            if (v != null) {
                CustomImageView iconView = (CustomImageView) v.findViewById(R.id.icon);

                if (iconView != null) {
                    if (drawable != null) {
                        v.setVisibility(View.VISIBLE);
                        iconView.setVisibility(View.VISIBLE);

                        ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                        if (layoutParams != null)
                            layoutParams.height = (int) StaticUtils.getPixelsFromDp(getContext(), getIconScale());
                        else
                            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) StaticUtils.getPixelsFromDp(getContext(), getIconScale()));

                        iconView.setLayoutParams(layoutParams);
                        iconView.setImageDrawable(drawable);
                    } else {
                        iconView.setVisibility(View.GONE);
                        if (getText() == null)
                            v.setVisibility(View.GONE);
                    }
                }
            }

            if (hasDrawableListener()) getDrawableListener().onUpdate(drawable);
            this.drawable = drawable;
        }
    }

    public final void onTextUpdate(@Nullable String text) {
        if (hasText()) {
            if (v != null) {
                TextView textView = (TextView) v.findViewById(R.id.text);

                if (text != null) {
                    v.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(text);
                } else {
                    textView.setVisibility(View.GONE);
                    if (getDrawable() == null)
                        v.setVisibility(View.GONE);
                }
            }

            if (hasTextListener()) getTextListener().onUpdate(text);
            this.text = text;
        }
    }

    public final boolean isVisible() {
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

    ;

    public IntentFilter getIntentFilter() {
        return new IntentFilter();
    }

    ;

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

    public final int getIconResource() {
        return resource[0];
    }

    public final int getIconResource(int level) {
        return resource[Math.abs(level % resource.length)];
    }

    public final int getIconPadding() {
        Integer padding = getIntegerPreference(PreferenceIdentifier.ICON_PADDING);
        if (padding == null) padding = 2;
        return padding;
    }

    public final int getIconScale() {
        Integer scale = getIntegerPreference(PreferenceIdentifier.ICON_SCALE);
        if (scale == null) scale = 24;
        return scale;
    }

    public final float getTextSize() {
        Integer size = getIntegerPreference(PreferenceIdentifier.TEXT_SIZE);
        if (size == null) size = 14;
        return size;
    }

    public final int getPosition() {
        Integer position = getIntegerPreference(PreferenceIdentifier.POSITION);
        if (position == null) position = 0;
        return position;
    }

    public final boolean isCentered() {
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

    @LayoutRes
    public int getIconLayout() {
        return R.layout.item_icon;
    }

    public View getIconView() {
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(getIconLayout(), null);
            v.setTag(this);

            float iconPaddingDp = StaticUtils.getPixelsFromDp(getContext(), getIconPadding());
            v.setPadding((int) iconPaddingDp, 0, (int) iconPaddingDp, 0);

            ((TextView) v.findViewById(R.id.text)).setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize());

            if (!hasDrawable()) v.findViewById(R.id.icon).setVisibility(View.GONE);
            if (!hasText()) v.findViewById(R.id.icon).setVisibility(View.GONE);
            v.setVisibility(View.GONE);
        }

        return v;
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
    public final Boolean getBooleanPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier))) {
            try {
                return prefs.getBoolean(getIdentifierString(identifier), false);
            } catch (ClassCastException e) {
                return null;
            }
        } else
            return null;
    }

    @Nullable
    public final Integer getIntegerPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier))) {
            try {
                return prefs.getInt(getIdentifierString(identifier), 0);
            } catch (ClassCastException e) {
                return null;
            }
        } else
            return null;
    }

    @Nullable
    public final String getStringPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier))) {
            try {
                return prefs.getString(getIdentifierString(identifier), null);
            } catch (ClassCastException e) {
                return null;
            }
        } else
            return null;
    }

    @Nullable
    public final int[] getResourceIntPreference(PreferenceIdentifier identifier, String resourceType) {
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

    public final void putPreference(PreferenceIdentifier identifier, boolean object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(getIdentifierString(identifier), object).apply();
    }

    public final void putPreference(PreferenceIdentifier identifier, int object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(getIdentifierString(identifier), object).apply();
    }

    public final void putPreference(PreferenceIdentifier identifier, String object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(getIdentifierString(identifier), object).apply();
    }

    public final void putPreference(PreferenceIdentifier identifier, int[] object) {
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

    public interface DrawableListener {
        void onUpdate(@Nullable Drawable drawable);
    }

    public interface TextListener {
        void onUpdate(@Nullable String text);
    }
}
