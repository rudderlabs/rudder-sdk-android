package com.rudderstack.android.sdk.core;

/*
 * Default value holder class
 * */
class Constants {
    // how often config should be fetched from the server (in hours) (2 hrs by default)
    static final int CONFIG_REFRESH_INTERVAL = 2; // probably change it to 1 hr, or every time
    // default base url or rudder-backend-server
    static final String DATA_PLANE_URL = "https://hosted.rudderlabs.com";
    // default flush queue size for the events to be flushed to server
    static final int FLUSH_QUEUE_SIZE = 30;
    // default threshold of number of events to be persisted in sqlite db
    static final int DB_COUNT_THRESHOLD = 10000;
    // default timeout for event flush
    // if events are registered and flushQueueSize is not reached
    // events will be flushed to server after sleepTimeOut seconds
    static final int SLEEP_TIMEOUT = 10;
    // config-plane url to get the config for the writeKey
    static final String CONTROL_PLANE_URL = "https://api.rudderlabs.com";
    // whether we should trackLifecycle events
    static final boolean TRACK_LIFECYCLE_EVENTS = true;
    // whether we should record screen views automatically
    static final boolean RECORD_SCREEN_VIEWS = false;
    // current version of the library
    static final String RUDDER_LIBRARY_VERSION = "1.2.0";
}
