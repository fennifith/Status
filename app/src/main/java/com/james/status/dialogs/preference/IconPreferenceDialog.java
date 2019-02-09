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

package com.james.status.dialogs.preference;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.james.status.R;
import com.james.status.Status;
import com.james.status.adapters.PreferenceAdapter;
import com.james.status.data.icon.IconData;
import com.james.status.dialogs.ThemedCompatDialog;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IconPreferenceDialog extends ThemedCompatDialog implements Status.OnIconPreferenceChangedListener, DialogInterface.OnDismissListener {

    private Status status;
    private IconData icon;

    private RecyclerView recyclerView;

    public IconPreferenceDialog(@NonNull IconData icon) {
        super(icon.getContext(), Status.Theme.DIALOG_BOTTOM_SHEET);
        status = (Status) icon.getContext().getApplicationContext();
        this.icon = icon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sheet_icon_preference);

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

        TextView textView = findViewById(R.id.iconName);
        recyclerView = findViewById(R.id.recycler);

        textView.setText(icon.getTitle());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new PreferenceAdapter(getContext(), icon.getPreferences()));

        status.addListener(this);
        setOnDismissListener(this);
    }

    @Override
    public void onIconPreferenceChanged(IconData... icons) {
        for (IconData icon : icons) {
            if (icon != null && this.icon.equals(icon) && recyclerView != null && recyclerView.getAdapter() != null)
                recyclerView.setAdapter(new PreferenceAdapter(getContext(), icon.getPreferences()));
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        status.removeListener(this);
    }
}
