package com.james.status.dialogs.picker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.WhileHeldListener;

public class IntegerPickerDialog extends PreferenceDialog<Integer> {

    private String unit;

    @Nullable
    private Integer min, max;

    private TextView scale;

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

        findViewById(R.id.scaleUp).setOnTouchListener(new WhileHeldListener() {
            @Override
            public void onHeld() {
                Integer preference = getPreference();
                if (preference != null) {
                    preference++;
                    if (min != null) preference = Math.max(min, preference);
                    if (max != null) preference = Math.min(max, preference);

                    setPreference(preference);

                    scale.setText(String.valueOf(preference));
                }
            }
        });

        findViewById(R.id.scaleDown).setOnTouchListener(new WhileHeldListener() {
            @Override
            public void onHeld() {
                Integer preference = getPreference();
                if (preference != null) {
                    preference--;
                    if (min != null) preference = Math.max(min, preference);
                    if (max != null) preference = Math.min(max, preference);

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

    public IntegerPickerDialog setMinMax(@Nullable Integer min, @Nullable Integer max) {
        this.min = min;
        this.max = max;
        return this;
    }
}
