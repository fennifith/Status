package com.james.status.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.utils.ColorUtils;
import com.james.status.views.CustomImageView;

public class ColorPickerDialog extends PreferenceDialog<Integer> {

    private CustomImageView colorImage;
    private TextView colorHex, redInt, greenInt, blueInt;
    private AppCompatSeekBar red, green, blue;

    public ColorPickerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_picker);

        colorImage = (CustomImageView) findViewById(R.id.color);
        colorHex = (TextView) findViewById(R.id.colorHex);
        red = (AppCompatSeekBar) findViewById(R.id.red);
        redInt = (TextView) findViewById(R.id.redInt);
        green = (AppCompatSeekBar) findViewById(R.id.green);
        greenInt = (TextView) findViewById(R.id.greenInt);
        blue = (AppCompatSeekBar) findViewById(R.id.blue);
        blueInt = (TextView) findViewById(R.id.blueInt);

        int color = getPreference();

        colorImage.setImageDrawable(new ColorDrawable(color));
        colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
        colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
        red.setProgress(Color.red(color));
        redInt.setText(String.valueOf(Color.red(color)));
        green.setProgress(Color.green(color));
        greenInt.setText(String.valueOf(Color.green(color)));
        blue.setProgress(Color.blue(color));
        blueInt.setText(String.valueOf(Color.blue(color)));

        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, i, Color.green(color), Color.blue(color));
                colorImage.setImageDrawable(new ColorDrawable(color));
                colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
                colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
                redInt.setText(String.valueOf(i));

                setPreference(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, Color.red(color), i, Color.blue(color));
                colorImage.setImageDrawable(new ColorDrawable(color));
                colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
                colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
                greenInt.setText(String.valueOf(i));

                setPreference(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, Color.red(color), Color.green(color), i);
                colorImage.setImageDrawable(new ColorDrawable(color));
                colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
                colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
                blueInt.setText(String.valueOf(i));

                setPreference(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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

    @Override
    public void show() {
        super.show();
    }

    public interface OnColorPickedListener {
        void onColorPicked(int color);
        void onCancel();
    }
}
