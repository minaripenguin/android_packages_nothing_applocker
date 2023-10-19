package com.nothing.applocker;

import android.app.ActivityManager;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.List;

public class WindowModeUtil {
    private static final String PKG_APP_LOCKER = "com.nothing.applocker";
    private static final String TAG = "WindowModeUtil";

    public static boolean isAppInMultiWindowMode() {
        return isAppInWindowMode("inMultiWindowMode");
    }

    public static boolean isAppInPinnedWindowWindowMode() {
        return isAppInWindowMode("isNtPinnedWindowWindowMode");
    }

    public static boolean isAppInWindowformWindowMode() {
        return isAppInWindowMode("isNtWindowformWindowMode");
    }

    static boolean isAppInWindowMode(String str) {
        List<ActivityManager.RunningTaskInfo> task = getTask(3);
        if (task != null && !task.isEmpty()) {
            for (int i = 0; i < task.size(); i++) {
                try {
                    int windowingMode = getWindowingMode(task.get(i));
                    String packageName = task.get(i).topActivity.getPackageName();
                    Log.d(TAG, "isAppInWindowMode " + str + i + " - " + packageName + " windowingMode " + windowingMode);
                    if (TextUtils.equals(packageName, "com.nothing.applocker") && checkMode(str, windowingMode)) {
                        return true;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "isAppInWindowMode Exception: " + e.toString());
                }
            }
        }
        return false;
    }

    static List<ActivityManager.RunningTaskInfo> getTask(int i) {
        try {
            Class<?> cls = Class.forName("android.app.ActivityTaskManager");
            Method declaredMethod = cls.getDeclaredMethod("getInstance", new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(null, new Object[0]);
            Method declaredMethod2 = cls.getDeclaredMethod("getTasks", Integer.TYPE);
            declaredMethod2.setAccessible(true);
            return (List) declaredMethod2.invoke(invoke, Integer.valueOf(i));
        } catch (Exception e) {
            Log.d(TAG, "getTask Exception: " + e.toString());
            return null;
        }
    }

    static int getWindowingMode(ActivityManager.RunningTaskInfo runningTaskInfo) {
        try {
            return ((Integer) Class.forName("android.app.TaskInfo").getDeclaredMethod("getWindowingMode", new Class[0]).invoke(runningTaskInfo, new Object[0])).intValue();
        } catch (Exception e) {
            Log.d(TAG, "getWindowingMode Exception: " + e.toString());
            return 0;
        }
    }

    static boolean checkMode(String str, int i) {
        try {
            boolean booleanValue = ((Boolean) Class.forName("android.app.WindowConfiguration").getDeclaredMethod(str, Integer.TYPE).invoke(null, Integer.valueOf(i))).booleanValue();
            Log.d(TAG, "checkMode() = [" + booleanValue + "]");
            return booleanValue;
        } catch (Exception e) {
            Log.d(TAG, "checkMode method " + str + " Exception: " + e.toString());
            return false;
        }
    }
}
