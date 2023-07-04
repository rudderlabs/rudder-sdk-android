package com.rudderstack.android.sdk.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    void startDeviceModeTransformationProcessor() {
        deviceModeExecutor.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        long deviceModeEventsCount = dbManager.getDeviceModeRecordCount();
                        RudderLogger.logDebug("DeviceModeTransformationManager: DeviceModeTransformationProcessor: fetching device mode events to flush to transformation service");
                        if ((deviceModeSleepCount >= config.getSleepTimeOut() && deviceModeEventsCount > 0) || deviceModeEventsCount >= DMT_BATCH_SIZE) {
                            int retryCount = 0;
                            int delay;
                            do {
                                messages.clear();
                                messageIds.clear();
                                synchronized (MessageUploadLock.DEVICE_TRANSFORMATION_LOCK) {
                                    dbManager.fetchDeviceModeEventsFromDb(messageIds, messages, DMT_BATCH_SIZE);
                                }
                                ArrayList<String> transformationEnabledDestinationIds = getTransformationEnabledDestinationIds(messages);
                                String requestJson = createDeviceTransformPayload(messageIds, messages, transformationEnabledDestinationIds);
                                RudderLogger.logDebug(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: Payload: %s", requestJson));
                                RudderLogger.logInfo(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: EventCount: %d", messageIds.size()));
                                Result result = rudderNetworkManager.sendNetworkRequest(requestJson, addEndPoint(dataResidencyManager.getDataPlaneUrl(), TRANSFORMATION_ENDPOINT), RequestMethod.POST, false, true);
                                if (result.status == NetworkResponses.WRITE_KEY_ERROR) {
                                    RudderLogger.logDebug("DeviceModeTransformationManager: TransformationProcessor: Wrong WriteKey. Aborting");
                                    break;
                                } else if (result.status == NetworkResponses.NETWORK_UNAVAILABLE) {
                                    RudderLogger.logDebug("DeviceModeTransformationManager: TransformationProcessor: Network unavailable. Aborting");
                                    break;
                                } else if (result.status == NetworkResponses.ERROR) {
                                    delay = Math.min((1 << retryCount) * 500, MAX_DELAY); // Exponential backoff
                                    if (retryCount++ == MAX_RETRIES) {
                                        deviceModeSleepCount = 0;
                                        rudderDeviceModeManager.dumpOriginalEvents(gson.fromJson(requestJson, TransformationRequest.class), true);
                                        completeDeviceModeEventProcessing();
                                        break;
                                    }
                                    RudderLogger.logDebug("DeviceModeTransformationManager: TransformationProcessor: Retrying in " + delay + "s");
                                    try {
                                        Thread.sleep(delay);
                                    } catch (Exception e) {
                                        RudderLogger.logError(e);
                                        Thread.currentThread().interrupt();
                                    }
                                } else if (result.status == NetworkResponses.RESOURCE_NOT_FOUND) { // dumping back the original messages itself to the factories as transformation feature is not enabled
                                    deviceModeSleepCount = 0;
                                    rudderDeviceModeManager.dumpOriginalEvents(gson.fromJson(requestJson, TransformationRequest.class), false);
                                    completeDeviceModeEventProcessing();
                                } else {
                                    deviceModeSleepCount = 0;
                                    try {
                                        TransformationResponse transformationResponse = gson.fromJson(result.response, TransformationResponse.class);
                                        rudderDeviceModeManager.dumpTransformedEvents(transformationResponse);
                                    } catch (Exception e) {
                                        RudderLogger.logError("DeviceModeTransformationManager: TransformationProcessor: Error encountered during transformed response conversion to TransformationResponse format: " + e);
                                    }
                                    completeDeviceModeEventProcessing();
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

    private void completeDeviceModeEventProcessing() {
        RudderLogger.logDebug(String.format(Locale.US, "DeviceModeTransformationManager: TransformationProcessor: Updating status as DEVICE_MODE_PROCESSING DONE for events %s", messageIds));
        dbManager.markDeviceModeDone(messageIds);
        dbManager.runGcForEvents();
    }

    RudderMessage getEventFromMessageId(int messageId) {
        String message = messages.get(messageIds.indexOf(messageId));
        return gson.fromJson(message, RudderMessage.class);
    }

    // For each message get the list of destinationIds for which transformation is enabled
    private ArrayList<String> getTransformationEnabledDestinationIds(List<String> messages) {
        ArrayList<List<String>> destinationIds = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            RudderMessage message = gson.fromJson(messages.get(i), RudderMessage.class);
            destinationIds.add(this.rudderDeviceModeManager.getTransformationEnabledDestinationIds(message));
        }
        return convertIntoString(destinationIds);
    }

    private ArrayList<String> convertIntoString(List<List<String>> destinationIds) {
        ArrayList<String> destinationIdsString = new ArrayList<>();
        for (int i = 0; i < destinationIds.size(); i++) {
            StringBuilder destinationId = new StringBuilder();
            destinationId.append("[");
            for (int j = 0; j < destinationIds.get(i).size(); j++) {
                destinationId.append("\"").append(destinationIds.get(i).get(j)).append("\"");
                if (j != destinationIds.get(i).size() - 1) {
                    destinationId.append(",");
                }
            }
            destinationId.append("]");
            destinationIdsString.add(destinationId.toString());
        }
        return destinationIdsString;
    }

    private static String createDeviceTransformPayload(List<Integer> rowIds, List<String> messages, ArrayList<String> transformationEnabledDestinationIds) {
        if (rowIds.isEmpty() || messages.isEmpty() || rowIds.size() != messages.size())
            return null;
        StringBuilder jsonPayload = new StringBuilder();
        jsonPayload.append("{");
        jsonPayload.append("\"batch\" :");
        jsonPayload.append("[");
        int totalBatchSize = Utils.getUTF8Length(jsonPayload) + 2;
        try {
            for (int i = 0; i < rowIds.size(); i++) {
                StringBuilder message = new StringBuilder();
                message.append("{");
                message.append("\"orderNo\":").append(rowIds.get(i)).append(",");
                message.append("\"destinationIds\":").append(transformationEnabledDestinationIds.get(i)).append(",");
                message.append("\"event\":").append(messages.get(i));
                message.append("}");
                message.append(",");
                totalBatchSize += Utils.getUTF8Length(message);
                if (totalBatchSize >= Utils.MAX_BATCH_SIZE) {
                    RudderLogger.logDebug(String.format(Locale.US, "DeviceModeTransformationManager: createDeviceTransformPayload: MAX_BATCH_SIZE reached at index: %d | Total: %d", i, totalBatchSize));
                    break;
                }
                jsonPayload.append(message);
            }
        } catch (Exception e) {
            RudderLogger.logError(e);
        }
        if (jsonPayload.charAt(jsonPayload.length() - 1) == ',') {
            jsonPayload.deleteCharAt(jsonPayload.length() - 1);
        }
        jsonPayload.append("]");
        jsonPayload.append("}");
        return jsonPayload.toString();
    }
}