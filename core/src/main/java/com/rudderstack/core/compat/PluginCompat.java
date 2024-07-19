package com.rudderstack.core.compat;

import com.rudderstack.core.Analytics;
import com.rudderstack.core.Configuration;
import com.rudderstack.core.Plugin;
import com.rudderstack.core.models.Message;
import com.rudderstack.core.models.RudderServerConfig;

import org.jetbrains.annotations.NotNull;

public abstract class PluginCompat implements Plugin {
    @NotNull
    @Override
    public Message intercept(@NotNull Chain chain){
        return chain.proceed(chain.message());
    }

    @Override
    public void setup(@NotNull Analytics analytics) {
    }

    @Override
    public void updateConfiguration(@NotNull Configuration configuration) {
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

}
