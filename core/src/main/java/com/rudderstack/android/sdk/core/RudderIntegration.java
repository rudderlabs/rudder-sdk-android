package com.rudderstack.android.sdk.core;

public abstract class RudderIntegration<T> {
    public interface Factory {
        RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig config);

        String key();
    }

    public abstract void reset();

    public abstract void dump(RudderMessage element);

    public void flush() {
    }

    /**
     * @return Instance of the initiated SDK
     */
    public T getUnderlyingInstance() {
        return null;
    }
}
