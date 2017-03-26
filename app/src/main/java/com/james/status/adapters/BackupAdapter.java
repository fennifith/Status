package com.james.status.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.BackupCreatorDialog;

import java.io.File;
import java.util.List;

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.ViewHolder> {

    private Context context;
    private List<File> files;
    private BackupCreatorDialog.OnBackupChangedListener listener;

    public BackupAdapter(Context context, List<File> files, BackupCreatorDialog.OnBackupChangedListener listener) {
        this.context = context;
        this.files = files;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        File file = files.get(position);
        holder.title.setText(file.getName().substring(0, file.getName().length() - 4));
        holder.subtitle.setVisibility(View.GONE);

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackupCreatorDialog dialog = new BackupCreatorDialog(context, files, files.get(holder.getAdapterPosition()));
                dialog.setListener(listener);
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView title, subtitle;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            title = (TextView) v.findViewById(R.id.title);
            subtitle = (TextView) v.findViewById(R.id.subtitle);
        }
    }
}
