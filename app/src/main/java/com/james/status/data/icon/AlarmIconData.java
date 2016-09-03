package com.james.status.data.icon;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;

@TargetApi(21)
public class AlarmIconData extends IconData<AlarmIconData.AlarmReceiver> {

    private AlarmManager alarmManager;

    public AlarmIconData(Context context) {
        super(context);
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public AlarmReceiver getReceiver() {
        return new AlarmReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
    }

    @Override
    public void register() {
        super.register();
        if (alarmManager.getNextAlarmClock() != null)
            onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
    }

    @Override
    public int[] getDefaultIconResource() {
        return new int[]{R.drawable.ic_alarm};
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (alarmManager.getNextAlarmClock() != null)
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
            else onDrawableUpdate(null);
        }
    }
}
