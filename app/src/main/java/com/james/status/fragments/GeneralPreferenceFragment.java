package com.james.status.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.adapters.PreferenceSectionAdapter;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneralPreferenceFragment extends SimpleFragment {

    private PreferenceSectionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        List<PreferenceData> preferences = new ArrayList<>();

        preferences.addAll(Arrays.asList(
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO,
                                getString(R.string.preference_bar_color_auto),
                                getString(R.string.preference_bar_color_auto_desc),
                                PreferenceData.Identifier.SectionIdentifier.COLORS
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                            @Override
                            public void onPreferenceChange(Boolean preference) {
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                ),
                new ColorPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_COLOR,
                                getString(R.string.preference_bar_color_chooser),
                                PreferenceData.Identifier.SectionIdentifier.COLORS
                        ),
                        Color.BLACK,
                        new PreferenceData.OnPreferenceChangeListener<Integer>() {
                            @Override
                            public void onPreferenceChange(Integer preference) {
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS_NOTIFICATIONS,
                                getString(R.string.preference_color_notification),
                                getString(R.string.preference_color_notification_desc),
                                PreferenceData.Identifier.SectionIdentifier.COLORS
                        ),
                        true,
                        null
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_HOME_TRANSPARENT,
                                getString(R.string.preference_transparent_home),
                                getString(R.string.preference_transparent_home_desc),
                                PreferenceData.Identifier.SectionIdentifier.COLORS
                        ),
                        true,
                        null
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS,
                                getString(R.string.preference_dark_icons),
                                getString(R.string.preference_dark_icons_desc),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        true,
                        null
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_NOTIFICATIONS,
                                getString(R.string.preference_show_notifications),
                                PreferenceData.Identifier.SectionIdentifier.NOTIFICATIONS
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                            @Override
                            public void onPreferenceChange(Boolean preference) {
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_NOTIFICATIONS_HEADS_UP,
                                getString(R.string.preference_heads_up),
                                getString(R.string.preference_heads_up_desc),
                                PreferenceData.Identifier.SectionIdentifier.NOTIFICATIONS
                        ),
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP,
                        null
                )
        ));

        adapter = new PreferenceSectionAdapter(getContext(), preferences);
        recycler.setAdapter(adapter);

        return v;
    }

    @Override
    public void filter(@Nullable String filter) {
        if (adapter != null) adapter.filter(filter);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_settings);
    }
}
