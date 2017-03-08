package com.james.status.data.preference;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

public class PreferenceData<T> implements View.OnClickListener {

    private final Context context;
    private final Identifier identifier;
    private final OnPreferenceChangeListener<T> listener;

    public PreferenceData(Context context, Identifier identifier) {
        this.context = context;
        this.identifier = identifier;
        listener = null;
    }

    public PreferenceData(Context context, Identifier identifier, OnPreferenceChangeListener<T> listener) {
        this.context = context;
        this.identifier = identifier;
        this.listener = listener;
    }

    public Context getContext() {
        return context;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_text, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        if (identifier != null) {
            TextView title = (TextView) holder.v.findViewById(R.id.title);
            TextView subtitle = (TextView) holder.v.findViewById(R.id.subtitle);

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

    public static class Identifier {

        @Nullable
        private String title, subtitle;
        private PreferenceUtils.PreferenceIdentifier identifier;
        private SectionIdentifier sectionIdentifier;

        public Identifier(@Nullable String title) {
            this.title = title;
        }

        public Identifier(@Nullable String title, @Nullable String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        public Identifier(@Nullable String title, SectionIdentifier sectionIdentifier) {
            this.title = title;
            this.sectionIdentifier = sectionIdentifier;
        }

        public Identifier(@Nullable String title, @Nullable String subtitle, SectionIdentifier sectionIdentifier) {
            this.title = title;
            this.subtitle = subtitle;
            this.sectionIdentifier = sectionIdentifier;
        }

        public Identifier(PreferenceUtils.PreferenceIdentifier identifier, @Nullable String title, @Nullable String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        public Identifier(PreferenceUtils.PreferenceIdentifier identifier, @Nullable String title, SectionIdentifier sectionIdentifier) {
            this.identifier = identifier;
            this.title = title;
            this.sectionIdentifier = sectionIdentifier;
        }

        public Identifier(PreferenceUtils.PreferenceIdentifier identifier, @Nullable String title, @Nullable String subtitle, SectionIdentifier sectionIdentifier) {
            this.identifier = identifier;
            this.title = title;
            this.subtitle = subtitle;
            this.sectionIdentifier = sectionIdentifier;
        }

        @NonNull
        public String getTitle() {
            if (title != null) return title;
            else return "";
        }

        @NonNull
        public String getSubtitle() {
            if (subtitle != null) return subtitle;
            else return "";
        }

        @Nullable
        public PreferenceUtils.PreferenceIdentifier getPreference() {
            return identifier;
        }

        @Nullable
        public SectionIdentifier getSection() {
            return sectionIdentifier;
        }

        public enum SectionIdentifier {
            COLORS,
            ICONS,
            ANIMATIONS,
            NOTIFICATIONS,
            OTHER
        }
    }
}
