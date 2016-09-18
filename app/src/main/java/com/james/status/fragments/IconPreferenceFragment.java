package com.james.status.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.adapters.IconAdapter;
import com.james.status.data.icon.IconData;
import com.james.status.utils.StaticUtils;

import java.util.List;

public class IconPreferenceFragment extends SimpleFragment {

    private IconAdapter adapter;
    private Status.OnPreferenceChangedListener listener;
    private Status status;
    private String filter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_icons, container, false);
        status = (Status) getContext().getApplicationContext();

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adapter = new IconAdapter(getContext());
        recycler.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (filter == null)
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
                else return 0;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                if (filter == null) {
                    List<IconData> icons = adapter.getIcons();
                    IconData icon = icons.get(viewHolder.getAdapterPosition());

                    icon.putPreference(IconData.PreferenceIdentifier.POSITION, target.getAdapterPosition());
                    icons.remove(icon);
                    icons.add(target.getAdapterPosition(), icon);

                    adapter.setIcons(icons);
                    adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    StaticUtils.updateStatusService(getContext());

                    return false;
                }
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }
        }).attachToRecyclerView(recycler);

        listener = new Status.OnPreferenceChangedListener() {
            @Override
            public void onPreferenceChanged() {
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        };

        status.addListener(listener);

        return v;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_icons);
    }

    @Override
    public void filter(@Nullable String filter) {
        if (adapter != null) {
            this.filter = filter;
            adapter.filter(filter);
        }
    }

    @Override
    public void onDestroy() {
        status.removeListener(listener);
        super.onDestroy();
    }
}
