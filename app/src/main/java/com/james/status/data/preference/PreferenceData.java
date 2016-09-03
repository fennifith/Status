package com.james.status.data.preference;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.utils.PreferenceUtils;

public class PreferenceData<T> {

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
        return null;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    public void onPreferenceChange(T preference) {
        if (listener != null) listener.onPreferenceChange(preference);
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

        public String getTitle() {
            if (title != null) return title;
            else return "";
        }

        public String getSubtitle() {
            if (subtitle != null) return subtitle;
            else return "";
        }

        public PreferenceUtils.PreferenceIdentifier getPreference() {
            return identifier;
        }

        public SectionIdentifier getSection() {
            return sectionIdentifier;
        }

        public enum SectionIdentifier {
            COLORS,
            ICONS,
            NOTIFICATIONS
        }
    }
}
