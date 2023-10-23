package com.rudderstack.android.sdk.core.gsonadapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;


public class RudderJSONArrayAdapter implements JsonSerializer<JSONArray>, JsonDeserializer<JSONArray> {

    @Override
    public JsonElement serialize(JSONArray src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < src.length(); i++) {
            Object object = src.opt(i);
            JsonElement jsonElement = context.serialize(object, object.getClass());
            jsonArray.add(jsonElement);
        }
        return jsonArray;
    }

    @Override
    public JSONArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        try {
            return new JSONArray(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JsonParseException(e);
        }
    }
}