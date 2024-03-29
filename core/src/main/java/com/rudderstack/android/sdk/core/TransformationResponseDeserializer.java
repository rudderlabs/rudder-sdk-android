package com.rudderstack.android.sdk.core;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.rudderstack.android.sdk.core.gson.RudderGson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TransformationResponseDeserializer implements JsonDeserializer<TransformationResponse> {
    public static final String EVENT = "event";

    @Override
    public TransformationResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray transformedBatchArray = jsonObject.getAsJsonArray("transformedBatch");

        List<TransformationResponse.TransformedDestination> transformedBatch = new ArrayList<>();
        for (JsonElement transformedBatchElement : transformedBatchArray) {
            JsonObject transformedBatchObject = transformedBatchElement.getAsJsonObject();
            String id = transformedBatchObject.get("id").getAsString();
            JsonArray payloadArray = transformedBatchObject.getAsJsonArray("payload");
            List<TransformationResponse.TransformedEvent> payloadList = new ArrayList<>();

            for (JsonElement payloadElement : payloadArray) {
                JsonObject payloadObject = payloadElement.getAsJsonObject();
                int orderNo = payloadObject.get("orderNo").getAsInt();
                String status = payloadObject.get("status").getAsString();
                RudderMessage message = null;

                if (payloadObject.has(EVENT) && !payloadObject.get(EVENT).isJsonNull()) {
                    JsonObject eventObject = payloadObject.getAsJsonObject(EVENT);
                    if (eventObject.size() > 0) {
                        message = RudderGson.deserialize(eventObject, RudderMessage.class);
                        if (message == null) {
                            RudderLogger.logError(String.format("TransformationResponseDeserializer: Error while parsing event object for the destinationId: %s", id));
                            continue;
                        }
                    }
                }
                TransformationResponse.TransformedEvent payload = new TransformationResponse.TransformedEvent(orderNo, status, message);
                payloadList.add(payload);
            }
            transformedBatch.add(new TransformationResponse.TransformedDestination(id, payloadList));
        }
        return new TransformationResponse(transformedBatch);
    }
}