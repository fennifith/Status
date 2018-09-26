package com.james.status.dialogs.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.adapters.AppNotificationsAdapter;
import com.james.status.data.AppPreferenceData;
import com.james.status.data.preference.AppNotificationsPreferenceData;
import com.james.status.utils.tasks.PackagesGetterTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppNotificationsPreferenceDialog extends AppCompatDialog implements PackagesGetterTask.OnGottenListener {

    private RecyclerView recycler;
    private ProgressBar progress;

    private PackagesGetterTask task;
    private List<AppPreferenceData> packages;
    private AppNotificationsPreferenceData preference;

    public AppNotificationsPreferenceDialog(AppNotificationsPreferenceData preference) {
        super(preference.getContext(), R.style.AppTheme_Dialog_BottomSheet);
        this.preference = preference;
        packages = preference.getApps();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sheet_apps_preference);

        TextView title = findViewById(R.id.title);
        recycler = findViewById(R.id.recycler);
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

        title.setText(preference.getIdentifier().getTitle());

        recycler.setVisibility(View.VISIBLE);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        if (packages != null) {
            recycler.setAdapter(new AppNotificationsAdapter(getContext(), packages));
            progress.setVisibility(View.GONE);
        } else if (task == null) {
            task = new PackagesGetterTask(getContext(), this);
            task.execute();
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
        this.packages = new ArrayList<>(Arrays.asList(packages));
        preference.setApps(this.packages);
        if (recycler != null) {
            recycler.setAdapter(new AppNotificationsAdapter(getContext(), this.packages));
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
