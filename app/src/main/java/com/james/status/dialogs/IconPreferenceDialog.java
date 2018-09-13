package com.james.status.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.adapters.PreferenceAdapter;
import com.james.status.data.icon.IconData;

public class IconPreferenceDialog extends AppCompatDialog implements Status.OnIconPreferenceChangedListener, DialogInterface.OnDismissListener {

    private Status status;
    private IconData icon;

    private RecyclerView recyclerView;

    public IconPreferenceDialog(@NonNull IconData icon) {
        super(icon.getContext(), R.style.AppTheme_Dialog_BottomSheet);
        status = (Status) icon.getContext().getApplicationContext();
        this.icon = icon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_icon_preference);

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
