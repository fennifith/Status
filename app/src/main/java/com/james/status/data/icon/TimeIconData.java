package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import com.james.status.R;
import com.james.status.data.preference.FormatPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.receivers.IconUpdateReceiver;
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
    public int getDefaultGravity() {
        return CENTER_GRAVITY;
    }

    @Override
    public TimeReceiver getReceiver() {
        return new TimeReceiver(this);
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

    static class TimeReceiver extends IconUpdateReceiver<TimeIconData> {

        private TimeReceiver(TimeIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(TimeIconData icon, Intent intent) {
            icon.calendar.setTimeInMillis(System.currentTimeMillis());
            icon.onTextUpdate(DateFormat.format(icon.format, icon.calendar).toString());
        }
    }
}
