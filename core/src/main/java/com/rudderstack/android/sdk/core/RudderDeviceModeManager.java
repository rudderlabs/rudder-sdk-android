package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.util.Utils.getBooleanFromMap;
import static com.rudderstack.android.sdk.core.TransformationResponse.TransformedEvent;
import static com.rudderstack.android.sdk.core.TransformationResponse.TransformedDestination;
import static com.rudderstack.android.sdk.core.TransformationRequest.TransformationRequestEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.consent.ConsentFilterHandler;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

enum TRANSFORMATION_STATUS {
    ENABLED(true),
    DISABLED(false);

    final boolean status;

    private TRANSFORMATION_STATUS(boolean status) {
        this.status = status;
    }

}

public class RudderDeviceModeManager {

    private final DBPersistentManager dbPersistentManager;
    private final RudderNetworkManager networkManager;
    private final RudderConfig rudderConfig;
    private boolean areFactoriesInitialized;
    private RudderEventFilteringPlugin rudderEventFilteringPlugin;
    private final Map<String, RudderIntegration<?>> integrationOperationsMap;
    private final Map<String, RudderClient.Callback> integrationCallbacks;
    private final Map<Integer, RudderMessage> eventReplayMessageQueue;
    private final RudderDataResidencyManager dataResidencyManager;
    // required for device mode transform
    private final Map<String, String> destinationsWithTransformationsEnabled = new HashMap<>(); //destination display name to destinationId
    private final Set<String> destinationsExcludedOnTransformationError = new HashSet<>();
    RudderDeviceModeTransformationManager deviceModeTransformationManager;

    static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();

    RudderDeviceModeManager(DBPersistentManager dbPersistentManager, RudderNetworkManager networkManager, RudderConfig rudderConfig, RudderDataResidencyManager dataResidencyManager) {
        this.dbPersistentManager = dbPersistentManager;
        this.networkManager = networkManager;
        this.rudderConfig = rudderConfig;
        this.dataResidencyManager = dataResidencyManager;
        this.areFactoriesInitialized = false;
        this.integrationOperationsMap = new HashMap<>();
        this.integrationCallbacks = new HashMap<>();
        this.eventReplayMessageQueue = Collections.synchronizedMap(new HashMap<Integer, RudderMessage>());
    }

    void initiate(RudderServerConfig serverConfig, ConsentFilterHandler consentFilterHandler) {
        RudderLogger.logDebug("RudderDeviceModeManager: DeviceModeProcessor: Starting the Device Mode Processor");
        List<RudderServerDestination> consentedDestinations = getConsentedDestinations(serverConfig.source,
                consentFilterHandler);
        setupNativeFactoriesWithFiltering(consentedDestinations);
        segregateDestinations(consentedDestinations);
        initiateCustomFactories();
        replayMessageQueue();
        this.areFactoriesInitialized = true;
        if (doPassedFactoriesHaveTransformationsEnabled()) {
            RudderLogger.logDebug("RudderDeviceModeManager: DeviceModeProcessor: Starting the Device Mode Transformation Processor");
            this.deviceModeTransformationManager = new RudderDeviceModeTransformationManager(dbPersistentManager, networkManager, this, rudderConfig, dataResidencyManager);
            this.deviceModeTransformationManager.startDeviceModeTransformationProcessor();
        } else {
            RudderLogger.logDebug("RudderDeviceModeManager: DeviceModeProcessor: No Device Mode Destinations with transformations attached hence device mode transformation processor need not to be started");
        }
    }

    private List<RudderServerDestination> getConsentedDestinations(RudderServerConfigSource serverConfigSource,
                                                                   ConsentFilterHandler consentFilterHandler) {
        if (serverConfigSource == null)
            return Collections.emptyList();
        List<RudderServerDestination> destinations = serverConfigSource.destinations;
        if (destinations == null) {
            RudderLogger.logDebug("EventRepository: initiateSDK: No native SDKs are found");
            return Collections.emptyList();
        }
        List<RudderServerDestination> consentedDestinations = consentFilterHandler !=
                null ? consentFilterHandler.filterDestinationList(destinations) : destinations;
        if (consentedDestinations == null)
            return Collections.emptyList();
        return consentedDestinations;

    }

    // The sourceConfig will be used to generate two lists: one containing destinations with enabled transformations,
    // and another containing destinations to be excluded in case of a transformation error.
    private void segregateDestinations(List<RudderServerDestination> destinations) {
        for (RudderServerDestination destination : destinations) {
            if (destination.isDestinationEnabled && destination.shouldApplyDeviceModeTransformation) {
                destinationsWithTransformationsEnabled.put(destination.destinationDefinition.displayName, destination.destinationId);
                if (!destination.propagateEventsUntransformedOnError) {
                    destinationsExcludedOnTransformationError.add(destination.destinationDefinition.displayName);
                }
            }
        }
    }

    private void setupNativeFactoriesWithFiltering(List<RudderServerDestination> destinations) {
        if (!areFactoriesPassedInConfig()) {
            RudderLogger.logInfo("RudderDeviceModeManager: initiateFactories: No native SDK factory found");
            return;
        }
        // initiate factories if client is initialized properly
        if (destinations.isEmpty()) {
            RudderLogger.logInfo("RudderDeviceModeManager: initiateFactories: No destination found in the config");
            return;
        }
        initiateFactories(destinations);
        RudderLogger.logDebug("EventRepository: initiating event filtering plugin for device mode destinations");
        rudderEventFilteringPlugin = new RudderEventFilteringPlugin(destinations);
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
        if (!areCustomFactoriesPassedInConfig()) {
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

    private void initiateFactories(List<RudderServerDestination> destinations) {
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
                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: Initiating %s native SDK factory", key));
                    RudderIntegration<?> nativeOp = factory.create(destinationConfig, RudderClient.getInstance(), rudderConfig);
                    RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: Initiated %s native SDK factory", key));
                    integrationOperationsMap.put(key, nativeOp);
                    handleCallBacks(key, nativeOp);
                } else {
                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: destination was null or not enabled for %s", key));
                }
            } else {
                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: %s is not present in configMap", key));
            }
        }
    }

    private void replayMessageQueue() {
        synchronized (eventReplayMessageQueue) {
            RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: replayMessageQueue: replaying old messages with factories. Count: %d", eventReplayMessageQueue.size()));
            if (!eventReplayMessageQueue.isEmpty()) {
                SortedSet<Integer> rowIds = new TreeSet<>(eventReplayMessageQueue.keySet());
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

                List<String> destinationsWithTransformations = getDestinationsWithTransformationStatus(TRANSFORMATION_STATUS.ENABLED, eligibleDestinations);
                if (destinationsWithTransformations.isEmpty()) {
                    RudderLogger.logVerbose(String.format(Locale.US, "RudderDeviceModeManager: makeFactoryDump: Marking event with rowId %s as DEVICE_MODE_PROCESSING DONE as it has no device mode destinations with transformations", rowId));
                    dbPersistentManager.markDeviceModeDone(Arrays.asList(rowId));
                } else {
                    for (String destinationName : destinationsWithTransformations) {
                        RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: makeFactoryDump: Destination %s needs transformation, hence the event will be batched and sent to transformation service", destinationName));
                    }
                }

                List<String> destinationsWithoutTransformations = getDestinationsWithTransformationStatus(TRANSFORMATION_STATUS.DISABLED, eligibleDestinations);
                dumpEventToDestinations(message, destinationsWithoutTransformations, "makeFactoryDump");
            } else {
                RudderLogger.logDebug("RudderDeviceModeManager: makeFactoryDump: factories are not initialized. dumping to replay queue");
                eventReplayMessageQueue.put(rowId, message);
            }
        }
    }

    /**
     * @param message      The message object which should be dumped to the supplied list of device mode destinations.
     * @param destinations The List of Device Mode Destinations to which this message should be dumped
     * @param logTag       name of the calling method, which is supposed to be printed in the logs, as this method is utilized by multiple methods
     */
    void dumpEventToDestinations(RudderMessage message, List<String> destinations, String logTag) {
        for (String destinationName : destinations) {
            RudderIntegration<?> integration = integrationOperationsMap.get(destinationName);
            if (integration != null) {
                try {
                    RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: %s: dumping event %s for %s", logTag, message.getEventName(), destinationName));
                    RudderLogger.logVerbose(String.format(Locale.US, "RudderDeviceModeManager: Dumping: %s", gson.toJson(message)));
                    integration.dump(message);
                } catch (Exception e) {
                    RudderLogger.logError(String.format(Locale.US, "RudderDeviceModeManager: %s: Exception in dumping message %s to %s factory %s", logTag, message.getEventName(), destinationName, e.getMessage()));
                }
            }
        }
    }

    void dumpOriginalEvents(TransformationRequest transformationRequest, boolean onTransformationError) {
        if (transformationRequest.batch != null) {
            RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: dumpOriginalEvents: dumping back the original events to the transformations enabled destinations as there is transformation error."));
            for (TransformationRequestEvent transformationRequestEvent : transformationRequest.batch) {
                if (transformationRequestEvent != null && transformationRequestEvent.message != null) {
                    List<String> destinationsWithTransformationsEnabled = getDestinationNameForIds(transformationRequestEvent.destinationIds);
                    List<String> destinations = onTransformationError ? getDestinationsAcceptingEventsOnTransformationError(destinationsWithTransformationsEnabled) : destinationsWithTransformationsEnabled;
                    dumpEventToDestinations(transformationRequestEvent.message, destinations, "dumpOriginalEvents");
                }
            }
        }
    }

    private List<String> getDestinationNameForIds(List<String> destinationIds) {
        List<String> destinationsWithTransformationsEnabled = new ArrayList<>();
        for (String destinationId : destinationIds) {
            destinationsWithTransformationsEnabled.add(Utils.getKeyForValueFromMap(this.destinationsWithTransformationsEnabled, destinationId));
        }
        return destinationsWithTransformationsEnabled;
    }

    private List<String> getDestinationsAcceptingEventsOnTransformationError(List<String> destinationsWithTransformationsEnabled) {
        List<String> destinationsAcceptingEventsOnTransformationError = new ArrayList<>();
        for (String destinationName : destinationsWithTransformationsEnabled) {
            if (destinationsExcludedOnTransformationError.contains(destinationName)) {
                RudderLogger.logWarn("RudderDeviceModeManager: getDestinationsAcceptingEventsOnTransformationError: " + destinationName + " is excluded from accepting events on transformation error. " +
                        "Hence not sending event to this destination.");
                continue;
            }
            destinationsAcceptingEventsOnTransformationError.add(destinationName);
        }
        return destinationsAcceptingEventsOnTransformationError;
    }

    void dumpTransformedEvents(TransformationResponse transformationResponse) {
        if (transformationResponse.transformedBatch == null)
            return;
        for (TransformedDestination transformedDestination : transformationResponse.transformedBatch) {
            if (transformedDestination.id == null || transformedDestination.payload == null)
                continue;
            String destinationName = Utils.getKeyForValueFromMap(destinationsWithTransformationsEnabled, transformedDestination.id);
            if (destinationName == null)
                return;
            List<TransformedEvent> transformedEvents = transformedDestination.payload;
            sortTransformedEventBasedOnOrderNo(transformedEvents);
            sendEventsToTransformedDestinations(transformedDestination, destinationName);
        }
    }

    private void sendEventsToTransformedDestinations(TransformedDestination transformedDestination, String destinationName) {
        if (transformedDestination.payload == null)
            return;
        for (TransformedEvent transformedEvent : transformedDestination.payload) {
            RudderMessage message = transformedEvent.event;
            boolean onTransformationError = !transformedEvent.status.equals("200");
            if (onTransformationError) {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("RudderDeviceModeManager: dumpTransformedEvents: ");
                if (transformedEvent.status.equals("410")) {
                    errorMsg.append("The requested transformation is not available on the destination or there is a configuration issue. ");
                } else {
                    // For all other status codes
                    errorMsg.append("There is a transformation error. ");
                }
                // If their is a transformation error then response payload will not contain the original event. So we need to get the original event based on messageId/orderNo.
                message = this.deviceModeTransformationManager.getEventFromMessageId(transformedEvent.orderNo);
                if (destinationsExcludedOnTransformationError.contains(destinationName)) {
                    errorMsg.append(destinationName).append(" is excluded from accepting event ").append(message.getEventName()).append(" on transformation error. Hence dropping this event.");
                    RudderLogger.logWarn(errorMsg.toString());
                    continue;
                } else {
                    errorMsg.append("Sending the untransformed event ").append(message.getEventName());
                    RudderLogger.logWarn(errorMsg.toString());
                }
            } else if (message == null) {
                // If there is no transformation error and message is null then it means that the event is dropped in the transformation.
                RudderLogger.logDebug(String.format(Locale.US, "RudderDeviceModeManager: dumpTransformedEvents: event is dropped in the transformation for %s", destinationName));
                continue;
            }
            dumpEventToDestinations(message, Collections.singletonList(destinationName), "dumpTransformedEvents");
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

    List<String> getTransformationEnabledDestinationIds(RudderMessage message) {
        List<String> eligibleDestinations = getEligibleDestinations(message);
        List<String> destinationIdsWithTransformationEnabled = new ArrayList<>();
        for (String eligibleDestination : eligibleDestinations) {
            if (destinationsWithTransformationsEnabled.containsKey(eligibleDestination)) {
                destinationIdsWithTransformationEnabled.add(destinationsWithTransformationsEnabled.get(eligibleDestination));
            }
        }
        return destinationIdsWithTransformationEnabled;
    }

    private List<String> getDestinationsWithTransformationStatus(TRANSFORMATION_STATUS transformationStatus, List<String> inputDestinations) {
        List<String> outputDestinations = new ArrayList<>();
        for (String inputDestination : inputDestinations) {
            boolean isDestinationWithTransformation = destinationsWithTransformationsEnabled.containsKey(inputDestination);
            if (isDestinationWithTransformation == transformationStatus.status) {
                outputDestinations.add(inputDestination);
            }
        }
        return outputDestinations;
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
        if (destinationsWithTransformationsEnabled.isEmpty())
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
