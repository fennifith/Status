package com.james.status.dialogs.preference;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.james.status.R;
import com.james.status.adapters.AppAdapter;
import com.james.status.adapters.PreferenceAdapter;
import com.james.status.data.AppPreferenceData;
import com.james.status.utils.tasks.ActivitiesGetterTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AppPreferenceDialog extends AppCompatDialog implements ActivitiesGetterTask.OnGottenListener {

    private RecyclerView recycler;
    private ProgressBar progress;

    private ActivitiesGetterTask task;
    private List<AppPreferenceData> activities;
    private AppPreferenceData app;

    public AppPreferenceDialog(Context context, AppPreferenceData app) {
        super(context, R.style.AppTheme_Dialog_BottomSheet);
        this.app = app;
        activities = app.getActivities();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sheet_app_preference);

        TextView appName = findViewById(R.id.appName);
        TextView packageName = findViewById(R.id.packageName);
        RecyclerView preferenceRecycler = findViewById(R.id.preferences);
        recycler = findViewById(R.id.activities);
        progress = findViewById(R.id.progress);

        BottomSheetBehavior behavior = BottomSheetBehavior.from(findViewById(R.id.root));
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN)
                    dismiss();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        appName.setText(app.getLabel(getContext()));
        packageName.setText(app.getComponentName().replace("/", "\n"));

        preferenceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        preferenceRecycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        preferenceRecycler.setAdapter(new PreferenceAdapter(getContext(), app.getPreferences(getContext())));
        preferenceRecycler.setNestedScrollingEnabled(false);

        if (app.isActivity()) {
            recycler.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            recycler.setLayoutManager(new LinearLayoutManager(getContext()));
            recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            recycler.setNestedScrollingEnabled(false);
            if (activities != null) {
                recycler.setAdapter(new AppAdapter(getContext(), activities));
                progress.setVisibility(View.GONE);
            } else if (task == null) {
                task = new ActivitiesGetterTask(getContext(), this);
                task.execute(app.getPackageName());
            }
        }
    }

    @Override
    public void onGottenProgressUpdate(int progress, int max) {
        if (this.progress != null) {
            this.progress.setMax(max);
            this.progress.setProgress(progress);
        }
    }

    @Override
    public void onGottenPackages(AppPreferenceData[] packages) {
        this.activities = new ArrayList<>(Arrays.asList(packages));
        app.setActivities(this.activities);
        if (recycler != null) {
            recycler.setAdapter(new AppAdapter(getContext(), this.activities));
            if (progress != null)
                progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            task = null;
        }
    }
}
