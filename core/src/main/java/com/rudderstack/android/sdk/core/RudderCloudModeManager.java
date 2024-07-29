package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.ReportManager.incrementCloudModeUploadRetryCounter;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.NetworkResponses;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.RequestMethod;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.Result;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.addEndPoint;

import com.rudderstack.android.sdk.core.gson.RudderGson;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RudderCloudModeManager {

    private final DBPersistentManager dbManager;
    private final RudderNetworkManager networkManager;
    private final RudderDataResidencyManager dataResidencyManager;
    private final RudderConfig config;
    static final String BATCH_ENDPOINT = "v1/batch";

    RudderCloudModeManager(DBPersistentManager dbManager, RudderNetworkManager networkManager, RudderConfig config, RudderDataResidencyManager dataResidencyManager) {
        this.dbManager = dbManager;
        this.networkManager = networkManager;
        this.dataResidencyManager = dataResidencyManager;
        this.config = config;
    }

    void startCloudModeProcessor() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                long upTimeInMillis = Utils.getUpTimeInMillis();
                Result result = null;
                final ArrayList<Integer> messageIds = new ArrayList<>();
                final ArrayList<String> messages = new ArrayList<>();
                while (true) {
                    // clear lists for reuse
                    messageIds.clear();
                    messages.clear();
                    result = null;
                    maintainDBThreshold();
                    long sleepCount = Utils.getSleepDurationInSecond(upTimeInMillis, Utils.getUpTimeInMillis());
                    RudderLogger.logDebug("CloudModeManager: cloudModeProcessor: Fetching events to flush to server");
                    synchronized (MessageUploadLock.UPLOAD_LOCK) {
                        dbManager.fetchCloudModeEventsFromDB(messageIds, messages, config.getFlushQueueSize());
                        if (messages.size() >= config.getFlushQueueSize() || (!messages.isEmpty() && sleepCount >= config.getSleepTimeOut())) {
                            // form payload JSON form the list of messages
                            String payload = FlushUtils.getPayloadFromMessages(messageIds, messages);
                            RudderLogger.logDebug(String.format(Locale.US, "CloudModeManager: cloudModeProcessor: payload: %s", payload));
                            RudderLogger.logInfo(String.format(Locale.US, "CloudModeManager: cloudModeProcessor: %d", messageIds.size()));
                            if (payload != null) {
                                result = networkManager.sendNetworkRequest(payload, addEndPoint(dataResidencyManager.getDataPlaneUrl(), BATCH_ENDPOINT), RequestMethod.POST, true);
                                RudderLogger.logInfo(String.format(Locale.US, "CloudModeManager: cloudModeProcessor: ServerResponse: %d", result.statusCode));
                                if (result.status == NetworkResponses.SUCCESS) {
                                    ReportManager.incrementCloudModeUploadSuccessCounter(messageIds.size());
                                    dbManager.markCloudModeDone(messageIds);
                                    dbManager.runGcForEvents();
                                    upTimeInMillis = Utils.getUpTimeInMillis();
                                    sleepCount = Utils.getSleepDurationInSecond(upTimeInMillis, Utils.getUpTimeInMillis());
                                } else {
                                    incrementCloudModeUploadRetryCounter(1);
                                }
                            }
                        }
                    }
                    RudderLogger.logDebug(String.format(Locale.US, "CloudModeManager: cloudModeProcessor: SleepCount: %d", sleepCount));
                    try {
                        if (result == null) {
                            RudderLogger.logDebug("CloudModeManager: cloudModeProcessor: Sleeping for next: " + config.getEventDispatchSleepInterval() + "ms");
                            Thread.sleep(config.getEventDispatchSleepInterval());
                            continue;
                        }
                        switch (result.status) {
                            case WRITE_KEY_ERROR:
                                RudderLogger.logError("CloudModeManager: cloudModeProcessor: Wrong WriteKey. Terminating the Cloud Mode Processor");
                                return;
                            case MISSING_ANONYMOUSID_AND_USERID:
                                RudderLogger.logError("CloudModeManager: cloudModeProcessor: Request Failed as the batch payload contains events without anonymousId and userId, hence deleting those events from DB");
                                deleteEventsWithoutAnonymousId(messages, messageIds);
                                break;
                            case ERROR:
                            case NETWORK_UNAVAILABLE:
                                RudderLogger.logWarn("CloudModeManager: cloudModeProcessor: Retrying in " + Math.abs(sleepCount - config.getSleepTimeOut()) + "s");
                                Thread.sleep(Math.abs(sleepCount - config.getSleepTimeOut()) * 1000L);
                                break;
                            default:
                                RudderLogger.logWarn("CloudModeManager: cloudModeProcessor: Retrying in 1s");
                                Thread.sleep(1000);
                        }
                    } catch (Exception ex) {
                        ReportManager.reportError(ex);
                        RudderLogger.logError(String.format("CloudModeManager: cloudModeProcessor: Exception while trying to send events to Data plane URL %s due to %s", config.getDataPlaneUrl(), ex.getLocalizedMessage()));
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }.start();
    }

    private void deleteEventsWithoutAnonymousId(ArrayList<String> messages, ArrayList<Integer> messageIds) {
        List<Integer> eventsToDelete = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            Map<String, Object> message = RudderGson.deserialize(messages.get(i), Map.class);
            if (message != null && (!message.containsKey("anonymousId") || message.get("anonymousId") == null)) {
                eventsToDelete.add(messageIds.get(i));
            }
        }
        if (!eventsToDelete.isEmpty()) {
            dbManager.clearEventsFromDB(eventsToDelete);
            RudderLogger.logDebug(String.format(Locale.US, "CloudModeManager: deleteEventsWithoutUserIdAndAnonymousId: Deleted %d events from DB", eventsToDelete.size()));
        }
    }

    /*
     * check if the number of events in the db crossed the dbCountThreshold then delete the older events which are in excess.
     */
    private void maintainDBThreshold() {
        int recordCount = 0;
        try {
            // get current record count from db
            recordCount = dbManager.getDBRecordCount();
        }
        // Added RuntimeException in order to catch CursorWindowAllocationException (this requires API level 33 and above).
        catch (RuntimeException ex) {
            RudderLogger.logError("CloudModeManager: maintainDBThreshold: Exception while fetching count from DB due to: " + Arrays.toString(ex.getStackTrace()));
            ReportManager.reportError(ex);
        }
        RudderLogger.logDebug(String.format(Locale.US, "CloudModeManager: getPayloadFromMessages: DBRecordCount: %d", recordCount));
        // if record count exceeds threshold count, remove older events
        if (recordCount > config.getDbCountThreshold()) {
            // fetch extra old events
            RudderLogger.logDebug(String.format(Locale.US, "CloudModeManager: getPayloadFromMessages: OldRecordCount: %d", (recordCount - config.getDbCountThreshold())));
            int toDelete = recordCount - config.getDbCountThreshold();
            dbManager.deleteFirstEvents(toDelete);
            ReportManager.incrementDiscardedCounter(toDelete, Collections.singletonMap(
                    ReportManager.LABEL_TYPE, ReportManager.LABEL_TYPE_OUT_OF_MEMORY
            ));
        }
    }
}
