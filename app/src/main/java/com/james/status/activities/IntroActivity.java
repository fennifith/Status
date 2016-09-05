package com.james.status.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.adapters.SimplePagerAdapter;
import com.james.status.fragments.SimpleFragment;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;
import com.james.status.views.PageIndicator;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private SimplePagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setPageTransformer(false, new TransitionPageTransformer());

        adapter = new SimplePagerAdapter(this, getSupportFragmentManager(), viewPager, new FirstPageFragment(), new SecondPageFragment(), new ThirdPageFragment(), new FourthPageFragment(), new FifthPageFragment());
        viewPager.setAdapter(adapter);

        PageIndicator indicator = (PageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }

    public static class FirstPageFragment extends SimpleFragment {

        private CustomImageView image;
        private TextView title, subtitle;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_intro_one, container, false);

            image = (CustomImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            subtitle = (TextView) v.findViewById(R.id.subtitle);

            return v;
        }

        @Override
        public void onEnterScroll(float offset) {
            if (image != null) image.setTranslationX(-image.getWidth() * (1 - offset));
            if (title != null) title.setTranslationY(title.getHeight() * (1 - offset));
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * (1 - offset));
        }

        @Override
        public void onExitScroll(float offset) {
            if (image != null) image.setTranslationX(-image.getWidth() * offset);
            if (title != null) title.setTranslationY(title.getHeight() * offset);
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * offset);
        }
    }

    public static class SecondPageFragment extends SimpleFragment {

        private View junk;
        private CustomImageView battery, signal, wifi;
        private TextView title, subtitle;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_intro_two, container, false);

            junk = v.findViewById(R.id.junk);
            battery = (CustomImageView) v.findViewById(R.id.battery);
            signal = (CustomImageView) v.findViewById(R.id.signal);
            wifi = (CustomImageView) v.findViewById(R.id.wifi);
            title = (TextView) v.findViewById(R.id.title);
            subtitle = (TextView) v.findViewById(R.id.subtitle);

            return v;
        }

        @Override
        public void onSelect() {
            if (junk != null) junk.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (junk != null) junk.setVisibility(View.GONE);
                }
            }, 1500);
        }

        @Override
        public void onEnterScroll(float offset) {
            if (junk != null) junk.setTranslationX(-junk.getWidth() * offset);
            if (wifi != null) wifi.setTranslationY(wifi.getHeight() * offset);
            if (signal != null) signal.setTranslationY(-signal.getHeight() * offset);
            if (battery != null) battery.setTranslationX(battery.getWidth() * offset);
            if (title != null) title.setTranslationY(title.getHeight() * offset);
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * offset);
        }

        @Override
        public void onExitScroll(float offset) {
            if (wifi != null) wifi.setTranslationY(wifi.getHeight() * offset);
            if (signal != null) signal.setTranslationY(-signal.getHeight() * offset);
            if (battery != null) battery.setTranslationX(battery.getWidth() * offset);
            if (title != null) title.setTranslationY(title.getHeight() * offset);
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * offset);
        }
    }

    public static class ThirdPageFragment extends SimpleFragment {

        private CustomImageView battery, signal, wifi;
        private TextView title, subtitle;

        Handler handler;
        Runnable runnable;

        private int resource;
        private int[]
                batteryRes = new int[]{
                R.drawable.ic_battery_charging_90,
                R.drawable.ic_battery_circle_charging_90,
                R.drawable.ic_battery_retro_90,
                R.drawable.ic_battery_circle_outline_90
        },
                signalRes = new int[]{
                        R.drawable.ic_signal_2,
                        R.drawable.ic_signal_square_2,
                        R.drawable.ic_signal_retro_2
                },
                wifiRes = new int[]{
                        R.drawable.ic_wifi_3,
                        R.drawable.ic_wifi_triangle_3,
                        R.drawable.ic_wifi_retro_3
                };

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_intro_three, container, false);

            battery = (CustomImageView) v.findViewById(R.id.battery);
            signal = (CustomImageView) v.findViewById(R.id.signal);
            wifi = (CustomImageView) v.findViewById(R.id.wifi);
            title = (TextView) v.findViewById(R.id.title);
            subtitle = (TextView) v.findViewById(R.id.subtitle);

            handler = new Handler();

            return v;
        }

        @Override
        public void onSelect() {
            if (runnable == null && handler != null) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        resource++;
                        if (resource >= batteryRes.length || resource >= signalRes.length || resource >= wifiRes.length)
                            resource = 0;

                        if (battery != null)
                            ImageUtils.tintDrawable(battery, ImageUtils.getVectorDrawable(getContext(), batteryRes[resource]), Color.BLACK);
                        if (signal != null)
                            ImageUtils.tintDrawable(signal, ImageUtils.getVectorDrawable(getContext(), signalRes[resource]), Color.BLACK);
                        if (wifi != null)
                            ImageUtils.tintDrawable(wifi, ImageUtils.getVectorDrawable(getContext(), wifiRes[resource]), Color.BLACK);

                        if (getActivity() != null) handler.postDelayed(this, 1000);
                        else runnable = null;
                    }
                };

                handler.postDelayed(runnable, 1000);
            }
        }

        @Override
        public void onEnterScroll(float offset) {
            if (wifi != null) wifi.setTranslationX(-wifi.getWidth() * offset);
            if (signal != null) signal.setTranslationY(signal.getHeight() * offset);
            if (battery != null) battery.setTranslationY(-battery.getHeight() * offset);
            if (title != null) title.setTranslationY(title.getHeight() * offset);
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * offset);
        }

        @Override
        public void onExitScroll(float offset) {
            if (wifi != null) wifi.setTranslationX(-wifi.getWidth() * offset);
            if (signal != null) signal.setTranslationY(signal.getHeight() * offset);
            if (battery != null) battery.setTranslationY(-battery.getHeight() * offset);
            if (title != null) title.setTranslationY(title.getHeight() * offset);
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * offset);
        }
    }

    public static class FourthPageFragment extends SimpleFragment {

        private View search, editText;
        private TextView title, subtitle;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_intro_four, container, false);

            search = v.findViewById(R.id.search);
            editText = v.findViewById(R.id.editText);
            title = (TextView) v.findViewById(R.id.title);
            subtitle = (TextView) v.findViewById(R.id.subtitle);

            return v;
        }

        @Override
        public void onEnterScroll(float offset) {
            if (search != null) search.setTranslationX(-search.getWidth() * offset);
            if (editText != null) editText.setTranslationY(-editText.getHeight() * offset);
            if (title != null) title.setTranslationY(title.getHeight() * offset);
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * offset);
        }

        @Override
        public void onExitScroll(float offset) {
            if (search != null) search.setTranslationX(-search.getWidth() * offset);
            if (editText != null) editText.setTranslationY(-editText.getHeight() * offset);
            if (title != null) title.setTranslationY(title.getHeight() * offset);
            if (subtitle != null) subtitle.setTranslationY(subtitle.getHeight() * offset);
        }
    }

    public static class FifthPageFragment extends SimpleFragment {

        private TextView title;
        private ProgressBar progress;

        private ValueAnimator animator;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_intro_five, container, false);

            title = (TextView) v.findViewById(R.id.title);
            progress = (ProgressBar) v.findViewById(R.id.progressBar);

            return v;
        }

        @Override
        public void onEnterScroll(float offset) {
            if (title != null) title.setTranslationY(-title.getHeight() * offset);
            if (progress != null) progress.setTranslationY(progress.getHeight() * offset);

            onScroll(offset);
        }

        @Override
        public void onExitScroll(float offset) {
            if (title != null) title.setTranslationY(-title.getHeight() * offset);
            if (progress != null) progress.setTranslationY(progress.getHeight() * offset);

            onScroll(offset);
        }

        private void onScroll(float offset) {
            if (offset == 0) {
                animator = ValueAnimator.ofInt(0, 100);
                animator.setDuration(1500);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        progress.setProgress((int) animation.getAnimatedValue());

                        if (animation.getAnimatedFraction() == 1) {
                            PreferenceUtils.putPreference(getContext(), PreferenceUtils.PreferenceIdentifier.SHOW_TUTORIAL, false);

                            startActivity(new Intent(getContext(), MainActivity.class));
                            getActivity().finish();
                        }
                    }
                });
                animator.start();
            } else {
                if (animator != null) animator.cancel();
            }
        }
    }

    public class TransitionPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            page.setTranslationX(page.getWidth() * -position);

            if (position <= -1.0f || position >= 1.0f) {
                page.setAlpha(0.0f);
            } else if (position == 0.0f) {
                page.setAlpha(1.0f);
            } else {
                page.setAlpha(1.0f - Math.abs(position));
            }
        }
    }
}
