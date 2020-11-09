package com.wellleo.common.utils;

import android.util.Log;

import com.wellleo.common.BuildConfig;

public class CommonLog {

    private static final String TAG = "CustomViewExample";
    private static final boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void DEBUG(String log) {
        if (IS_DEBUG) {
            Log.d(TAG, log);
        }
    }

    public static void VERBOSE(String log) {
        if (IS_DEBUG) {
            Log.v(TAG, log);
        }
    }

    public static void INFO(String log) {
        if (IS_DEBUG) {
            Log.i(TAG, log);
        }
    }

    public static void WARN(String log) {
        if (IS_DEBUG) {
            Log.w(TAG, log);
        }
    }

    public static void ERROR(String log) {
        if (IS_DEBUG) {
            Log.e(TAG, log);
        }
    }
}
