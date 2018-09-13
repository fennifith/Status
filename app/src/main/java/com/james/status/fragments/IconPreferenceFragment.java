package com.james.status.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.adapters.IconAdapter;
import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.utils.StaticUtils;

import java.util.List;

public class IconPreferenceFragment extends SimpleFragment implements Status.OnIconPreferenceChangedListener {

    private Status status;
    private IconAdapter adapter;
    private boolean isSelected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_icons, container, false);
        status = (Status) getContext().getApplicationContext();
        status.addListener(this);

        RecyclerView recycler = v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                List<IconData> icons = adapter.getIcons();
                IconData icon = icons.get(viewHolder.getAdapterPosition());
                icons.remove(icon);
                icons.add(target.getAdapterPosition(), icon);

                adapter.setIcons(icons);
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                List<IconData> icons = adapter.getIcons();
                for (int i = 0; i < icons.size(); i++)
                    PreferenceData.ICON_POSITION.setValue(getContext(), i, icons.get(i).getIdentifierArgs());

                StaticUtils.updateStatusService(getActivity(), true);
            }
        });

        helper.attachToRecyclerView(recycler);
        adapter = new IconAdapter(getActivity(), helper);
        recycler.setAdapter(adapter);

        return v;
    }

    @Override
    public void onDestroy() {
        if (status != null) status.removeListener(this);
        super.onDestroy();
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_icons);
    }

    @Override
    public void onSelect() {
        isSelected = true;
    }

    @Override
    public void onEnterScroll(float offset) {
        isSelected = offset == 0;
    }

    @Override
    public void onExitScroll(float offset) {
        isSelected = offset == 0;
    }

    @Override
    public void filter(@Nullable String filter) {
        if (adapter != null) {
            adapter.filter(filter);
        }
    }

    @Override
    public void onIconPreferenceChanged(IconData... icons) {
        if (adapter != null)
            adapter.notifyIconsChanged(icons);
    }
}
