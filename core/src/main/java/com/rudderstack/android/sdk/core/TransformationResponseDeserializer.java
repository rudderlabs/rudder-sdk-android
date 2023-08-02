package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.RudderDeviceModeManager.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TransformationResponseDeserializer implements JsonDeserializer<TransformationResponse> {
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

                if (payloadObject.has("event") && !payloadObject.get("event").isJsonNull()) {
                    JsonObject eventObject = payloadObject.getAsJsonObject("event");
                    if (eventObject.size() > 0) {
                        try {
                            message = gson.fromJson(eventObject, RudderMessage.class);
                        } catch (Exception e) {
                            RudderLogger.logError(String.format("TransformationResponseDeserializer: Error while parsing event object for the destinationId: %s, and error: %s", id, e));
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