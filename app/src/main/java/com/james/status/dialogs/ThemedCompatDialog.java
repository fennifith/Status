package com.james.status.dialogs;

import android.content.Context;

import com.james.status.Status;

import androidx.appcompat.app.AppCompatDialog;

public abstract class ThemedCompatDialog extends AppCompatDialog {

    public ThemedCompatDialog(Context context) {
        this(context, Status.Theme.DIALOG_NORMAL);
    }

    public ThemedCompatDialog(Context context, Status.Theme theme) {
        super(context, theme.getTheme(context));
    }

}
