package com.rudderstack.android.sdk.core.consent;

import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderServerConfigSource;

@FunctionalInterface
public interface ConsentInterceptor {
    RudderMessage intercept(RudderServerConfigSource rudderServerConfigSource, RudderMessage rudderMessage);
}
