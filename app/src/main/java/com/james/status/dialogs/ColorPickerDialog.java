package com.james.status.dialogs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.utils.ColorUtils;
import com.james.status.views.CustomImageView;

public class ColorPickerDialog extends PreferenceDialog<Integer> {

    private CustomImageView colorImage;
    private TextView colorHex, redInt, greenInt, blueInt;
    private AppCompatSeekBar red, green, blue;

    private boolean isTrackingTouch;

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

        red.setProgress(Color.red(color));
        green.setProgress(Color.green(color));
        blue.setProgress(Color.blue(color));
        setColor(color);

        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, i, Color.green(color), Color.blue(color));
                setColor(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
            }
        });

        green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, Color.red(color), i, Color.blue(color));
                setColor(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
            }
        });

        blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, Color.red(color), Color.green(color), i);
                setColor(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
            }
        });

        LinearLayout presetLayout = (LinearLayout) findViewById(R.id.colors);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        int[] colors = getContext().getResources().getIntArray(R.array.defaultColors);
        for (int preset : colors) {
            View v = inflater.inflate(R.layout.item_color, null);

            CustomImageView colorView = (CustomImageView) v.findViewById(R.id.color);
            colorView.setImageDrawable(new ColorDrawable(preset));
            colorView.setTag(preset);
            colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    if (tag != null && tag instanceof Integer) {
                        setColor((int) tag);
                    }
                }
            });

            presetLayout.addView(v);
        }

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(getDefaultPreference());
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

    private void setColor(@ColorInt int color) {
        if (!isTrackingTouch && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator animator = ValueAnimator.ofArgb(getPreference(), color);
            animator.setDuration(250);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int color = (int) animation.getAnimatedValue();

                    colorImage.setImageDrawable(new ColorDrawable(color));
                    colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
                    colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);

                }
            });
            animator.start();
        } else {
            colorImage.setImageDrawable(new ColorDrawable(color));
            colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
            colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
            redInt.setText(String.valueOf(Color.red(color)));
            greenInt.setText(String.valueOf(Color.green(color)));
            blueInt.setText(String.valueOf(Color.blue(color)));
        }

        if (red.getProgress() != Color.red(color)) {
            red.setProgress(Color.red(color));
            redInt.setText(String.valueOf(Color.red(color)));
        }

        if (green.getProgress() != Color.green(color)) {
            green.setProgress(Color.green(color));
            greenInt.setText(String.valueOf(Color.green(color)));
        }

        if (blue.getProgress() != Color.blue(color)) {
            blue.setProgress(Color.blue(color));
            blueInt.setText(String.valueOf(Color.blue(color)));
        }

        findViewById(R.id.reset).setVisibility(getDefaultPreference() != null && color != getDefaultPreference() ? View.VISIBLE : View.GONE);

        setPreference(color);
    }

    @Override
    public void show() {
        super.show();
    }
}
