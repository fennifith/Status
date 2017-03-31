package com.james.status.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.james.status.R;
import com.james.status.adapters.BackupAdapter;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupDialog extends AppCompatDialog implements BackupCreatorDialog.OnBackupChangedListener, DialogInterface.OnDismissListener {

    private Activity activity;
    private List<File> files;
    private RecyclerView recyclerView;
    private boolean isSettingsChanged;

    public BackupDialog(Activity activity) {
        super(activity, R.style.AppTheme_Dialog);
        this.activity = activity;

        setTitle(R.string.preference_backups);
        setOnDismissListener(this);
        onFileChanged(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_backup);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setAdapter(new BackupAdapter(getContext(), files, this));

        findViewById(R.id.newBackup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackupCreatorDialog dialog = new BackupCreatorDialog(getContext(), files, null);
                dialog.setListener(BackupDialog.this);
                dialog.show();
            }
        });

        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onFileChanged(boolean isSettingsChanged) {
        if (files != null) files.clear();
        else files = new ArrayList<>();

        File dir = new File(PreferenceUtils.getBackupsDir());
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
            StaticUtils.updateStatusService(getContext());
            this.isSettingsChanged = true;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (isSettingsChanged)
            activity.recreate();
    }
}
