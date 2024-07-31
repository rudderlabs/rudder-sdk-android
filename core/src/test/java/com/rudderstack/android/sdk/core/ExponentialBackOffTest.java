package com.rudderstack.android.sdk.core;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.rudderstack.android.sdk.core.util.ExponentialBackOff;

public class ExponentialBackOffTest {

    private ExponentialBackOff backOff;

    @Before
    public void setUp() {
        backOff = new ExponentialBackOff(5 * 60) {
            @Override
            protected long withJitter(long delayInSecs) {
                // Return a custom value for testing
                long jitter = 1; // 1 sec
                return delayInSecs + jitter;
            }
        };
    }

    @Test
    public void testExponentialDelay() {
        // Check delays for the first few attempts
        long delay1 = backOff.nextDelayInMillis();
        long delay2 = backOff.nextDelayInMillis();
        long delay3 = backOff.nextDelayInMillis();

        assertEquals((3 + 1) * 1000, delay1);       // Expected delay: (initialDelayInSecs * Math.pow(base, attempt)) * 1000
        assertEquals(((3 * 2) + 1) * 1000, delay2); // Expected delay: (initialDelayInSecs * Math.pow(base, attempt)) * 1000
        assertEquals(((3 * 4) + 1) * 1000, delay3); // Expected delay: (initialDelayInSecs * Math.pow(base, attempt)) * 1000
    }

    @Test
    public void testResetBackOff() {
        // Advance to exceed the max delay - 1
        for (int i = 1; i <= 7; i++) {
            backOff.nextDelayInMillis(); // At the 7th attempt, the delay will be 3 * 2^6 + 1 = 193 sec
        }

        long delayAfterReset = backOff.nextDelayInMillis(); // Should reset backoff
        assertEquals(5 * 60 * 1000, delayAfterReset); // Expected delay: (initialDelayInSecs * Math.pow(base, attempt)) * 1000

        long delayAfterReset2 = backOff.nextDelayInMillis(); // Should start from initial delay
        assertEquals((3 + 1) * 1000, delayAfterReset2); // Expected delay: (initialDelayInSecs * Math.pow(base, attempt)) * 1000
    }
}
