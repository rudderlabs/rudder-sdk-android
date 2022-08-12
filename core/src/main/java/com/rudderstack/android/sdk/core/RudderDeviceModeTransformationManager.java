package com.rudderstack.android.sdk.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final RudderConfig config;
    // batch size for device mode transformation
    private static final int DMT_BATCH_SIZE = 12;
    //scheduled executor for device mode transformation
    private final ScheduledExecutorService deviceModeExecutor = Executors.newScheduledThreadPool(2);


    final ArrayList<Integer> messageIds = new ArrayList<>();
    final ArrayList<String> messages = new ArrayList<>();

    private static final String TRANSFORMATION_ENDPOINT = "transform";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();

    RudderDeviceModeTransformationManager(DBPersistentManager dbManager, RudderNetworkManager rudderNetworkManager, RudderDeviceModeManager rudderDeviceModeManager, RudderConfig config) {
        this.dbManager = dbManager;
        this.rudderNetworkManager = rudderNetworkManager;
        this.rudderDeviceModeManager = rudderDeviceModeManager;
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
                            do {
                                messages.clear();
                                messageIds.clear();
                                synchronized (MessageUploadLock.DEVICE_TRANSFORMATION_LOCK) {
                                    dbManager.fetchDeviceModeEventsFromDb(messageIds, messages, DMT_BATCH_SIZE);
                                }
                                String requestJson = createDeviceTransformPayload(messageIds, messages);
                                Result result = rudderNetworkManager.sendNetworkRequest(requestJson, addEndPoint(config.getDataPlaneUrl(), TRANSFORMATION_ENDPOINT), RequestMethod.POST);
                                if (result.status == NetworkResponses.WRITE_KEY_ERROR) {
                                    RudderLogger.logInfo("DeviceModeTransformationManager: DeviceModeTransformationProcessor: Wrong WriteKey. Aborting");
                                    break;
                                } else if (result.status == NetworkResponses.ERROR) {
                                    RudderLogger.logInfo("DeviceModeTransformationManager: DeviceModeTransformationProcessor: Retrying in " + Math.abs(deviceModeSleepCount - config.getSleepTimeOut()) + "s");
                                    try {
                                        Thread.sleep(Math.abs(deviceModeSleepCount - config.getSleepTimeOut()) * 1000L);
                                    } catch (Exception e) {
                                        RudderLogger.logError(e);
                                    }
                                } else if (result.status == NetworkResponses.RESOURCE_NOT_FOUND) {
                                    // Todo; We should use or dump the original messages itself to the factories.
                                } else {
                                    deviceModeSleepCount = 0;
                                    processTransformationResponse(gson.fromJson(result.response, TransformationResponse.class));
                                    dbManager.markDeviceModeDone(messageIds);
                                    dbManager.runGcForEvents();
                                }
                            } while (dbManager.getDeviceModeRecordCount() > 0);
                        }
                        deviceModeSleepCount++;
                    }
                }
                , 1, 1, TimeUnit.SECONDS);
    }

    private static String createDeviceTransformPayload(List<Integer> rowIds, List<String> messages) {
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
            e.printStackTrace();
        }
        if (jsonPayload.charAt(jsonPayload.length() - 1) == ',') {
            jsonPayload.deleteCharAt(jsonPayload.length() - 1);
        }
        jsonPayload.append("]");
        jsonPayload.append("}");
        return jsonPayload.toString();
    }

    private void processTransformationResponse(TransformationResponse transformationResponse) {
        if (transformationResponse.transformedBatch != null) {
            for (TransformationResponse.TransformedDestination transformedDestination : transformationResponse.transformedBatch) {
                processTransformedDestination(transformedDestination);
            }
        }
    }

    private void processTransformedDestination(TransformationResponse.TransformedDestination transformedDestination) {
        RudderIntegration<?> rudderIntegration = rudderDeviceModeManager.getRudderIntegrationObject(transformedDestination.id);
        if (rudderIntegration != null && transformedDestination.payload != null) {
            List<TransformationResponse.TransformedEvent> transformedEvents = transformedDestination.payload;
            sortTransformedEventBasedOnOrderNo(transformedEvents);
            for (TransformationResponse.TransformedEvent transformedEvent : transformedEvents) {
                if (transformedEvent.status.equals("200")) {
                    rudderIntegration.dump(transformedEvent.event);
                }
            }
        }
    }

    public static void sortTransformedEventBasedOnOrderNo
            (List<TransformationResponse.TransformedEvent> transformedEvents) {
        Collections.sort(transformedEvents, new Comparator<TransformationResponse.TransformedEvent>() {
            @Override
            public int compare(TransformationResponse.TransformedEvent o1, TransformationResponse.TransformedEvent o2) {
                return o1.orderNo - o2.orderNo;
            }
        });
    }
}