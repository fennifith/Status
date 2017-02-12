package com.james.status.data.icon;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
import java.util.List;

public class AlarmIconData extends IconData<AlarmIconData.AlarmReceiver> {

    private AlarmManager alarmManager;

    public AlarmIconData(Context context) {
        super(context);
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public AlarmReceiver getReceiver() {
        return new AlarmReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return new IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
        else
            return new IntentFilter("android.intent.action.ALARM_CHANGED");
    }

    @Override
    public void register() {
        super.register();

        Object alarm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            alarm = alarmManager.getNextAlarmClock();
        else
            alarm = Settings.System.getString(getContext().getContentResolver(), android.provider.Settings.System.NEXT_ALARM_FORMATTED);

        if (alarm != null)
            onDrawableUpdate(0);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_alarm);
    }

    @Override
    public int getIconStyleSize() {
        return 1;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_alarm
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_system),
                                IconStyleData.TYPE_IMAGE,
                                android.R.drawable.ic_popup_reminder
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_clear),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_alarm_clear
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_school),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_alarm_school
                        )
                )
        );

        return styles;
    }

    static class AlarmReceiver extends IconUpdateReceiver<AlarmIconData> {

        private AlarmReceiver(AlarmIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(AlarmIconData icon, Intent intent) {
            Object alarm = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                alarm = icon.alarmManager.getNextAlarmClock();
            else
                alarm = Settings.System.getString(icon.getContext().getContentResolver(), android.provider.Settings.System.NEXT_ALARM_FORMATTED);

            if (alarm != null)
                icon.onDrawableUpdate(0);
            else icon.onDrawableUpdate(-1);
        }
    }
}
