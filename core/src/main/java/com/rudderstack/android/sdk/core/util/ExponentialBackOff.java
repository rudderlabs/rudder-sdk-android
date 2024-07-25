package com.rudderstack.android.sdk.core.util;

import androidx.annotation.VisibleForTesting;

import java.security.SecureRandom;

/**
 * This class implements an exponential backoff strategy with jitter for handling retries.
 * It allows for configurable maximum delay and includes methods to calculate the next delay
 * with jitter and reset the backoff attempts.
 * When the calculated delay reaches or exceeds the maximum delay limit, the backoff resets
 * and starts again from beginning.
 */
public class ExponentialBackOff {
    private int attempt = 0;
    private final int maxDelayInSecs;
    private final SecureRandom random;

    /**
     * Constructor to initialize the ExponentialBackOff with a maximum delay.
     *
     * @param maxDelayInSecs Maximum delay in seconds for the backoff.
     */
    public ExponentialBackOff(int maxDelayInSecs) {
        this.maxDelayInSecs = maxDelayInSecs;
        this.random = new SecureRandom();
    }

    /**
     * Calculates the next delay with exponential backoff and jitter.
     *
     * @return The next delay in milliseconds.
     */
    public long nextDelayInMillis() {
        int base = 2;
        int initialDelayInSecs = 3;
        long delayInSecs = (long) (initialDelayInSecs * Math.pow(base, attempt++));
        long exponentialIntervalInSecs = Math.min(maxDelayInSecs, withJitter(delayInSecs));

        // Reset the backoff if the delay reaches or exceeds the maximum limit
        if (exponentialIntervalInSecs >= maxDelayInSecs) {
            resetBackOff();
        }

        return exponentialIntervalInSecs * 1000;
    }

    @VisibleForTesting
    protected long withJitter(long delayInSecs) {
        long jitter = random.nextInt((int) delayInSecs);
        return delayInSecs + jitter;
    }

    /**
     * Resets the backoff attempt counter to 0.
     */
    public void resetBackOff() {
        attempt = 0;
    }
}
