/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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

import com.james.status.R;
import com.james.status.Status;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import me.jfenn.androidutils.ColorUtils;
import me.jfenn.androidutils.DimenUtils;
import me.jfenn.androidutils.anim.AnimatedColor;
import me.jfenn.androidutils.anim.AnimatedFloat;
import me.jfenn.androidutils.anim.AnimatedInteger;

public abstract class IconData<T extends IconUpdateReceiver> {

    public static final int LEFT_GRAVITY = -1, CENTER_GRAVITY = 0, RIGHT_GRAVITY = 1;

    private Context context;
    private ReDrawListener reDrawListener;
    private Typeface typeface;
    private T receiver;
    int backgroundColor;

    private List<IconStyleData> styles;
    private IconStyleData style;
    private int level;

    private Bitmap bitmap;
    private String text;
    Paint iconPaint, textPaint;
    private boolean isIcon, isText;

    private AnimatedColor textColor;
    private AnimatedFloat textSize;
    private AnimatedInteger textAlpha;
    private int defaultTextDarkColor;
    private AnimatedInteger textOffsetX, textOffsetY;

    AnimatedColor iconColor;
    AnimatedInteger iconSize;
    AnimatedInteger iconAlpha;
    private int defaultIconDarkColor;
    private AnimatedInteger iconOffsetX, iconOffsetY;

    AnimatedInteger padding;

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

        textColor = new AnimatedColor(Color.WHITE);
        textSize = new AnimatedFloat(0);
        textAlpha = new AnimatedInteger(0);
        textOffsetX = new AnimatedInteger(0);
        textOffsetY = new AnimatedInteger(0);
        iconColor = new AnimatedColor(Color.WHITE);
        iconSize = new AnimatedInteger(0);
        iconAlpha = new AnimatedInteger(0);
        iconOffsetX = new AnimatedInteger(0);
        iconOffsetY = new AnimatedInteger(0);
        padding = new AnimatedInteger(0);

        init(true);
    }

    public void init() {
        init(false);
    }

    /**
     * Initialize all of the settings / variables of the icon to their
     * preference values. If it's the first init, this will also set up
     * the animated attributes; if not, it will simply update them to match
     * their 'default' values.
     *
     * @param isFirstInit Whether this is the first initialization of
     *                    the icon (this method may be called externally
     *                    to update values on a preference change without
     *                    restarting Status's entire service).
     */
    protected void init(boolean isFirstInit) {
        iconColor.setDefault((int) PreferenceData.ICON_ICON_COLOR_LIGHT.getSpecificOverriddenValue(getContext(),
                PreferenceData.STATUS_ICON_COLOR.getValue(getContext()), getIdentifierArgs()));
        defaultIconDarkColor = (int) PreferenceData.ICON_ICON_COLOR_DARK.getSpecificOverriddenValue(getContext(),
                PreferenceData.STATUS_DARK_ICON_COLOR.getValue(getContext()), getIdentifierArgs());

        textColor.setDefault((int) PreferenceData.ICON_TEXT_COLOR_LIGHT.getSpecificOverriddenValue(getContext(),
                PreferenceData.STATUS_ICON_TEXT_COLOR.getValue(getContext()), getIdentifierArgs()));
        defaultTextDarkColor = (int) PreferenceData.ICON_TEXT_COLOR_DARK.getSpecificOverriddenValue(getContext(),
                PreferenceData.STATUS_DARK_ICON_TEXT_COLOR.getValue(getContext()), getIdentifierArgs());

        iconSize.setDefault(DimenUtils.dpToPx((int) PreferenceData.ICON_ICON_SCALE.getSpecificValue(getContext(), getIdentifierArgs())));
        iconOffsetX.to((int) PreferenceData.ICON_ICON_OFFSET_X.getSpecificValue(getContext(), getIdentifierArgs()));
        iconOffsetY.to((int) PreferenceData.ICON_ICON_OFFSET_Y.getSpecificValue(getContext(), getIdentifierArgs()));
        textSize.setDefault((float) DimenUtils.spToPx((float) (int) PreferenceData.ICON_TEXT_SIZE.getSpecificValue(getContext(), getIdentifierArgs())));
        textOffsetX.to((int) PreferenceData.ICON_TEXT_OFFSET_X.getSpecificValue(getContext(), getIdentifierArgs()));
        textOffsetY.to((int) PreferenceData.ICON_TEXT_OFFSET_Y.getSpecificValue(getContext(), getIdentifierArgs()));
        padding.to(DimenUtils.dpToPx((int) PreferenceData.ICON_ICON_PADDING.getSpecificValue(getContext(), getIdentifierArgs())));

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
            onIconUpdate(level);
        }

        if (isFirstInit) {
            textAlpha.to(255);
            iconAlpha.to(255);
            textColor.setCurrent(textColor.getDefault());
            iconColor.setCurrent(iconColor.getDefault());
            textSize.toDefault();
            textOffsetX.setCurrent(textOffsetX.getTarget());
            textOffsetY.setCurrent(textOffsetY.getTarget());
            iconSize.toDefault();
            iconOffsetX.setCurrent(iconOffsetX.getTarget());
            iconOffsetY.setCurrent(iconOffsetY.getTarget());
        } else {
            if (isText)
                textSize.toDefault();
            if (isIcon)
                iconSize.toDefault();
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
        if (hasIcon() && style != null) {
            Bitmap bitmap = style.getBitmap(context, level);
            isIcon = bitmap != null;
            if (isIcon) {
                this.bitmap = bitmap;
                iconSize.toDefault();
            } else iconSize.to(0);

            if (reDrawListener != null)
                reDrawListener.onRequestReDraw();
        }
    }

    public final void onTextUpdate(@Nullable String text) {
        isText = text != null;
        if (isText) {
            this.text = text;
            textSize.toDefault();
        } else textSize.to(0f);

        if (hasText() && reDrawListener != null)
            reDrawListener.onRequestReDraw();
    }

    /**
     * Called when a message is sent to the icon from an external
     * service. This is mainly used for the NotificationIconData, so
     * that the NotificationService can send it information without
     * calling a static method or holding a direct reference to it.
     *
     * @param message           The message that was sent.
     */
    public void onMessage(Object... message) {
        // implemented in extending classes
    }

    /**
     * Called to request that the icon be drawn again; indicates that
     * an attribute of the icon has changed that the user should be
     * made aware of.
     */
    public final void requestReDraw() {
        if (reDrawListener != null)
            reDrawListener.onRequestReDraw();
    }

    /**
     * Evaluates whether the icon should be visible on the screen.
     *
     * @return True if the icon should be drawn.
     */
    public final boolean isVisible() {
        return PreferenceData.ICON_VISIBILITY.getSpecificOverriddenValue(getContext(), isDefaultVisible(), getIdentifierArgs()) && StaticUtils.isPermissionsGranted(getContext(), getPermissions());
    }

    /**
     * Determine whether the icon should be visible by default.
     *
     * @return True if the icon should be visible by
     *                          default.
     */
    boolean isDefaultVisible() {
        return true;
    }

    /**
     * Determine whether the element has the ability to draw an icon
     * (the Data icon, for example, is only text and will never contain
     * an icon).
     *
     * @return True if the element can draw an icon.
     */
    public boolean canHazIcon() {
        //i can haz drawable resource
        return true;
    }

    /**
     * Determine whether the element should have an icon, given its current
     * state and preferences.
     *
     * @return True if the element should have an icon.
     */
    public boolean hasIcon() {
        return canHazIcon() && PreferenceData.ICON_ICON_VISIBILITY.getSpecificOverriddenValue(getContext(), true, getIdentifierArgs()) && style != null;
    }

    /**
     * Determine whether the element has the ability to draw text data
     * (the NFC icon, for example, is only an indicator and can never
     * contain any text).
     *
     * @return True if the element can draw text.
     */
    public boolean canHazText() {
        //u can not haz text tho
        return false;
    }

    /**
     * Determine whether the element should have any text, given its current
     * state and preferences.
     *
     * @return True if the element should have text.
     */
    public boolean hasText() {
        return canHazText() && PreferenceData.ICON_TEXT_VISIBILITY.getSpecificOverriddenValue(getContext(), !canHazIcon(), getIdentifierArgs());
    }

    /**
     * Get an array of the app permissions required for the icon to function (if any).
     * Returns an empty array by default.
     *
     * @return A String[] of the permissions required by the icon.
     */
    public String[] getPermissions() {
        return new String[]{};
    }

    /**
     * Get the BroadcastReceiver tied to the icon.
     *
     * @return The BroadcastReceiver instance tied to the icon.
     */
    public T getReceiver() {
        return null;
    }

    /**
     * Get the IntentFilter that the icon receives updates from.
     *
     * @return An IntentFilter that the icon receives updates from.
     */
    public IntentFilter getIntentFilter() {
        return new IntentFilter();
    }

    /**
     * Register the icon to listen for updates. `requestRedraw()` can be called after this.
     */
    public void register() {
        if (receiver == null) receiver = getReceiver();
        if (receiver != null) getContext().registerReceiver(receiver, getIntentFilter());
        onIconUpdate(-1);
    }

    /**
     * Unregister the icon so that it no longer receives any updates. `requestRedraw()` should no longer
     * be called after this.
     */
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
        /*if (PreferenceData.STATUS_TINTED_ICONS.getValue(getContext())) { TODO: #137
            iconColor.to(color);
            textColor.to(color);
        } else {*/
        boolean isIconContrast = (Boolean) PreferenceData.STATUS_DARK_ICONS.getValue(getContext()) && !ColorUtils.isColorDark(color);
        iconColor.to(isIconContrast ? defaultIconDarkColor : iconColor.getDefault());
        textColor.to(isIconContrast ? defaultTextDarkColor : textColor.getDefault());
        //}

        requestReDraw();
    }

    /**
     * Sets the icon color manually.
     *
     * @param iconColor the color to use to draw the icon
     */
    public void setIconColor(@ColorInt @Nullable Integer iconColor) {
        if (iconColor != null)
            this.iconColor.to(iconColor);
        //else this.iconColor.toDefault();

        requestReDraw();
    }

    /**
     * Sets the text color manually.
     *
     * @param textColor the color to use to draw the text
     */
    public void setTextColor(@ColorInt @Nullable Integer textColor) {
        if (textColor != null)
            this.textColor.to(textColor);
        //else this.textColor.toDefault();

        requestReDraw();
    }

    public AnimatedColor getIconColor() {
        return iconColor;
    }

    public AnimatedInteger getIconAlpha() {
        return iconAlpha;
    }

    public int getBackgroundColor() {
        return backgroundColor;
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

    /**
     * Determine whether the icon needs to have another frame drawn.
     *
     * @return True if the icon should be re-drawn for
     *                      the next frame.
     */
    public boolean needsDraw() {
        return !iconSize.isTarget() ||
                !iconOffsetX.isTarget() ||
                !iconOffsetY.isTarget() ||
                !iconColor.isTarget() ||
                !textSize.isTarget() ||
                !textOffsetX.isTarget() ||
                !textOffsetY.isTarget() ||
                !textColor.isTarget() ||
                !padding.isTarget() ||
                !textAlpha.isTarget() ||
                !iconAlpha.isTarget();
    }

    /**
     * Update the animated attributes of the icon to match the current system
     * time.
     */
    public void updateAnimatedValues() {
        iconColor.next(isAnimations);
        iconSize.next(isAnimations);
        iconOffsetX.next(isAnimations);
        iconOffsetY.next(isAnimations);
        textColor.next(isAnimations);
        textSize.next(isAnimations);
        textOffsetX.next(isAnimations);
        textOffsetY.next(isAnimations);
        padding.next(isAnimations);
        textAlpha.next(isAnimations);
        iconAlpha.next(isAnimations);

        int drawnIconColor = iconColor.val();
        float drawnIconAlpha = ((float) iconAlpha.val() / 255) * ((float) Color.alpha(drawnIconColor) / 255);
        int iconColor = Color.rgb(
                StaticUtils.getMergedValue(Color.red(drawnIconColor), Color.red(backgroundColor), drawnIconAlpha),
                StaticUtils.getMergedValue(Color.green(drawnIconColor), Color.green(backgroundColor), drawnIconAlpha),
                StaticUtils.getMergedValue(Color.blue(drawnIconColor), Color.blue(backgroundColor), drawnIconAlpha)
        );
        iconPaint.setColor(iconColor);
        iconPaint.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
        textPaint.setColor(textColor.val());
        textPaint.setAlpha((int) (((float) textAlpha.val() / 255) * ((float) Color.alpha(textColor.val()) / 255) * 255));
        textPaint.setTextSize(textSize.val());
        textPaint.setTypeface(typeface);
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

        x += padding.val();

        if (hasIcon() && bitmap != null && iconSize.val() > 0) {
            x += iconOffsetX.val();

            if (iconSize.isTarget() && iconSize.val() != bitmap.getHeight()) {
                if (bitmap.getHeight() > iconSize.val())
                    bitmap = Bitmap.createScaledBitmap(bitmap, iconSize.val(), iconSize.val(), true);
                else {
                    Bitmap bitmap = style.getBitmap(context, level);
                    if (bitmap != null) {
                        this.bitmap = bitmap;
                        if (this.bitmap.getHeight() != iconSize.val())
                            this.bitmap = Bitmap.createScaledBitmap(bitmap, iconSize.val(), iconSize.val(), true);
                    }
                }
            }

            Matrix matrix = new Matrix();
            matrix.postScale((float) iconSize.val() / bitmap.getHeight(), (float) iconSize.val() / bitmap.getHeight());
            matrix.postTranslate(x, (((float) canvas.getHeight() - iconSize.val()) / 2) - iconOffsetY.val());
            canvas.drawBitmap(bitmap, matrix, iconPaint);

            x += iconSize.val() + padding.val() - iconOffsetX.val();
        }

        if (hasText() && text != null) {
            Paint.FontMetrics metrics = textPaint.getFontMetrics();
            canvas.drawText(text, x + textOffsetX.val(), ((canvas.getHeight() - metrics.descent - metrics.ascent) / 2) - textOffsetY.val(), textPaint);
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
        if ((!hasIcon() || iconSize.nextVal() == 0) && (!hasText() || textSize.nextVal() == 0))
            return 0;

        int width = 0;
        if ((hasIcon() && bitmap != null) || (hasText() && text != null))
            width += padding.nextVal();

        if (hasIcon() && bitmap != null) {
            width += iconSize.nextVal();
            width += padding.nextVal();
        }

        if (hasText() && text != null) {
            Paint textPaint = new Paint();
            textPaint.setTextSize(textSize.nextVal());
            textPaint.setTypeface(typeface);

            Rect bounds = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            width += hasText() ? bounds.width() : 0;
            width += padding.nextVal();
        }

        return width;
    }

    /**
     * Obtain a list of all of the "preferences" that can be used to modify the icon
     * in the user-facing settings UI.
     *
     * @return A list of the preferences that can be used to modify the
     *                      icon.
     */
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
                    preference -> {
                        StaticUtils.updateStatusService(getContext(), true);
                        ((Status) getContext().getApplicationContext()).onIconPreferenceChanged(IconData.this);
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
                    preference -> {
                        StaticUtils.updateStatusService(getContext(), true);
                        ((Status) getContext().getApplicationContext()).onIconPreferenceChanged(IconData.this);
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
                        preference -> StaticUtils.updateStatusService(getContext(), true),
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
                        preference -> StaticUtils.updateStatusService(getContext(), true)
                )
        ));

        if (hasIcon()) {
            preferences.add(new ColorPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_ICON_COLOR_LIGHT,
                            getContext().getString(R.string.preference_icon_color_light),
                            null,
                            getIdentifierArgs()
                    ),
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ).withAlpha(() -> true).withNullable(true));

            preferences.add(new ColorPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_ICON_COLOR_DARK,
                            getContext().getString(R.string.preference_icon_color_dark),
                            null,
                            getIdentifierArgs()
                    ),
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ).withAlpha(() -> true).withNullable(true));

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
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ));
        }

        if (hasText()) {
            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_SIZE,
                            getContext().getString(R.string.preference_text_size),
                            getIdentifierArgs()
                    ),
                    getContext().getString(R.string.unit_sp),
                    0,
                    null,
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ));

            preferences.add(new ColorPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_COLOR_LIGHT,
                            getContext().getString(R.string.preference_text_color_light),
                            null,
                            getIdentifierArgs()
                    ),
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ).withAlpha(() -> true).withNullable(true));

            preferences.add(new ColorPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_COLOR_DARK,
                            getContext().getString(R.string.preference_text_color_dark),
                            null,
                            getIdentifierArgs()
                    ),
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ).withAlpha(() -> true).withNullable(true));

            preferences.add(new FontPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<String>(
                            PreferenceData.ICON_TEXT_TYPEFACE,
                            getContext().getString(R.string.preference_text_font),
                            getIdentifierArgs()
                    ),
                    preference -> StaticUtils.updateStatusService(getContext(), true),
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
                    preference -> StaticUtils.updateStatusService(getContext(), true),
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_none), Typeface.NORMAL),
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_bold), Typeface.BOLD),
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_italic), Typeface.ITALIC),
                    new ListPreferenceData.ListPreference(getContext().getString(R.string.text_effect_bold_italic), Typeface.BOLD_ITALIC)
            ));

            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_OFFSET_X,
                            "Horizontal Text Offset",
                            getIdentifierArgs()
                    ),
                    "px",
                    -100,
                    100,
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ));

            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_TEXT_OFFSET_Y,
                            "Vertical Text Offset",
                            getIdentifierArgs()
                    ),
                    "px",
                    -100,
                    100,
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ));
        }

        if (hasIcon() && getIconStyleSize() > 0) {
            preferences.add(new IconPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<String>(
                            PreferenceData.ICON_ICON_STYLE,
                            getContext().getString(R.string.preference_icon_style),
                            getIdentifierArgs()
                    ),
                    this,
                    preference -> {
                        style = preference;
                        StaticUtils.updateStatusService(getContext(), true);
                    }
            ));

            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_ICON_OFFSET_X,
                            "Horizontal Icon Offset",
                            getIdentifierArgs()
                    ),
                    "px",
                    -100,
                    100,
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ));

            preferences.add(new IntegerPreferenceData(
                    getContext(),
                    new BasePreferenceData.Identifier<Integer>(
                            PreferenceData.ICON_ICON_OFFSET_Y,
                            "Vertical Icon Offset",
                            getIdentifierArgs()
                    ),
                    "px",
                    -100,
                    100,
                    preference -> StaticUtils.updateStatusService(getContext(), true)
            ));
        }

        return preferences;
    }

    /**
     * Get the amount of icons that each "icon style" should contain.
     *
     * @return An integer signifying the amount of icons
     *                          that each style should contain.
     */
    public int getIconStyleSize() {
        return 0;
    }

    /**
     * Obtain an array containing user-facing "names" of each icon in the
     * styles.
     *
     * @return An array containing user-facing "names" of
     *                          each icon in the style.
     */
    public String[] getIconNames() {
        return new String[]{};
    }

    /**
     * Get a list of all of the available icon styles for the icon.
     *
     * @return A list of all of the available icon styles for
     *                          the icon.
     */
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

    /**
     * Add a new icon style to the styles that can be used by the element. This
     * verifies the size of the style, then writes it to the icon's preferences.
     *
     * @param style             The new icon style to be added to the list of
     *                          styles used by the element.
     */
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

    /**
     * Remove an icon style from the styles that can be used by the element.
     *
     * @param style             The style to be removed.
     */
    public final void removeIconStyle(IconStyleData style) {
        List<String> list = new ArrayList<>(Arrays.asList((String[]) PreferenceData.ICON_ICON_STYLE_NAMES.getSpecificValue(getContext(), getIdentifierArgs())));

        list.remove(style.name);
        PreferenceData.ICON_ICON_STYLE_NAMES.setValue(context, list.toArray(new String[list.size()]), getIdentifierArgs());
    }

    /**
     * Get a list of the arguments that can be used to identify the icon in
     * SharedPreferences.
     *
     * @return A String[] of the arguments that can be used
     *                          to identify the icon in SharedPreferences.
     */
    public String[] getIdentifierArgs() {
        return new String[]{getClass().getName()};
    }

    public interface ReDrawListener {
        void onRequestReDraw();
    }
}
