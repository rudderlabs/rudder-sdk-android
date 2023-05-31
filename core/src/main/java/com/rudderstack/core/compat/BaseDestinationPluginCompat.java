/*
 * Creator: Debanjan Chatterjee on 10/10/22, 10:11 PM Last modified: 10/10/22, 10:11 PM
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

package com.rudderstack.core.compat;

import com.rudderstack.core.Analytics;
import com.rudderstack.core.BaseDestinationPlugin;
import com.rudderstack.core.Settings;
import com.rudderstack.models.Message;
import com.rudderstack.models.RudderServerConfig;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import org.jetbrains.annotations.NotNull;

public abstract class BaseDestinationPluginCompat<T> extends BaseDestinationPlugin<T> {
    protected BaseDestinationPluginCompat(@NotNull String name) {
        super(name);
    }

    @NotNull
    @Override
    public Message intercept(@NotNull Chain chain) {
        return chain.proceed(chain.message());
    }

    @Override
    public void setup(@NotNull Analytics analytics) {
        super.setup(analytics);
    }

    @Override
    public void updateSettings(@NotNull Settings settings) {
        super.updateSettings(settings);
    }

    @Override
    public void updateRudderServerConfig(@NotNull RudderServerConfig config) {
        super.updateRudderServerConfig(config);
    }

    @Override
    public void onShutDown() {
        super.onShutDown();
    }

    @Override
    public void reset() {
        super.reset();
    }

    @NotNull
    @Override
    public JsonAdapter getJsonAdapter(@NotNull Analytics analytics) {
        return super.getJsonAdapter(analytics);

    }


    public static class DestinationInterceptorCompat implements DestinationInterceptor {

        @NotNull
        @Override
        public Message intercept(@NotNull Chain chain) {
            return chain.proceed(chain.message());
        }

        @Override
        public void setup(@NotNull Analytics analytics) {
        }

        @Override
        public void updateSettings(@NotNull Settings settings) {
        }

        @Override
        public void updateRudderServerConfig(@NotNull RudderServerConfig config) {
        }

        @Override
        public void onShutDown() {
        }

        @Override
        public void reset() {
        }

        @NotNull
        @Override
        public JsonAdapter getJsonAdapter(@NotNull Analytics analytics) {
            return analytics.getJsonAdapter();
        }
    }
}
