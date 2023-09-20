package com.rudderstack.android.sdk.core.util;

import android.app.Activity;
import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ParseException;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rudderstack.android.sdk.core.ReportManager;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderProperty;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.UI_MODE_SERVICE;
import static android.provider.Settings.Secure.ANDROID_ID;
import static android.provider.Settings.System.getString;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Utils {

    // range constants
    public static final int MIN_CONFIG_REFRESH_INTERVAL = 1;
    public static final int MAX_CONFIG_REFRESH_INTERVAL = 24;
    public static final int MIN_SLEEP_TIMEOUT = 1;
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
        formatter.setCalendar(new GregorianCalendar());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }

    public static Long getCurrentTimeInSecondsLong() {
        return new Long(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    }

    public static String getCurrentTimeSeconds() {
        return String.format(Locale.US, "%d", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    }

    public static Long getCurrentTimeInMilliSeconds() {
        return new Long(new Date().getTime());
    }

    public static String toDateString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formatter.setCalendar(new GregorianCalendar());
        return formatter.format(date);
    }

    @Nullable
    public static String getDeviceId(Application application) {
        String androidId = getString(application.getContentResolver(), ANDROID_ID);
        if (!TextUtils.isEmpty(androidId)
                && !"9774d56d682e549c".equals(androidId)
                && !"unknown".equals(androidId)
                && !"000000000000000".equals(androidId)
        ) {
            return androidId;
        }
        return null;
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
            ReportManager.reportError(ex);
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

    public static boolean lifeCycleDependenciesExists() {
        return isOnClassPath("androidx.lifecycle.DefaultLifecycleObserver")
                && isOnClassPath("androidx.lifecycle.LifecycleOwner") && isOnClassPath("androidx.lifecycle.ProcessLifecycleOwner");
    }

    public static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file != null && file.exists();
    }

    /**
     * Returns information about who launched this activity.
     */
    public static String getReferrer(Activity activity) {
        // If devices running on SDK versions greater than equal to 22
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return activity.getReferrer().toString();
        }
        // If devices running on SDK versions greater than equal to 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = activity.getIntent();
            try {
                Uri referrer = intent.getParcelableExtra(Intent.EXTRA_REFERRER);
                if (referrer != null) {
                    return referrer.toString();
                }
                // Intent.EXTRA_REFERRER_NAME
                String referrerName = intent.getStringExtra("android.intent.extra.REFERRER_NAME");
                if (referrerName != null) {
                    return Uri.parse(referrerName).toString();
                }
            } catch (BadParcelableException | ParseException e) {
                ReportManager.reportError(e);
                return null;
            }
        }
        return null;
    }

    /**
     * Returns whether the app is running on a TV device.
     *
     * @param context Any context.
     * @return Whether the app is running on a TV device.
     */
    public static boolean isTv(Context context) {
        UiModeManager uiModeManager =
                (UiModeManager) context.getApplicationContext().getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
                && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    /**
     * Returns the number of batches the given number of events can be split into considering the batch size configured.
     */
    public static int getNumberOfBatches(int numberOfEvents, int flushQueueSize) {
        if (numberOfEvents % flushQueueSize == 0) {
            return numberOfEvents / flushQueueSize;
        } else {
            return (numberOfEvents / flushQueueSize) + 1;
        }
    }

    /**
     * Returns a batch of messageDetails from a list of messageDetails provided considering the batch size configured.
     */
    public static <T> List<T> getBatch(List<T> messageDetails, int flushQueueSize) {
        if (messageDetails.size() <= flushQueueSize) {
            return messageDetails;
        } else {
            return new ArrayList<>(messageDetails.subList(0, flushQueueSize));
        }
    }

    /**
     * @param integers the input list of integers which are to be converted into a csv string
     * @return a string which is the csv format of the List<Integer> provided.
     */
    public static String getCSVString(List<Integer> integers) {
        int size = integers.size();
        if (size <= 0) return null;
        StringBuilder sb = new StringBuilder("(" + integers.get(0));
        if (size > 1)
            for (int i = 1; i < size; i++) {
                sb.append(",").append(integers.get(i));
            }
        sb.append(")");
        return sb.toString();
    }

    /**
     * @param map   the map on which we need to check for the key associated with the value provided.
     * @param value the value for which we need to find the key associated with it.
     * @param <K>   the type of the key
     * @param <V>   the type of the value
     * @return the key associated for the value in the map provided.
     */
    public static <K, V> K getKeyForValueFromMap(Map<K, V> map, Object value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the value associated with key in the map after casting it into boolean, if the key exists and casting it boolean is possible, else returns false.
     */
    public static <K, V> boolean getBooleanFromMap(Map<K, V> map, K key) {
        if (!map.containsKey(key))
            return false;
        V value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    @NonNull
    public static String appendSlashToUrl(@NonNull String dataPlaneUrl) {
        if (!dataPlaneUrl.endsWith("/")) dataPlaneUrl += "/";
        return dataPlaneUrl;
    }

    public static boolean isEmpty(@Nullable Map value) {
        return (value == null || value.isEmpty());
    }

    public static boolean isEmpty(@Nullable String value) {
        return (value == null || value.isEmpty());
    }

    public static boolean isEmpty(@Nullable List value) {
        return (value == null || value.isEmpty());
    }
}
