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

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.james.status.R;
import com.james.status.adapters.BackupAdapter;
import com.james.status.data.PreferenceData;
import com.james.status.utils.StaticUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BackupDialog extends ThemedCompatDialog implements BackupCreatorDialog.OnBackupChangedListener, DialogInterface.OnDismissListener {

    private Activity activity;
    private List<File> files;
    private RecyclerView recyclerView;
    private boolean isSettingsChanged;

    public BackupDialog(Activity activity) {
        super(activity);
        this.activity = activity;

        setTitle(R.string.preference_backups);
        setOnDismissListener(this);
        onFileChanged(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_backup);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setAdapter(new BackupAdapter(getContext(), files, this));

        findViewById(R.id.newBackup).setOnClickListener(v -> {
            BackupCreatorDialog dialog = new BackupCreatorDialog(getContext(), files, null);
            dialog.setListener(BackupDialog.this);
            dialog.show();
        });

        findViewById(R.id.dismiss).setOnClickListener(v -> dismiss());
    }

    @Override
    public void onFileChanged(boolean isSettingsChanged) {
        if (files != null) files.clear();
        else files = new ArrayList<>();

        File dir = new File(PreferenceData.getBackupsDir());
        if (!dir.exists())
            dir.mkdirs();

        File[] array = dir.listFiles();
        if (array != null) {
            for (File file : array) {
                if (file.getName().endsWith(".txt"))
                    files.add(file);
            }
        }

        if (recyclerView != null && recyclerView.getAdapter() != null)
            recyclerView.getAdapter().notifyDataSetChanged();

        if (isSettingsChanged) {
            StaticUtils.updateStatusService(getContext(), false);
            this.isSettingsChanged = true;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (isSettingsChanged)
            activity.recreate();
    }
}
