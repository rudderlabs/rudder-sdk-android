package com.rudderstack.sample_java;

import com.rudderstack.core.Analytics;
import com.rudderstack.core.RudderOptions;
import com.rudderstack.core.Settings;
import com.rudderstack.core.compat.AnalyticsBuilderCompat;
import com.rudderstack.moshirudderadapter.MoshiAdapter;

import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        Analytics analytics = new AnalyticsBuilderCompat("wk", new Settings(100, 10000),
                new MoshiAdapter())
                .withDataPlaneUrl("")
                .withControlPlaneUrl("")
                .build();
        analytics.identify("some_user_id", Collections.singletonMap("trait_1", "trait"));
        analytics.track("test_ track", new RudderOptions.Builder()
                .withIntegrations(Collections.singletonMap("firebase", false))
                .build());
        analytics.alias("new_user_id");
        analytics.screen("screen_name", "dummy_cat", Collections.emptyMap());
        analytics.group("our_group", null, Collections.singletonMap("trait", "my_trait"));
        analytics.blockingFlush();
    }
}
