package com.rudderstack.android.sdk.core.consent;

import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderMessage;

@FunctionalInterface
public interface Interceptor {
    void intercept(RudderConfig config, RudderMessage rudderMessage);
}
