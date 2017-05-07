package com.james.status.fragments;

import android.app.Activity;
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
import com.james.status.Status;
import com.james.status.adapters.PreferenceSectionAdapter;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.data.preference.ListPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.dialogs.BackupDialog;
import com.james.status.services.AccessibilityService;
import com.james.status.services.StatusService;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneralPreferenceFragment extends SimpleFragment implements PreferenceData.OnPreferenceChangeListener {

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
                        this
                ),
                new ColorPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_COLOR,
                                getString(R.string.preference_bar_color_chooser),
                                PreferenceData.Identifier.SectionIdentifier.COLORS
                        ),
                        Color.BLACK,
                        this
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
                new ColorPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_ICON_COLOR,
                                getString(R.string.preference_default_color_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Color.WHITE,
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
                        this
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_TINTED_ICONS,
                                getString(R.string.preference_tinted_icons),
                                getString(R.string.preference_tinted_icons_desc),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        false,
                        this
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_BACKGROUND_ANIMATIONS,
                                getString(R.string.preference_background_animations),
                                getString(R.string.preference_background_animations_desc),
                                PreferenceData.Identifier.SectionIdentifier.ANIMATIONS
                        ),
                        true,
                        this
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_ICON_ANIMATIONS,
                                getString(R.string.preference_icon_animations),
                                getString(R.string.preference_icon_animations_desc),
                                PreferenceData.Identifier.SectionIdentifier.ANIMATIONS
                        ),
                        true,
                        this
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
                ),
                new IntegerPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_HEADS_UP_DURATION,
                                getString(R.string.preference_heads_up_duration),
                                getString(R.string.preference_heads_up_duration_desc),
                                PreferenceData.Identifier.SectionIdentifier.NOTIFICATIONS
                        ),
                        10,
                        getString(R.string.unit_seconds),
                        0,
                        20,
                        null
                ),
                new ListPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_HEADS_UP_LAYOUT,
                                getString(R.string.preference_heads_up_layout),
                                PreferenceData.Identifier.SectionIdentifier.NOTIFICATIONS
                        ),
                        null,
                        StatusService.HEADSUP_LAYOUT_PLAIN,
                        new ListPreferenceData.ListPreference(
                                getString(R.string.heads_up_plain),
                                StatusService.HEADSUP_LAYOUT_PLAIN
                        ),
                        new ListPreferenceData.ListPreference(
                                getString(R.string.heads_up_card),
                                StatusService.HEADSUP_LAYOUT_CARD
                        ),
                        new ListPreferenceData.ListPreference(
                                getString(R.string.heads_up_condensed),
                                StatusService.HEADSUP_LAYOUT_CONDENSED
                        ),
                        new ListPreferenceData.ListPreference(
                                getString(R.string.heads_up_transparent),
                                StatusService.HEADSUP_LAYOUT_TRANSPARENT
                        )
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_PERSISTENT_NOTIFICATION,
                                getString(R.string.preference_persistent_notification),
                                getString(R.string.preference_persistent_notification_desc),
                                PreferenceData.Identifier.SectionIdentifier.OTHER
                        ),
                        true,
                        this
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_BURNIN_PROTECTION,
                                getString(R.string.preference_burnin_protection),
                                getString(R.string.preference_burnin_protection_desc),
                                PreferenceData.Identifier.SectionIdentifier.OTHER
                        ),
                        false,
                        this
                ),
                new PreferenceData<Serializable>(
                        getContext(),
                        new PreferenceData.Identifier(
                                getString(R.string.preference_backups),
                                getString(R.string.preference_backups_desc),
                                PreferenceData.Identifier.SectionIdentifier.OTHER
                        )
                ) {
                    @Override
                    public void onClick(View v) {
                        new BackupDialog((Activity) getContext()).show();
                    }
                },
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_HIDE_ON_VOLUME,
                                getString(R.string.preference_hide_on_volume),
                                getString(R.string.preference_hide_on_volume_desc),
                                PreferenceData.Identifier.SectionIdentifier.OTHER
                        ),
                        AccessibilityService.shouldHideOnVolume(getContext()),
                        null
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_DEBUG,
                                getString(R.string.preference_debug),
                                getString(R.string.preference_debug_desc),
                                PreferenceData.Identifier.SectionIdentifier.OTHER
                        ),
                        Status.isDebug(getContext()),
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

    @Override
    public void onPreferenceChange(Object preference) {
        StaticUtils.updateStatusService(getContext());
    }
}
