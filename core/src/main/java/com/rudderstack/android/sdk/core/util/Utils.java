package com.rudderstack.android.sdk.core.util;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rudderstack.android.sdk.core.RudderLogger;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static android.provider.Settings.Secure.ANDROID_ID;
import static android.provider.Settings.System.getString;

public class Utils {

    // range constants
    public static final int MIN_CONFIG_REFRESH_INTERVAL = 1;
    public static final int MAX_CONFIG_REFRESH_INTERVAL = 24;
    public static final int MIN_SLEEP_TIMEOUT = 10;
    public static final int MIN_FLUSH_QUEUE_SIZE = 1;
    public static final int MAX_FLUSH_QUEUE_SIZE = 100;
    public static final int MAX_EVENT_SIZE = 32 * 1024; // 32 KB
    public static final int MAX_BATCH_SIZE = 500 * 1024; // 500 KB

    public static String getTimeZone() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getID();
    }

    public static String getTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        formatter.setCalendar(new GregorianCalendar());
        return formatter.format(new Date());
    }

    public static String toDateString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formatter.setCalendar(new GregorianCalendar());
        return formatter.format(date);
    }

    public static String getDeviceId(Application application) {
        if (Build.VERSION.SDK_INT >= 17) {
            String androidId = getString(application.getContentResolver(), ANDROID_ID);
            if (!TextUtils.isEmpty(androidId)
                    && !"9774d56d682e549c".equals(androidId)
                    && !"unknown".equals(androidId)
                    && !"000000000000000".equals(androidId)
            ) {
                return androidId;
            }
        }

        // If this still fails, generate random identifier that does not persist across installations
        return UUID.randomUUID().toString();
    }

    public static Map<String, Object> convertToMap(String json) {
        return new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    public static List<Map<String, Object>> convertToList(String json) {
        return new Gson().fromJson(json, new TypeToken<List<Map<String, Object>>>() {
        }.getType());
    }

    public static String getWriteKeyFromStrings(Context context) {
        int id = context.getResources().getIdentifier(
                context.getPackageName(),
                "string",
                "rudder_write_key"
        );
        if (id != 0) {
            return context.getResources().getString(id);
        } else {
            return null;
        }
    }

    public static int getUTF8Length(String message) {
        int utf8Length;
        try {
            utf8Length = message.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException ex) {
            RudderLogger.logError(ex);
            utf8Length = -1;
        }
        return utf8Length;
    }

    public static int getUTF8Length(StringBuilder message) {
        return getUTF8Length(message.toString());
    }

    public static boolean isOnClassPath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public enum NetworkResponses {
        SUCCESS,
        ERROR,
        WRITE_KEY_ERROR
    }
}
