package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.james.status.R;
import com.james.status.adapters.IconStyleAdapter;
import com.james.status.data.IconStyleData;

import java.util.List;

public class IconPickerDialog extends PreferenceDialog<IconStyleData> {

    private List<IconStyleData> styles;
    private IconStyleAdapter adapter;

    public IconPickerDialog(Context context, List<IconStyleData> styles) {
        super(context);
        this.styles = styles;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_icon_picker);

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
}
