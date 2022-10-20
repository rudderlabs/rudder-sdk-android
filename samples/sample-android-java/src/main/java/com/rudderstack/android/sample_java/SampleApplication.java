/*
 * Creator: Debanjan Chatterjee on 08/10/22, 4:54 PM Last modified: 08/10/22, 4:54 PM
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

package com.rudderstack.android.sample_java;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.compat.RudderAnalyticsBuilderCompat;
import com.rudderstack.core.Analytics;
import com.rudderstack.core.BaseDestinationPlugin;
import com.rudderstack.core.Callback;
import com.rudderstack.core.Settings;
import com.rudderstack.core.compat.BaseDestinationPluginCompat;
import com.rudderstack.core.compat.PluginCompat;
import com.rudderstack.models.Message;
import com.rudderstack.moshirudderadapter.MoshiAdapter;

import kotlin.Unit;

public class SampleApplication extends Application {
    private static Analytics analytics;

    public static Analytics getAnalytics() {
        if(analytics == null){
            throw new NullPointerException("Application not initialized yet");
        }
        return analytics;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = new RudderAnalyticsBuilderCompat(this, "", new Settings(), new MoshiAdapter())
                .withControlPlaneUrl("https:://www.cp.com")
                .withDataPlaneUrl("https://hosted.rudderlaps.com")
                .build();
        analytics.addPlugin(new PluginCompat() {
            @NonNull
            @Override
            public Message intercept(@NonNull Chain chain) {
                return chain.proceed(chain.message());
            }
        });
        BaseDestinationPlugin<Object> bdp= new BaseDestinationPluginCompat<Object>("") {
            @NonNull
            @Override
            public Message intercept(@NonNull Chain chain) {
                return super.intercept(chain);
            }
        };
        bdp.addSubPlugin(new BaseDestinationPluginCompat.PluginInterceptorCompat(){
            @NonNull
            @Override
            public Message intercept(@NonNull Chain chain) {

                return super.intercept(chain);
            }
        });
        analytics.addCallback(new Callback() {
            @Override
            public void success(@Nullable Message message) {

            }

            @Override
            public void failure(@Nullable Message message, @Nullable Throwable throwable) {

            }
        });
        analytics.applyClosure(plugin -> {
            if(plugin instanceof BaseDestinationPlugin){
                ((BaseDestinationPlugin<?>) plugin).setReady(true, null);
            }
            return Unit.INSTANCE;
        });
        analytics.addPlugin();
    }
}
