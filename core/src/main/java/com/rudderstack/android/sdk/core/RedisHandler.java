package com.rudderstack.android.sdk.core;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.MessageUploadLock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    // TODO: Remove context variable
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

    final List<RedisTransformationHandler> tasks = new ArrayList<>();

    void process(@NonNull final Map<String, Object> inputData, @NonNull final RedisCallback callback) {
        synchronized (MessageUploadLock.REDIS_TRANSFORM_LOCK) {
            if (getServerConfigStatus() == ServerConfig.EMPTY) {
                tasks.add(
                        new RedisTransformationHandler(
                                rudderNetworkManager,
                                inputData,
                                callback
                                )
                );
                flushCachedEvents();
            } else if (isRedisDestinationPresent()) {
                redisService.execute(
                        () -> {
                            RedisTransformationHandler redisTransformationHandler = new RedisTransformationHandler(
                                    rudderNetworkManager,
                                    inputData,
                                    callback
                            );
                            RedisStoreResponse redisStoreResponse = redisTransformationHandler.makeNetworkRequest();
                            redisTransformationHandler.handleNetworkResponse(redisStoreResponse);
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

    private void clearCachedEvents() {
        RudderLogger.logDebug("Since there is some problem in the server config, dropping all thr trasform API call");
        tasks.clear();
        cacheEvents.shutdown();
    }

    private void flushCachedEvents() {
        if (!cacheEvents.isTerminated()) {
            System.out.println("Length is: " + tasks.size());
            for (RedisTransformationHandler task : tasks) {
                cacheEvents.submit(task);
            }
            tasks.clear();
            cacheEvents.shutdown();
        }
    }

    private boolean isGivenDestinationTransformationsEnabled(RudderServerDestination destination) {
        if (destination != null)
            return destination.isDestinationEnabled && destination.areTransformationsConnected;
        return false;
    }

    private ServerConfig getServerConfigStatus() {
        return status;
    }

    private static boolean isRedisDestinationPresent() {
        return isRedisDestinationPresent;
    }
}