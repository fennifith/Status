package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.james.status.R;

public class FormatDialog extends PreferenceDialog<String> {

    private EditText editText;
    private Integer hintResId;

    public FormatDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_format);

        editText = (EditText) findViewById(R.id.editText);

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

    public FormatDialog setHint(@StringRes int resId) {
        hintResId = resId;
        return this;
    }
}
