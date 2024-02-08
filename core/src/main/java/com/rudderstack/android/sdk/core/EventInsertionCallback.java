package com.rudderstack.android.sdk.core;

interface Callback {
    void onInsertion(Integer rowId);
}

class EventInsertionCallback implements Callback {
    private final RudderMessage message;
    private final RudderDeviceModeManager deviceModeManager;

    public EventInsertionCallback(RudderMessage message, RudderDeviceModeManager deviceModeManager) {
        this.message = message;
        this.deviceModeManager = deviceModeManager;
    }

    @Override
    public void onInsertion(Integer rowId) {
        deviceModeManager.processMessage(message, rowId, false);
    }
}
