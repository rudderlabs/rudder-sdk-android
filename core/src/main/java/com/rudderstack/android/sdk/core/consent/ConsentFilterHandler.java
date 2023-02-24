package com.rudderstack.android.sdk.core.consent;

import androidx.annotation.NonNull;

import com.rudderstack.android.sdk.core.RudderContext;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderMessageBuilder;
import com.rudderstack.android.sdk.core.RudderOption;
import com.rudderstack.android.sdk.core.RudderServerConfigSource;
import com.rudderstack.android.sdk.core.RudderServerDestination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConsentFilterHandler {

    private final RudderServerConfigSource serverConfigSource;
    private final Map<String, Boolean> consentedIntegrationsMap;

    public ConsentFilterHandler(RudderServerConfigSource serverConfigSource,
                                RudderConsentFilter consentFilter) {
        this.serverConfigSource = serverConfigSource;
        consentedIntegrationsMap = updatedConsentedIntegrationsMap(consentFilter);
    }

    private Map<String, Boolean> updatedConsentedIntegrationsMap(RudderConsentFilter consentFilter) {
        List<RudderServerDestination> allDestinations = serverConfigSource.getDestinations();
        if(null == allDestinations || allDestinations.isEmpty())
            return Collections.emptyMap();
        return consentFilter.filterConsentedDestinations(allDestinations);
    }

    public RudderMessage applyConsent(RudderMessage message){
        if (consentedIntegrationsMap.isEmpty())
            return message;
        Map<String, Object> messageIntegrationsWithConsentMap =
                createUpdatedMessageIntegrationsMapWithConsentedDestinations(message.getIntegrations());

        return buildUpdatedMessageWithFilteredDestinations(message, messageIntegrationsWithConsentMap);
    }
    private RudderMessage buildUpdatedMessageWithFilteredDestinations(RudderMessage oldMessage,
                                                                      Map<String, Object> messageIntegrationsWithConsentUpdatedMap) {
        RudderOption newOptions = mimicRudderOptionFromMessageWithConsentedDestinations(oldMessage,
                messageIntegrationsWithConsentUpdatedMap);
        return RudderMessageBuilder.from(oldMessage)
                .setRudderOption(newOptions)
                .build();
    }

    private RudderOption mimicRudderOptionFromMessageWithConsentedDestinations(RudderMessage oldMessage,
                                                                               Map<String, Object> messageIntegrationsWithConsentUpdatedMap) {
        RudderOption newOptions = new RudderOption();
        updateRudderOptionWithMessageCustomContexts(newOptions, oldMessage);
        updateRudderOptionWithMessageExternalIds(newOptions, oldMessage);
        updateRudderOptionWithConsentedMessageIntegrations(newOptions, messageIntegrationsWithConsentUpdatedMap);

        return newOptions;
    }
    private void updateRudderOptionWithMessageExternalIds(RudderOption option, RudderMessage message) {
        Map<String, Object> messageExternalIds = getExternalIdsFromMessage(message);
        if (messageExternalIds.isEmpty())
            return;
        updateRudderOptionWithExternalIds(option, messageExternalIds);
    }
    private Map<String, Object> getExternalIdsFromMessage(RudderMessage message) {
        RudderContext context = message.getContext();
        if(context == null)
            return Collections.emptyMap();
        List<Map<String, Object>> externalIdsPairList = context.getExternalIds();
        if (externalIdsPairList == null || externalIdsPairList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> collectedExternalIdsPair = new HashMap<>();
        for (Map<String, Object> externalIdPair : externalIdsPairList) {
            collectedExternalIdsPair.putAll(externalIdPair);
        }
        return collectedExternalIdsPair;
    }
    private void updateRudderOptionWithExternalIds(RudderOption option, Map<String, Object> externalIds) {
        for (Map.Entry<String, Object> externalIdPair : externalIds.entrySet()) {
            Object externalIdValue = externalIdPair.getValue();
            if (externalIdValue instanceof String) {
                option.putExternalId(externalIdPair.getKey(), (String) externalIdPair.getValue());
            }
        }
    }


    private void updateRudderOptionWithMessageCustomContexts(RudderOption option, RudderMessage message) {
        RudderContext rudderContext = message.getContext();
        if(rudderContext == null)
            return;
        Map<String, Object> extractedCustomContexts = rudderContext.customContextMap;
        if (extractedCustomContexts == null || extractedCustomContexts.isEmpty()) {
            return;
        }
        setCustomContextsToOption(option, extractedCustomContexts);
    }

    private void setCustomContextsToOption(RudderOption option, Map<String, Object> customContexts) {
        for (Map.Entry<String, Object> customContextEntry : customContexts.entrySet()) {
            Object customContextValue = customContextEntry.getValue();
            try {
                option.putCustomContext(customContextEntry.getKey(), (Map<String, Object>) customContextValue);
            } catch (Exception e) {
                //ignore
            }
        }
    }

    private void updateRudderOptionWithConsentedMessageIntegrations(RudderOption option,
                                                                    Map<String, Object> messageIntegrationsWithConsentUpdatedMap) {
        if (messageIntegrationsWithConsentUpdatedMap.isEmpty())
            return;

        updateRudderOptionWithNewIntegrations(option, messageIntegrationsWithConsentUpdatedMap);
    }


    private void updateRudderOptionWithNewIntegrations(RudderOption option, Map<String, Object> newIntegrationsMap) {
        for (Map.Entry<String, Object> newIntegrationEntry : newIntegrationsMap.entrySet()) {
            Object integrationEntryValue = newIntegrationEntry.getValue();
            if (integrationEntryValue instanceof Boolean) {
                option.putIntegration(newIntegrationEntry.getKey(), (boolean) integrationEntryValue);
            }
        }
    }
    private @NonNull
    Map<String, Object>
    createUpdatedMessageIntegrationsMapWithConsentedDestinations(Map<String, Object> integrations) {
        Map<String, Object> updatedMessageIntegrationsMapWithConsent = new HashMap<String, Object>(integrations);
        for (Map.Entry<String, Boolean> messageIntegrationEntry: consentedIntegrationsMap.entrySet()) {
            String destinationIdentifier = messageIntegrationEntry.getKey();
            if(! messageIntegrationEntry.getValue()){
                updatedMessageIntegrationsMapWithConsent.put(destinationIdentifier, false);
            }
        }
        return updatedMessageIntegrationsMapWithConsent;
    }
    public List<RudderServerDestination> filterDestinationList(List<RudderServerDestination> destinations){
        if(consentedIntegrationsMap.isEmpty())
            return destinations;
        List<RudderServerDestination> filteredList = new ArrayList<>(destinations);
        for (RudderServerDestination destination: destinations) {
            String destinationKey = destination.getDestinationDefinition().getDisplayName();
            if(consentedIntegrationsMap.containsKey(destinationKey) && !consentedIntegrationsMap.get(destinationKey)){
                filteredList.remove(destination);
            }
        }
        return filteredList;
    }



}
