/*
 * Creator: Debanjan Chatterjee on 23/01/22, 11:37 PM Last modified: 23/01/22, 11:37 PM
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

import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * A retry strategy mechanism.
 * Users can either use statically provided exponential strategy or create a strategy of their own
 *
 */
fun interface RetryStrategy {
//    val executorService: ExecutorService
//    val work : ()-> Boolean

    companion object {
        /**
         * An utility function to get [ExponentialRetryStrategy].
         *
         * @param maxAttempts
         * @return [ExponentialRetryStrategy]
         */
        @JvmStatic
        @JvmOverloads
        fun exponential(
            maxAttempts: Int = 5
        ) = ExponentialRetryStrategy(maxAttempts)

    }

    fun perform(work: () -> Boolean,listener: (success: Boolean) -> Unit)

    /**
     * A retry strategy that increases the waiting time by exponents of 2
     * The waiting time increases as (2^n),i.e 1,2,4,8....
     *
     * @property maxAttempts The max number of attempts to make. It will occur at 2^nth second after
     * (n-1)th attempt
     */
    class ExponentialRetryStrategy internal constructor(
        private val maxAttempts: Int,
    ) : RetryStrategy {
        private var retryCount = AtomicInteger(0)
        private var lastWaitTime =  AtomicLong(0L)
        override fun perform(
            work: () -> Boolean,
            listener: (success: Boolean) -> Unit
        ) {
            check(maxAttempts >= 0){
                "Max attempts needs to be at least 1"
            }
            check(retryCount.get() < 1){
                "perform() can be called only once. Create another instance for a new job."
            }
            val executorService = ScheduledThreadPoolExecutor(2)

            fun scheduleWork() {
                executorService.schedule({
                    lastWaitTime.set((1 shl retryCount.getAndIncrement()) * 1000L) // 2 to the power of retry count

                    val workDone = work.invoke()
                    if (workDone) {
                        listener.invoke(true)
                        executorService.shutdown()
                        return@schedule
                    } else {
                        if(retryCount.get() > maxAttempts){
                            listener.invoke(false)
                            executorService.shutdown()
                            return@schedule
                        }
                        scheduleWork()
                    }
                }, lastWaitTime.get(), TimeUnit.MILLISECONDS)
            }
            scheduleWork()
        }
    }
}