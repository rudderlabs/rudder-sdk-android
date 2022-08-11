package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.util.Utils.getBooleanFromMap;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RudderDeviceModeManager {

    private final DBPersistentManager dbPersistentManager;
    private final RudderNetworkManager networkManager;
    private final RudderDeviceModeManager rudderDeviceModeManager;
    private final RudderConfig rudderConfig;
    private RudderServerConfig serverConfig;
    private boolean areFactoriesInitialized;
    private RudderEventFilteringPlugin rudderEventFilteringPlugin;
    private final Map<String, RudderIntegration<?>> integrationOperationsMap;
    private final Map<String, RudderClient.Callback> integrationCallbacks;
    private final List<RudderMessage> eventReplayMessageQueue;
    // required for device mode transform
    private final Map<String, String> destinationsWithTransformationsEnabled = new HashMap<>(); //destination display name to destinationId

    RudderDeviceModeManager(DBPersistentManager dbPersistentManager, RudderNetworkManager networkManager, RudderConfig rudderConfig) {
        this.dbPersistentManager = dbPersistentManager;
        this.networkManager = networkManager;
        this.rudderConfig = rudderConfig;
        this.areFactoriesInitialized = false;
        this.integrationOperationsMap = new HashMap<>();
        this.integrationCallbacks = new HashMap<>();
        this.eventReplayMessageQueue = Collections.synchronizedList(new ArrayList<RudderMessage>());
        rudderDeviceModeManager = this;
    }

    void initiate(RudderServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.rudderEventFilteringPlugin = new RudderEventFilteringPlugin(serverConfig.source.destinations);
        setDestinationsWithTransformationsEnabled(serverConfig);
        initiateFactories(serverConfig.source.destinations);
        initiateCustomFactories();
        replayMessageQueue();
        this.areFactoriesInitialized = true;
        if (destinationsWithTransformationsEnabled.size() > 0) {
            RudderDeviceModeTransformationManager deviceModeTransformationManager = new RudderDeviceModeTransformationManager(dbPersistentManager, networkManager, rudderDeviceModeManager, rudderConfig);
            deviceModeTransformationManager.startDeviceModeTransformationProcessor();
        }
    }

    private void setDestinationsWithTransformationsEnabled(RudderServerConfig serverConfig) {
        if (serverConfig != null && serverConfig.source != null && serverConfig.source.destinations != null)
            for (RudderServerDestination destination : serverConfig.source.destinations) {
                if (destination.isDestinationEnabled && destination.areTransformationsConnected)
                    destinationsWithTransformationsEnabled.put(destination.destinationDefinition.displayName, destination.destinationId);
            }
    }

    private void initiateFactories(List<RudderServerDestination> destinations) {
        if (rudderConfig == null || rudderConfig.getFactories() == null || rudderConfig.getFactories().isEmpty()) {
            RudderLogger.logInfo("RudderDeviceModeManager: initiateFactories: No native SDK factory found");
            return;
        }
        // initiate factories if client is initialized properly
        if (destinations.isEmpty()) {
            RudderLogger.logInfo("RudderDeviceModeManager: initiateFactories: No destination found in the config");
            return;
        }
        // check for multiple destinations
        Map<String, RudderServerDestination> destinationConfigMap = new HashMap<>();
        for (RudderServerDestination destination : destinations) {
            destinationConfigMap.put(destination.destinationDefinition.displayName, destination);
        }

        for (RudderIntegration.Factory factory : rudderConfig.getFactories()) {
            // if factory is present in the config
            String key = factory.key();
            if (destinationConfigMap.containsKey(key)) {
                RudderServerDestination destination = destinationConfigMap.get(key);
                // initiate factory if destination is enabled from the dashboard
                if (destination != null && destination.isDestinationEnabled) {
                    Object destinationConfig = destination.destinationConfig;
                    RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: initiateFactories: Initiating %s native SDK factory", key));
                    try {
                        RudderIntegration<?> nativeOp = factory.create(destinationConfig, RudderClient.getInstance(), rudderConfig);
                        RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: Initiated %s native SDK factory", key));
                        integrationOperationsMap.put(key, nativeOp);
                        handleCallBacks(key, nativeOp);
                    } catch (Exception e) {
                        RudderLogger.logError(String.format(Locale.US, "EventRepository: initiateFactories: Failed to initiate %s native SDK Factory due to %s", key, e.getLocalizedMessage()));
                    }
                } else {
                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: destination was null or not enabled for %s", key));
                }
            } else {
                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: %s is not present in configMap", key));
            }
        }
    }

    private void handleCallBacks(String key, RudderIntegration nativeOp) {
        if (integrationCallbacks.containsKey(key)) {
            Object nativeInstance = nativeOp.getUnderlyingInstance();
            RudderClient.Callback callback = integrationCallbacks.get(key);
            if (nativeInstance != null && callback != null) {
                RudderLogger.logInfo(String.format(Locale.US, "RudderDeviceModeManager: handleCallBacks: Callback for %s factory invoked", key));
                callback.onReady(nativeInstance);
            } else {
                RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: handleCallBacks: Callback for %s factory is null", key));
            }
        }
    }

    private void initiateCustomFactories() {
        if (rudderConfig == null || rudderConfig.getCustomFactories() == null || rudderConfig.getCustomFactories().isEmpty()) {
            RudderLogger.logInfo("RudderDeviceModeManager: initiateCustomFactories: No custom factory found");
            return;
        }
        for (RudderIntegration.Factory customFactory : rudderConfig.getCustomFactories()) {
            String key = customFactory.key();
            try {
                RudderIntegration<?> nativeOp = customFactory.create(null, RudderClient.getInstance(), rudderConfig);
                RudderLogger.logInfo(String.format(Locale.US, "RudderDeviceModeManager: initiateCustomFactories: Initiated %s custom factory", key));
                integrationOperationsMap.put(key, nativeOp);
                handleCallBacks(key, nativeOp);
            } catch (Exception e) {
                RudderLogger.logError(String.format(Locale.US, "RudderDeviceModeManager: initiateCustomFactories: Failed to initiate %s native SDK Factory due to %s", key, e.getLocalizedMessage()));
            }
        }
    }

    private void replayMessageQueue() {
        synchronized (eventReplayMessageQueue) {
            RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: replayMessageQueue: replaying old messages with factories. Count: %d", eventReplayMessageQueue.size()));
            if (!eventReplayMessageQueue.isEmpty()) {
                for (RudderMessage message : eventReplayMessageQueue) {
                    try {
                        makeFactoryDump(message, true);
                    } catch (Exception e) {
                        RudderLogger.logError(String.format(Locale.US, "RudderDeviceModeManager: replayMessageQueue: Exception in dumping message %s due to %s", message.getEventName(), e.getMessage()));
                    }
                }
            }
            eventReplayMessageQueue.clear();
        }
    }

    void makeFactoryDump(RudderMessage message, boolean fromHistory) {
        synchronized (eventReplayMessageQueue) {
            if (areFactoriesInitialized || fromHistory) {
                for (String destinationName : integrationOperationsMap.keySet()) {
                    if (isEventAllowed(message, destinationName)) {
                        RudderIntegration<?> integration = integrationOperationsMap.get(destinationName);
                        if (integration != null) {
                            if (!destinationsWithTransformationsEnabled.containsKey(destinationName)) {
                                try {
                                    RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: makeFactoryDump: dumping for %s", destinationName));
                                    integration.dump(message);
                                } catch (Exception e) {
                                    RudderLogger.logError(String.format(Locale.US, "RudderDeviceModeManager: makeFactoryDump: Exception in dumping message %s to %s factory %s", message.getEventName(), destinationName, e.getMessage()));
                                }
                            } else {
                                RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: makeFactoryDump: Destination %s needs transformation, hence it will be batched and sent to transformation service", destinationName));
                            }
                        }
                    }
                }
            } else {
                RudderLogger.logDebug("EventRepository: makeFactoryDump: factories are not initialized. dumping to replay queue");
                eventReplayMessageQueue.add(message);
            }
        }
    }

    void reset() {
        if (areFactoriesInitialized) {
            RudderLogger.logDebug("DeviceModeManager: reset: resetting native SDKs");
            for (String key : integrationOperationsMap.keySet()) {
                RudderLogger.logDebug(String.format(Locale.US, "DeviceModeManager: reset for %s", key));
                RudderIntegration<?> integration = integrationOperationsMap.get(key);
                if (integration != null) {
                    integration.reset();
                }
            }
        } else {
            RudderLogger.logDebug("DeviceModeManager: reset: factories are not initialized. ignored");
        }
    }

    void flush() {
        if (areFactoriesInitialized) {
            RudderLogger.logDebug("DeviceModeManager: flush: flush native SDKs");
            for (Map.Entry<String, RudderIntegration<?>> entry : integrationOperationsMap.entrySet()) {
                RudderLogger.logDebug(String.format(Locale.US, "DeviceModeManager: flush for %s", entry.getKey()));
                RudderIntegration<?> integration = entry.getValue();
                if (integration != null) {
                    integration.flush();
                }
            }
        } else {
            RudderLogger.logDebug("DeviceModeManager: flush: factories are not initialized. ignored");
        }
    }

    void addCallBackForIntegration(String integrationName, RudderClient.Callback callback) {
        RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: addCallBackForIntegration: callback registered for %s", integrationName));
        integrationCallbacks.put(integrationName, callback);
    }

    RudderIntegration<?> getRudderIntegrationObject(String destinationId) {
        String destinationName = Utils.getKeyForValueFromMap(destinationsWithTransformationsEnabled, destinationId);
        if (!integrationOperationsMap.containsKey(destinationName))
            return null;
        return integrationOperationsMap.get(destinationName);
    }

    private boolean isEventAllowed(RudderMessage message, String destinationName) {
        Boolean isDestinationEnabledForMessage = isDestinationEnabled(destinationName, message);
        Boolean isEventAllowedForDestination = rudderEventFilteringPlugin.isEventAllowed(destinationName, message);
        return isDestinationEnabledForMessage && isEventAllowedForDestination;
    }

    private Boolean isDestinationEnabled(String destinationName, RudderMessage message) {
        Map<String, Object> integrationOptions = message.getIntegrations();
        // If All is set to true and the destination is absent in the integrations object
        Boolean isAllTrueAndDestinationAbsent = (getBooleanFromMap(integrationOptions, "All") && !integrationOptions.containsKey(destinationName));
        // If the destination is present and true in the integrations object
        Boolean isDestinationEnabled = getBooleanFromMap(integrationOptions, destinationName);
        return isAllTrueAndDestinationAbsent || isDestinationEnabled;
    }
}
