package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import com.james.status.R;
import com.james.status.data.preference.FormatPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.utils.StaticUtils;

import java.util.Calendar;
import java.util.List;

public class TimeIconData extends IconData<TimeIconData.TimeReceiver> {

    private Calendar calendar;
    private String format;

    public TimeIconData(Context context) {
        super(context);

        calendar = Calendar.getInstance();

        format = getStringPreference(PreferenceIdentifier.TEXT_FORMAT);
        if (format == null) format = "h:mm a";
    }

    @Override
    public boolean canHazDrawable() {
        return false;
    }

    @Override
    public boolean hasDrawable() {
        return false;
    }

    @Override
    public boolean canHazText() {
        return true;
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
    public int getDefaultGravity() {
        return CENTER_GRAVITY;
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

        preferences.add(new FormatPreferenceData(
                getContext(),
                new PreferenceData.Identifier(
                        getContext().getString(R.string.preference_time_format)
                ),
                format,
                new PreferenceData.OnPreferenceChangeListener<String>() {
                    @Override
                    public void onPreferenceChange(String preference) {
                        format = preference;
                        putPreference(PreferenceIdentifier.TEXT_FORMAT, preference);
                        StaticUtils.updateStatusService(getContext());
                    }
                }
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
