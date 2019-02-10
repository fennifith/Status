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
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ArrayAdapter extends RecyclerView.Adapter<ArrayAdapter.ViewHolder> {

    private Context context;
    private List<CharSequence> originalItems;
    private List<CharSequence> items;

    public ArrayAdapter(Context context, @ArrayRes int stringArray) {
        this.context = context;

        originalItems = new ArrayList<>();
        originalItems.addAll(Arrays.asList(context.getResources().getTextArray(stringArray)));
        items = new ArrayList<>(originalItems);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setVisibility(View.GONE);

        holder.subtitle.setText(items.get(position));
        holder.subtitle.setMovementMethod(new LinkMovementMethod());

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void filter(@Nullable String filter) {
        items.clear();

        if (filter == null || filter.length() < 1) {
            items.addAll(originalItems);
        } else {
            for (CharSequence item : originalItems) {
                String string = item.toString();
                if (string.toLowerCase().contains(filter) || filter.contains(string.toLowerCase()))
                    items.add(item);
            }
        }

        notifyDataSetChanged();
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
