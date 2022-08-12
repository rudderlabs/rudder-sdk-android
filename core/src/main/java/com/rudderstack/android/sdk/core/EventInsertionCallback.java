package com.rudderstack.android.sdk.core;

interface Callback {
    void onInsertion(Integer rowId);
}

class EventInsertionCallback implements Callback {
    RudderMessage message;
    RudderDeviceModeManager deviceModeManager;

    public EventInsertionCallback(RudderMessage message, RudderDeviceModeManager deviceModeManager) {
        this.message = message;
        this.deviceModeManager = deviceModeManager;
    }

    @Override
    public void onInsertion(Integer rowId) {
        deviceModeManager.makeFactoryDump(message, rowId, false);
    }
}
