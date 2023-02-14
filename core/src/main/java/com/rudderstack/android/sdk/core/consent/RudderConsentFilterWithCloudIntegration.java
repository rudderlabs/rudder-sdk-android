package com.rudderstack.android.sdk.core.consent;

import com.rudderstack.android.sdk.core.RudderContext;

import java.util.Map;

public interface RudderConsentFilterWithCloudIntegration extends RudderConsentFilter {
    Map<String, Boolean> getConsentCategoriesMap();
}
