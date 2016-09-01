package com.james.status.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.james.status.data.IconStyleData;
import com.james.status.data.preference.AppsColorPreferenceData;
import com.james.status.data.preference.AppsStatusPreferenceData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;
import com.james.status.data.preference.IconPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.services.StatusService;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneralPreferenceFragment extends SimpleFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler, container, false);

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler.setNestedScrollingEnabled(false);

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
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
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
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new AppsColorPreferenceData(getContext()),
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
                new AppsStatusPreferenceData(getContext()),
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
                new IntegerPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_ICON_PADDING,
                                getString(R.string.preference_icon_padding),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        2,
                        getString(R.string.unit_dp),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IntegerPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_ICON_SCALE,
                                getString(R.string.preference_icon_scale),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        24,
                        getString(R.string.unit_dp),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_NOTIFICATIONS,
                                getString(R.string.preference_show_notifications),
                                PreferenceData.Identifier.SectionIdentifier.NOTIFICATIONS
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
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
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_CLOCK,
                                getString(R.string.preference_show_clock),
                                PreferenceData.Identifier.SectionIdentifier.CLOCK
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_24H,
                                getString(R.string.preference_24h),
                                getString(R.string.preference_24h_desc),
                                PreferenceData.Identifier.SectionIdentifier.CLOCK
                        ),
                        false,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM,
                                getString(R.string.preference_ampm),
                                getString(R.string.preference_ampm_desc),
                                PreferenceData.Identifier.SectionIdentifier.CLOCK
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_DATE,
                                getString(R.string.preference_date),
                                getString(R.string.preference_date_desc),
                                PreferenceData.Identifier.SectionIdentifier.CLOCK
                        ),
                        false,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_ALARM_ICON,
                                getString(R.string.preference_show_alarm_icon),
                                PreferenceData.Identifier.SectionIdentifier.CLOCK
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_ALARM_ICON,
                                getString(R.string.preference_alarm_icon),
                                PreferenceData.Identifier.SectionIdentifier.CLOCK
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_alarm
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_BATTERY_ICON,
                                getString(R.string.preference_show_battery_icon),
                                PreferenceData.Identifier.SectionIdentifier.BATTERY
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_BATTERY_ICON,
                                getString(R.string.preference_battery_icon),
                                PreferenceData.Identifier.SectionIdentifier.BATTERY
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_battery_alert,
                                        R.drawable.ic_battery_20,
                                        R.drawable.ic_battery_30,
                                        R.drawable.ic_battery_50,
                                        R.drawable.ic_battery_60,
                                        R.drawable.ic_battery_80,
                                        R.drawable.ic_battery_90,
                                        R.drawable.ic_battery_full,
                                        R.drawable.ic_battery_charging_20,
                                        R.drawable.ic_battery_charging_30,
                                        R.drawable.ic_battery_charging_50,
                                        R.drawable.ic_battery_charging_60,
                                        R.drawable.ic_battery_charging_80,
                                        R.drawable.ic_battery_charging_90,
                                        R.drawable.ic_battery_charging_full
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_circle),
                                        R.drawable.ic_battery_circle_alert,
                                        R.drawable.ic_battery_circle_20,
                                        R.drawable.ic_battery_circle_30,
                                        R.drawable.ic_battery_circle_50,
                                        R.drawable.ic_battery_circle_60,
                                        R.drawable.ic_battery_circle_80,
                                        R.drawable.ic_battery_circle_90,
                                        R.drawable.ic_battery_circle_full,
                                        R.drawable.ic_battery_circle_charging_20,
                                        R.drawable.ic_battery_circle_charging_30,
                                        R.drawable.ic_battery_circle_charging_50,
                                        R.drawable.ic_battery_circle_charging_60,
                                        R.drawable.ic_battery_circle_charging_80,
                                        R.drawable.ic_battery_circle_charging_90,
                                        R.drawable.ic_battery_circle_charging_full
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_retro),
                                        R.drawable.ic_battery_retro_alert,
                                        R.drawable.ic_battery_retro_20,
                                        R.drawable.ic_battery_retro_30,
                                        R.drawable.ic_battery_retro_50,
                                        R.drawable.ic_battery_retro_60,
                                        R.drawable.ic_battery_retro_80,
                                        R.drawable.ic_battery_retro_90,
                                        R.drawable.ic_battery_retro_full,
                                        R.drawable.ic_battery_retro_20,
                                        R.drawable.ic_battery_retro_30,
                                        R.drawable.ic_battery_retro_50,
                                        R.drawable.ic_battery_retro_60,
                                        R.drawable.ic_battery_retro_80,
                                        R.drawable.ic_battery_retro_90,
                                        R.drawable.ic_battery_retro_full
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_circle_outline),
                                        R.drawable.ic_battery_circle_outline_alert,
                                        R.drawable.ic_battery_circle_outline_20,
                                        R.drawable.ic_battery_circle_outline_30,
                                        R.drawable.ic_battery_circle_outline_50,
                                        R.drawable.ic_battery_circle_outline_60,
                                        R.drawable.ic_battery_circle_outline_80,
                                        R.drawable.ic_battery_circle_outline_90,
                                        R.drawable.ic_battery_circle_outline_full,
                                        R.drawable.ic_battery_circle_outline_20,
                                        R.drawable.ic_battery_circle_outline_30,
                                        R.drawable.ic_battery_circle_outline_50,
                                        R.drawable.ic_battery_circle_outline_60,
                                        R.drawable.ic_battery_circle_outline_80,
                                        R.drawable.ic_battery_circle_outline_90,
                                        R.drawable.ic_battery_circle_outline_full
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_BATTERY_PERCENT,
                                getString(R.string.preference_battery_percent),
                                getString(R.string.preference_battery_percent_desc),
                                PreferenceData.Identifier.SectionIdentifier.BATTERY
                        ),
                        false,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_NETWORK_ICON,
                                getString(R.string.preference_show_network_icon),
                                PreferenceData.Identifier.SectionIdentifier.NETWORK
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_NETWORK_ICON,
                                getString(R.string.preference_network_icon),
                                PreferenceData.Identifier.SectionIdentifier.NETWORK
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_signal_0,
                                        R.drawable.ic_signal_1,
                                        R.drawable.ic_signal_2,
                                        R.drawable.ic_signal_3,
                                        R.drawable.ic_signal_4
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_square),
                                        R.drawable.ic_signal_square_0,
                                        R.drawable.ic_signal_square_1,
                                        R.drawable.ic_signal_square_2,
                                        R.drawable.ic_signal_square_3,
                                        R.drawable.ic_signal_square_4
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_retro),
                                        R.drawable.ic_signal_retro_0,
                                        R.drawable.ic_signal_retro_1,
                                        R.drawable.ic_signal_retro_2,
                                        R.drawable.ic_signal_retro_3,
                                        R.drawable.ic_signal_retro_4
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_DATA,
                                getString(R.string.preference_data),
                                getString(R.string.preference_data_desc),
                                PreferenceData.Identifier.SectionIdentifier.NETWORK
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_WIFI_ICON,
                                getString(R.string.preference_show_wifi_icon),
                                PreferenceData.Identifier.SectionIdentifier.WIFI
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_WIFI_ICON,
                                getString(R.string.preference_wifi_icon),
                                PreferenceData.Identifier.SectionIdentifier.WIFI
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_wifi_0,
                                        R.drawable.ic_wifi_1,
                                        R.drawable.ic_wifi_2,
                                        R.drawable.ic_wifi_3,
                                        R.drawable.ic_wifi_4
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_triangle),
                                        R.drawable.ic_wifi_triangle_0,
                                        R.drawable.ic_wifi_triangle_1,
                                        R.drawable.ic_wifi_triangle_2,
                                        R.drawable.ic_wifi_triangle_3,
                                        R.drawable.ic_wifi_triangle_4
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_retro),
                                        R.drawable.ic_wifi_retro_0,
                                        R.drawable.ic_wifi_retro_1,
                                        R.drawable.ic_wifi_retro_2,
                                        R.drawable.ic_wifi_retro_3,
                                        R.drawable.ic_wifi_retro_4
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_GPS_ICON,
                                getString(R.string.preference_show_gps_icon),
                                PreferenceData.Identifier.SectionIdentifier.GPS
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_GPS_ICON,
                                getString(R.string.preference_gps_icon),
                                PreferenceData.Identifier.SectionIdentifier.GPS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_gps_searching,
                                        R.drawable.ic_gps_fixed
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_dish),
                                        R.drawable.ic_gps_dish_searching,
                                        R.drawable.ic_gps_dish_fixed
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_BLUETOOTH_ICON,
                                getString(R.string.preference_show_bluetooth_icon),
                                PreferenceData.Identifier.SectionIdentifier.BLUETOOTH
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_BLUETOOTH_ICON,
                                getString(R.string.preference_bluetooth_icon),
                                PreferenceData.Identifier.SectionIdentifier.BLUETOOTH
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_bluetooth,
                                        R.drawable.ic_bluetooth_connected
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_AIRPLANE_MODE_ICON,
                                getString(R.string.preference_show_airplane_mode_icon),
                                PreferenceData.Identifier.SectionIdentifier.AIRPLANE_MODE
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_AIRPLANE_MODE_ICON,
                                getString(R.string.preference_airplane_mode_icon),
                                PreferenceData.Identifier.SectionIdentifier.AIRPLANE_MODE
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_airplane
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.SHOW_RINGER_ICON,
                                getString(R.string.preference_show_ringer_icon),
                                PreferenceData.Identifier.SectionIdentifier.RINGER
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STYLE_RINGER_ICON,
                                getString(R.string.preference_ringer_icon),
                                PreferenceData.Identifier.SectionIdentifier.RINGER
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_sound_mute,
                                        R.drawable.ic_sound_vibration
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                )
        ));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            preferences.add(
                    new BooleanPreferenceData(
                            getContext(),
                            new PreferenceData.Identifier(
                                    PreferenceUtils.PreferenceIdentifier.SHOW_NETWORK_DUAL_SIM,
                                    getString(R.string.preference_dual_sim),
                                    getString(R.string.preference_dual_sim_desc),
                                    PreferenceData.Identifier.SectionIdentifier.NETWORK
                            ),
                            false,
                            new PreferenceData.OnPreferenceChangeListener() {
                                @Override
                                public void onPreferenceChange() {
                                    updateService();
                                }
                            }
                    )
            );
        }

        PreferenceSectionAdapter adapter = new PreferenceSectionAdapter(getContext(), preferences);
        recycler.setAdapter(adapter);

        return v;
    }

    private void updateService() {
        if (StaticUtils.isStatusServiceRunning(getContext())) {
            Intent intent = new Intent(StatusService.ACTION_START);
            intent.setClass(getContext(), StatusService.class);
            getContext().startService(intent);
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_settings);
    }
}
