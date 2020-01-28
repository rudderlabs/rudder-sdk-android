package com.rudderstack.android.sdk.core;

import android.app.Application;

class MetricsStatsManager {
    private static MetricsStatsManager instance;
    private DBPersistentManager dbPersistentManager;
    private RudderPreferenceManager preferenceManager;
    private boolean isEnabled;

    private MetricsStatsManager(Application application) {
        this.dbPersistentManager = DBPersistentManager.getInstance(application);
        this.preferenceManager = RudderPreferenceManager.getInstance(application);
    }

    static MetricsStatsManager getInstance(Application application) {
        if (instance == null) {
            instance = new MetricsStatsManager(application);
        }
        return instance;
    }


}
