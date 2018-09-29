package com.james.status.dialogs.picker;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.PreferenceDialog;

import androidx.annotation.StringRes;

public class FormatPickerDialog extends PreferenceDialog<String> {

    private EditText editText;
    private Integer hintResId;

    public FormatPickerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_format);

        editText = findViewById(R.id.editText);

        editText.setText(getPreference());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPreference(editText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (hintResId != null) ((TextView) findViewById(R.id.hint)).setText(hintResId);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    public FormatPickerDialog setHint(@StringRes int resId) {
        hintResId = resId;
        return this;
    }
}
