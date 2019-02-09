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

package com.james.status.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.BackupCreatorDialog;

import java.io.File;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

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
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
        }
    }
}
