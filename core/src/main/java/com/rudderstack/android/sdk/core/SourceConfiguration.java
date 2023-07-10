package com.rudderstack.android.sdk.core;

public class SourceConfiguration {
    public SourceConfiguration(StatsCollection statsCollection) {
        this.statsCollection = statsCollection;
    }

    private final StatsCollection statsCollection;

    public StatsCollection getStatsCollection() {
        return statsCollection;
    }

    static class StatsCollection{
        public StatsCollection(Errors errors, Metrics metrics) {
            this.errors = errors;
            this.metrics = metrics;
        }

        private final Errors errors;
        private final Metrics metrics;

        public Errors getErrors() {
            return errors;
        }

        public Metrics getMetrics() {
            return metrics;
        }
    }
    static class Errors{
        public Errors(boolean enabled) {
            this.enabled = enabled;
        }

        private final boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }
    }
    static class Metrics{
        public Metrics(boolean enabled) {
            this.enabled = enabled;
        }

        private final boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }
    }
}
