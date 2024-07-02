package com.rudderstack.core.compat;

import com.rudderstack.core.Analytics;
import com.rudderstack.core.BaseDestinationPlugin;
import com.rudderstack.core.Configuration;
import com.rudderstack.core.models.Message;
import com.rudderstack.core.models.RudderServerConfig;

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
    public void updateConfiguration(@NotNull Configuration configuration) {
        super.updateConfiguration(configuration);
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
}
