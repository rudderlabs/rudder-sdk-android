package com.rudderstack.android.sdk.core;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class DeviceModeUtils {

    private static final String TRANSFORMATION_ENDPOINT = "v1/transform";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();


    static Result transform(DBPersistentManager dbManager, int count, String dataPlaneUrl,
                            String authHeaderString, String anonymousIdHeaderString) {

        final ArrayList<Integer> messageIds = new ArrayList<>();
        final ArrayList<String> messages = new ArrayList<>();
        synchronized (MessageUploadLock.DEVICE_TRANSFORMATION_LOCK) {
            dbManager.fetchDeviceModeEventsFromDb(messageIds, messages, count);
        }
        String requestJson = createDeviceTransformPayload(dbManager, messageIds, messages);

        return transformDataThroughServer(requestJson, dataPlaneUrl, authHeaderString, anonymousIdHeaderString);
    }

    @VisibleForTesting
    static Result transformDataThroughServer(String requestPayload, String dataPlaneUrl, String authHeaderString, String anonymousIdHeaderString) {
        if (requestPayload == null)
            return new Result(Utils.NetworkResponses.ERROR, "Payload is Null", null);

        try {
            if (TextUtils.isEmpty(authHeaderString)) {
                RudderLogger.logError("EventRepository: flushEventsToServer: WriteKey was not correct. Aborting flush to server");
                return null;
            }

            // get endPointUrl form config object
            String dataPlaneEndPoint = dataPlaneUrl + TRANSFORMATION_ENDPOINT;
            RudderLogger.logDebug("TransformationUtils: transformDataThroughServer: dataPlaneEndPoint: " + dataPlaneEndPoint);

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
            synchronized (MessageUploadLock.DEVICE_TRANSFORMATION_LOCK) {
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(requestPayload);
                osw.flush();
                osw.close();
                os.close();
                // create connection
                httpConnection.connect();
            }
            // get input stream from connection to get output from the server
            if (httpConnection.getResponseCode() == 200) {

                BufferedInputStream bis = new BufferedInputStream(httpConnection.getInputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int res = -1;
                while ((res = bis.read()) != -1) {
                    baos.write(res);
                }

                return new Result(Utils.NetworkResponses.SUCCESS, null, gson.<List<TransformationResponse>>fromJson(baos.toString(),
                        new TypeToken<List<TransformationResponse>>() {
                        }.getType()));
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

                    return new Result(Utils.NetworkResponses.WRITE_KEY_ERROR, errorResp, null);
                }
                return new Result(Utils.NetworkResponses.ERROR, errorResp, null);

            }
        } catch (Exception ex) {
            RudderLogger.logError(ex);
            return new Result(Utils.NetworkResponses.ERROR, ex.getLocalizedMessage(), null);

        }

    }

    private static final String TRANSFORMATION_PAYLOAD_ORDER_NO_IDENTIFIER = "orderNo";
    private static final String TRANSFORMATION_PAYLOAD_EVENT_IDENTIFIER = "event";
    private static final String TRANSFORMATION_PAYLOAD_TRANSFORMATION_IDENTIFIER = "transformationIds";

    //shouldn't be called on main thread
    private static String createDeviceTransformPayload(DBPersistentManager dbPersistentManager,
                                                       List<Integer> rowIds, List<String> messages) {
        if (rowIds.size() != messages.size()) return null;

        Map<Integer, List<String>> eventRowTransformationIdMap = dbPersistentManager.fetchTransformationIdsGroupByEventRowId(rowIds);
        List<Map<String, Object>> request = new ArrayList<>(rowIds.size());

        for (int i = 0; i < rowIds.size(); i++) {

            Map<String, Object> deviceTransformRequestPayload = new HashMap<>(3);
            int rowId = rowIds.get(i);
            String msg = messages.get(i);

            deviceTransformRequestPayload.put(TRANSFORMATION_PAYLOAD_ORDER_NO_IDENTIFIER, rowId);
            deviceTransformRequestPayload.put(TRANSFORMATION_PAYLOAD_EVENT_IDENTIFIER, gson.fromJson(msg, Map.class));

            if (eventRowTransformationIdMap != null) {
                List<String> transformationIds = eventRowTransformationIdMap.get(rowId);
                deviceTransformRequestPayload.put(TRANSFORMATION_PAYLOAD_TRANSFORMATION_IDENTIFIER, transformationIds);
            }
            request.add(deviceTransformRequestPayload);
        }
        return gson.toJson(request);
    }

    static class Result {
        final Utils.NetworkResponses status;
        @Nullable
        final String error;
        final List<TransformationResponse> response;

        public Result(Utils.NetworkResponses status, @Nullable String error, List<TransformationResponse> response) {
            this.status = status;
            this.error = error;
            this.response = response;
        }
    }
}

