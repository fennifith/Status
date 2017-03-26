package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

import java.io.File;
import java.util.List;

public class BackupCreatorDialog extends AppCompatDialog implements View.OnClickListener {

    private OnBackupChangedListener listener;
    private List<File> files;
    private File file;

    private EditText editText;

    public BackupCreatorDialog(Context context, List<File> files, File file) {
        super(context, R.style.AppTheme_Dialog);
        setTitle(file != null ? R.string.preference_backups : R.string.action_new_backup);
        this.files = files;
        this.file = file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_backup_creator);

        editText = (EditText) findViewById(R.id.name);
        if (file != null)
            editText.setText(file.getName().substring(0, file.getName().length() - 4));
        else {
            String name = "backup";
            for (int i = 1; hasFile(name); i++) {
                name = "backup" + i;
            }

            file = new File(PreferenceUtils.getBackupsDir(getContext()), name + ".txt");
            editText.setText(name);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = editText.getText().toString();

                String replaced = name.replaceAll("[^a-zA-Z0-9.-]", "_");
                if (!name.equals(replaced)) {
                    editText.setText(replaced);
                    return;
                }

                if (hasFile(name)) {
                    editText.setError(getContext().getString(R.string.error_name_exists));
                    file = null;
                    return;
                }

                file = new File(PreferenceUtils.getBackupsDir(getContext()), name + ".txt");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TextView delete = (TextView) findViewById(R.id.delete);
        delete.setText(file.exists() ? R.string.action_delete : R.string.action_cancel);
        delete.setOnClickListener(this);

        if (file.exists())
            findViewById(R.id.restore).setOnClickListener(this);
        else findViewById(R.id.restore).setVisibility(View.GONE);

        TextView save = (TextView) findViewById(R.id.save);
        save.setText(file.exists() ? R.string.action_save_backup : R.string.action_create_backup);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                if (file != null) {
                    PreferenceUtils.toFile(getContext(), file);
                    if (listener != null)
                        listener.onFileChanged(false);
                    dismiss();
                } else editText.setError(getContext().getString(R.string.error_name_exists));
                break;
            case R.id.restore:
                if (file != null && file.exists()) {
                    PreferenceUtils.fromFile(getContext(), file);
                    if (listener != null)
                        listener.onFileChanged(true);
                    dismiss();
                }
                break;
            case R.id.delete:
                if (file != null && file.exists()) {
                    file.delete();
                    if (listener != null)
                        listener.onFileChanged(false);
                }
                dismiss();
                break;
        }
    }

    public void setListener(OnBackupChangedListener listener) {
        this.listener = listener;
    }

    private boolean hasFile(String name) {
        for (File file : files) {
            String fileName = file.getName().substring(0, file.getName().length() - 4);
            if (fileName.equals(name))
                return true;
        }

        return false;
    }

    public interface OnBackupChangedListener {
        void onFileChanged(boolean isSettingsChanged);
    }
}
