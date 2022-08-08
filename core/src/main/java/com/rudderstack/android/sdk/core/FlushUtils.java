package com.rudderstack.android.sdk.core;

import com.rudderstack.android.sdk.core.util.MessageUploadLock;

import static com.rudderstack.android.sdk.core.RudderCloudModeManager.getPayloadFromMessages;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.NetworkResponses;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.RequestMethod;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.addEndPoint;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.Result;
import static com.rudderstack.android.sdk.core.util.Utils.getBatch;
import static com.rudderstack.android.sdk.core.util.Utils.getNumberOfBatches;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Standard flush related calls
 */
class FlushUtils {
    private FlushUtils() {

    }

    /**
     * Should not be called from main thread.
     *
     * @param flushQueueSize queue size defined by the user on which the batch is flushed to the data plane.
     * @param dataPlaneUrl   dataPlaneUrl to which the events are flushed.
     * @param dbManager      Instance of the dbPersistentManager which is used to do all the operations with DB.
     * @return boolean indicating if the flush operation is successful or not.
     */
    static boolean flush(int flushQueueSize, String dataPlaneUrl, DBPersistentManager dbManager, RudderNetworkManager networkManager) {
        Result networkResponse;
        synchronized (MessageUploadLock.UPLOAD_LOCK) {
            final ArrayList<Integer> messageIds = new ArrayList<>();
            final ArrayList<String> messages = new ArrayList<>();
            RudderLogger.logDebug("FlushUtils: flush: Fetching events to flush to server");
            dbManager.fetchAllCloudModeEventsFromDB(messageIds, messages);
            int numberOfBatches = getNumberOfBatches(messages.size(), flushQueueSize);
            RudderLogger.logDebug(String.format(Locale.US, "FlushUtils: flush: %d batches of events to be flushed", numberOfBatches));
            boolean lastBatchFailed;
            for (int i = 1; i <= numberOfBatches; i++) {
                lastBatchFailed = true;
                int retries = 3;
                while (retries-- > 0) {
                    List<Integer> batchMessageIds = getBatch(messageIds, flushQueueSize);
                    List<String> batchMessages = getBatch(messages, flushQueueSize);
                    String payload = getPayloadFromMessages(batchMessageIds, batchMessages);
                    RudderLogger.logDebug(String.format(Locale.US, "FlushUtils: flush: payload: %s", payload));
                    RudderLogger.logInfo(String.format(Locale.US, "FlushUtils: flush: EventCount: %d", batchMessages.size()));
                    if (payload != null) {
                        // send payload to server if it is not null
                        networkResponse = networkManager.sendNetworkRequest(payload, addEndPoint(dataPlaneUrl, RudderCloudModeManager.BATCH_ENDPOINT), RequestMethod.POST);
                        RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: ServerResponse: %d", networkResponse.statusCode));
                        // if success received from server
                        if (networkResponse.status == NetworkResponses.SUCCESS) {
                            // remove events from DB
                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: flush: Successfully sent batch %d/%d ", i, numberOfBatches));
                            RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: clearingEvents of batch %d from DB: %s", i, networkResponse));
                            dbManager.markCloudModeDone(batchMessageIds);
                            messageIds.removeAll(batchMessageIds);
                            messages.removeAll(batchMessages);
                            lastBatchFailed = false;
                            break;
                        }
                    }
                    RudderLogger.logWarn(String.format(Locale.US, "EventRepository: flush: Failed to send batch %d/%d retrying again, %d retries left", i, numberOfBatches, retries));
                }
                if (lastBatchFailed) {
                    RudderLogger.logWarn(String.format(Locale.US, "EventRepository: flush: Failed to send batch %d/%d after 3 retries , dropping the remaining batches as well", i, numberOfBatches));
                    return false;
                }
            }
            return true;
        }
    }
}
