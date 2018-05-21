package com.james.status.data.preference;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.PreferenceData;

public class BasePreferenceData<T> implements View.OnClickListener {

    private final Context context;
    private final Identifier<T> identifier;
    private final OnPreferenceChangeListener<T> listener;

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

    public Context getContext() {
        return context;
    }

    public Identifier<T> getIdentifier() {
        return identifier;
    }

    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_text, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        if (identifier != null) {
            TextView title = holder.v.findViewById(R.id.title);
            TextView subtitle = holder.v.findViewById(R.id.subtitle);

            if (title != null)
                title.setText(identifier.getTitle());
            if (subtitle != null) {
                String text = identifier.getSubtitle();
                if (text.length() > 0) {
                    subtitle.setVisibility(View.VISIBLE);
                    subtitle.setText(text);
                } else subtitle.setVisibility(View.GONE);
            }
        }

        holder.v.setOnClickListener(this);
    }

    public void onPreferenceChange(T preference) {
        if (listener != null) listener.onPreferenceChange(preference);
    }

    @Override
    public void onClick(View v) {
    }

    public interface OnPreferenceChangeListener<T> {
        void onPreferenceChange(T preference);
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
        private T defaultValue;

        public Identifier(PreferenceData preference, @Nullable String title) {
            this(preference, title, null, null, null, (String[]) null);
        }

        public Identifier(PreferenceData preference, @Nullable String title, String... args) {
            this(preference, title, null, null, null, args);
        }

        public Identifier(PreferenceData preference, @Nullable String title, T defaultValue, String... args) {
            this(preference, title, null, null, defaultValue, args);
        }

        public Identifier(PreferenceData preference, @Nullable String title, SectionIdentifier sectionIdentifier) {
            this(preference, title, null, sectionIdentifier, null, (String[]) null);
        }

        public Identifier(PreferenceData preference, @Nullable String title, @Nullable String subtitle, SectionIdentifier sectionIdentifier) {
            this(preference, title, subtitle, sectionIdentifier, null, (String[]) null);
        }

        public Identifier(PreferenceData preference, @Nullable String title, @Nullable String subtitle, SectionIdentifier sectionIdentifier, T defaultValue, @Nullable String... args) {
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
            if (defaultValue != null) {
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
