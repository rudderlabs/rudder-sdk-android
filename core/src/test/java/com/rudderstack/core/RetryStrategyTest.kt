/*
 * Creator: Debanjan Chatterjee on 04/04/22, 5:10 PM Last modified: 04/04/22, 5:10 PM
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

package com.rudderstack.core

import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class RetryStrategyTest {

    @Test
    fun `exponential strategy succeeds on first attempt`() {
        val work: () -> Boolean = { true }
        val listener = mock<(Boolean) -> Unit>()
        val strategy = RetryStrategy.exponential(maxAttempts = 5)
        strategy.perform(work, listener)
        busyWait(100)
        verify(listener).invoke(eq(true))
    }
    // Test ExponentialRetryStrategy with a Failing Work
    @Test
    fun `exponential strategy fails when work always fails`() {
        val counter = AtomicInteger(0)
        val work: () -> Boolean = {
            // to simulate a failing work
            counter.getAndIncrement() == Int.MAX_VALUE // value is always false
        }
        val listener = mock<(Boolean) -> Unit>()
        val strategy = RetryStrategy.exponential(maxAttempts = 3)

        strategy.perform(work, listener)
        Awaitility.await().atMost(4500, TimeUnit.MILLISECONDS).untilAtomic (counter, equalTo(3))
        verify(listener).invoke(eq(false))
    }

    @Test
    fun `test exponential retry strategy success on nth attempt`(){
        val successOn = 3 //success on 3rd try
        val retryCount = AtomicInteger(0)
        val expectedTimeToCall = 1000L /*first*/ + 2000L /*second*/ + 4000L /*third*/
        val listener = mock<(Boolean) -> Unit>()
        RetryStrategy.exponential().perform({
            retryCount.incrementAndGet() == successOn
        }, listener)
        busyWait(expectedTimeToCall + 100)
        verify(listener).invoke(eq(true))
        assertThat(retryCount.get(), equalTo(successOn))

    }
    // Test ExponentialRetryStrategy with Limited Attempts
    @Test
    fun `exponential strategy succeeds before reaching max attempts`() {
        val counter = AtomicInteger(0)
        val work: () -> Boolean = { counter.incrementAndGet() == 2 }
        val listener = mock<(Boolean) -> Unit>()
        val strategy = RetryStrategy.exponential(maxAttempts = 5)
        strategy.perform(work, listener)
        Awaitility.await().atMost(3500, TimeUnit.MILLISECONDS).untilAtomic (counter, equalTo(2))

        verify(listener).invoke(eq(true))
    }
    @Test
    fun `once canceled there should be no more attempts`() {
        val counter = AtomicInteger(0)
        val work: () -> Boolean = { counter.incrementAndGet()
            false
        }
        val listener = mock<(Boolean) -> Unit>()
        val strategy = RetryStrategy.exponential(maxAttempts = 10)
        val job = strategy.perform(work, listener)
        Awaitility.await().atMost(3500, TimeUnit.MILLISECONDS).untilAtomic (counter, equalTo(2))
        job.cancel()
        busyWait(4000)
        assertThat(counter.get(), Matchers.lessThanOrEqualTo(3))
        verify(listener).invoke(eq(false))
    }
    // Test ExponentialRetryStrategy with Maximum Attempts Reached

    @Test
    fun `once success there should be no more attempts`() {
        val counter = AtomicInteger(0)
        val work: () -> Boolean = { counter.incrementAndGet()
            true
        }
        val listener = mock<(Boolean) -> Unit>()
        val strategy = RetryStrategy.exponential(maxAttempts = 10)
        val job = strategy.perform(work, listener)
        busyWait(500)
        assertThat(job.isDone(), equalTo(true))
//        while (!job.isDone()){
//        }
        assertThat(counter.get(), Matchers.equalTo(1))
        busyWait(1500) // if it is not done by now, it will never be
        verify(listener, times(1)).invoke(eq(true))
    }


}