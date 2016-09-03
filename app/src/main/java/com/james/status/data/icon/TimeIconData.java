package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimeIconData extends IconData<TimeIconData.TimeReceiver> {

    Calendar calendar;
    String format;

    public TimeIconData(Context context) {
        super(context);

        calendar = Calendar.getInstance();

        format = getStringPreference(PreferenceIdentifier.TEXT_FORMAT);
        if (format == null) format = "h:mm";
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

    public class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            onTextUpdate(DateFormat.format(format, calendar).toString());
        }
    }
}
