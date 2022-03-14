package com.rudderstack.android.sdk.core;


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
            System.out.println(TAG + " " + "Error: " + throwable);
        }
    }

    /*public  static void logError(Exception ex) {
        logError(ex.getMessage());
    }*/

    public static void logError(String message) {
        if (logLevel >= RudderLogLevel.ERROR) {
            System.out.println(TAG + " " + "Error: " + message);
        }
    }

    public static void logWarn(String message) {
        if (logLevel >= RudderLogLevel.WARN) {
            System.out.println(TAG + " "  + message);
        }
    }

    public static void logInfo(String message) {
        if (logLevel >= RudderLogLevel.INFO) {
            System.out.println(TAG + " " + message);
        }
    }

    public static void logDebug(String message) {
        if (logLevel >= RudderLogLevel.DEBUG) {
            System.out.println(TAG + " "+ message);
        }
    }

    public static void logVerbose(String message) {
        if (logLevel >= RudderLogLevel.VERBOSE) {
            System.out.println(TAG + " "+ message);
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
