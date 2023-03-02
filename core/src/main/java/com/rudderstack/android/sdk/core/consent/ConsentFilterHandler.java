package com.rudderstack.android.sdk.core.consent;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.RudderContext;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderServerConfigSource;
import com.rudderstack.android.sdk.core.RudderServerDestination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ConsentFilterHandler {

    private final RudderServerConfigSource serverConfigSource;
    private final Map<String, Boolean> consentedIntegrationsMap;
    @Nullable
    private final List<String> deniedConsentIds;

    public ConsentFilterHandler(RudderServerConfigSource serverConfigSource,
                                RudderConsentFilter consentFilter) {
        this.serverConfigSource = serverConfigSource;
        consentedIntegrationsMap = updatedConsentedIntegrationsMap(consentFilter);
        deniedConsentIds = filterDeniedConsentIdsFromConsentFilter(consentFilter);
    }

    private @Nullable
    List<String> filterDeniedConsentIdsFromConsentFilter(RudderConsentFilter consentFilter) {
        if(!(consentFilter instanceof RudderConsentFilterWithCloudIntegration)) {
            RudderLogger.logWarn("Update Rudder Onetrust Consent filter for Cloud mode filtering");
            return null;
        }
        Map<String, Boolean> categoryIdToConsentMap = ((RudderConsentFilterWithCloudIntegration) consentFilter)
                .getConsentCategoriesMap();
        return filterDeniedCategoryIdsFromCategoryMap(categoryIdToConsentMap);
    }

    private List<String> filterDeniedCategoryIdsFromCategoryMap(Map<String, Boolean> categoryIdToConsentMap) {
        List<String> deniedCategoryIdList = new ArrayList<>();
        for (Map.Entry<String,Boolean> categoryMapEntry: categoryIdToConsentMap.entrySet()) {
            if (!categoryMapEntry.getValue()){
                deniedCategoryIdList.add(categoryMapEntry.getKey());
            }
        }
        return deniedCategoryIdList;
    }

    private Map<String, Boolean> updatedConsentedIntegrationsMap(RudderConsentFilter consentFilter) {
        List<RudderServerDestination> allDestinations = serverConfigSource.getDestinations();
        if(null == allDestinations || allDestinations.isEmpty())
            return Collections.emptyMap();
        return consentFilter.filterConsentedDestinations(allDestinations);
    }

    public RudderMessage applyConsent(RudderMessage message){
        applyCloudModeFilteredConsents(message);
        return message;
    }

    private void applyCloudModeFilteredConsents(RudderMessage message) {
        if(deniedConsentIds == null || deniedConsentIds.isEmpty())
            return;
        RudderContext messageContext = message.getContext();
        if(messageContext == null)
            return;
        messageContext.setConsentManagement(new RudderContext.ConsentManagement(deniedConsentIds));
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
