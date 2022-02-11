package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class FlushEventsWorker extends Worker {

    public FlushEventsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        RudderLogger.logDebug("FlushEventsWorker: doWork: Started Periodic Flushing of Events ");

        // initiate lists for messageIds and messages
        RudderFlushConfig flushConfig = RudderServerConfigManager.getRudderFlushConfig(getApplicationContext());
        ArrayList<Integer> messageIds = new ArrayList<>();
        ArrayList<String> messages = new ArrayList<>();
        RudderLogger.logDebug("EventRepository: flush: Fetching events to flush to server");
        DBPersistentManager dbManager = DBPersistentManager.getInstance((Application) getApplicationContext());
        dbManager.fetchAllEventsFromDB(messageIds, messages);
        int numberOfBatches = Utils.getNumberOfBatches(messages.size(), flushConfig.getFlushQueueSize());
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: flush: %d batches of events to be flushed", numberOfBatches));
        boolean lastBatchFailed = false;
        Utils.NetworkResponses networkResponse;
        for (int i = 1; i <= numberOfBatches && !lastBatchFailed; i++) {
            lastBatchFailed = true;
            int retries = 3;
            while (retries-- > 0) {
                ArrayList<Integer> batchMessageIds = Utils.getBatch(messageIds, flushConfig.getFlushQueueSize());
                ArrayList<String> batchMessages = Utils.getBatch(messages, flushConfig.getFlushQueueSize());
                String payload = EventRepository.getPayloadFromMessages(batchMessageIds, batchMessages);
                RudderLogger.logDebug(String.format(Locale.US, "EventRepository: flush: payload: %s", payload));
                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: EventCount: %d", batchMessages.size()));
                if (payload != null) {
                    // send payload to server if it is not null
                    networkResponse = EventRepository.flushEventsToServer(payload, flushConfig.getDataPlaneUrl(), flushConfig.getAuthHeaderString(), flushConfig.getAnonymousHeaderString());
                    RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: ServerResponse: %s", networkResponse));
                    // if success received from server
                    if (networkResponse == Utils.NetworkResponses.SUCCESS) {
                        // remove events from DB
                        RudderLogger.logDebug(String.format("EventRepository: flush: Successfully sent batch %d/%d ", i, numberOfBatches));
                        RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: clearingEvents of batch %d from DB: %s", i, networkResponse));
                        dbManager.clearEventsFromDB(batchMessageIds);
                        messageIds.removeAll(batchMessageIds);
                        messages.removeAll(batchMessages);
                        lastBatchFailed = false;
                        break;
                    }
                }
                RudderLogger.logDebug(String.format("EventRepository: flush: Failed to send batch %d/%d retrying again, %d retries left", i, numberOfBatches, retries));
            }
            if (lastBatchFailed) {
                RudderLogger.logDebug(String.format("EventRepository: flush: Failed to send batch %d/%d after 3 retries , dropping the remaining batches as well", i, numberOfBatches));
            }
        }


        return Result.success();
    }
}
