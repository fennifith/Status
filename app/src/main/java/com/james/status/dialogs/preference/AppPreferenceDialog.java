package com.james.status.dialogs.preference;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.adapters.PreferenceAdapter;
import com.james.status.data.AppPreferenceData;

public class AppPreferenceDialog extends AppCompatDialog {

    private AppPreferenceData app;

    public AppPreferenceDialog(Context context, AppPreferenceData app) {
        super(context, R.style.AppTheme_Dialog_BottomSheet);
        this.app = app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_app_preference);

        TextView appName = findViewById(R.id.appName);
        TextView packageName = findViewById(R.id.packageName);
        RecyclerView preferenceRecycler = findViewById(R.id.preferences);
        RecyclerView activitiesRecycler = findViewById(R.id.activities);

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

        appName.setText(app.getLabel(getContext()));
        packageName.setText(app.getPackageName());

        preferenceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        preferenceRecycler.setAdapter(new PreferenceAdapter(getContext(), app.getPreferences(getContext())));
        activitiesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        activitiesRecycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }
}
