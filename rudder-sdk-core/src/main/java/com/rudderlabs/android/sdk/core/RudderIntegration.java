package com.rudderlabs.android.sdk.core;

public abstract class RudderIntegration<T> {
    public interface Factory {
        RudderIntegration<?> create(Object settings, RudderClient client);

        String key();
    }

    public void identify(RudderElement identify) {
    }

    public void group(RudderElement group) {
    }

    public void track(RudderElement track) {
    }

    public void alias(RudderElement alias) {
    }

    public void screen(RudderElement screen) {
    }

    public void flush() {
    }

    public void reset() {
    }

    public void dump(RudderElement element) {

    }

    public T getUnderlyingInstance() {
        return null;
    }
}
