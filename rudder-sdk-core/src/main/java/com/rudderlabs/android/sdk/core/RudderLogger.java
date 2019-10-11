package com.rudderlabs.android.sdk.core;

import android.util.Log;

/*
 * Logger class for RudderClient library
 * */
public class RudderLogger {
    private static int logLevel = RudderLogLevel.INFO;
    private static final String TAG = "RudderSDK";

    static void init(int l) {
        if (l > RudderLogLevel.DEBUG) l = RudderLogLevel.DEBUG;
        else if (l < RudderLogLevel.NONE) l = RudderLogLevel.NONE;
        logLevel = l;
    }

    static void logError(Throwable throwable) {
        if (logLevel >= RudderLogLevel.ERROR) {
            Log.e(TAG, "Error: ", throwable);
        }
    }

    static void logError(Exception ex) {
        if (logLevel >= RudderLogLevel.ERROR) {
            Log.e(TAG, "Error: ", ex.getCause());
        }
    }

    static void logError(String message) {
        if (logLevel >= RudderLogLevel.ERROR) {
            Log.e(TAG, "Error: " + message);
        }
    }

    public static void logWarn(String message) {
        if (logLevel >= RudderLogLevel.WARN) {
            Log.w(TAG, "Warn: " + message);
        }
    }

    static void logInfo(String message) {
        if (logLevel >= RudderLogLevel.INFO) {
            Log.i(TAG, "Info: " + message);
        }
    }

    public static void logDebug(String message) {
        if (logLevel >= RudderLogLevel.DEBUG) {
            Log.d(TAG, "Debug: " + message);
        }
    }

    static class RudderLogLevel {
        static final int DEBUG = 4;
        static final int INFO = 3;
        static final int WARN = 2;
        static final int ERROR = 1;
        static final int NONE = 0;
    }
}
