package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.james.status.R;

public class IntegerPickerDialog extends PreferenceDialog<Integer> {

    String unit;

    TextView scale;

    public IntegerPickerDialog(Context context, String unit) {
        super(context);
        this.unit = unit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_integer_picker);

        scale = (TextView) findViewById(R.id.scale);

        Integer preference = getPreference();
        if (preference == null) {
            preference = 0;
            setPreference(preference);
        }

        scale.setText(String.valueOf(preference));

        ((TextView) findViewById(R.id.unit)).setText(unit);

        findViewById(R.id.scaleUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer preference = getPreference();
                if (preference != null) {
                    preference++;
                    setPreference(preference);

                    scale.setText(String.valueOf(preference));
                }
            }
        });

        findViewById(R.id.scaleDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer preference = getPreference();
                if (preference != null) {
                    preference--;
                    setPreference(preference);

                    scale.setText(String.valueOf(preference));
                }
            }
        });

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
}
