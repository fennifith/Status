package com.james.status.data.icon;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;
import com.james.status.data.preference.IconPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.data.preference.ListPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.receivers.IconUpdateReceiver;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class IconData<T extends IconUpdateReceiver> {

    public static final int LEFT_GRAVITY = -1, CENTER_GRAVITY = 0, RIGHT_GRAVITY = 1;

    private Context context;
    private DrawableListener drawableListener;
    private TextListener textListener;
    private IconStyleData style;
    private T receiver;

    private Drawable drawable;
    private String text;
    private int color;

    private View v;

    public IconData(Context context) {
        this.context = context;

        color = ColorUtils.getDefaultColor(context);

        String name = getStringPreference(PreferenceIdentifier.ICON_STYLE);
        List<IconStyleData> styles = getIconStyles();
        if (styles.size() > 0) {
            if (name != null) {
                for (IconStyleData style : styles) {
                    if (style.name.equals(name)) {
                        this.style = style;
                        break;
                    }
                }
            }

            if (style == null) style = styles.get(0);
        }
    }

    public final Context getContext() {
        return context;
    }

    public final void setColor(@ColorInt int color) {
        this.color = color;
    }

    @ColorInt
    public final int getColor() {
        return color;
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

    public final void onDrawableUpdate(int level) {
        if (hasDrawable()) {
            drawable = style.getDrawable(context, level);

            if (v != null) {
                CustomImageView iconView = (CustomImageView) v.findViewById(R.id.icon);

                if (iconView != null) {
                    if (drawable != null) {
                        v.setVisibility(View.VISIBLE);
                        iconView.setVisibility(View.VISIBLE);

                        ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                        if (layoutParams != null)
                            layoutParams.height = (int) StaticUtils.getPixelsFromDp(getIconScale());
                        else
                            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) StaticUtils.getPixelsFromDp(getIconScale()));

                        iconView.setLayoutParams(layoutParams);
                        iconView.setImageDrawable(drawable);
                    } else {
                        iconView.setVisibility(View.GONE);
                        if (canHazText() && getText() == null)
                            v.setVisibility(View.GONE);
                    }
                }
            }
        }

        if (hasDrawableListener()) getDrawableListener().onUpdate(drawable);
    }

    public final void onTextUpdate(@Nullable String text) {
        if (hasText()) {
            if (v != null) {
                TextView textView = (TextView) v.findViewById(R.id.text);

                if (text != null) {
                    v.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);

                    Integer color = getTextColor();
                    Boolean isContrast = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);

                    if (color != null && !((isContrast == null || isContrast) && (color == Color.WHITE || color == Color.BLACK))) {
                        textView.setTextColor(color);
                        textView.setTag(color);
                    } else textView.setTag(null);

                    textView.setText(text);
                } else {
                    textView.setVisibility(View.GONE);
                    if (canHazDrawable() && getDrawable() == null)
                        v.setVisibility(View.GONE);
                }
            }

            if (hasTextListener()) getTextListener().onUpdate(text);
            this.text = text;
        }
    }

    public boolean isVisible() {
        Boolean isVisible = getBooleanPreference(PreferenceIdentifier.VISIBILITY);
        return isVisible == null || isVisible;
    }

    public boolean canHazDrawable() {
        //i can haz drawable resource
        return true;
    }

    public boolean hasDrawable() {
        Boolean hasDrawable = getBooleanPreference(PreferenceIdentifier.ICON_VISIBILITY);
        return canHazDrawable() && (hasDrawable == null || hasDrawable) && style != null;
    }

    public boolean canHazText() {
        //u can not haz text tho
        return false;
    }

    public boolean hasText() {
        Boolean hasText = getBooleanPreference(PreferenceIdentifier.TEXT_VISIBILITY);
        return canHazText() && (hasText != null && hasText);
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
        onDrawableUpdate(-1);
    }

    public void unregister() {
        if (receiver != null) getContext().unregisterReceiver(receiver);
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

    @Nullable
    @ColorInt
    public final Integer getTextColor() {
        return getIntegerPreference(PreferenceIdentifier.TEXT_COLOR);
    }

    public final int getPosition() {
        Integer position = getIntegerPreference(PreferenceIdentifier.POSITION);
        if (position == null) position = 0;
        return position;
    }

    public int getDefaultGravity() {
        return RIGHT_GRAVITY;
    }

    public final int getGravity() {
        Integer gravity = getIntegerPreference(PreferenceIdentifier.GRAVITY);
        if (gravity == null) gravity = getDefaultGravity();
        return gravity;
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

            float iconPaddingDp = StaticUtils.getPixelsFromDp(getIconPadding());
            v.setPadding((int) iconPaddingDp, 0, (int) iconPaddingDp, 0);

            TextView textView = (TextView) v.findViewById(R.id.text);
            if (textView != null) textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize());

            View iconView = v.findViewById(R.id.icon);
            if (iconView != null && !hasDrawable()) iconView.setVisibility(View.GONE);
            if (textView != null && !hasText()) textView.setVisibility(View.GONE);
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
                new ListPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                getContext().getString(R.string.preference_gravity)
                        ),
                        new PreferenceData.OnPreferenceChangeListener<Integer>() {
                            @Override
                            public void onPreferenceChange(Integer preference) {
                                putPreference(PreferenceIdentifier.GRAVITY, preference);
                                StaticUtils.updateStatusService(getContext());
                            }
                        },
                        getGravity(),
                        new ListPreferenceData.ListPreference(
                                getContext().getString(R.string.gravity_left),
                                LEFT_GRAVITY
                        ),
                        new ListPreferenceData.ListPreference(
                                getContext().getString(R.string.gravity_center),
                                CENTER_GRAVITY
                        ),
                        new ListPreferenceData.ListPreference(
                                getContext().getString(R.string.gravity_right),
                                RIGHT_GRAVITY
                        )
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

            Integer color = getTextColor();
            preferences.add(new ColorPreferenceData(
                    getContext(),
                    new PreferenceData.Identifier(
                            "Text Color"
                    ),
                    color != null ? color : Color.WHITE,
                    new PreferenceData.OnPreferenceChangeListener<Integer>() {
                        @Override
                        public void onPreferenceChange(Integer preference) {
                            putPreference(PreferenceIdentifier.TEXT_COLOR, preference);
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        if (hasDrawable()) {
            preferences.add(new IconPreferenceData(
                    getContext(),
                    new PreferenceData.Identifier(
                            getContext().getString(R.string.preference_icon_style)
                    ),
                    style,
                    this,
                    new PreferenceData.OnPreferenceChangeListener<IconStyleData>() {
                        @Override
                        public void onPreferenceChange(IconStyleData preference) {
                            style = preference;
                            putPreference(PreferenceIdentifier.ICON_STYLE, preference.name);
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        return preferences;
    }

    public int getIconStyleSize() {
        return 0;
    }

    public List<IconStyleData> getIconStyles() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        List<IconStyleData> styles = new ArrayList<>();
        String[] names = getStringArrayPreference(PreferenceIdentifier.ICON_STYLE_NAMES);
        if (names != null) {
            for (String name : names) {
                IconStyleData style = IconStyleData.fromSharedPreferences(prefs, getClass().getName(), name);
                if (style != null) styles.add(style);
            }
        }

        return styles;
    }

    public final void addIconStyle(IconStyleData style) {
        if (style.getSize() == getIconStyleSize()) {
            String[] names = getStringArrayPreference(PreferenceIdentifier.ICON_STYLE_NAMES);
            List<String> list = new ArrayList<>();
            if (names != null) list.addAll(Arrays.asList(names));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            style.writeToSharedPreferences(editor, getClass().getName());
            editor.apply();

            list.add(style.name);
            putPreference(PreferenceIdentifier.ICON_STYLE_NAMES, list.toArray(new String[list.size()]));
        }
    }

    public final void removeIconStyle(IconStyleData style) {
        String[] names = getStringArrayPreference(PreferenceIdentifier.ICON_STYLE_NAMES);
        List<String> list = new ArrayList<>();
        if (names != null) list.addAll(Arrays.asList(names));

        list.remove(style.name);
        putPreference(PreferenceIdentifier.ICON_STYLE_NAMES, list.toArray(new String[list.size()]));
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

    public final String[] getStringArrayPreference(PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier) + "-length")) {
            String[] array = new String[prefs.getInt(getIdentifierString(identifier) + "-length", 0)];
            for (int i = 0; i < array.length; i++) {
                array[i] = prefs.getString(getIdentifierString(identifier) + "-" + i, null);
            }

            return array;
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

    public final void putPreference(PreferenceIdentifier identifier, String[] object) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putInt(getIdentifierString(identifier) + "-length", object.length);
        for (int i = 0; i < object.length; i++) {
            editor.putString(getIdentifierString(identifier) + "-" + i, object[i]);
        }

        editor.apply();
    }

    private String getIdentifierString(PreferenceIdentifier identifier) {
        return getClass().getName() + "/" + identifier.toString();
    }

    public enum PreferenceIdentifier {
        VISIBILITY,
        POSITION,
        GRAVITY,
        TEXT_VISIBILITY,
        TEXT_FORMAT,
        TEXT_SIZE,
        TEXT_COLOR,
        ICON_VISIBILITY,
        ICON_STYLE,
        ICON_STYLE_NAMES,
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
