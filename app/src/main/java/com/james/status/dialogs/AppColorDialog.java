package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.james.status.R;
import com.james.status.adapters.AppAdapter;

public class AppColorDialog extends BottomSheetDialog {

    public AppColorDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_apps);

        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(new AppAdapter(getContext()));
    }
}
