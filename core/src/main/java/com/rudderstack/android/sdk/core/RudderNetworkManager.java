package com.rudderstack.android.sdk.core;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rudderstack.android.sdk.core.util.FunctionUtils;
import com.rudderstack.android.sdk.core.util.GzipUtils;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RudderNetworkManager {

    private static final String METADATA_KEY = "metadata";
    private final String authHeaderString;
    private final boolean isGzipConfigured;
    private String anonymousIdHeaderString;
    @Nullable
    private String dmtAuthorisationString;

    private static final String DMT_AUTHORISATION_KEY = "Custom-Authorization";

    public enum NetworkResponses {
        SUCCESS,
        ERROR,
        WRITE_KEY_ERROR,
        RESOURCE_NOT_FOUND,
        NETWORK_UNAVAILABLE,
        BAD_REQUEST
    }

    public enum RequestMethod {
        POST,
        GET
    }

    public RudderNetworkManager(String authHeaderString, String anonymousIdHeaderString, boolean isGzipConfigured) {
        this(authHeaderString, anonymousIdHeaderString, null, isGzipConfigured);
    }

    public RudderNetworkManager(String authHeaderString, String anonymousIdHeaderString,
                                @Nullable String dmtHeaderString, boolean isGzipConfigured) {
        this.authHeaderString = authHeaderString;
        this.anonymousIdHeaderString = anonymousIdHeaderString;
        this.dmtAuthorisationString = dmtHeaderString;
        this.isGzipConfigured = isGzipConfigured;
    }

    void updateAnonymousIdHeaderString() {
        try {
            this.anonymousIdHeaderString = Base64.encodeToString(RudderContext.getAnonymousId().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
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

    Result sendNetworkRequest(@Nullable String requestPayload, @NonNull String requestURL,
                              @NonNull RequestMethod requestMethod, boolean isGzipAvailableForApi) {
        return sendNetworkRequest(requestPayload, requestURL, requestMethod, isGzipAvailableForApi, false);
    }

    /**
     * @param requestPayload the payload which needs to be sent along in case of Post request and can be sent as null in case of Get Request
     * @param requestURL     the url to which the request should be sent
     * @param requestMethod  the http method which the request should be sent
     * @return sends back a Result Object including the response payload, error payload, statusCode.
     */
    Result sendNetworkRequest(@Nullable String requestPayload, @NonNull String requestURL,
                              @NonNull RequestMethod requestMethod, boolean isGzipAvailableForApi,
                              boolean isDMTRequest) {
        if (requestMethod == RequestMethod.POST && requestPayload == null)
            return new Result(NetworkResponses.ERROR, -1, null, "Payload is Null");

        if (TextUtils.isEmpty(authHeaderString)) {
            RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: WriteKey was in-correct, hence aborting the request to %s", requestURL));
            return new Result(NetworkResponses.ERROR, -1, null, "Write Key is Invalid");
        }

        try {
            HttpURLConnection httpConnection = updateHttpConnection(requestURL, requestMethod, requestPayload, isDMTRequest, isGzipAvailableForApi);
            if (httpConnection == null) {
                return new Result(NetworkResponses.NETWORK_UNAVAILABLE, -1, null, "Http Connection is Null");
            }
            synchronized (MessageUploadLock.REQUEST_LOCK) {
                httpConnection.connect();
            }
            return getResult(httpConnection);
        } catch (Exception ex) {
            RudderLogger.logError("RudderNetworkManager: sendNetworkRequest: Exception occurred while sending the request to " + requestURL + ex.getLocalizedMessage());
            return new Result(NetworkResponses.ERROR, -1, null, ex.getLocalizedMessage());
        }
    }

    private Result getResult(HttpURLConnection httpConnection) {
        String responsePayload = null;
        String errorPayload = null;
        NetworkResponses networkResponse = null;

        int responseCode = 0;
        try {
            responseCode = httpConnection.getResponseCode();
            if (responseCode == 200) {
                responsePayload = getResponse(httpConnection.getInputStream());
                RudderLogger.logInfo(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request to endpoint %s was successful with status code %d and response is %s",
                        httpConnection.getURL(), responseCode, responsePayload));
                networkResponse = NetworkResponses.SUCCESS;

            } else {
                errorPayload = getResponse(httpConnection.getErrorStream());
                RudderLogger.logError(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request to endpoint %s failed with status code %d and error %s",
                        httpConnection.getURL(), responseCode, errorPayload));
                if (errorPayload != null && errorPayload.toLowerCase().contains("invalid write key"))
                    networkResponse = NetworkResponses.WRITE_KEY_ERROR;
                else if (responseCode == 404)
                    networkResponse = NetworkResponses.RESOURCE_NOT_FOUND;
                else if (responseCode == 400)
                    networkResponse = NetworkResponses.BAD_REQUEST;
            }
            // if networkresponse is null (or) if both response and error payload is null, then it is an error
            if (networkResponse == null || (responsePayload == null && errorPayload == null))
                networkResponse = NetworkResponses.ERROR;
            return new Result(networkResponse, responseCode, responsePayload, errorPayload);
        } catch (Exception ex) {
            RudderLogger.logError("RudderNetworkManager: sendNetworkRequest: Exception occurred while getting the response from the request to " + httpConnection.getURL() + ex.getLocalizedMessage());
            return new Result(NetworkResponses.ERROR, responseCode, responsePayload, ex.getLocalizedMessage());
        } finally {
            httpConnection.disconnect();
        }
    }

    @Nullable
    private HttpURLConnection updateHttpConnection(String requestURL, RequestMethod requestMethod,
                                                   String requestPayload, boolean isDMTRequest, boolean isGzipSupported) {
        try {
            URL url = new URL(requestURL);
            RudderLogger.logDebug(String.format(Locale.US, "RudderNetworkManager: sendNetworkRequest: Request URL: %s", requestURL));
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            if (isGzipSupported && isGzipConfigured) {
                RudderLogger.logDebug("RudderNetworkManager: sendNetworkRequest: Gzip is enabled");
                Map<String, String> customRequestHeaders = new HashMap<>();
                customRequestHeaders.put("Content-Encoding", "gzip");
                return updateHttpConnection(httpConnection, requestMethod, requestPayload, isDMTRequest, customRequestHeaders,
                        GzipUtils::getGzipOutputStream);
            }
            return updateHttpConnection(httpConnection, requestMethod, requestPayload, isDMTRequest,
                    null, null);
        } catch (Exception ex) {
            RudderLogger.logError("RudderNetworkManager: sendNetworkRequest: Exception occurred while creating HttpURLConnection" + ex.getLocalizedMessage());
            return null;
        }
    }

    @VisibleForTesting
    @Nullable
    HttpURLConnection updateHttpConnection(HttpURLConnection httpConnection, RequestMethod requestMethod,
                                           String requestPayload, boolean isDMTRequest,
                                           @Nullable Map<String, String> customRequestHeaders,
                                           @Nullable FunctionUtils.Function<OutputStream, OutputStream> connectionWrapperOSGenerator) {
        httpConnection.setRequestProperty("Authorization", String.format(Locale.US, "Basic %s", authHeaderString));
        if (requestMethod == RequestMethod.GET) {
            return updateHttpConnectionForGetRequests(httpConnection);
        }
        return updateHttpConnectionForPostRequest(httpConnection, requestPayload, isDMTRequest, customRequestHeaders, connectionWrapperOSGenerator);
    }

    @Nullable
    private static HttpURLConnection updateHttpConnectionForGetRequests(HttpURLConnection httpConnection) {
        try {
            httpConnection.setRequestMethod("GET");
        } catch (Exception ex) {
            RudderLogger.logError("RudderNetworkManager: updateHttpConnection: Error while updating the http connection" + ex.getLocalizedMessage());
            return null;
        }
        return httpConnection;
    }

    @Nullable
    private HttpURLConnection updateHttpConnectionForPostRequest(HttpURLConnection httpConnection, String requestPayload, boolean isDMTRequest, @Nullable Map<String, String> customRequestHeaders, @Nullable FunctionUtils.Function<OutputStream, OutputStream> connectionWrapperOSGenerator) {
        // if the request is of type POST
        httpConnection.setDoOutput(true);
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setRequestProperty("AnonymousId", anonymousIdHeaderString);
        if (customRequestHeaders != null && !customRequestHeaders.isEmpty()) {
            for (Map.Entry<String, String> entry : customRequestHeaders.entrySet()) {
                httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        try (
                OutputStream os = httpConnection.getOutputStream();
                OutputStream oss = connectionWrapperOSGenerator != null ? connectionWrapperOSGenerator.apply(os) : null;
                OutputStreamWriter osw = new OutputStreamWriter(oss != null ? oss : os, StandardCharsets.UTF_8)
        ) {
            httpConnection.setRequestMethod("POST");
            String requestPayloadWithMetadata = withAddedMetadataToRequestPayload(requestPayload, isDMTRequest);
            osw.write(requestPayloadWithMetadata);
            osw.flush();
            return httpConnection;
        } catch (Exception ex) {
            RudderLogger.logError("RudderNetworkManager: updateHttpConnection: Error while updating the http connection" + ex.getLocalizedMessage());
            return null;
        }
    }

    @VisibleForTesting
    String withAddedMetadataToRequestPayload(String requestPayload, boolean isDMTRequest) {
        if (requestPayload == null || !isDMTRequest || dmtAuthorisationString == null)
            return requestPayload;
        JsonObject jsonObject = JsonParser.parseString(requestPayload).getAsJsonObject();
        JsonObject metadataJsonObject = new JsonObject();
        metadataJsonObject.addProperty(DMT_AUTHORISATION_KEY, dmtAuthorisationString);
        jsonObject.add(METADATA_KEY, metadataJsonObject);
        return jsonObject.toString();
    }

    private String getResponse(InputStream stream) {
        try (BufferedInputStream bis = new BufferedInputStream(stream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int res;
            while ((res = bis.read()) != -1) {
                baos.write((byte) res);
            }
            return baos.toString();
        } catch (Exception ex) {
            RudderLogger.logError("RudderNetworkManager: getResponse: Exception occurred while reading response" + ex.getLocalizedMessage());
            return null;
        }
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
