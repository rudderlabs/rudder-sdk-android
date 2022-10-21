package com.rudderstack.android.sdk.core;

import com.rudderstack.android.sdk.core.RedisStoreResponse;

@FunctionalInterface
public interface RedisCallback {
    void onResponse(RedisStoreResponse response);
}

/*

package com.rudderstack.android.sdk.core;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rudderstack.android.sdk.core.RedisCallback;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class RedisHandler {

    private enum ServerConfig {
        INITIALISED,
        EMPTY
    }

    private static boolean isRedisDestinationPresent = false;
    private final String destinationName = "Amplitude";
    private static ServerConfig status = ServerConfig.EMPTY;

    private final ExecutorService redisService = Executors.newFixedThreadPool(2);
    private final ExecutorService cacheEvents = Executors.newSingleThreadExecutor();
    private static RedisHandler instance = null;
    private final RudderNetworkManager rudderNetworkManager;
    private Context context;

    static RedisHandler getInstance(RudderNetworkManager rudderNetworkManager, Context context) {
        if (instance == null) {
            instance = new RedisHandler(rudderNetworkManager, context);
        }
        return instance;
    }

    static RedisHandler getInstance() {
        return instance;
    }

    RedisHandler(RudderNetworkManager rudderNetworkManager, Context context) {
        this.rudderNetworkManager = rudderNetworkManager;
        this.context = context;
    }

    final List<Callable<Object>> tasks = new ArrayList<>();

    void process(@NonNull final Map<String, Object> inputData, @NonNull final RedisCallback callback) {
        synchronized (MessageUploadLock.REDIS_TRANSFORM_LOCK) {

            if (getServerConfigStatus() == ServerConfig.EMPTY) {
                tasks.add(
                        () -> {
                            RedisStoreResponse redisStoreResponse = makeNetworkRequest(inputData);
                            handleNetworkResponse(redisStoreResponse, callback);
                            return null;
                        }
                );
            } else if (isRedisDestinationPresent()) {
//                flushCachedEvents();
                redisService.execute(
                        () -> {
                            RedisStoreResponse redisStoreResponse = makeNetworkRequest(inputData);
                            handleNetworkResponse(redisStoreResponse, callback);
                        }
                );
            } else {
                RudderLogger.logDebug("Dropping the Transform API call as Redis destination is not enabled.");
            }
        }
    }

    void setRedisDestinationPresent(@Nullable RudderServerConfig serverConfig) {
        synchronized (MessageUploadLock.REDIS_TRANSFORM_LOCK) {
            status = ServerConfig.INITIALISED;
            if (serverConfig != null && serverConfig.source.isSourceEnabled) {
                if (!serverConfig.source.destinations.isEmpty()) {
                    List<RudderServerDestination> destinationList = serverConfig.source.destinations;
                    for (RudderServerDestination destination :
                            destinationList) {
                        if (destination.isDestinationEnabled)
                            if (destination.destinationDefinition.displayName.equals(destinationName)) {
                                if (isGivenDestinationTransformationsEnabled(destination)) {
                                    flushCachedEvents();    // remove Events keyword
                                    isRedisDestinationPresent = true;
                                }
                                return;
                            }
                    }
                }
            }
            clearCachedEvents();
        }
    }

    private boolean isGivenDestinationTransformationsEnabled(RudderServerDestination destination) {
        if (destination != null)
            return destination.isDestinationEnabled && destination.areTransformationsConnected;
        return false;
    }

    private void clearCachedEvents() {
        RudderLogger.logDebug("Since there is some problem in the server config, dropping all thr trasform API call");
        tasks.clear();
        cacheEvents.shutdown();
    }

    private void flushCachedEvents() {
        if (!cacheEvents.isTerminated()) {
            System.out.println("Length is: " + tasks.size());
            for (Callable<Object> task : tasks) {
                cacheEvents.submit(task);
            }
            cacheEvents.shutdown();
        }
    }

    private ServerConfig getServerConfigStatus() {
        return status;
    }

    private static boolean isRedisDestinationPresent() {
        return isRedisDestinationPresent;
    }


    @NonNull
    private RedisStoreResponse makeNetworkRequest(final Map<String, Object> inputData) {
        // TODO: Make Request to CTS and return the response
        RedisStoreResponse redisStoreResponse = new RedisStoreResponse();

        String requestJson = String.valueOf(createDeviceTransformPayload(inputData));
        String debanjanDaNgrokUrl = "https://8e4b941e-d8dd-431b-8433-27888901944c.mock.pstmn.io/redis";
        String postmanServerUrl = "https://8e4b941e-d8dd-431b-8433-27888901944c.mock.pstmn.io/redis";
        RudderNetworkManager.Result result = rudderNetworkManager.sendNetworkRequest(requestJson, postmanServerUrl, RudderNetworkManager.RequestMethod.POST);

        Map<String, Object> returnPayload = new Gson().fromJson(
                result.response, new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );
        if (returnPayload != null && returnPayload.containsKey("transformedPayload")) {
            returnPayload = (Map<String, Object>) returnPayload.get("transformedPayload");
        }
        redisStoreResponse.success = true;
        redisStoreResponse.errorMsg = result.error;
        redisStoreResponse.payload = returnPayload;
        return redisStoreResponse;
    }

//    private JSONObject createDeviceTransformPayload(Map<String, Object> inputData) {
//        Map<String, Object> mp = new HashMap<>();
//        mp.put("payload", inputData);
//        return new JSONObject(mp);
//    }

    private void handleNetworkResponse(@Nullable RedisStoreResponse redisStoreResponse, @NonNull final RedisCallback callback) {
        callback.onResponse(redisStoreResponse);
    }
}
 */