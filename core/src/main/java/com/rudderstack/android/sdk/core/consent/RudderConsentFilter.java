package com.rudderstack.android.sdk.core.consent;

import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderServerConfigSource;
import com.rudderstack.android.sdk.core.RudderServerDestination;

import java.util.List;
import java.util.Map;

@FunctionalInterface
@Deprecated
/**
 * @deprecated
 * @see RudderConsentFilterWithCloudIntegration
  */
public interface RudderConsentFilter {
    Map<String, Boolean> filterConsentedDestinations(List<RudderServerDestination> destinationList);
}
