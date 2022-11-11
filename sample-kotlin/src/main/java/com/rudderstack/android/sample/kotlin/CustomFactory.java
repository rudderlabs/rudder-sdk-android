package com.rudderstack.android.sample.kotlin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;

public class CustomFactory extends RudderIntegration<CustomFactory> {
    private static final String FACTORY_KEY = "Custom Factory";

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new CustomFactory(client,rudderConfig);
        }

        @Override
        public String key() {
            return FACTORY_KEY;
        }
    };

    private CustomFactory(@NonNull RudderClient client, RudderConfig config) {
        RudderLogger.logDebug("CustomFactory: Initializing the Custom Factory");
    }

    private void processRudderEvent(RudderMessage element) {
        RudderLogger.logDebug("CustomFactory: Processing RudderEvent of type "+element.getType());

    }

    @Override
    public void reset() {
        RudderLogger.logDebug("CustomFactory: Reset being called");
    }

    @Override
    public void dump(@Nullable RudderMessage element) {
        try {
            if (element != null) {
                processRudderEvent(element);
            }
        } catch (Exception e) {
            RudderLogger.logError(e);
        }
    }

    @Override
    public CustomFactory getUnderlyingInstance() {
        return this;
    }
}