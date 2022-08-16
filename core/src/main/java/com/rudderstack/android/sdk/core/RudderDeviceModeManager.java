package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.util.Utils.getBooleanFromMap;
import static com.rudderstack.android.sdk.core.TransformationResponse.TransformedEvent;
import static com.rudderstack.android.sdk.core.TransformationResponse.TransformedDestination;
import static com.rudderstack.android.sdk.core.TransformationRequest.TransformationRequestEvent;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class RudderDeviceModeManager {

    private final DBPersistentManager dbPersistentManager;
    private final RudderNetworkManager networkManager;
    private final RudderDeviceModeManager rudderDeviceModeManager;
    private final RudderConfig rudderConfig;
    private boolean areFactoriesInitialized;
    private RudderEventFilteringPlugin rudderEventFilteringPlugin;
    private final Map<String, RudderIntegration<?>> integrationOperationsMap;
    private final Map<String, RudderClient.Callback> integrationCallbacks;
    private final Map<Integer, RudderMessage> eventReplayMessageQueue;
    // required for device mode transform
    private final Map<String, String> destinationsWithTransformationsEnabled = new HashMap<>(); //destination display name to destinationId

    RudderDeviceModeManager(DBPersistentManager dbPersistentManager, RudderNetworkManager networkManager, RudderConfig rudderConfig) {
        this.dbPersistentManager = dbPersistentManager;
        this.networkManager = networkManager;
        this.rudderConfig = rudderConfig;
        this.areFactoriesInitialized = false;
        this.integrationOperationsMap = new HashMap<>();
        this.integrationCallbacks = new HashMap<>();
        this.eventReplayMessageQueue = Collections.synchronizedMap(new HashMap<Integer, RudderMessage>());
        rudderDeviceModeManager = this;
    }

    void initiate(RudderServerConfig serverConfig) {
        this.rudderEventFilteringPlugin = new RudderEventFilteringPlugin(serverConfig.source.destinations);
        setDestinationsWithTransformationsEnabled(serverConfig);
        initiateFactories(serverConfig.source.destinations);
        initiateCustomFactories();
        replayMessageQueue();
        this.areFactoriesInitialized = true;
        if (doPassedFactoriesHaveTransformationsEnabled()) {
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
        if (areFactoriesPassedInConfig()) {
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
        if (areCustomFactoriesPassedInConfig()) {
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
                SortedSet<Integer> rowIds = new TreeSet<Integer>(eventReplayMessageQueue.keySet());
                for (Integer rowId : rowIds) {
                    try {
                        makeFactoryDump(eventReplayMessageQueue.get(rowId), rowId, true);
                    } catch (Exception e) {
                        RudderLogger.logError(String.format(Locale.US, "RudderDeviceModeManager: replayMessageQueue: Exception in dumping message %s due to %s", eventReplayMessageQueue.get(rowId).getEventName(), e.getMessage()));
                    }
                }
            }
            eventReplayMessageQueue.clear();
        }
    }

    void makeFactoryDump(RudderMessage message, Integer rowId, boolean fromHistory) {
        synchronized (eventReplayMessageQueue) {
            if (areFactoriesInitialized || fromHistory) {
                List<String> eligibleDestinations = getEligibleDestinations(message);
                boolean isTransformationNeeded = isTransformationNeeded(eligibleDestinations);
                if (!isTransformationNeeded) {
                    RudderLogger.logVerbose("EventRepository: makeFactoryDump: Marking event %s with rowId %d as DEVICE_MODE_PROCESSING DONE as it has no device mode destinations with transformations");
                    dbPersistentManager.markDeviceModeDone(Arrays.asList(rowId));
                }
                dumpEventToDestinations(message, eligibleDestinations, false, "makeFactoryDump");
            } else {
                RudderLogger.logDebug("EventRepository: makeFactoryDump: factories are not initialized. dumping to replay queue");
                eventReplayMessageQueue.put(rowId, message);
            }
        }
    }

    /**
     * @param message                 The message object which should be dumped to the supplied list of device mode destinations.
     * @param destinations            The List of Device Mode Destinations to which this message should be dumped
     * @param transformationAttempted if true, we will dump the message directly to all the supplied device mode destinations including the
     *                                ones with transformations attached, because we already attempted the transformation once and got failed, else we will dump
     *                                the message only to the device mode destinations with no transformations
     * @param logTag                  name of the calling method, which is supposed to be printed in the logs, as this method is utilized by multiple methods
     */
    void dumpEventToDestinations(RudderMessage message, List<String> destinations, boolean transformationAttempted, String logTag) {
        for (String destinationName : destinations) {
            RudderIntegration<?> integration = integrationOperationsMap.get(destinationName);
            if (integration != null) {
                if (transformationAttempted || !destinationsWithTransformationsEnabled.containsKey(destinationName)) {
                    try {
                        RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: %s: dumping for %s", logTag, destinationName));
                        integration.dump(message);
                    } catch (Exception e) {
                        RudderLogger.logError(String.format(Locale.US, "RudderDeviceModeManager: %s: Exception in dumping message %s to %s factory %s", logTag, message.getEventName(), destinationName, e.getMessage()));
                    }
                } else {
                    RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: %s: Destination %s needs transformation, hence it will be batched and sent to transformation service", logTag, destinationName));
                }
            }
        }
    }

    void dumpOriginalEvents(TransformationRequest transformationRequest) {
        if (transformationRequest.batch != null) {
            for (TransformationRequestEvent transformationRequestEvent : transformationRequest.batch) {
                if (transformationRequestEvent != null && transformationRequestEvent.message != null) {
                    dumpEventToDestinations(transformationRequestEvent.message, getEligibleDestinations(transformationRequestEvent.message), true, "dumpOriginalEvents");
                }
            }
        }
    }

    void dumpTransformedEvents(TransformationResponse transformationResponse) {
        if (transformationResponse.transformedBatch == null)
            return;
        for (TransformedDestination transformedDestination : transformationResponse.transformedBatch) {
            if (transformedDestination.id == null || transformedDestination.payload == null)
                continue;
            RudderIntegration<?> rudderIntegration = getRudderIntegrationObject(transformedDestination.id);
            if (rudderIntegration == null)
                continue;
            List<TransformedEvent> transformedEvents = transformedDestination.payload;
            sortTransformedEventBasedOnOrderNo(transformedEvents);
            for (TransformedEvent transformedEvent : transformedEvents) {
                if (transformedEvent.status.equals("200")) {
                    rudderIntegration.dump(transformedEvent.event);
                }
            }
        }
    }

    void sortTransformedEventBasedOnOrderNo(List<TransformedEvent> transformedEvents) {
        Collections.sort(transformedEvents, new Comparator<TransformedEvent>() {
            @Override
            public int compare(TransformedEvent o1, TransformedEvent o2) {
                return o1.orderNo - o2.orderNo;
            }
        });
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

    /**
     * @param message The RudderMessage object for which we need to return the eligible destinations
     * @return Eligible destinations are the device mode destinations to which this message can be sent after considering the message level integrations object
     * as well as the Event Filtering Configuration (AllowList / DenyList) out of all the device mode destinations which were initialized.
     */
    private List<String> getEligibleDestinations(RudderMessage message) {
        List<String> eligibleDestinations = new ArrayList<>();
        for (String destinationName : integrationOperationsMap.keySet()) {
            if (isEventAllowed(message, destinationName)) {
                eligibleDestinations.add(destinationName);
            }
        }
        return eligibleDestinations;
    }

    /**
     * @param eligibleDestinations List of device mode destinations for which we need to check if the transformations are attached.
     * @returns true if atleast one of the destination in the eligibleDestinations has a transformation attached to it.
     */
    private boolean isTransformationNeeded(List<String> eligibleDestinations) {
        for (String destinationName : destinationsWithTransformationsEnabled.keySet()) {
            if (eligibleDestinations.contains(destinationName))
                return true;
        }
        return false;
    }

    /**
     * @param message         The message object for which we need to check if its allowed by the passed destinationName
     * @param destinationName The destination for which we need to check if this message is allowed
     * @return This method checks if this destination is enabled in the message level integrations object and then if this event is allowed
     * by the destination via the Event Filtering feature, and if both of them are true only then it returns true
     */
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

    private boolean doPassedFactoriesHaveTransformationsEnabled() {
        if (!areFactoriesPassedInConfig())
            return false;
        if (destinationsWithTransformationsEnabled == null || destinationsWithTransformationsEnabled.size() == 0)
            return false;
        for (RudderIntegration.Factory factory : rudderConfig.getFactories()) {
            if (destinationsWithTransformationsEnabled.containsKey(factory.key()))
                return true;
        }
        return false;
    }

    private boolean areFactoriesPassedInConfig() {
        if (rudderConfig == null || rudderConfig.getFactories() == null || rudderConfig.getFactories().isEmpty())
            return false;
        return true;
    }

    private boolean areCustomFactoriesPassedInConfig() {
        if (rudderConfig == null || rudderConfig.getCustomFactories() == null || rudderConfig.getCustomFactories().isEmpty())
            return false;
        return true;
    }
}
