package com.rudderstack.android.sdk.core.gsonadapters;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.rudderstack.android.sdk.core.ReportManager;
import com.rudderstack.android.sdk.core.RudderTraits;

import java.lang.reflect.Type;
import java.util.Map;

public class RudderTraitsSerializer implements JsonSerializer<RudderTraits> {
    @Override
    public JsonElement serialize(RudderTraits traits,
                                 Type typeOfSrc,
                                 JsonSerializationContext context) {
        try {
            Gson gson = new Gson();
            JsonObject outputTraits = new JsonObject();
            JsonObject inputTraits = (JsonObject) gson.toJsonTree(traits);
            for (Map.Entry<String, JsonElement> entry : inputTraits.entrySet()) {
                if (entry.getKey().equals("extras")) {
                    JsonObject extrasObject = (JsonObject) gson.toJsonTree(entry.getValue());
                    for (Map.Entry<String, JsonElement> extrasEntry : extrasObject.entrySet()) {
                        outputTraits.add(extrasEntry.getKey(), extrasEntry.getValue());
                    }
                    continue;
                }
                outputTraits.add(entry.getKey(), entry.getValue());
            }
            return outputTraits;
        } catch (Exception e) {
            ReportManager.reportError(e);
            return null;
        }
    }
}
