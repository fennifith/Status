package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import com.james.status.utils.PreferenceUtils;

import java.util.Calendar;

public class TimeIconData extends IconData<TimeIconData.TimeReceiver> {

    Calendar calendar;
    String format;

    public TimeIconData(Context context) {
        super(context, null);

        calendar = Calendar.getInstance();

        Boolean is24h = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_24H);
        if (is24h != null && is24h) format = "kk:mm";
        else format = "h:mm";

        Boolean isAmPmEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM);
        if (isAmPmEnabled == null || isAmPmEnabled) format += " a";

        Boolean isDateEnabled = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_DATE);
        if (isDateEnabled != null && isDateEnabled) format += ", EEE MMM d";
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

    public class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            onTextUpdate(DateFormat.format(format, calendar).toString());
        }
    }
}
