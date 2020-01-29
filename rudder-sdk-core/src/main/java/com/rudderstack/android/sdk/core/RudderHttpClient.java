package com.rudderstack.android.sdk.core;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class RudderHttpClient {
    private static RudderHttpClient client;
    private static String authHeaderToken;

    private RudderHttpClient() {
        // stop instantiating
        try {
            authHeaderToken = "Basic " + Base64.encodeToString((RudderClient.getWriteKey() + ":").getBytes("UTF-8"), Base64.DEFAULT);
            RudderLogger.logDebug("RudderHttpClient: authHeaderToken: " + authHeaderToken);
        } catch (UnsupportedEncodingException ex) {
            RudderLogger.logError(ex);
        }
    }

    static RudderHttpClient getInstance() {
        if (client == null) {
            client = new RudderHttpClient();
        }
        return client;
    }

    String get(@NonNull String requestUrl, @Nullable Map<String, String> headers) {
        return makeRequest(requestUrl, "GET", null, headers);
    }

    String post(@NonNull String requestUrl, @Nullable String payload, @Nullable Map<String, String> headers) {
        return makeRequest(requestUrl, "POST", payload, headers);
    }

    private String makeRequest(@NonNull String requestUrl, @NonNull String method, @Nullable String payload, @Nullable Map<String, String> headers) {
        try {
            RudderLogger.logDebug("RudderHttpClient: requestUrl: " + requestUrl);
            // create url object
            URL url = new URL(requestUrl);
            // get connection object
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod(method);
            // set connection object to return output
            if (method.equalsIgnoreCase("POST")) {
                httpConnection.setDoOutput(true);
            }
            // set headers
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put("Authorization", authHeaderToken);
            for (String key : headers.keySet()) {
                httpConnection.setRequestProperty(key, headers.get(key));
            }

            // get output stream and write payload content
            if (payload != null) {
                OutputStream os = httpConnection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(payload);
                osw.flush();
                osw.close();
                os.close();
            }
            //  set content type for network request
            // set request method
            // create connection
            httpConnection.connect();
            // get input stream from connection to get output from the server
            RudderLogger.logDebug("RudderHttpClient: Response Code: " + httpConnection.getResponseCode());
            if (httpConnection.getResponseCode() == 200) {
                BufferedInputStream bis = new BufferedInputStream(httpConnection.getInputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int res = bis.read();
                // read response from the server
                while (res != -1) {
                    baos.write((byte) res);
                    res = bis.read();
                }
                // finally return response when reading from server is completed
                String response = baos.toString();
                RudderLogger.logDebug("RudderHttpClient: response: " + response);
                return response;
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
                RudderLogger.logError("RudderHttpClient: error response: " + baos.toString() + " || url: " + requestUrl);
                return null;
            }
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
        return null;
    }
}
