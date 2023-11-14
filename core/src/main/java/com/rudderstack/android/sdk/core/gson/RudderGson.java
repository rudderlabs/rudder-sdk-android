package com.rudderstack.android.sdk.core.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.RudderContext;
import com.rudderstack.android.sdk.core.RudderTraits;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderContextTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderJSONArrayTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderJSONObjectTypeAdapter;
import com.rudderstack.android.sdk.core.gson.gsonadapters.RudderTraitsTypeAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class RudderGson {
    private static Gson instance;

    private RudderGson() {
        // private constructor to prevent instantiation
    }

    public static Gson getInstance() {
        if (instance == null) {
            instance = new GsonBuilder()
                    .registerTypeAdapter(RudderTraits.class, new RudderTraitsTypeAdapter())
                    .registerTypeAdapter(RudderContext.class, new RudderContextTypeAdapter())
                    .registerTypeAdapter(JSONObject.class, new RudderJSONObjectTypeAdapter())
                    .registerTypeAdapter(JSONArray.class, new RudderJSONArrayTypeAdapter())
                    .create();
        }
        return instance;
    }
}
