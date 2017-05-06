package com.james.status.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.james.status.R;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;

import me.drozdzynski.library.steppers.OnCancelAction;
import me.drozdzynski.library.steppers.OnFinishAction;
import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;

public class StartActivity extends AppCompatActivity {

    public static final int REQUEST_ACCESSIBILITY = 7369, REQUEST_NOTIFICATION = 2285, REQUEST_PERMISSIONS = 9374, REQUEST_OPTIMIZATION = 6264, REQUEST_OVERLAY = 7451;

    SteppersItem accessibilityStep, notificationStep, permissionsStep, optimizationStep, overlayStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        SteppersView steppersView = (SteppersView) findViewById(R.id.steppersView);

        SteppersView.Config steppersViewConfig = new SteppersView.Config();
        steppersViewConfig.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {
                finish();
            }
        });
        steppersViewConfig.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {
                System.exit(0);
            }
        });

        steppersViewConfig.setFragmentManager(getSupportFragmentManager());

        ArrayList<SteppersItem> steps = new ArrayList<>();

        accessibilityStep = new SteppersItem();
        accessibilityStep.setLabel(getString(R.string.service_name));
        accessibilityStep.setSubLabel(getString(R.string.service_desc));
        accessibilityStep.setFragment(new AccessibilityStepFragment());
        accessibilityStep.setPositiveButtonEnable(StaticUtils.isAccessibilityGranted(this));

        steps.add(accessibilityStep);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            optimizationStep = new SteppersItem();
            optimizationStep.setLabel(getString(R.string.optimizations_name));
            optimizationStep.setSubLabel(getString(R.string.optimizations_desc));
            optimizationStep.setFragment(new OptimizationStepFragment());
            optimizationStep.setPositiveButtonEnable(StaticUtils.isIgnoringOptimizations(this));

            steps.add(optimizationStep);


            permissionsStep = new SteppersItem();
            permissionsStep.setLabel(getString(R.string.permissions_name));
            permissionsStep.setSubLabel(getString(R.string.permissions_desc));
            permissionsStep.setFragment(new PermissionsStepFragment());
            permissionsStep.setPositiveButtonEnable(StaticUtils.isPermissionsGranted(this));

            steps.add(permissionsStep);

            overlayStep = new SteppersItem();
            overlayStep.setLabel(getString(R.string.overlay_name));
            overlayStep.setSubLabel(getString(R.string.overlay_desc));
            overlayStep.setFragment(new OverlayStepFragment());
            overlayStep.setPositiveButtonEnable(StaticUtils.canDrawOverlays(this));

            steps.add(overlayStep);
        }

        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accessibilityStep != null)
            accessibilityStep.setPositiveButtonEnable(StaticUtils.isAccessibilityGranted(this));
        if (notificationStep != null)
            notificationStep.setPositiveButtonEnable(StaticUtils.isNotificationGranted(this));
        if (permissionsStep != null)
            permissionsStep.setPositiveButtonEnable(StaticUtils.isPermissionsGranted(this));
        if (optimizationStep != null)
            optimizationStep.setPositiveButtonEnable(StaticUtils.isIgnoringOptimizations(this));
        if (overlayStep != null)
            overlayStep.setPositiveButtonEnable(StaticUtils.canDrawOverlays(this));
    }

    @Override
    public void onBackPressed() {
        if (!StaticUtils.isAccessibilityGranted(this) || !StaticUtils.isNotificationGranted(this) || !StaticUtils.isPermissionsGranted(this) || !StaticUtils.isIgnoringOptimizations(this) || !StaticUtils.canDrawOverlays(this))
            System.exit(0);
        else super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ACCESSIBILITY:
                if (accessibilityStep != null)
                    accessibilityStep.setPositiveButtonEnable(StaticUtils.isAccessibilityGranted(this));
                break;
            case REQUEST_NOTIFICATION:
                if (notificationStep != null)
                    notificationStep.setPositiveButtonEnable(StaticUtils.isNotificationGranted(this));
                break;
            case REQUEST_OPTIMIZATION:
                if (optimizationStep != null)
                    optimizationStep.setPositiveButtonEnable(StaticUtils.isIgnoringOptimizations(this));
            case REQUEST_OVERLAY:
                if (overlayStep != null)
                    overlayStep.setPositiveButtonEnable(StaticUtils.canDrawOverlays(this));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionsStep != null)
            permissionsStep.setPositiveButtonEnable(StaticUtils.isPermissionsGranted(this));
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
                        startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_NOTIFICATION);
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

    public static class PermissionsStepFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            AppCompatButton button = new AppCompatButton(inflater.getContext());
            button.setText(R.string.action_access_grant);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StaticUtils.requestPermissions(getActivity());
                }
            });

            return button;
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
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                    startActivityForResult(intent, REQUEST_OPTIMIZATION);
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
