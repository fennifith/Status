package com.james.status.adapters;

import android.app.Notification;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.james.status.R;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;

public class NotificationIconAdapter extends ArrayAdapter<StatusBarNotification> {

    public NotificationIconAdapter(Context context, ArrayList<StatusBarNotification> notifications) {
        super(context, 0, notifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_icon, parent, false);

        ((CustomImageView) convertView.findViewById(R.id.icon)).transition(getIcon(getItem(position).getNotification()));

        return convertView;
    }

    public Drawable getIcon(Notification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return ContextCompat.getDrawable(getContext(), notification.icon);
        else
            return notification.getSmallIcon().loadDrawable(getContext());
    }
}
