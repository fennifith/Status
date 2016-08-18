package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.james.status.R;
import com.james.status.adapters.AppStatusAdapter;

public class AppStatusDialog extends BottomSheetDialog {

    public AppStatusDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_app_colors);

        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(new AppStatusAdapter(getContext()));
    }
}
