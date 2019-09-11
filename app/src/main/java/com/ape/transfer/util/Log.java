package com.ape.transfer.util;


import com.ape.transfer.BuildConfig;

public class Log {

    private static final String LOG_TAG = "ApeTransfer";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void i(String TAG, String msg) {
        if (DEBUG)
            android.util.Log.i(TAG, LOG_TAG + "-->" + msg);
    }

    public static void i(String msg) {
        if (DEBUG)
            android.util.Log.i(LOG_TAG, msg);
    }

    public static void i(String msg, Throwable e) {
        if (DEBUG)
            android.util.Log.i(LOG_TAG, msg, e);
    }

    public static void i(String TAG, String msg, Throwable e) {
        if (DEBUG)
            android.util.Log.i(TAG, LOG_TAG + "-->" + msg, e);
    }

    public static void d(String TAG, String msg) {
        if (DEBUG)
            android.util.Log.d(TAG, LOG_TAG + "-->" + msg);
    }

    public static void d(String msg) {
        if (DEBUG)
            android.util.Log.d(LOG_TAG, msg);
    }

    public static void d(String msg, Throwable e) {
        if (DEBUG)
            android.util.Log.d(LOG_TAG, msg, e);
    }

    public static void d(String TAG, String msg, Throwable e) {
        if (DEBUG)
            android.util.Log.d(TAG, LOG_TAG + "-->" + msg, e);
    }

    public static void e(String TAG, String msg) {
        if (DEBUG)
            android.util.Log.e(TAG, LOG_TAG + "-->" + msg);
    }

    public static void e(String msg) {
        if (DEBUG)
            android.util.Log.e(LOG_TAG, msg);
    }

    public static void e(String msg, Throwable e) {
        if (DEBUG)
            android.util.Log.e(LOG_TAG, msg, e);
    }

    public static void e(String TAG, String msg, Throwable e) {
        if (DEBUG)
            android.util.Log.e(TAG, LOG_TAG + "-->" + msg, e);
    }
}
