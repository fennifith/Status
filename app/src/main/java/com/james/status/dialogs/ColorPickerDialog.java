package com.james.status.dialogs;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.activities.ImagePickerActivity;
import com.james.status.utils.ColorUtils;
import com.james.status.views.CustomImageView;

public class ColorPickerDialog extends PreferenceDialog<Integer> {

    private Status status;
    private Status.OnColorPickedListener listener;
    private TextWatcher textWatcher;

    private CustomImageView colorImage;
    private AppCompatEditText colorHex;
    private TextView redInt, greenInt, blueInt;
    private AppCompatSeekBar red, green, blue;
    private View reset;

    private boolean isTrackingTouch;

    public ColorPickerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_picker);

        status = (Status) getContext().getApplicationContext();

        colorImage = (CustomImageView) findViewById(R.id.color);
        colorHex = (AppCompatEditText) findViewById(R.id.colorHex);
        red = (AppCompatSeekBar) findViewById(R.id.red);
        redInt = (TextView) findViewById(R.id.redInt);
        green = (AppCompatSeekBar) findViewById(R.id.green);
        greenInt = (TextView) findViewById(R.id.greenInt);
        blue = (AppCompatSeekBar) findViewById(R.id.blue);
        blueInt = (TextView) findViewById(R.id.blueInt);

        reset = findViewById(R.id.reset);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int color = Color.parseColor(colorHex.getText().toString());
                    setColor(color, true);
                    setPreference(color);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        colorHex.addTextChangedListener(textWatcher);

        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, i, Color.green(color), Color.blue(color));
                setColor(color, false);
                setPreference(color);
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
                setColor(color, false);
                setPreference(color);
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
                setColor(color, false);
                setPreference(color);
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

        setColor(getPreference(), false);

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
                        setColor((int) tag, true);
                        setPreference((int) tag);
                    }
                }
            });

            presetLayout.addView(v);
        }

        listener = new Status.OnColorPickedListener() {
            @Override
            public void onColorPicked(@Nullable Integer color) {
                if (color != null) {
                    setColor(color, false);
                    setPreference(color);
                }

                status.removeListener(this);
            }
        };

        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.addListener(listener);
                getContext().startActivity(new Intent(getContext(), ImagePickerActivity.class));
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = getDefaultPreference();
                setColor(color, true);
                setPreference(color);
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

    private void setColor(@ColorInt int color, boolean animate) {
        if (!isTrackingTouch && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && animate) {
            ValueAnimator animator = ValueAnimator.ofArgb(getPreference(), color);
            animator.setDuration(250);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int color = (int) animation.getAnimatedValue();

                    red.setProgress(Color.red(color));
                    green.setProgress(Color.green(color));
                    blue.setProgress(Color.blue(color));
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isTrackingTouch = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isTrackingTouch = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        } else {
            colorImage.setImageDrawable(new ColorDrawable(color));
            colorHex.removeTextChangedListener(textWatcher);
            colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
            colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
            colorHex.addTextChangedListener(textWatcher);
            redInt.setText(String.valueOf(Color.red(color)));
            greenInt.setText(String.valueOf(Color.green(color)));
            blueInt.setText(String.valueOf(Color.blue(color)));

            if (red.getProgress() != Color.red(color)) red.setProgress(Color.red(color));
            if (green.getProgress() != Color.green(color)) green.setProgress(Color.green(color));
            if (blue.getProgress() != Color.blue(color)) blue.setProgress(Color.blue(color));
        }
    }

    @Override
    public PreferenceDialog setPreference(@ColorInt Integer preference) {
        Integer defaultPreference = getDefaultPreference();
        if (preference != null && defaultPreference != null && reset != null) {
            if (preference.equals(defaultPreference))
                reset.setVisibility(View.GONE);
            else reset.setVisibility(View.VISIBLE);
        }

        return super.setPreference(preference);
    }
}
