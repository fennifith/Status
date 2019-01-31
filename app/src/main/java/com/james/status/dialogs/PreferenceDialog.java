package com.james.status.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import com.james.status.Status;

public class PreferenceDialog<T> extends ThemedCompatDialog {

    private T preference, defaultPreference;
    private OnPreferenceListener<T> listener;

    public PreferenceDialog(Context context) {
        super(context);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                cancel();
            }
        });
    }

    public PreferenceDialog(Context context, Status.Theme theme) {
        super(context, theme);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                cancel();
            }
        });
    }

    public void confirm() {
        if (hasListener()) getListener().onPreference(this, getPreference());
        if (isShowing()) dismiss();
    }

    public void cancel() {
        if (hasListener()) getListener().onCancel(this);
        if (isShowing()) dismiss();
    }

    public PreferenceDialog<T> setPreference(T preference) {
        this.preference = preference;
        return this;
    }

    public T getPreference() {
        return preference != null ? preference : getDefaultPreference();
    }

    public PreferenceDialog<T> setDefaultPreference(T preference) {
        defaultPreference = preference;
        return this;
    }

    public T getDefaultPreference() {
        return defaultPreference;
    }

    public PreferenceDialog<T> setListener(OnPreferenceListener<T> listener) {
        this.listener = listener;
        return this;
    }

    public boolean hasListener() {
        return listener != null;
    }

    public OnPreferenceListener<T> getListener() {
        return listener;
    }

    public interface OnPreferenceListener<T> {
        void onPreference(PreferenceDialog<T> dialog, T preference);

        void onCancel(PreferenceDialog<T> dialog);
    }
}
