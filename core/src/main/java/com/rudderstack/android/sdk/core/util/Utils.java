package com.rudderstack.android.sdk.core.util;

import android.app.Activity;
import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ParseException;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderProperty;

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
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.UI_MODE_SERVICE;
import static android.provider.Settings.Secure.ANDROID_ID;
import static android.provider.Settings.System.getString;

import androidx.annotation.NonNull;

public class Utils {

    // range constants
    public static final int MIN_CONFIG_REFRESH_INTERVAL = 1;
    public static final int MAX_CONFIG_REFRESH_INTERVAL = 24;
    public static final int MIN_SLEEP_TIMEOUT = 5;
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

    /**
     * Returns referring_application, url and its query parameter.
     */
    @NonNull
    public static RudderProperty trackDeepLink(Activity activity, AtomicBoolean isFirstLaunch, int versionCode) {
        RudderProperty rudderProperty = new RudderProperty()
                .putValue("from_background", !isFirstLaunch.get());
        // If it is not firstLaunch then return RudderProperty instance
        if (!isFirstLaunch.getAndSet(false)) {
            return rudderProperty;
        }
        rudderProperty.putValue("version", versionCode);
        try {
            Intent intent = activity.getIntent();
            if (intent == null || intent.getData() == null) {
                return rudderProperty;
            }

            // Get information about who launched this activity
            String referrer = getReferrer(activity);
            if (referrer != null) {
                rudderProperty.putValue("referring_application", referrer);
            }

            Uri uri = intent.getData();
            if (uri != null) {
                try {
                    for (String parameter : uri.getQueryParameterNames()) {
                        String value = uri.getQueryParameter(parameter);
                        if (value != null && !value.trim().isEmpty()) {
                            rudderProperty.putValue(parameter, value);
                        }
                    }
                } catch (Exception e) {
                    RudderLogger.logError("Failed to get uri query parameters: " + e);
                }
                rudderProperty.putValue("url", uri.toString());
            }
        } catch (Exception e) {
            RudderLogger.logError("Error occurred while tracking deep link" + e);
        }
        return rudderProperty;
    }

    /**
     * Returns information about who launched this activity.
     */
    private static String getReferrer(Activity activity) {
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

    public enum NetworkResponses {
        SUCCESS,
        ERROR,
        WRITE_KEY_ERROR
    }

    // Comment : We should decide on having a separate Utils class as this is public to everyone and we are placing SDK useful methods as well in this
    public static int getNumberOfBatches(int numberOfEvents, int flushQueueSize) {
        if (numberOfEvents % flushQueueSize == 0) {
            return numberOfEvents / flushQueueSize;
        } else {
            return (numberOfEvents / flushQueueSize) + 1;
        }
    }

    public static <T> ArrayList<T> getBatch(ArrayList<T> messageDetails, int flushQueueSize) {
        if (messageDetails.size() <= flushQueueSize) {
            return messageDetails;
        } else {
            return new ArrayList<T>(messageDetails.subList(0, flushQueueSize));
        }
    }

}
