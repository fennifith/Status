package com.james.status.data.icon;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

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
import com.james.status.utils.ColorUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class IconData<T extends IconUpdateReceiver> {

    public static final int LEFT_GRAVITY = -1, CENTER_GRAVITY = 0, RIGHT_GRAVITY = 1;

    private Context context;
    private ReDrawListener reDrawListener;
    private Typeface typeface;
    private T receiver;
    private int backgroundColor;

    private List<IconStyleData> styles;
    private IconStyleData style;
    private int level;

    private Bitmap bitmap;
    Paint iconPaint;
    Paint textPaint;
    private String text;

    int defaultTextColor;
    int defaultTextDarkColor;
    int defaultIconColor;
    int defaultIconDarkColor;

    int drawnTextColor;
    int drawnTextSize;
    int drawnTextAlpha;
    int drawnIconColor;
    int drawnIconSize;
    int drawnIconAlpha;
    int drawnPadding;

    int targetTextColor;
    int targetTextSize;
    int targetTextAlpha;
    int targetIconColor;
    int targetIconSize;
    int targetIconAlpha;
    int targetPadding;

    boolean isAnimations;

    public IconData(Context context) {
        this.context = context;

        iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setDither(true);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);

        styles = getIconStyles();
        level = 0;
        init();
        drawnIconColor = defaultIconColor;
        drawnTextColor = defaultTextColor;
    }

    public void init() {
        defaultIconColor = targetIconColor = (int) PreferenceData.ICON_ICON_COLOR.getSpecificOverriddenValue(
                getContext(), PreferenceData.STATUS_ICON_COLOR.getValue(getContext()), getIdentifierArgs());
        defaultIconDarkColor = PreferenceData.STATUS_DARK_ICON_COLOR.getValue(getContext());
        defaultTextColor = targetTextColor = (int) PreferenceData.ICON_TEXT_COLOR.getSpecificOverriddenValue(
                getContext(), PreferenceData.STATUS_ICON_TEXT_COLOR.getValue(getContext()), getIdentifierArgs());
        defaultTextDarkColor = PreferenceData.STATUS_DARK_ICON_TEXT_COLOR.getValue(getContext());
        targetIconSize = (int) StaticUtils.getPixelsFromDp((int) PreferenceData.ICON_ICON_SCALE.getSpecificValue(getContext(), getIdentifierArgs()));
        targetTextSize = StaticUtils.getPixelsFromSp(getContext(), (float) (int) PreferenceData.ICON_TEXT_SIZE.getSpecificValue(getContext(), getIdentifierArgs()));
        targetPadding = (int) StaticUtils.getPixelsFromDp((int) PreferenceData.ICON_ICON_PADDING.getSpecificValue(getContext(), getIdentifierArgs()));
        backgroundColor = PreferenceData.STATUS_COLOR.getValue(getContext());

        Typeface typefaceFont = Typeface.DEFAULT;
        String typefaceName = PreferenceData.ICON_TEXT_TYPEFACE.getSpecificOverriddenValue(getContext(), null, getIdentifierArgs());
        if (typefaceName != null) {
            try {
                typefaceFont = Typeface.createFromAsset(getContext().getAssets(), typefaceName);
            } catch (Exception ignored) {
            }
        }

        typeface = Typeface.create(typefaceFont, (int) PreferenceData.ICON_TEXT_EFFECT.getSpecificValue(getContext(), getIdentifierArgs()));

        drawnTextAlpha = 0;
        targetTextAlpha = 255;
        drawnIconAlpha = 0;
        targetIconAlpha = 255;

        isAnimations = PreferenceData.STATUS_ICON_ANIMATIONS.getValue(getContext());

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

    public final void setReDrawListener(ReDrawListener listener) {
        reDrawListener = listener;
    }

    public final void onIconUpdate(int level) {
        this.level = level;
        if (hasIcon()) {
            bitmap = style.getBitmap(context, level);
            if (reDrawListener != null)
                reDrawListener.onRequestReDraw();
        }
    }

    public final void onTextUpdate(@Nullable String text) {
        if (hasText()) {
            this.text = text;
            if (reDrawListener != null)
                reDrawListener.onRequestReDraw();
        }
    }

    public final void requestReDraw() {
        if (reDrawListener != null)
            reDrawListener.onRequestReDraw();
    }

    public final boolean isVisible() {
        return PreferenceData.ICON_VISIBILITY.getSpecificOverriddenValue(getContext(), isDefaultVisible(), getIdentifierArgs()) && StaticUtils.isPermissionsGranted(getContext(), getPermissions());
    }

    boolean isDefaultVisible() {
        return true;
    }

    public boolean canHazIcon() {
        //i can haz drawable resource
        return true;
    }

    public boolean hasIcon() {
        return canHazIcon() && PreferenceData.ICON_ICON_VISIBILITY.getSpecificOverriddenValue(getContext(), true, getIdentifierArgs()) && style != null;
    }

    public boolean canHazText() {
        //u can not haz text tho
        return false;
    }

    public boolean hasText() {
        return canHazText() && PreferenceData.ICON_TEXT_VISIBILITY.getSpecificOverriddenValue(getContext(), !canHazIcon(), getIdentifierArgs());
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
        onIconUpdate(-1);
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

    public final int getPosition() {
        return PreferenceData.ICON_POSITION.getSpecificValue(getContext(), getIdentifierArgs());
    }

    public int getDefaultGravity() {
        return RIGHT_GRAVITY;
    }

    public final int getGravity() {
        return PreferenceData.ICON_GRAVITY.getSpecificOverriddenValue(getContext(), getDefaultGravity(), getIdentifierArgs());
    }

    /**
     * Determines the color of the icon based on various settings,
     * some of which are icon-specific.
     *
     * @param color the color to be drawn behind the icon
     */
    public void setBackgroundColor(@ColorInt int color) {
        backgroundColor = color;
        if (PreferenceData.STATUS_TINTED_ICONS.getValue(getContext())) {
            targetIconColor = color;
            targetTextColor = color;
        } else {
            if ((boolean) PreferenceData.STATUS_DARK_ICONS.getValue(getContext()) && ColorUtils.isColorDark(color)) {
                targetIconColor = defaultIconColor;
                targetTextColor = defaultTextColor;
            } else {
                targetIconColor = defaultIconDarkColor;
                targetTextColor = defaultTextDarkColor;
            }
        }

        requestReDraw();
    }

    @Nullable
    public Bitmap getBitmap() {
        if (hasIcon()) return bitmap;
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

    public boolean needsDraw() {
        return drawnIconSize != targetIconSize ||
                Color.red(drawnIconColor) != Color.red(targetIconColor) ||
                Color.green(drawnIconColor) != Color.green(targetIconColor) ||
                Color.blue(drawnIconColor) != Color.blue(targetIconColor) ||
                drawnTextSize != targetTextSize ||
                Color.red(drawnTextColor) != Color.red(drawnTextColor) ||
                Color.green(drawnTextColor) != Color.green(drawnTextColor) ||
                Color.blue(drawnTextColor) != Color.blue(drawnTextColor) ||
                drawnPadding != targetPadding ||
                drawnTextAlpha != targetTextAlpha ||
                drawnIconAlpha != targetIconAlpha;
    }

    public void updateAnimatedValues() {
        if (isAnimations) {
            drawnIconColor = Color.rgb(
                    StaticUtils.getAnimatedValue(Color.red(drawnIconColor), Color.red(targetIconColor)),
                    StaticUtils.getAnimatedValue(Color.green(drawnIconColor), Color.green(targetIconColor)),
                    StaticUtils.getAnimatedValue(Color.blue(drawnIconColor), Color.blue(targetIconColor))
            );
            drawnIconSize = StaticUtils.getAnimatedValue(drawnIconSize, targetIconSize);
            drawnTextColor = Color.rgb(
                    StaticUtils.getAnimatedValue(Color.red(drawnTextColor), Color.red(targetTextColor)),
                    StaticUtils.getAnimatedValue(Color.green(drawnTextColor), Color.green(targetTextColor)),
                    StaticUtils.getAnimatedValue(Color.blue(drawnTextColor), Color.blue(targetTextColor))
            );
            drawnTextSize = StaticUtils.getAnimatedValue(drawnTextSize, targetTextSize);
            drawnPadding = StaticUtils.getAnimatedValue(drawnPadding, targetPadding);
            drawnTextAlpha = StaticUtils.getAnimatedValue(drawnTextAlpha, targetTextAlpha);
            drawnIconAlpha = StaticUtils.getAnimatedValue(drawnIconAlpha, targetIconAlpha);
        } else {
            drawnIconColor = targetIconColor;
            drawnIconSize = targetIconSize;
            drawnTextColor = targetTextColor;
            drawnTextSize = targetTextSize;
            drawnPadding = targetPadding;
            drawnTextAlpha = targetTextAlpha;
            drawnIconAlpha = targetIconAlpha;
        }
    }

    /**
     * Draws the icon on a canvas.
     *
     * @param canvas the canvas to draw on
     * @param x      the x position (LTR px) to start drawing the icon at
     * @param width  the available width for the icon to be drawn within
     */
    public void draw(Canvas canvas, int x, int width) {
        updateAnimatedValues();

        int iconColor = Color.rgb(
                StaticUtils.getMergedValue(Color.red(drawnIconColor), Color.red(backgroundColor), (float) drawnIconAlpha / 255),
                StaticUtils.getMergedValue(Color.green(drawnIconColor), Color.green(backgroundColor), (float) drawnIconAlpha / 255),
                StaticUtils.getMergedValue(Color.blue(drawnIconColor), Color.blue(backgroundColor), (float) drawnIconAlpha / 255)
        );
        iconPaint.setColor(iconColor);
        iconPaint.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
        textPaint.setColor(drawnTextColor);
        textPaint.setAlpha(drawnTextAlpha);
        textPaint.setTextSize(drawnTextSize);
        textPaint.setTypeface(typeface);

        x += drawnPadding;

        if (hasIcon() && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postScale((float) drawnIconSize / bitmap.getWidth(), (float) drawnIconSize / bitmap.getWidth());
            matrix.postTranslate(x, ((float) canvas.getHeight() - drawnIconSize) / 2);
            canvas.drawBitmap(bitmap, matrix, iconPaint);

            x += drawnIconSize + drawnPadding;
        }

        if (hasText() && text != null) {
            Paint.FontMetrics metrics = textPaint.getFontMetrics();
            canvas.drawText(text, x, (canvas.getHeight() / 2) + metrics.descent, textPaint);
        }
    }

    /**
     * Returns the estimated width (px) of the icon, or -1
     * if the icon needs to know the available space
     * first.
     *
     * @param height    the height (px) to scale the icon to
     * @param available the available width for the icon, or -1 if not yet calculated
     * @return the estimated width (px) of the icon
     */
    public int getWidth(int height, int available) {
        int width = 0;
        if ((hasIcon() && bitmap != null) || (hasText() && text != null))
            width += StaticUtils.getAnimatedValue(drawnPadding, targetPadding);

        if (hasIcon() && bitmap != null) {
            width += StaticUtils.getAnimatedValue(drawnIconSize, targetIconSize);
            width += StaticUtils.getAnimatedValue(drawnPadding, targetPadding);
        }

        if (hasText() && text != null) {
            Paint textPaint = new Paint();
            textPaint.setTextSize(StaticUtils.getAnimatedValue(drawnTextSize, targetTextSize));
            textPaint.setTypeface(typeface);

            Rect bounds = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            width += hasText() ? bounds.width() : 0;
            width += StaticUtils.getAnimatedValue(drawnPadding, targetPadding);
        }

        return width;
    }

    public List<BasePreferenceData> getPreferences() {
        List<BasePreferenceData> preferences = new ArrayList<>();

        if (canHazIcon() && (hasText() || !hasIcon())) {
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

        if (canHazText() && (hasIcon() || !hasText())) {
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

        if (hasIcon()) {
            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_ICON_SCALE,
                            getContext().getString(R.string.preference_icon_scale),
                            getIdentifierArgs()
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

        if (hasIcon()) {
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

    public interface ReDrawListener {
        void onRequestReDraw();
    }
}
