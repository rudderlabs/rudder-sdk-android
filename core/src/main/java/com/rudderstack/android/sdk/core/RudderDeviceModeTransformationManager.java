package com.rudderstack.android.sdk.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.rudderstack.android.sdk.core.RudderNetworkManager.Result;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.NetworkResponses;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.RequestMethod;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.addEndPoint;

public class RudderDeviceModeTransformationManager {

    private final DBPersistentManager dbManager;
    private final RudderNetworkManager rudderNetworkManager;
    private final RudderDeviceModeManager rudderDeviceModeManager;
    private final RudderDataResidencyManager dataResidencyManager;
    private final RudderConfig config;
    // batch size for device mode transformation
    private static final int DMT_BATCH_SIZE = 12;
    //scheduled executor for device mode transformation
    private final ScheduledExecutorService deviceModeExecutor = Executors.newScheduledThreadPool(2);


    final ArrayList<Integer> messageIds = new ArrayList<>();
    final ArrayList<String> messages = new ArrayList<>();

    private static final String TRANSFORMATION_ENDPOINT = "transform";
    private static final int MAX_RETRIES = 2; // Maximum number of retries
    private static final int MAX_DELAY = 1000; // Maximum delay in milliseconds
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();

    RudderDeviceModeTransformationManager(DBPersistentManager dbManager, RudderNetworkManager rudderNetworkManager, RudderDeviceModeManager rudderDeviceModeManager, RudderConfig config, RudderDataResidencyManager dataResidencyManager) {
        this.dbManager = dbManager;
        this.rudderNetworkManager = rudderNetworkManager;
        this.rudderDeviceModeManager = rudderDeviceModeManager;
        this.dataResidencyManager = dataResidencyManager;
        this.config = config;
    }

    // checking how many seconds passed since last successful transformation
    private int deviceModeSleepCount = 0;
    private int retryCount = 0;
    private final Map<Integer, RudderTransformationRequest> messageMap = new HashMap<>();

    void startDeviceModeTransformationProcessor() {
        deviceModeExecutor.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        long deviceModeEventsCount = dbManager.getDeviceModeRecordCount();
                        RudderLogger.logDebug("DeviceModeTransformationManager: DeviceModeTransformationProcessor: fetching device mode events to flush to transformation service");
                        if ((deviceModeSleepCount >= config.getSleepTimeOut() && deviceModeEventsCount > 0) || deviceModeEventsCount >= DMT_BATCH_SIZE) {
                            retryCount = 0;
                            do {
                                messages.clear();
                                messageIds.clear();
                                messageMap.clear();
                                synchronized (MessageUploadLock.DEVICE_TRANSFORMATION_LOCK) {
                                    dbManager.fetchDeviceModeEventsFromDb(messageIds, messages, DMT_BATCH_SIZE);
                                }
                                createMessageMap();
                                String requestJson = createDeviceTransformPayload();

                                RudderLogger.logDebug(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: Payload: %s", requestJson));
                                RudderLogger.logInfo(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: EventCount: %d", messageIds.size()));

                                Result result = rudderNetworkManager.sendNetworkRequest(requestJson, addEndPoint(dataResidencyManager.getDataPlaneUrl(), TRANSFORMATION_ENDPOINT), RequestMethod.POST, false, true);
                                boolean isTransformationIssuePresent = handleTransformationResponse(result, requestJson);
                                if (isTransformationIssuePresent) {
                                    break;
                                }

                                RudderLogger.logDebug(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: SleepCount: %d", deviceModeSleepCount));
                            } while (dbManager.getDeviceModeRecordCount() > 0);
                        }
                        RudderLogger.logDebug(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: SleepCount: %d", deviceModeSleepCount));
                        deviceModeSleepCount++;
                    }
                }
                , 1, 1, TimeUnit.SECONDS);
    }

    private void createMessageMap() {
        for (int i = 0; i < messageIds.size(); i++) {
            RudderMessage message = gson.fromJson(messages.get(i), RudderMessage.class);
            RudderTransformationRequest transformationRequest = new RudderTransformationRequest();
            transformationRequest.setMessage(message);
            messageMap.put(messageIds.get(i), transformationRequest);
        }
    }

    private boolean handleTransformationResponse(Result result, String requestJson) {
        if (result.status == NetworkResponses.WRITE_KEY_ERROR) {
            RudderLogger.logDebug("DeviceModeTransformationManager: TransformationProcessor: Wrong WriteKey. Aborting");
            return true;
        } else if (result.status == NetworkResponses.NETWORK_UNAVAILABLE) {
            RudderLogger.logDebug("DeviceModeTransformationManager: TransformationProcessor: Network unavailable. Aborting");
            return true;
        } else if (result.status == NetworkResponses.ERROR) {
            handleError(requestJson);
        } else if (result.status == NetworkResponses.RESOURCE_NOT_FOUND) { // dumping back the original messages itself to the factories as transformation feature is not enabled
            handleResourceNotFound(requestJson);
        } else {
            handleSuccess(result);
        }
        return false;
    }

    private void handleError(String requestJson) {
        int delay = Math.min((1 << retryCount) * 500, MAX_DELAY); // Exponential backoff
        if (retryCount++ == MAX_RETRIES) {
            retryCount = 0;
            deviceModeSleepCount = 0;
            rudderDeviceModeManager.dumpOriginalEvents(gson.fromJson(requestJson, TransformationRequest.class), true);
            completeDeviceModeEventProcessing();
        } else {
            RudderLogger.logDebug("DeviceModeTransformationManager: TransformationProcessor: Retrying in " + delay + "s");
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                RudderLogger.logError(e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleResourceNotFound(String requestJson) {
        deviceModeSleepCount = 0;
        rudderDeviceModeManager.dumpOriginalEvents(gson.fromJson(requestJson, TransformationRequest.class), false);
        completeDeviceModeEventProcessing();
    }

    private void handleSuccess(Result result) {
        deviceModeSleepCount = 0;
        try {
            TransformationResponse transformationResponse = gson.fromJson(result.response, TransformationResponse.class);
            rudderDeviceModeManager.dumpTransformedEvents(transformationResponse);
        } catch (Exception e) {
            RudderLogger.logError("DeviceModeTransformationManager: TransformationProcessor: Error encountered during transformed response conversion to TransformationResponse format: " + e);
        }
        completeDeviceModeEventProcessing();
    }

    private void completeDeviceModeEventProcessing() {
        RudderLogger.logDebug(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: Updating status as DEVICE_MODE_PROCESSING DONE for events %s", messageIds));
        dbManager.markDeviceModeDone(messageIds);
        dbManager.runGcForEvents();
    }

    RudderMessage getEventFromMessageId(int messageId) {
        RudderTransformationRequest transformationRequest = messageMap.get(messageId);
        if (transformationRequest == null) {
            throw new NullPointerException();
        }
        return transformationRequest.getMessage();
    }

    // For each message get the list of destinationIds for which transformation is enabled
    private void setTransformationEnabledDestinationIds(List<String> messages) {
        for (int i = 0; i < messages.size(); i++) {
            RudderTransformationRequest transformationRequest = messageMap.get(messageIds.get(i));
            if (transformationRequest == null) {
                throw new NullPointerException();
            }
            RudderMessage message = transformationRequest.getMessage();
            List<String>  destinationIds = this.rudderDeviceModeManager.getTransformationEnabledDestinationIds(message);
            transformationRequest.setDestinationIds(destinationIds);
        }
    }

    private String createDeviceTransformPayload() {
        try {
            setTransformationEnabledDestinationIds(messages);
        } catch (NullPointerException e) {
            RudderLogger.logError("DeviceModeTransformationManager: createDeviceTransformPayload: Error while getting transformation enabled destination Ids. Aborting. " + e);
            return null;
        }

        if (this.messageIds.isEmpty() || this.messages.isEmpty() || this.messageIds.size() != this.messages.size()) {
            RudderLogger.logError("DeviceModeTransformationManager: createDeviceTransformPayload: Error while creating transformation payload. Aborting.");
            return null;
        }

        JsonArray batchArray = new JsonArray();

        for (Map.Entry<Integer, RudderTransformationRequest> entry : messageMap.entrySet()) {
            Integer orderNo = entry.getKey();
            RudderTransformationRequest transformationRequest = entry.getValue();
            JsonElement event = gson.toJsonTree(transformationRequest.getMessage());
            JsonElement destinationIds = gson.toJsonTree(transformationRequest.getDestinationIds());

            JsonObject batchItem = new JsonObject();
            batchItem.addProperty("orderNo", orderNo);
            batchItem.add("event", event);

            batchItem.add("destinationIds", destinationIds);

            batchArray.add(batchItem);
        }

        JsonObject json = new JsonObject();
        json.add("batch", batchArray);

        return gson.toJson(json);
    }
}