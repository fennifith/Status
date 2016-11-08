package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.james.status.R;
import com.james.status.adapters.ListPreferenceAdapter;
import com.james.status.data.preference.ListPreferenceData;

import java.util.List;

public class ListPickerDialog extends PreferenceDialog<ListPreferenceData.ListPreference> {

    private List<ListPreferenceData.ListPreference> styles;
    private ListPreferenceAdapter adapter;

    public ListPickerDialog(Context context, List<ListPreferenceData.ListPreference> styles) {
        super(context);
        this.styles = styles;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list_picker);

        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adapter = new ListPreferenceAdapter(getContext(), styles, new ListPreferenceAdapter.OnCheckedChangeListener() {
            @Override
            public void onCheckedChange(ListPreferenceData.ListPreference selected) {
                setPreference(selected);
            }
        });

        adapter.setListPreference(getPreference());

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
