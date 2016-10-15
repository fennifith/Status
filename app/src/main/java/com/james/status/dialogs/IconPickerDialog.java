package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.james.status.R;
import com.james.status.adapters.IconStyleAdapter;
import com.james.status.data.IconStyleData;
import com.james.status.utils.ImageUtils;

import java.util.List;

public class IconPickerDialog extends PreferenceDialog<IconStyleData> {

    private List<IconStyleData> styles;
    private IconStyleAdapter adapter;

    private String title;

    public IconPickerDialog(Context context, List<IconStyleData> styles) {
        super(context, R.style.AppTheme_Dialog_FullScreen);
        this.styles = styles;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_icon_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (title != null) toolbar.setTitle(title);

        toolbar.setNavigationIcon(ImageUtils.getVectorDrawable(getContext(), R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowing()) dismiss();
            }
        });

        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adapter = new IconStyleAdapter(getContext(), styles, new IconStyleAdapter.OnCheckedChangeListener() {
            @Override
            public void onCheckedChange(IconStyleData selected) {
                setPreference(selected);
            }
        });

        adapter.setIconStyle(getPreference());

        recycler.setAdapter(adapter);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title.toString();
    }

    @Override
    public void setTitle(int titleId) {
        title = getContext().getString(titleId);
    }
}
