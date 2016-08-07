package com.james.status.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatDialog;

import com.james.status.R;

public class PreferenceDialog<T> extends AppCompatDialog {

    private T preference;
    private OnPreferenceListener<T> listener;

    public PreferenceDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (hasListener()) getListener().onCancel();
            }
        });
    }

    public void confirm() {
        if (hasListener()) getListener().onPreference(getPreference());
        if (isShowing()) dismiss();
    }

    public void cancel() {
        if (hasListener()) getListener().onCancel();
        if (isShowing()) dismiss();
    }

    public PreferenceDialog setPreference(T preference) {
        this.preference = preference;
        return this;
    }

    public T getPreference() {
        return preference;
    }

    public PreferenceDialog setListener(OnPreferenceListener<T> listener) {
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
        void onPreference(T preference);

        void onCancel();
    }
}
