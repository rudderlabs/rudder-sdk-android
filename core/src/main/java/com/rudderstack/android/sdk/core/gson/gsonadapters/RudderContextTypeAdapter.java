package com.rudderstack.android.sdk.core.gson.gsonadapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.bind.TypeAdapters;
import com.rudderstack.android.sdk.core.ReportManager;
import com.rudderstack.android.sdk.core.RudderContext;

import java.lang.reflect.Type;
import java.util.Map;

public class RudderContextTypeAdapter implements JsonSerializer<RudderContext> {
    @Override
    public JsonElement serialize(RudderContext rudderContext,
                                 Type typeOfSrc,
                                 JsonSerializationContext context) {
        try {
            JsonObject outputContext = new JsonObject();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, new DoubleTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, new FloatTypeAdapter()))
                    .create();
            JsonObject inputContext = (JsonObject) gson.toJsonTree(rudderContext);
            for (Map.Entry<String, JsonElement> entry : inputContext.entrySet()) {
                if (entry.getKey().equals("customContextMap")) {
                    JsonObject customContextMapObject = (JsonObject) gson.toJsonTree(entry.getValue());
                    for (Map.Entry<String, JsonElement> customContextEntry : customContextMapObject.entrySet()) {
                        outputContext.add(customContextEntry.getKey(), customContextEntry.getValue());
                    }
                    continue;
                }
                outputContext.add(entry.getKey(), entry.getValue());
            }
            return outputContext;
        } catch (Exception e) {
            ReportManager.reportError(e);
            return null;
        }
    }
}
