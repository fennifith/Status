package com.james.status.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

public class ViewService extends Service {

    private WindowManager windowManager;
    private ArrayList<View> views;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        views = new ArrayList<>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void addView(@NonNull View view, @Nullable WindowManager.LayoutParams params) {
        if (params == null) params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.format = PixelFormat.TRANSLUCENT;

        windowManager.addView(view, params);
        views.add(view);
    }

    public void removeView(@NonNull View view) {
        if (views != null && views.contains(view)) {
            windowManager.removeView(view);
            views.remove(view);
        }
    }

    @Override
    public void onDestroy() {
        if (views != null) {
            for (View view : views) {
                windowManager.removeView(view);
            }
        }
        views = null;
        super.onDestroy();
    }
}
