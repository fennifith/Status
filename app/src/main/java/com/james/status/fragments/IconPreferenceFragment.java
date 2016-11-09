package com.james.status.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTargetView;
import com.james.status.R;
import com.james.status.Status;
import com.james.status.adapters.IconAdapter;
import com.james.status.utils.StaticUtils;

public class IconPreferenceFragment extends SimpleFragment implements Status.OnPreferenceChangedListener {

    private Status status;
    private IconAdapter adapter;
    private boolean isSelected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_icons, container, false);
        status = (Status) getContext().getApplicationContext();
        status.addListener(this);

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adapter = new IconAdapter(getActivity());
        recycler.setAdapter(adapter);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter != null && adapter.itemView != null && newState == RecyclerView.SCROLL_STATE_IDLE && isSelected) {
                    if (StaticUtils.shouldShowTutorial(getContext(), "disableicon")) {
                        new TapTargetView.Builder(getActivity())
                                .title(R.string.tutorial_icon_switch)
                                .description(R.string.tutorial_icon_switch_desc)
                                .outerCircleColor(R.color.colorAccent)
                                .dimColor(android.R.color.black)
                                .drawShadow(false)
                                .listener(new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        view.dismiss(true);
                                    }

                                    @Override
                                    public void onTargetLongClick(TapTargetView view) {
                                    }
                                })
                                .cancelable(true)
                                .showFor(adapter.itemView.findViewById(R.id.iconCheckBox));
                    } else if (StaticUtils.shouldShowTutorial(getContext(), "moveicon", 1)) {
                        View moveDown = adapter.itemView.findViewById(R.id.moveDown), moveUp = adapter.itemView.findViewById(R.id.moveUp);
                        new TapTargetView.Builder(getActivity())
                                .title(R.string.tutorial_icon_order)
                                .description(R.string.tutorial_icon_order_desc)
                                .textColor(R.color.textColorPrimary)
                                .outerCircleColor(R.color.colorPrimaryLight)
                                .targetCircleColor(android.R.color.black)
                                .dimColor(android.R.color.black)
                                .drawShadow(false)
                                .listener(new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        view.dismiss(true);
                                    }

                                    @Override
                                    public void onTargetLongClick(TapTargetView view) {
                                    }
                                })
                                .cancelable(true)
                                .showFor(moveDown.getVisibility() == View.VISIBLE ? moveDown : moveUp);
                    }
                }
            }
        });

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
    public void onPreferenceChanged() {
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
