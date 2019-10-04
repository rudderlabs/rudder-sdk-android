package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.os.Handler;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * utility class for event processing
 * */
class EventRepository {
    private RudderConfig config;
    private String writeKey;
    private DBPersistentManager dbManager;
    private RudderServerConfigManager configManager;
    private Map<String, Object> integrationsMap;
    private Map<String, RudderIntegration> integrationOperationsMap = new HashMap<>();

    /*
     * constructor to be called from RudderClient internally.
     * -- tasks to be performed
     * 1. set the values of writeKey, config
     * 2. initiate RudderElementCache
     * 3. initiate DBPersistentManager for SQLite operations
     * 4. initiate RudderServerConfigManager
     * 5. start processor thread
     * */
    EventRepository(Application _application, String _writeKey, RudderConfig _config) {
        // 1. set the values of writeKey, config
        this.config = _config;
        this.writeKey = _writeKey;

        try {
            // 2. initiate RudderElementCache
            RudderElementCache.initiate(_application);

            // 3. initiate DBPersistentManager for SQLite operations
            this.dbManager = DBPersistentManager.getInstance(_application);

            // 4. initiate RudderServerConfigManager
            this.configManager = RudderServerConfigManager.getInstance(_application, _writeKey);

            // 5. start processor thread
            Thread processorThread = new Thread(getProcessorRunnable());
            processorThread.start();

            // 6. initiate factories
            this.initiateFactories(_config);

            // 7. get advertising id
//            "ADVERTISING ID: " +
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    private void initiateFactories(final RudderConfig _config) {
        // initiate factory initialization after 10s
        // let the factories capture everything they want to capture
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RudderServerConfig config = configManager.getConfig();
                if (config == null) return;

                // get destinations from server
                List<RudderServerDestination> destinations = configManager.getConfig().source.destinations;
                // create a map for ease of handling
                Map<String, RudderServerDestination> destinationMap = new HashMap<>();
                for (RudderServerDestination destination : destinations)
                    destinationMap.put(destination.destinationDefinition.definitionName, destination);
                // check the factories integrated
                for (RudderIntegration.Factory factory : _config.getFactories()) {
                    // if factory is present in the config
                    if (destinationMap.containsKey(factory.key())) {
                        RudderServerDestination destination = destinationMap.get(factory.key());
                        // initiate factory if destination is enabled from the dashboard
                        if (destination != null && destination.isDestinationEnabled) {
                            Object destinationConfig = destination.destinationConfig;
                            integrationOperationsMap.put(factory.key(), factory.create(destinationConfig, RudderClient.getInstance()));
                        }
                    }
                }
            }
        }, 10000);
    }

    private Runnable getProcessorRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // initiate sleepCount
                int sleepCount = 0;

                // initiate lists for messageId and message
                ArrayList<Integer> messageIds = new ArrayList<>();
                ArrayList<String> messages = new ArrayList<>();

                while (true) {
                    try {
                        // clear lists for reuse
                        messageIds.clear();
                        messages.clear();

                        // get current record count from db
                        int recordCount = dbManager.getDBRecordCount();
                        // if record count exceeds threshold count, remove older events
                        if (recordCount > config.getDbCountThreshold()) {
                            // fetch extra old events
                            dbManager.fetchEventsFromDB(messageIds, messages,
                                    recordCount - config.getDbCountThreshold());
                            // remove events
                            dbManager.clearEventsFromDB(messageIds);
                            // clear lists for reuse
                            messageIds.clear();
                            messages.clear();
                        }

                        // fetch enough events to form a batch
                        dbManager.fetchEventsFromDB(messageIds, messages, config.getFlushQueueSize());
                        // if there are enough events to form a batch and flush to server
                        // OR
                        // sleepTimeOut seconds has elapsed since last successful flush and
                        // we have at least one event to flush to server
                        if (messages.size() >= config.getFlushQueueSize() || (!messages.isEmpty() && sleepCount >= config.getSleepTimeOut())) {
                            // form payload JSON form the list of messages
                            String payload = getPayloadFromMessages(messages);
                            if (payload != null) {
                                // send payload to server if it is not null
                                String response = flushEventsToServer(payload);
                                System.out.println("response: " + response);
                                System.out.println("eventcount: " + messages.size());
                                // if success received from server
                                if (response != null && response.equalsIgnoreCase("OK")) {
                                    // remove events from DB
                                    dbManager.clearEventsFromDB(messageIds);
                                    // reset sleep count to indicate successful flush
                                    sleepCount = 0;
                                }
                            }
                        }
                        // increment sleepCount to track total elapsed seconds
                        sleepCount += 1;
                        // retry entire logic in 1 second
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        RudderLogger.logError(ex);
                        ex.printStackTrace();
                    }
                }
            }
        };
    }

    /*
     * create payload string from messages list
     * - we created payload from individual message json strings to reduce the complexity
     * of deserialization and forming the payload object and creating the json string
     * again from the object
     * */
    private String getPayloadFromMessages(ArrayList<String> messages) {
        try {
            // get string builder
            StringBuilder builder = new StringBuilder();
            // append initial json token
            builder.append("{");
            // append sent_at time stamp
            builder.append("\"sent_at\":\"").append(Utils.getTimeStamp()).append("\",");
            // initiate batch array in the json
            builder.append("\"batch\": [");
            // loop through messages list and add in the builder
            for (int index = 0; index < messages.size(); index++) {
                builder.append(messages.get(index));
                // if not last item in the list, add a ","
                if (index != messages.size() - 1) builder.append(",");
            }
            // close batch array in the json
            builder.append("],");
            // add writeKey in the json
            builder.append("\"writeKey\":\"").append(writeKey).append("\"");
            // append closing token in the json
            builder.append("}");
            // finally return the entire payload
            return builder.toString();
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
        return null;
    }

    /*
     * flush events payload to server and return response as String
     * */
    private String flushEventsToServer(String payload) throws IOException {
        // get endPointUrl form config object
        String endPointUri = config.getEndPointUri() + "hello";

        // create url object
        URL url = new URL(endPointUri);
        // get connection object
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        // set connection object to return output
        httpConnection.setDoOutput(true);
        //  set content type for network request
        httpConnection.setRequestProperty("Content-Type", "application/json");
        // set request method
        httpConnection.setRequestMethod("POST");
        // get output stream and write payload content
        OutputStream os = httpConnection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        osw.write(payload);
        osw.flush();
        osw.close();
        os.close();
        // create connection
        httpConnection.connect();
        // get input stream from connection to get output from the server
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
            return baos.toString();
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
            System.out.println("ServerError: " + baos.toString());

            return null;
        }
    }

    /*
     * generic method for dumping all the events
     * */
    void dump(RudderElement event) {
        if (this.integrationsMap == null) prepareIntegrations();
        event.setIntegrations(this.integrationsMap);
        for (String key : integrationOperationsMap.keySet()) {
            RudderIntegration integration = integrationOperationsMap.get(key);
            if (integration != null) {
                integration.dump(event);
            }
        }
        String eventJson = new Gson().toJson(event);
        dump(eventJson);
    }

    void dump(String eventJson) {
        dbManager.saveEvent(eventJson);
    }

    private void prepareIntegrations() {
        if (this.configManager.getConfig() == null) return;

        this.integrationsMap = new HashMap<>();
        for (RudderServerDestination destination : this.configManager.getConfig().source.destinations) {
            if (!this.integrationsMap.containsKey(destination.destinationDefinition.definitionName))
                this.integrationsMap.put(destination.destinationDefinition.definitionName, destination.isDestinationEnabled);
        }
    }
}
