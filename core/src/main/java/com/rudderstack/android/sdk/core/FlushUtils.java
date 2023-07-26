package com.rudderstack.android.sdk.core;

import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.Utils;

import static com.rudderstack.android.sdk.core.ReportManager.LABEL_TYPE;
import static com.rudderstack.android.sdk.core.ReportManager.incrementCloudModeEventCounter;
import static com.rudderstack.android.sdk.core.ReportManager.incrementDiscardedCounter;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.NetworkResponses;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.RequestMethod;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.addEndPoint;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.Result;
import static com.rudderstack.android.sdk.core.util.Utils.getBatch;
import static com.rudderstack.android.sdk.core.util.Utils.getNumberOfBatches;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Standard flush related calls
 */
class FlushUtils {
    private FlushUtils() {

    }

    private static final Object FLUSH_LOCK = new Object();

    /**
     * Should not be called from main thread.
     *
     * @param flushQueueSize queue size defined by the user on which the batch is flushed to the data plane.
     * @param dataPlaneUrl   dataPlaneUrl to which the events are flushed.
     * @param dbManager      Instance of the dbPersistentManager which is used to do all the operations with DB.
     * @return boolean indicating if the flush operation is successful or not.
     */
    static boolean flushToServer(int flushQueueSize, String dataPlaneUrl, DBPersistentManager dbManager, RudderNetworkManager networkManager) {
        Result networkResponse;
        final ArrayList<Integer> messageIds = new ArrayList<>();
        final ArrayList<String> messages = new ArrayList<>();
        RudderLogger.logDebug("FlushUtils: flush: Fetching events to flush to server");
        synchronized (MessageUploadLock.UPLOAD_LOCK) {
            dbManager.fetchAllCloudModeEventsFromDB(messageIds, messages);
            int numberOfBatches = getNumberOfBatches(messages.size(), flushQueueSize);
            RudderLogger.logDebug(String.format(Locale.US, "FlushUtils: flush: %d batches of events to be flushed", numberOfBatches));
            boolean lastBatchFailed;
            String lastErrorMessage = "";
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
                        networkResponse = networkManager.sendNetworkRequest(payload, addEndPoint(dataPlaneUrl, RudderCloudModeManager.BATCH_ENDPOINT), RequestMethod.POST, true);
                        RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: ServerResponse: %d", networkResponse.statusCode));
                        // if success received from server
                        if (networkResponse.status == NetworkResponses.SUCCESS) {
                            ReportManager.incrementCloudModeUploadSuccessCounter(batchMessageIds.size());
                            // remove events from DB
                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: flush: Successfully sent batch %d/%d ", i, numberOfBatches));
                            RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: clearingEvents of batch %d from DB: %s", i, networkResponse));
                            dbManager.markCloudModeDone(batchMessageIds);
                            messageIds.removeAll(batchMessageIds);
                            messages.removeAll(batchMessages);
                            lastBatchFailed = false;
                            break;
                        }
                        ReportManager.incrementCloudModeUploadRetryCounter(1);
                        lastErrorMessage = getLastErrorMessage(networkResponse);

                    }else {
                        lastErrorMessage = ReportManager.LABEL_TYPE_PAYLOAD_NULL;
                    }
                    RudderLogger.logWarn(String.format(Locale.US, "EventRepository: flush: Failed to send batch %d/%d retrying again, %d retries left", i, numberOfBatches, retries));
                }
                if (lastBatchFailed) {
                    ReportManager.incrementCloudModeUploadAbortCounter(1, Collections.singletonMap(LABEL_TYPE, lastErrorMessage));
                    RudderLogger.logWarn(String.format(Locale.US, "EventRepository: flush: Failed to send batch %d/%d after 3 retries , dropping the remaining batches as well", i, numberOfBatches));
                    return false;
                }
            }
            reportBatchesAndMessages(numberOfBatches, messages.size());
            return true;
        }
    }

    private static String getLastErrorMessage(Result networkResponse) {
        String lastErrorMessage;
        switch(networkResponse.error){
            case "Request Timed Out": lastErrorMessage = ReportManager.LABEL_TYPE_REQUEST_TIMEOUT;
            break;
            case "Invalid Url": lastErrorMessage = ReportManager.LABEL_TYPE_DATA_PLANE_URL_INVALID;
            break;
            default: lastErrorMessage = networkResponse.error;
        }
        if(networkResponse.status == NetworkResponses.RESOURCE_NOT_FOUND){
            lastErrorMessage = ReportManager.LABEL_TYPE_DATA_PLANE_URL_INVALID;
        }
        return lastErrorMessage;
    }

    private static void reportBatchesAndMessages(int numberOfBatches, int messagesSize) {
        incrementCloudModeEventCounter(numberOfBatches, Collections.singletonMap(ReportManager.LABEL_TYPE, ReportManager.LABEL_FLUSH_NUMBER_OF_QUEUES));
        incrementCloudModeEventCounter(messagesSize, Collections.singletonMap(ReportManager.LABEL_TYPE, ReportManager.LABEL_FLUSH_NUMBER_OF_MESSAGES));
    }


    /*
     * flush events payload to server and return response as String
     * */
    static NetworkResponses flushEventsToServer(String payload, String dataPlaneUrl, String authHeaderString, String anonymousIdHeaderString) {
        try {
            if (TextUtils.isEmpty(authHeaderString)) {
                RudderLogger.logError("EventRepository: flushEventsToServer: WriteKey was not correct. Aborting flush to server");
                return null;
            }

            // get endPointUrl form config object
            String dataPlaneEndPoint = dataPlaneUrl + "v1/batch";
            RudderLogger.logDebug("EventRepository: flushEventsToServer: dataPlaneEndPoint: " + dataPlaneEndPoint);

            // create url object
            URL url = new URL(dataPlaneEndPoint);
            // get connection object
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            // set connection object to return output
            httpConnection.setDoOutput(true);
            //  set content type for network request
            httpConnection.setRequestProperty("Content-Type", "application/json");
            // set authorization header
            httpConnection.setRequestProperty("Authorization", String.format(Locale.US, "Basic %s", authHeaderString));
            // set anonymousId header for definitive routing
            httpConnection.setRequestProperty("AnonymousId", anonymousIdHeaderString);
            // set request method
            httpConnection.setRequestMethod("POST");
            // get output stream and write payload content
            OutputStream os = httpConnection.getOutputStream();
            //locks to prevent concurrent server access.
            synchronized (FLUSH_LOCK) {
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(payload);
                osw.flush();
                osw.close();
                os.close();
                // create connection
                httpConnection.connect();
            }
            // get input stream from connection to get output from the server
            if (httpConnection.getResponseCode() == 200) {
                return NetworkResponses.SUCCESS;
            } else {
                BufferedInputStream bis = new BufferedInputStream(httpConnection.getErrorStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int res = bis.read();
                // read response from the server
                while (res != -1) {
                    baos.write((byte) res);
                    res = bis.read();
                }
                // finally return response when reading from server is completed
                String errorResp = baos.toString();
                RudderLogger.logError("EventRepository: flushEventsToServer: ServerError: " + errorResp);
                // return null as request made is not successful
                if (errorResp.toLowerCase().contains("invalid write key")) {
                    return NetworkResponses.WRITE_KEY_ERROR;
                }
            }
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
        return NetworkResponses.ERROR;
    }

    /*
     * create payload string from messages list
     * - we created payload from individual message json strings to reduce the complexity
     * of deserialization and forming the payload object and creating the json string
     * again from the object
     * */
    static String getPayloadFromMessages(List<Integer> messageIds, List<String> messages) {
        try {
            RudderLogger.logDebug("EventRepository: getPayloadFromMessages: recordCount: " + messages.size());
            String sentAtTimestamp = Utils.getTimeStamp();
            RudderLogger.logDebug("EventRepository: getPayloadFromMessages: sentAtTimestamp: " + sentAtTimestamp);
            // initialize ArrayLists to store current batch
            ArrayList<Integer> batchMessageIds = new ArrayList<>();
            // get string builder
            StringBuilder builder = new StringBuilder();
            // append initial json token
            builder.append("{");
            // append sent_at time stamp
            builder.append("\"sentAt\":\"").append(sentAtTimestamp).append("\",");
            // initiate batch array in the json
            builder.append("\"batch\": [");
            int totalBatchSize = Utils.getUTF8Length(builder) + 2; // we add 2 characters at the end
            int messageSize;
            // loop through messages list and add in the builder
            for (int index = 0; index < messages.size(); index++) {
                String message = messages.get(index);
                // strip last ending object character
                message = message.substring(0, message.length() - 1);
                // add sentAt time stamp
                message = String.format("%s,\"sentAt\":\"%s\"},", message, sentAtTimestamp);
                // add message size to batch size
                messageSize = Utils.getUTF8Length(message);
                totalBatchSize += messageSize;
                // check batch size
                if (totalBatchSize >= Utils.MAX_BATCH_SIZE) {
                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: getPayloadFromMessages: MAX_BATCH_SIZE reached at index: %d | Total: %d", index, totalBatchSize));
                    incrementDiscardedCounter(1, Collections.singletonMap(LABEL_TYPE, ReportManager.LABEL_TYPE_BATCH_SIZE_INVALID));
                    break;
                }
                // finally add message string to builder
                builder.append(message);
                // add message to batch ArrayLists
                batchMessageIds.add(messageIds.get(index));
            }
            if (builder.charAt(builder.length() - 1) == ',') {
                // remove trailing ','
                builder.deleteCharAt(builder.length() - 1);
            }
            // close batch array in the json
            builder.append("]");
            // append closing token in the json
            builder.append("}");
            // retain all events belonging to the batch
            messageIds.retainAll(batchMessageIds);
            // finally return the entire payload
            return builder.toString();
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
        return null;
    }
}
