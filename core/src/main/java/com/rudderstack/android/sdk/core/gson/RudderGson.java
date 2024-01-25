package com.rudderstack.android.sdk.core.gson;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.internal.bind.TypeAdapters;
import com.rudderstack.android.sdk.core.ReportManager;
import com.rudderstack.android.sdk.core.RudderContext;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderTraits;
import com.rudderstack.android.sdk.core.gson.gsonadapters.DoubleTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.FloatTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderContextTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderJSONArrayTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderJSONObjectTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderTraitsTypeAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;

public class RudderGson {
    public static final String RUDDER_GSON_DESERIALIZE_EXCEPTION = "RudderGson: deserialize: Exception: ";
    private static Gson gson = buildGsonInstance();

    private RudderGson() {
        // private constructor to prevent instantiation
    }

    private static Gson buildGsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, new DoubleTypeAdapter()))
                .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, new FloatTypeAdapter()))
                .registerTypeAdapter(RudderTraits.class, new RudderTraitsTypeAdapter())
                .registerTypeAdapter(RudderContext.class, new RudderContextTypeAdapter())
                .registerTypeAdapter(JSONObject.class, new RudderJSONObjectTypeAdapter())
                .registerTypeAdapter(JSONArray.class, new RudderJSONArrayTypeAdapter())
                .create();
    }

    @Nullable
    public static String serialize(Object object) {
        try {
            return gson.toJson(object);
        } catch (Exception e) {
            RudderLogger.logError("RudderGson: serialize: Exception: " + e.getMessage());
            ReportManager.reportError(e);
        }
        return null;
    }

    @Nullable
    public static <T> T deserialize(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            RudderLogger.logError(RUDDER_GSON_DESERIALIZE_EXCEPTION + e.getMessage());
            ReportManager.reportError(e);
        }
        return null;
    }

    @Nullable
    public static <T> T deserialize(String json, Type typeOfT) {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (Exception e) {
            RudderLogger.logError(RUDDER_GSON_DESERIALIZE_EXCEPTION + e.getMessage());
            ReportManager.reportError(e);
        }
        return null;
    }

    @Nullable
    public static <T> T deserialize(JsonElement element, Class<T> classOfT) {
        try {
            return gson.fromJson(element, classOfT);
        } catch (Exception e) {
            RudderLogger.logError(RUDDER_GSON_DESERIALIZE_EXCEPTION + e.getMessage());
            ReportManager.reportError(e);
        }
        return null;
    }

    @Nullable
    public static <T> T deserialize(JsonElement element, Type typeOfT) {
        try {
            return gson.fromJson(element, typeOfT);
        } catch (Exception e) {
            RudderLogger.logError(RUDDER_GSON_DESERIALIZE_EXCEPTION + e.getMessage());
            ReportManager.reportError(e);
        }
        return null;
    }
}
