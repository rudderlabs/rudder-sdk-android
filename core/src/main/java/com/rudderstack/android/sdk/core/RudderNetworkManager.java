package com.rudderstack.android.sdk.core;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class RudderNetworkManager {

    private static final String METADATA_KEY = "metadata";
    private final String authHeaderString;
    private String anonymousIdHeaderString;
    @Nullable
    private String dmtAuthorisationString;

    private final Gson gson = new GsonBuilder().create();

    private static final String DMT_AUTHORISATION_KEY = "Custom-Authorization";

    public enum NetworkResponses {
        SUCCESS,
        ERROR,
        WRITE_KEY_ERROR,
        RESOURCE_NOT_FOUND
    }

    public enum RequestMethod {
        POST,
        GET
    }

    public RudderNetworkManager(String authHeaderString, String anonymousIdHeaderString) {
        this(authHeaderString, anonymousIdHeaderString, null);
    }

    public RudderNetworkManager(String authHeaderString, String anonymousIdHeaderString, @Nullable String dmtHeaderString) {
        this.authHeaderString = authHeaderString;
        this.anonymousIdHeaderString = anonymousIdHeaderString;
        this.dmtAuthorisationString = dmtHeaderString;
    }

    void updateAnonymousIdHeaderString() {
        try {
            this.anonymousIdHeaderString = Base64.encodeToString(RudderContext.getAnonymousId().getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    void updateDMTCustomToken(@Nullable String dmtCustomToken) {
        if (dmtCustomToken == null) {
            this.dmtAuthorisationString = null;
            return;
        }
        try {
            this.dmtAuthorisationString = dmtCustomToken;
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    Result sendNetworkRequest(@Nullable String requestPayload, @NonNull String requestURL, @NonNull RequestMethod requestMethod) {
        return sendNetworkRequest(requestPayload, requestURL, requestMethod, false);
    }

    /**
     * @param requestPayload the payload which needs to be sent along in case of Post request and can be sent as null in case of Get Request
     * @param requestURL     the url to which the request should be sent
     * @param requestMethod  the http method which the request should be sent
     * @return sends back a Result Object including the response payload, error payload, statusCode.
     */
    Result sendNetworkRequest(@Nullable String requestPayload, @NonNull String requestURL, @NonNull RequestMethod requestMethod, boolean isDMTRequest) {
        if (requestMethod == RequestMethod.POST && requestPayload == null)
            return new Result(NetworkResponses.ERROR, -1, null, "Payload is Null");

        if (TextUtils.isEmpty(authHeaderString)) {
            RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: WriteKey was in-correct, hence aborting the request to %s", requestURL));
            return new Result(NetworkResponses.ERROR, -1, null, "Write Key is Invalid");
        }

        try {
            HttpURLConnection httpConnection = getHttpConnection(requestURL, requestMethod, requestPayload, isDMTRequest);
            synchronized (MessageUploadLock.REQUEST_LOCK) {
                httpConnection.connect();
            }

            return getResult(httpConnection);
        }  catch (Exception ex) {
            RudderLogger.logError(ex);
            return new Result(NetworkResponses.ERROR, -1, null, ex.getLocalizedMessage());
        }
    }

    private Result getResult(HttpURLConnection httpConnection) throws IOException {
        String responsePayload = null;
        String errorPayload = null;
        NetworkResponses networkResponse = null;

        int responseCode = httpConnection.getResponseCode();
        if (responseCode == 200) {
            responsePayload = getResponse(httpConnection.getInputStream());
            RudderLogger.logInfo(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request to endpoint %s was successful with status code %d and response is %s",
                    httpConnection.getURL(), responseCode, responsePayload));
            networkResponse = NetworkResponses.SUCCESS;

        } else {
            errorPayload = getResponse(httpConnection.getErrorStream());
            RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request to endpoint %s failed with status code %d and error %s",
                    httpConnection.getURL(), responseCode, errorPayload));
            if (errorPayload.toLowerCase().contains("invalid write key"))
                networkResponse = NetworkResponses.WRITE_KEY_ERROR;
            if (responseCode == 404)
                networkResponse = NetworkResponses.RESOURCE_NOT_FOUND;
        }
        return new Result(networkResponse == null ? NetworkResponses.ERROR : networkResponse, responseCode, responsePayload, errorPayload);
    }

    private HttpURLConnection getHttpConnection(String requestURL, RequestMethod requestMethod,
                                                String requestPayload, boolean isDMTRequest) throws IOException {
        RudderLogger.logDebug(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request URL: %s", requestURL));
        URL url = new URL(requestURL);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestProperty("Authorization", String.format(Locale.US, "Basic %s", authHeaderString));
        if (requestMethod == RequestMethod.GET) {
            httpConnection.setRequestMethod("GET");
        } else {
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("AnonymousId", anonymousIdHeaderString);
            requestPayload = withAddedMetadataToRequestPayload(requestPayload, isDMTRequest);
            OutputStream os = httpConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write(requestPayload);
            osw.flush();
            osw.close();
            os.close();
        }
        return httpConnection;
    }
    @VisibleForTesting
    String withAddedMetadataToRequestPayload(String requestPayload, boolean isDMTRequest) {
        if (requestPayload == null || !isDMTRequest || dmtAuthorisationString == null)
            return requestPayload;
        JsonObject jsonObject =  JsonParser.parseString(requestPayload).getAsJsonObject();
        JsonObject metadataJsonObject = new JsonObject();
        metadataJsonObject.addProperty(DMT_AUTHORISATION_KEY, dmtAuthorisationString);
        jsonObject.add(METADATA_KEY, metadataJsonObject);
        return jsonObject.toString();
    }

    private String getResponse(InputStream stream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int res;
        while ((res = bis.read()) != -1) {
            baos.write((byte) res);
        }
        return baos.toString();
    }

    static String addEndPoint(String url, String endPoint) {
        if (url.endsWith("/"))
            return url + endPoint;
        return url + "/" + endPoint;
    }

    static class Result {

        final NetworkResponses status;
        final int statusCode;
        @Nullable
        final String response;
        @Nullable
        final String error;

        public Result(RudderNetworkManager.NetworkResponses status, int statusCode, @Nullable String response, @Nullable String error) {
            this.status = status;
            this.statusCode = statusCode;
            this.response = response;
            this.error = error;
        }
    }
}
