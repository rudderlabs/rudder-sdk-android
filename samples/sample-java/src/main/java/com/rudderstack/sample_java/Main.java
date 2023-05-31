/*
 * Creator: Debanjan Chatterjee on 13/10/22, 12:13 PM Last modified: 13/10/22, 12:04 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

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