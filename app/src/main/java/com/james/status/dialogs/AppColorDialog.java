package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.james.status.R;
import com.james.status.adapters.AppColorAdapter;
import com.james.status.utils.ImageUtils;

public class AppColorDialog extends AppCompatDialog {

    public AppColorDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_app_colors);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowing()) dismiss();
            }
        });

        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler.setAdapter(new AppColorAdapter(getContext()));
    }
}
