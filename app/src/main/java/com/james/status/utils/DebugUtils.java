package com.james.status.utils;

import android.app.Application;

import com.james.status.Status;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DebugUtils {

    private static final String[] SETUP_TASKS = {
            "com.james.status.utils.LeakCanaryTask"
    };

    /**
     * Set up any debug modules from the registered tasks. Should
     * be called inside the Application class's onCreate.
     *
     * @param application An instance of the current application class.
     */
    public static void setup(Application application) {
        for (String task : SETUP_TASKS) {
            try {
                Constructor<SetupTask> constructor = (Constructor<SetupTask>) Class.forName(task).getConstructor();
                constructor.setAccessible(true);
                constructor.newInstance().setup(application);
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public interface SetupTask {
        void setup(Application application);
    }

}
