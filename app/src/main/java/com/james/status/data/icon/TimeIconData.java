package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import com.james.status.R;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.utils.StaticUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TimeIconData extends IconData<TimeIconData.TimeReceiver> {

    Calendar calendar;
    String format;

    public TimeIconData(Context context) {
        super(context);

        calendar = Calendar.getInstance();

        format = getStringPreference(PreferenceIdentifier.TEXT_FORMAT);
        if (format == null) format = "h:mm a";
    }

    @Override
    public boolean hasDrawable() {
        return false;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getFakeText() {
        return format;
    }

    @Override
    public TimeReceiver getReceiver() {
        return new TimeReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        return filter;
    }

    @Override
    public void register() {
        super.register();

        calendar.setTimeInMillis(System.currentTimeMillis());
        onTextUpdate(DateFormat.format(format, calendar).toString());
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_clock);
    }

    @Override
    public List<PreferenceData> getPreferences() {
        List<PreferenceData> preferences = super.getPreferences();

        preferences.addAll(Arrays.asList(
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                getContext().getString(R.string.preference_24h),
                                getContext().getString(R.string.preference_24h_desc)
                        ),
                        format.contains("kk"),
                        new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                            @Override
                            public void onPreferenceChange(Boolean preference) {
                                if (preference) format = format.replace("h", "kk");
                                else format = format.replace("kk", "h");

                                putPreference(PreferenceIdentifier.TEXT_FORMAT, format);
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                getContext().getString(R.string.preference_ampm),
                                getContext().getString(R.string.preference_ampm_desc)
                        ),
                        format.contains("a"),
                        new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                            @Override
                            public void onPreferenceChange(Boolean preference) {
                                if (preference && !format.contains("a")) {
                                    if (format.contains(",")) {
                                        format = format.substring(0, format.indexOf(",")) + " a" + format.substring(format.indexOf(","), format.length());
                                    } else format += " a";
                                } else format = format.replace(" a", "");

                                putPreference(PreferenceIdentifier.TEXT_FORMAT, format);
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                ),
                new BooleanPreferenceData(
                        getContext(),
                        new PreferenceData.Identifier(
                                getContext().getString(R.string.preference_date),
                                getContext().getString(R.string.preference_date_desc)
                        ),
                        format.contains(", EEE MMM d"),
                        new PreferenceData.OnPreferenceChangeListener<Boolean>() {
                            @Override
                            public void onPreferenceChange(Boolean preference) {
                                if (preference && !format.contains(", EEE MMM d"))
                                    format += ", EEE MMM d";
                                else format = format.replace(", EEE MMM d", "");

                                putPreference(PreferenceIdentifier.TEXT_FORMAT, format);
                                StaticUtils.updateStatusService(getContext());
                            }
                        }
                )
        ));

        return preferences;
    }

    public class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            onTextUpdate(DateFormat.format(format, calendar).toString());
        }
    }
}
