/*
 * Creator Debanjan Chatterjee on 10/10/22, 528 PM Last modified 10/10/22, 528 PM
 * Copyright All rights reserved Ⓒ 2022 http//rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http//www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.compat;

import com.rudderstack.android.ConfigurationAndroid;
import com.rudderstack.android.storage.AndroidStorage;
import com.rudderstack.android.storage.AndroidStorageImpl;
import com.rudderstack.core.Logger;
import com.rudderstack.core.Storage;
import com.rudderstack.core.compat.AnalyticsBuilderCompat;

import java.util.concurrent.Executors;

/**
 * To be used by java projects
 */
public final class RudderAnalyticsBuilderCompat extends AnalyticsBuilderCompat {
    private Logger logger;
    public RudderAnalyticsBuilderCompat(String writeKey, ConfigurationAndroid configuration) {
        super(writeKey, configuration);
        configuration.getLogger();
        logger = configuration.getLogger();
        withStorage( new AndroidStorageImpl(configuration.getApplication(),
                ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER, Executors.newSingleThreadExecutor()));
    }

    @Override
    public AnalyticsBuilderCompat withStorage(Storage storage) {
        if (storage instanceof AndroidStorage) {
            return super.withStorage(storage);
        }else {
            logger.error(Logger.DEFAULT_TAG, "Storage should be of type AndroidStorage. Using " +
                    "default storage", null);
        }
        return this;
    }

}
