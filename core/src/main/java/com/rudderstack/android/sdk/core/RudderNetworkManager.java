package com.rudderstack.android.sdk.core;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private String authHeaderString;
    private String anonymousIdHeaderString;

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
        this.authHeaderString = authHeaderString;
        this.anonymousIdHeaderString = anonymousIdHeaderString;
    }

    void updateAnonymousIdHeaderString() {
        try {
            this.anonymousIdHeaderString = Base64.encodeToString(RudderContext.getAnonymousId().getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    /**
     * @param requestPayload the payload which needs to be sent along in case of Post request and can be sent as null in case of Get Request
     * @param requestURL     the url to which the request should be sent
     * @param requestMethod  the http method which the request should be sent
     * @return
     */
    Result sendNetworkRequest(@Nullable String requestPayload, @NonNull String requestURL, @NonNull RequestMethod requestMethod) {
        if (requestPayload == null)
            return new Result(NetworkResponses.ERROR, -1, "Payload is Null", null);

        if (TextUtils.isEmpty(authHeaderString)) {
            RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: WriteKey was in-correct, hence aborting the request to %s", requestURL));
            return new Result(NetworkResponses.ERROR, -1, "Write Key is Invalid", null);
        }

        try {
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
                OutputStream os = httpConnection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                osw.write(requestPayload);
                osw.flush();
                osw.close();
                os.close();
            }
            // Todo; check if the request is sent only after making the connect call
            synchronized (MessageUploadLock.REQUEST_LOCK) {
                httpConnection.connect();
            }

            String responsePayload = null;
            String errorPayload = null;
            NetworkResponses networkResponse = null;

            int responseCode = httpConnection.getResponseCode();
            if (responseCode == 200) {
                try {
                    responsePayload = getResponse(httpConnection.getInputStream());
                    RudderLogger.logInfo(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request to endpoint %s was successful with status code %d and response is %s", requestURL, responseCode, responsePayload));
                    networkResponse = NetworkResponses.SUCCESS;
                } catch (IOException e) {
                    RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Failed to parse the response from endpoint %s due to %s", requestURL, e.getLocalizedMessage()));
                }
            } else {
                try {
                    errorPayload = getResponse(httpConnection.getErrorStream());
                    RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request to endpoint %s failed with status code %d and error %s", requestURL, responseCode, errorPayload));
                    if (errorPayload.toLowerCase().contains("invalid write key"))
                        networkResponse = NetworkResponses.WRITE_KEY_ERROR;
                    if (responseCode == 404)
                        networkResponse = NetworkResponses.RESOURCE_NOT_FOUND;

                } catch (IOException e) {
                    RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Failed to parse the response from endpoint %s due to %s", requestURL, e.getLocalizedMessage()));
                }
            }
            return new Result(networkResponse == null ? NetworkResponses.ERROR : networkResponse, responseCode, responsePayload, errorPayload);
        } catch (Exception ex) {
            RudderLogger.logError(ex);
            return new Result(NetworkResponses.ERROR, -1, null, ex.getLocalizedMessage());
        }
    }

    String getResponse(InputStream stream) throws IOException {
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
        int statusCode;
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
