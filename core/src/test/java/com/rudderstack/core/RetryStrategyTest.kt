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
        Awaitility.await().untilAtomic(counter, equalTo(3))
        verify(listener).invoke(eq(false))
    }

    @Test
    fun `test exponential retry strategy`(){
        val isComplete = AtomicBoolean(false)

        val successOn = 3 //success on 3rd try
        var retryCount = 0
        val startedAt = System.currentTimeMillis()
        val expectedTime = 1000L /*first*/ + 2000L /*second*/ + 4000L /*third*/
        RetryStrategy.exponential().perform({
            retryCount ++ == successOn
        }, {
            assertThat(it, Matchers.`is`(true))
            assertThat(retryCount, Matchers.`is`(4))
            //on 3rd retry, probable 8 seconds


            isComplete.set(true)
        })
        //50 milliseconds, max buffer for processing
        Awaitility.await().atLeast(expectedTime, TimeUnit.MILLISECONDS).atMost((expectedTime + 200L),
            TimeUnit.MILLISECONDS).untilTrue(isComplete)
    }
    // Test ExponentialRetryStrategy with Limited Attempts
    @Test
    fun `exponential strategy succeeds before reaching max attempts`() {
        val counter = AtomicInteger(0)
        val work: () -> Boolean = { counter.incrementAndGet() == 2 }
        val listener = mock<(Boolean) -> Unit>()
        val strategy = RetryStrategy.exponential(maxAttempts = 5)
        strategy.perform(work, listener)
        Awaitility.await().untilAtomic(counter, equalTo(2))

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
        while (counter.get() < 2){
        }
        job.cancel()
        assertThat(counter.get(), Matchers.lessThanOrEqualTo(3))
//        Awaitility.await().untilAtomic(counter, equalTo(2))

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
        while (!job.isDone()){
        }
        Thread.sleep(500L) // to be sure the job is done
        assertThat(counter.get(), Matchers.equalTo(1))
        verify(listener).invoke(eq(true))
    }


}