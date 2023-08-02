package com.rudderstack.android.sdk.core;

import java.io.Serializable;

public class SourceConfiguration implements Serializable {
    public SourceConfiguration(StatsCollection statsCollection) {
        this.statsCollection = statsCollection;
    }

    private StatsCollection statsCollection;

    public StatsCollection getStatsCollection() {
        return statsCollection;
    }

    static class StatsCollection implements Serializable {
        public StatsCollection(Errors errors, Metrics metrics) {
            this.errors = errors;
            this.metrics = metrics;
        }

        private Errors errors;
        private Metrics metrics;

        public Errors getErrors() {
            return errors;
        }

        public Metrics getMetrics() {
            return metrics;
        }
    }

    static class Errors implements Serializable {
        public Errors(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }
    }

    static class Metrics implements Serializable {
        public Metrics(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }
    }
}
