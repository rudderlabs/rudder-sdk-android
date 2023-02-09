package com.rudderstack.android.sdk.core;

import android.util.Log;

/*
 * Logger class for RudderClient library
 * */
public class RudderLogger {
    private static int logLevel = RudderLogLevel.INFO;
    private static final String TAG = "RudderSDK";

    static void init(int l) {
        if (l > RudderLogLevel.VERBOSE) l = RudderLogLevel.VERBOSE;
        else if (l < RudderLogLevel.NONE) l = RudderLogLevel.NONE;
        logLevel = l;
    }

    public static void logError(Throwable throwable) {
        if (logLevel >= RudderLogLevel.ERROR) {
            Log.e(TAG, "Error: ", throwable);
        }
    }

    public static void logError(Exception ex) {
        logError(ex.getMessage());
    }

    public static void logError(String message) {
        if (logLevel >= RudderLogLevel.ERROR) {
            Log.e(TAG, "Error: " + message);
        }
    }

    public static void logWarn(String message) {
        if (logLevel >= RudderLogLevel.WARN) {
            Log.w(TAG, "Warn: " + message);
        }
    }

    public static void logInfo(String message) {
        if (logLevel >= RudderLogLevel.INFO) {
            Log.i(TAG, "Info: " + message);
        }
    }

    public static void logDebug(String message) {
        if (logLevel >= RudderLogLevel.DEBUG) {
            Log.d(TAG, "Debug: " + message);
        }
    }

    public static void logVerbose(String message) {
        if (logLevel >= RudderLogLevel.VERBOSE) {
            Log.v(TAG, "Verbose: " + message);
        }
    }

    public static class RudderLogLevel {
        public static final int VERBOSE = 5;
        public static final int DEBUG = 4;
        public static final int INFO = 3;
        public static final int WARN = 2;
        public static final int ERROR = 1;
        public static final int NONE = 0;
    }
}
