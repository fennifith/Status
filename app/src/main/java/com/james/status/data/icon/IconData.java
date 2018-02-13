package com.james.status.data.icon;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.james.status.data.PreferenceData;
import com.james.status.data.preference.BasePreferenceData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;
import com.james.status.data.preference.FontPreferenceData;
import com.james.status.data.preference.IconPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.data.preference.ListPreferenceData;
import com.james.status.receivers.IconUpdateReceiver;
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
    private Typeface typeface;
    private T receiver;

    private Drawable drawable;
    private String text;
    private int color;

    private View v;

    public IconData(Context context) {
        this.context = context;

        color = PreferenceData.STATUS_COLOR.getValue(context);

        List<IconStyleData> styles = getIconStyles();
        if (styles.size() > 0) {
            String name = PreferenceData.ICON_ICON_STYLE.getSpecificOverriddenValue(context, styles.get(0).name, getIdentifierArgs());
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

                    if (color != null && !((boolean) PreferenceData.STATUS_DARK_ICONS.getValue(getContext()) && (color == Color.WHITE || color == Color.BLACK))) {
                        textView.setTextColor(color);
                        textView.setTag(color);
                    } else textView.setTag(null);

                    textView.setTypeface(getTypeface(), getTextEffect());
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

    public final boolean isVisible() {
        return PreferenceData.ICON_VISIBILITY.getSpecificOverriddenValue(getContext(), isDefaultVisible(), getIdentifierArgs()) && StaticUtils.isPermissionsGranted(getContext(), getPermissions());
    }

    boolean isDefaultVisible() {
        return true;
    }

    public boolean canHazDrawable() {
        //i can haz drawable resource
        return true;
    }

    public boolean hasDrawable() {
        return canHazDrawable() && PreferenceData.ICON_ICON_VISIBILITY.getSpecificOverriddenValue(getContext(), true, getIdentifierArgs()) && style != null;
    }

    public boolean canHazText() {
        //u can not haz text tho
        return false;
    }

    public boolean hasText() {
        return canHazText() && PreferenceData.ICON_TEXT_VISIBILITY.getSpecificOverriddenValue(getContext(), !canHazDrawable(), getIdentifierArgs());
    }

    public String[] getPermissions() {
        return new String[]{};
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
        return PreferenceData.ICON_ICON_PADDING.getSpecificValue(getContext(), getIdentifierArgs());
    }

    public final int getIconScale() {
        return PreferenceData.ICON_ICON_SCALE.getSpecificValue(getContext(), getIdentifierArgs());
    }

    public final float getTextSize() {
        return (float) (int) PreferenceData.ICON_TEXT_SIZE.getSpecificValue(getContext(), getIdentifierArgs());
    }

    @ColorInt
    public final Integer getTextColor() {
        return PreferenceData.ICON_TEXT_COLOR.getSpecificValue(getContext(), getIdentifierArgs());
    }

    public final Integer getTextEffect() {
        return PreferenceData.ICON_TEXT_EFFECT.getSpecificValue(getContext(), getIdentifierArgs());
    }

    @Nullable
    public String getTypefaceName() {
        return PreferenceData.ICON_TEXT_TYPEFACE.getSpecificOverriddenValue(getContext(), null, getIdentifierArgs());
    }

    @Nullable
    public Typeface getTypeface() {
        if (typeface == null) {
            String name = getTypefaceName();
            if (name != null) {
                try {
                    typeface = Typeface.createFromAsset(getContext().getAssets(), name);
                } catch (Exception ignored) {
                }
            }
        }

        return typeface;
    }

    public final int getPosition() {
        return PreferenceData.ICON_POSITION.getSpecificValue(getContext(), getIdentifierArgs());
    }

    public int getDefaultGravity() {
        return RIGHT_GRAVITY;
    }

    public final int getGravity() {
        return PreferenceData.ICON_GRAVITY.getSpecificOverriddenValue(getContext(), getDefaultGravity(), getIdentifierArgs());
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

    public List<BasePreferenceData> getPreferences() {
        List<BasePreferenceData> preferences = new ArrayList<>();

        if (canHazDrawable() && (hasText() || !hasDrawable())) {
            preferences.add(new BooleanPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Boolean>(
                            PreferenceData.ICON_ICON_VISIBILITY,
                            getContext().getString(R.string.preference_show_drawable),
                            getIdentifierArgs()
                    ),
                    new BasePreferenceData.OnPreferenceChangeListener<Boolean>() {
                        @Override
                        public void onPreferenceChange(Boolean preference) {
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        if (canHazText() && (hasDrawable() || !hasText())) {
            preferences.add(new BooleanPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Boolean>(
                            PreferenceData.ICON_TEXT_VISIBILITY,
                            getContext().getString(R.string.preference_show_text),
                            getIdentifierArgs()
                    ),
                    new BasePreferenceData.OnPreferenceChangeListener<Boolean>() {
                        @Override
                        public void onPreferenceChange(Boolean preference) {
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        preferences.addAll(Arrays.asList(
                new ListPreferenceData(
                        getContext(),
                        new BasePreferenceData.Identifier<>(
                                PreferenceData.ICON_GRAVITY,
                                getContext().getString(R.string.preference_gravity),
                                getDefaultGravity(),
                                getIdentifierArgs()
                        ),
                        new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                            @Override
                            public void onPreferenceChange(Integer preference) {
                                StaticUtils.updateStatusService(getContext());
                            }
                        },
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
                        new BasePreferenceData.Identifier<Integer>(
                                PreferenceData.ICON_ICON_PADDING,
                                getContext().getString(R.string.preference_icon_padding),
                                getIdentifierArgs()
                        ),
                        getContext().getString(R.string.unit_dp),
                        null,
                        null,
                        new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                            @Override
                            public void onPreferenceChange(Integer preference) {
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                )
        ));

        if (hasDrawable()) {
            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_ICON_SCALE,
                            getContext().getString(R.string.preference_icon_scale)
                    ),
                    getContext().getString(R.string.unit_dp),
                    0,
                    null,
                    new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                        @Override
                        public void onPreferenceChange(Integer preference) {
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));
        }

        if (hasText()) {
            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_SIZE,
                            getContext().getString(R.string.preference_text_size)
                    ),
                    getContext().getString(R.string.unit_sp),
                    0,
                    null,
                    new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                        @Override
                        public void onPreferenceChange(Integer preference) {
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));

            preferences.add(new ColorPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_COLOR,
                            getContext().getString(R.string.preference_text_color),
                            getIdentifierArgs()
                    ),
                    new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                        @Override
                        public void onPreferenceChange(Integer preference) {
                            StaticUtils.updateStatusService(getContext());
                        }
                    }
            ));

            preferences.add(new FontPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<String>(
                            PreferenceData.ICON_TEXT_TYPEFACE,
                            getContext().getString(R.string.preference_text_font),
                            getIdentifierArgs()
                    ),
                    new BasePreferenceData.OnPreferenceChangeListener<String>() {
                        @Override
                        public void onPreferenceChange(String preference) {
                            StaticUtils.updateStatusService(getContext());
                        }
                    },
                    "Audiowide.ttf",
                    "BlackOpsOne.ttf",
                    "HennyPenny.ttf",
                    "Iceland.ttf",
                    "Megrim.ttf",
                    "Monoton.ttf",
                    "NewRocker.ttf",
                    "Nosifer.ttf",
                    "PermanentMarker.ttf",
                    "Playball.ttf",
                    "Righteous.ttf",
                    "Roboto.ttf",
                    "RobotoCondensed.ttf",
                    "RobotoSlab.ttf",
                    "VT323.ttf",
                    "Wallpoet.ttf"
            ));

            preferences.add(new ListPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_EFFECT,
                            getContext().getString(R.string.preference_text_effect),
                            getIdentifierArgs()
                    ),
                    new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                        @Override
                        public void onPreferenceChange(Integer preference) {
                            StaticUtils.updateStatusService(getContext());
                        }
                    },
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_none), Typeface.NORMAL),
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_bold), Typeface.BOLD),
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_italic), Typeface.ITALIC),
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_bold_italic), Typeface.BOLD_ITALIC)
            ));
        }

        if (hasDrawable()) {
            preferences.add(new IconPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<String>(
                            PreferenceData.ICON_ICON_STYLE,
                            getContext().getString(R.string.preference_icon_style),
                            getIdentifierArgs()
                    ),
                    this,
                    new BasePreferenceData.OnPreferenceChangeListener<IconStyleData>() {
                        @Override
                        public void onPreferenceChange(IconStyleData preference) {
                            style = preference;
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

    public String[] getIconNames() {
        return new String[]{};
    }

    public List<IconStyleData> getIconStyles() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        List<IconStyleData> styles = new ArrayList<>();
        String[] names = PreferenceData.ICON_ICON_STYLE_NAMES.getSpecificValue(getContext(), getIdentifierArgs());
        for (String name : names) {
            IconStyleData style = IconStyleData.fromSharedPreferences(prefs, getClass().getName(), name);
            if (style != null) styles.add(style);
        }

        return styles;
    }

    public final void addIconStyle(IconStyleData style) {
        if (style.getSize() == getIconStyleSize()) {
            List<String> list = new ArrayList<>(Arrays.asList((String[]) PreferenceData.ICON_ICON_STYLE_NAMES.getSpecificValue(getContext(), getIdentifierArgs())));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            style.writeToSharedPreferences(editor, getClass().getName());
            editor.apply();

            list.add(style.name);
            PreferenceData.ICON_ICON_STYLE_NAMES.setValue(context, list.toArray(new String[list.size()]), getIdentifierArgs());
        }
    }

    public final void removeIconStyle(IconStyleData style) {
        List<String> list = new ArrayList<>(Arrays.asList((String[]) PreferenceData.ICON_ICON_STYLE_NAMES.getSpecificValue(getContext(), getIdentifierArgs())));

        list.remove(style.name);
        PreferenceData.ICON_ICON_STYLE_NAMES.setValue(context, list.toArray(new String[list.size()]), getIdentifierArgs());
    }

    public String[] getIdentifierArgs() {
        return new String[]{getClass().getName()};
    }

    public interface DrawableListener {
        void onUpdate(@Nullable Drawable drawable);
    }

    public interface TextListener {
        void onUpdate(@Nullable String text);
    }
}
