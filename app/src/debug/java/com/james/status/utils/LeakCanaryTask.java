package com.james.status.utils;

import android.app.Application;

public final class LeakCanaryTask implements DebugUtils.SetupTask {

    @Override
    public void setup(Application application) {
        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectNetwork()
                .penaltyLog()
                .penaltyDeath()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());*/

        LeakLoggerService.setupLeakCanary(application);
    }
}
