package com.james.status.dialogs;

import android.Manifest;
import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.activities.ImagePickerActivity;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CircleColorView;
import com.james.status.views.ColorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColorPickerDialog extends PreferenceDialog<Integer> implements Status.OnActivityResultListener {

    private Status status;
    private TextWatcher textWatcher;
    private List<Integer> presetColors;

    private ColorView colorImage;
    private AppCompatEditText colorHex;
    private TextView redInt, greenInt, blueInt, alphaInt;
    private AppCompatSeekBar red, green, blue, alpha;
    private View reset;

    private boolean isAlpha, isTrackingTouch;

    public ColorPickerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_picker);

        status = (Status) getContext().getApplicationContext();
        status.addListener(this);

        colorImage = findViewById(R.id.color);
        colorHex = findViewById(R.id.colorHex);
        red = findViewById(R.id.red);
        redInt = findViewById(R.id.redInt);
        green = findViewById(R.id.green);
        greenInt = findViewById(R.id.greenInt);
        blue = findViewById(R.id.blue);
        blueInt = findViewById(R.id.blueInt);
        alpha = findViewById(R.id.alpha);
        alphaInt = findViewById(R.id.alphaInt);
        reset = findViewById(R.id.reset);

        if (!isAlpha)
            findViewById(R.id.alphaView).setVisibility(View.GONE);

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
                color = Color.argb(isAlpha ? Color.alpha(color) : 255, i, Color.green(color), Color.blue(color));
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
                color = Color.argb(isAlpha ? Color.alpha(color) : 255, Color.red(color), i, Color.blue(color));
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
                color = Color.argb(isAlpha ? Color.alpha(color) : 255, Color.red(color), Color.green(color), i);
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

        alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(isAlpha ? i : 255, Color.red(color), Color.green(color), Color.blue(color));
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

        LinearLayout presetLayout = findViewById(R.id.colors);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (presetColors == null) presetColors = new ArrayList<>();
        for (int color : getContext().getResources().getIntArray(R.array.defaultColors)) {
            presetColors.add(color);
        }

        List<Integer> colors = new ArrayList<>();
        for (Integer color : presetColors) {
            if (!colors.contains(color)) colors.add(color);
        }

        for (int preset : colors) {
            View v = inflater.inflate(R.layout.item_color, presetLayout, false);

            CircleColorView colorView = v.findViewById(R.id.color);
            colorView.setColor(preset);
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

        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (!StaticUtils.isPermissionsGranted(getContext(), permissions) && getOwnerActivity() != null)
                    StaticUtils.requestPermissions(getOwnerActivity(), permissions);
                else
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
        if (!isTrackingTouch && animate) {
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), getPreference(), color);
            animator.setDuration(250);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int color = (int) animation.getAnimatedValue();
                    red.setProgress(Color.red(color));
                    green.setProgress(Color.green(color));
                    blue.setProgress(Color.blue(color));
                    alpha.setProgress(Color.alpha(color));
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
            colorImage.setColor(color);
            colorHex.removeTextChangedListener(textWatcher);
            colorHex.setText(String.format("#%06X", isAlpha ? color : (0xFFFFFF & color)));
            colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
            colorHex.addTextChangedListener(textWatcher);
            redInt.setText(String.valueOf(Color.red(color)));
            greenInt.setText(String.valueOf(Color.green(color)));
            blueInt.setText(String.valueOf(Color.blue(color)));
            alphaInt.setText(String.valueOf(Color.alpha(color)));

            if (red.getProgress() != Color.red(color)) red.setProgress(Color.red(color));
            if (green.getProgress() != Color.green(color)) green.setProgress(Color.green(color));
            if (blue.getProgress() != Color.blue(color)) blue.setProgress(Color.blue(color));
            if (alpha.getProgress() != Color.alpha(color)) alpha.setProgress(Color.alpha(color));
        }
    }

    public ColorPickerDialog withAlpha(boolean isAlpha) {
        this.isAlpha = isAlpha;
        return this;
    }

    public ColorPickerDialog setPresetColors(List<Integer> presetColors) {
        this.presetColors = presetColors;
        return this;
    }

    @Override
    public ColorPickerDialog setPreference(@ColorInt Integer preference) {
        Integer defaultPreference = getDefaultPreference();
        if (preference != null && defaultPreference != null && reset != null) {
            if (preference.equals(defaultPreference))
                reset.setVisibility(View.GONE);
            else reset.setVisibility(View.VISIBLE);
        }

        return (ColorPickerDialog) super.setPreference(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;

        if (requestCode == ImagePickerActivity.ACTION_PICK_IMAGE && resultCode == ImagePickerActivity.RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bitmap != null) {
            new ImageColorPickerDialog(getContext(), bitmap).setDefaultPreference(Color.BLACK).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                @Override
                public void onPreference(PreferenceDialog dialog, Integer preference) {
                    setColor(preference, false);
                    setPreference(preference);
                }

                @Override
                public void onCancel(PreferenceDialog dialog) {
                }
            }).show();
        }
    }

    @Override
    public void dismiss() {
        status.removeListener(this);
        super.dismiss();
    }
}
