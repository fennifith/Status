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

package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.james.status.R;
import com.james.status.Status;
import com.james.status.adapters.AppAdapter;
import com.james.status.data.AppPreferenceData;
import com.james.status.utils.tasks.PackagesGetterTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AppChooserDialog extends ThemedCompatDialog implements PackagesGetterTask.OnGottenListener {

    private RecyclerView recycler;
    private ProgressBar progress;
    private BottomSheetBehavior behavior;

    private PackagesGetterTask task;
    private List<AppPreferenceData> packages;

    public AppChooserDialog(Context context) {
        super(context, Status.Theme.DIALOG_BOTTOM_SHEET);
    }

    public AppChooserDialog(Context context, List<AppPreferenceData> packages) {
        this(context);
        this.packages = packages;
    }

    public List<AppPreferenceData> getPackages() {
        return packages;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sheet_apps_preference);

        recycler = findViewById(R.id.recycler);
        progress = findViewById(R.id.progress);

        behavior = BottomSheetBehavior.from(findViewById(R.id.root));
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        if (packages != null) {
            recycler.setAdapter(new AppAdapter(getContext(), packages));
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
        if (recycler != null) {
            recycler.setAdapter(new AppAdapter(getContext(), this.packages));
            if (progress != null)
                progress.setVisibility(View.GONE);
            if (behavior != null)
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
