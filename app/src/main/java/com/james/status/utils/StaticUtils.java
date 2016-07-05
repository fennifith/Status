package com.james.status.utils;

import android.content.Context;
import android.util.TypedValue;

public class StaticUtils {

    public static int getStatusBarMargin(Context context) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, context.getResources().getDisplayMetrics());
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) height = context.getResources().getDimensionPixelSize(resId);
        return height;
    }

}
