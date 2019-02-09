/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.data.PreferenceData;
import com.james.status.services.StatusServiceImpl;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;
import me.drozdzynski.library.steppers.interfaces.OnCancelAction;
import me.drozdzynski.library.steppers.interfaces.OnFinishAction;

public class StartActivity extends AppCompatActivity {

    public static final int REQUEST_ACCESSIBILITY = 7369, REQUEST_NOTIFICATION = 2285, REQUEST_PERMISSIONS = 9374, REQUEST_OPTIMIZATION = 6264, REQUEST_OVERLAY = 7451;

    SteppersItem accessibilityStep, notificationStep, optimizationStep, overlayStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Status.Theme.ACTIVITY_SPLASH.getTheme(this));
        setContentView(R.layout.activity_start);

        SteppersView steppersView = findViewById(R.id.steppersView);

        SteppersView.Config steppersViewConfig = new SteppersView.Config();
        steppersViewConfig.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {
                PreferenceData.STATUS_ENABLED.setValue(StartActivity.this, true);

                StatusServiceImpl.start(StartActivity.this);
                finish();
            }
        });
        steppersViewConfig.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {
                finish();
            }
        });

        steppersViewConfig.setFragmentManager(getSupportFragmentManager());

        ArrayList<SteppersItem> steps = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).resolveActivity(getPackageManager()) != null) {
                optimizationStep = new SteppersItem();
                optimizationStep.setLabel(getString(R.string.optimizations_name));
                optimizationStep.setSubLabel(getString(R.string.optimizations_desc));
                optimizationStep.setFragment(new OptimizationStepFragment());
                optimizationStep.setPositiveButtonEnable(true);

                steps.add(optimizationStep);
            }

            overlayStep = new SteppersItem();
            overlayStep.setLabel(getString(R.string.overlay_name));
            overlayStep.setSubLabel(getString(R.string.overlay_desc));
            overlayStep.setFragment(new OverlayStepFragment());
            overlayStep.setPositiveButtonEnable(StaticUtils.canDrawOverlays(this));

            steps.add(overlayStep);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            notificationStep = new SteppersItem();
            notificationStep.setLabel(getString(R.string.notification_name));
            notificationStep.setSubLabel(getString(R.string.notification_desc));
            notificationStep.setFragment(new NotificationStepFragment());
            notificationStep.setPositiveButtonEnable(StaticUtils.isNotificationGranted(this));

            steps.add(notificationStep);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.notifications_compat)
                    .setMessage(R.string.notifications_compat_desc)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }

        accessibilityStep = new SteppersItem();
        accessibilityStep.setLabel(getString(R.string.service_name));
        accessibilityStep.setSubLabel(getString(R.string.service_desc));
        accessibilityStep.setFragment(new AccessibilityStepFragment());
        accessibilityStep.setPositiveButtonEnable(StaticUtils.isAccessibilityServiceRunning(this));

        steps.add(accessibilityStep);

        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accessibilityStep != null)
            accessibilityStep.setPositiveButtonEnable(StaticUtils.isAccessibilityServiceRunning(this));
        if (notificationStep != null)
            notificationStep.setPositiveButtonEnable(StaticUtils.isNotificationGranted(this));
        if (overlayStep != null)
            overlayStep.setPositiveButtonEnable(StaticUtils.canDrawOverlays(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ACCESSIBILITY:
                if (accessibilityStep != null)
                    accessibilityStep.setPositiveButtonEnable(StaticUtils.isAccessibilityServiceRunning(this));
                break;
            case REQUEST_NOTIFICATION:
                if (notificationStep != null)
                    notificationStep.setPositiveButtonEnable(StaticUtils.isNotificationGranted(this));
                break;
            case REQUEST_OVERLAY:
                if (overlayStep != null)
                    overlayStep.setPositiveButtonEnable(StaticUtils.canDrawOverlays(this));
                break;
        }
    }

    public static class AccessibilityStepFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            AppCompatButton button = new AppCompatButton(inflater.getContext());
            button.setText(R.string.action_access_grant);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQUEST_ACCESSIBILITY);
                    Toast.makeText(getContext(), R.string.msg_notification_switch_enable, Toast.LENGTH_LONG).show();
                }
            });

            return button;
        }
    }

    public static class NotificationStepFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout linearLayout = new LinearLayout(inflater.getContext());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                AppCompatButton button = new AppCompatButton(inflater.getContext());
                button.setText(R.string.action_access_grant);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PackageManager pm = v.getContext().getPackageManager();
                        pm.setComponentEnabledSetting(new ComponentName(v.getContext(), StatusServiceImpl.getCompatClass(v.getContext())),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP); //this should be redundant, but it isn't

                        startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_NOTIFICATION);
                        Toast.makeText(getContext(), R.string.msg_accessibility_switch_enable, Toast.LENGTH_LONG).show();
                    }
                });

                linearLayout.addView(button);
            }

            AppCompatButton button = new AppCompatButton(inflater.getContext());
            button.setText(R.string.notifications_compat);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(getActivity(), NotificationCompatActivity.class), REQUEST_NOTIFICATION);
                }
            });

            linearLayout.addView(button);

            return linearLayout;
        }
    }

    @TargetApi(23)
    public static class OptimizationStepFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            AppCompatButton button = new AppCompatButton(inflater.getContext());
            button.setText(R.string.optimizations_name);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
                        // This intent launches the "evil dialog of misleading and restrictive user freedom", according
                        // to Google Play's policy, but there shouldn't actually be anything wrong with it. The policy
                        // states that this is only acceptable if battery optimization affects the "core functionality" of
                        // the app in question, which... it does. However, it seems their moderators have decided otherwise,
                        // as the update that I pushed containing this intent was taken down. Which is why the permission to use
                        // this intent is now only granted in the OSS product flavor. :(

                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getContext().getApplicationContext().getPackageName()));
                        startActivityForResult(intent, REQUEST_OPTIMIZATION);
                    } else {
                        startActivityForResult(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), REQUEST_OPTIMIZATION);
                        Toast.makeText(getContext(), R.string.msg_battery_optimizations_switch_enable, Toast.LENGTH_LONG).show();
                    }
                }
            });

            return button;
        }
    }

    @TargetApi(23)
    public static class OverlayStepFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            AppCompatButton button = new AppCompatButton(inflater.getContext());
            button.setText(R.string.action_access_grant);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName())), REQUEST_OVERLAY);
                }
            });

            return button;
        }
    }
}
