package com.rudderstack.android.sdk.core.gsonadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Iterator;

public class RudderJSONObjectAdapter implements JsonSerializer<JSONObject>, JsonDeserializer<JSONObject> {

    @Override
    public JsonElement serialize(JSONObject src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }

        JsonObject jsonObject = new JsonObject();
        Iterator<String> keys = src.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = src.opt(key);

            JsonElement jsonElement = context.serialize(value, value.getClass());
            jsonObject.add(key, jsonElement);
        }
        return jsonObject;
    }

    @Override
    public JSONObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        try {
            return new JSONObject(json.toString());
        } catch (JSONException e) {
            throw new JsonParseException(e);
        }
    }
}

