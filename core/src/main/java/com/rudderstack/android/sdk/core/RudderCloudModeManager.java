package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.ReportManager.LABEL_TYPE;
import static com.rudderstack.android.sdk.core.ReportManager.incrementCloudModeUploadRetryCounter;
import static com.rudderstack.android.sdk.core.ReportManager.incrementDiscardedCounter;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.NetworkResponses;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.RequestMethod;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.Result;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.addEndPoint;

import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
                int sleepCount = 0;
                Result result = null;
                final ArrayList<Integer> messageIds = new ArrayList<>();
                final ArrayList<String> messages = new ArrayList<>();
                while (true) {
                    // clear lists for reuse
                    messageIds.clear();
                    messages.clear();
                    result = null;
                    maintainDBThreshold();
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
                                    sleepCount = 0;
                                } else {
                                    incrementCloudModeUploadRetryCounter(1);
                                }
                            }
                        }
                    }
                    sleepCount += 1;
                    RudderLogger.logDebug(String.format(Locale.US, "CloudModeManager: cloudModeProcessor: SleepCount: %d", sleepCount));
                    try {
                        if (result == null) {
                            Thread.sleep(1000);
                            continue;
                        }
                        switch (result.status) {
                            case WRITE_KEY_ERROR:
                                RudderLogger.logError("CloudModeManager: cloudModeProcessor: Wrong WriteKey. Terminating the Cloud Mode Processor");
                                return;
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

    /*
     * check if the number of events in the db crossed the dbCountThreshold then delete the older events which are in excess.
     */
    private void maintainDBThreshold() {
        // get current record count from db
        int recordCount = dbManager.getDBRecordCount();
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
