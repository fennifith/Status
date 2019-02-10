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

package com.james.status.data.preference;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.PreferenceData;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

public class BasePreferenceData<T> implements View.OnClickListener {

    private final Context context;
    private final Identifier<T> identifier;
    private final OnPreferenceChangeListener<T> listener;
    private boolean isNullable;
    private T nullValue;
    private VisibilityInterface visibility;

    public BasePreferenceData(Context context, Identifier<T> identifier) {
        this.context = context;
        this.identifier = identifier;
        listener = null;
    }

    public BasePreferenceData(Context context, Identifier<T> identifier, OnPreferenceChangeListener<T> listener) {
        this.context = context;
        this.identifier = identifier;
        this.listener = listener;
    }

    /**
     * Specifies an interface to use to determine whether the preference should
     * be visible in the settings or not.
     *
     * @param visibility An interface invoked to determine whether the
     *                   preference should be visible under a set of
     *                   conditions.
     * @return "this", for method chaining.
     */
    public BasePreferenceData withVisibility(VisibilityInterface visibility) {
        this.visibility = visibility;
        return this;
    }

    /**
     * Determine whether the preference should be visible.
     *
     * @return True if the preference is visible in the settings.
     */
    public boolean isVisible() {
        if (visibility != null) {
            Object value = visibility.getValue();
            return value != null && value.equals(visibility.getDependent().getValue(context));
        } else return true;
    }

    /**
     * Obtain the "preference" that this setting is dependent on, assuming that
     * this preference's visibility is based on the value of another setting.
     *
     * If this preference doesn't have a "visibility" interface, this will return
     * null. It might return null anyway, who knows.
     *
     * @return A PreferenceData that this setting is dependent on. Or null.
     */
    @Nullable
    public PreferenceData getVisibilityDependent() {
        return visibility != null ? visibility.getDependent() : null;
    }

    /**
     * Set whether this preference could use "null" as a value. In other words, if
     * a nonexistent preference should be interpreted differently from simply
     * returning the default value.
     *
     * @param isNullable            Whether the preference's value can be null.
     * @return                      "this", for method chaining.
     */
    public BasePreferenceData withNullable(boolean isNullable) {
        if (isNullable)
            return withNullValue(null);
        else {
            this.isNullable = false;
            nullValue = null;
            return this;
        }
    }

    /**
     * Set a value of the preference that is equivalent to "null". For example,
     * an invalid integer value may be treated separately from the actual preference.
     * Ex: the height of an object, or "0" to disable the object entirely.
     *
     * @param nullValue             The value that should be treated as "null".
     * @return                      "this", for method chaining.
     */
    public BasePreferenceData withNullValue(T nullValue) {
        isNullable = true;
        this.nullValue = nullValue;
        return this;
    }

    /**
     * Determine whether the preference can contain a null value.
     *
     * @return True if the preference can contain a null value.
     */
    public boolean isNullable() {
        return isNullable;
    }

    /**
     * Obtain the value of the preference to be interpreted as "null". If a value
     * has not been set, this just returns null. Fair enough.
     *
     * @return The value of the preference to be interpreted
     *                              as "null" Might just be null.
     */
    @Nullable
    public T getNullValue() {
        return nullValue;
    }

    /**
     * Get the current application context.
     *
     * @return The current application context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Get the identifier of the preference. I'm not sure why this
     * is a separate class, it's mostly for search indexing and stuff.
     * I think I was intending to separate it from the PreferenceData
     * classes to index things other than settings as well, but never
     * got around to it.
     *
     * @return A redundant Identifier class containing... an identifier.
     */
    public Identifier<T> getIdentifier() {
        return identifier;
    }

    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_text, parent, false));
    }

    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (identifier != null) {
            final TextView title = holder.v.findViewById(R.id.title);
            TextView subtitle = holder.v.findViewById(R.id.subtitle);
            AppCompatCheckBox checkBox = holder.v.findViewById(R.id.checkBox);

            if (title != null)
                title.setText(identifier.getTitle());
            if (subtitle != null) {
                String text = identifier.getSubtitle();
                if (text.length() > 0) {
                    subtitle.setVisibility(View.VISIBLE);
                    subtitle.setText(text);
                } else subtitle.setVisibility(View.GONE);
            }

            if (checkBox != null) {
                checkBox.setVisibility(isNullable ? View.VISIBLE : View.GONE);
                if (isNullable) {
                    Object value = identifier.getPreferenceValue(getContext());
                    boolean isNonNull = value != null && !value.equals(nullValue);

                    if (title != null)
                        title.setAlpha(isNonNull ? 1 : 0.5f);

                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(isNonNull);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (title != null)
                                title.animate().alpha(isChecked ? 1 : 0.5f).start();

                            if (isChecked)
                                onClick(holder.itemView);
                            else {
                                identifier.setPreferenceValue(getContext(), null);
                                onPreferenceChange(null);
                            }

                            onBindViewHolder(holder, -1);
                        }
                    });
                }
            }
        }

        holder.v.setOnClickListener(this);
    }

    /**
     * Notify all observers that the preference has been changed.
     *
     * @param preference            The new value of the preference.
     */
    public void onPreferenceChange(T preference) {
        if (listener != null) listener.onPreferenceChange(preference);
    }

    @Override
    public void onClick(View v) {
    }

    public interface OnPreferenceChangeListener<T> {
        void onPreferenceChange(T preference);
    }

    public interface VisibilityInterface {
        PreferenceData getDependent();

        Object getValue();
    }

    public interface ValueGetter<X> {
        X get();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

    public static class Identifier<T> {

        @Nullable
        private String title, subtitle;
        private PreferenceData preference;
        private String[] args;
        private SectionIdentifier sectionIdentifier;
        private boolean isDefaultValue;
        private T defaultValue;

        public Identifier(PreferenceData preference, @Nullable String title) {
            init(preference, title, null, null, null, (String[]) null);
        }

        public Identifier(PreferenceData preference, @Nullable String title, String... args) {
            init(preference, title, null, null, null, args);
        }

        public Identifier(PreferenceData preference, @Nullable String title, T defaultValue, String... args) {
            init(preference, title, null, null, defaultValue, args);
            isDefaultValue = true;
        }

        public Identifier(PreferenceData preference, @Nullable String title, SectionIdentifier sectionIdentifier) {
            init(preference, title, null, sectionIdentifier, null, (String[]) null);
        }

        public Identifier(PreferenceData preference, @Nullable String title, @Nullable String subtitle, SectionIdentifier sectionIdentifier) {
            init(preference, title, subtitle, sectionIdentifier, null, (String[]) null);
        }

        public Identifier(PreferenceData preference, @Nullable String title, @Nullable String subtitle, SectionIdentifier sectionIdentifier, T defaultValue, @Nullable String... args) {
            init(preference, title, subtitle, sectionIdentifier, defaultValue, args);
            isDefaultValue = true;
        }

        private void init(PreferenceData preference, @Nullable String title, @Nullable String subtitle, SectionIdentifier sectionIdentifier, T defaultValue, @Nullable String... args) {
            this.preference = preference;
            this.title = title;
            this.subtitle = subtitle;
            this.sectionIdentifier = sectionIdentifier;
            this.defaultValue = defaultValue;
            this.args = args;
        }

        public String getTitle() {
            if (title != null) return title;
            else return "";
        }

        public String getSubtitle() {
            if (subtitle != null) return subtitle;
            else return "";
        }

        public PreferenceData getPreference() {
            return preference;
        }

        public T getPreferenceValue(Context context) {
            if (isDefaultValue) {
                return getPreferenceValue(context, defaultValue);
            } else return preference.getSpecificValue(context, args);
        }

        public T getPreferenceValue(Context context, T defaultValue) {
            return preference.getSpecificOverriddenValue(context, defaultValue, args);
        }

        public void setPreferenceValue(Context context, T value) {
            preference.setValue(context, value, args);
        }

        @Nullable
        public SectionIdentifier getSection() {
            return sectionIdentifier;
        }

        public enum SectionIdentifier {
            COLORS(R.string.section_colors),
            ICONS(R.string.section_icons),
            ANIMATIONS(R.string.section_animations),
            NOTIFICATIONS(R.string.section_notifications),
            OTHER(R.string.section_other);

            @StringRes
            private int nameRes;

            SectionIdentifier(@StringRes int nameRes) {
                this.nameRes = nameRes;
            }

            public String getName(Context context) {
                return context.getString(nameRes);
            }
        }
    }
}
