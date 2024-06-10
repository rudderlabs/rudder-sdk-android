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

import java.lang.ref.WeakReference
import java.util.concurrent.Future
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * A retry strategy mechanism.
 * Users can either use statically provided exponential strategy or create a strategy of their own
 *
 */
fun interface RetryStrategy {

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

    fun Analytics.perform(work: Analytics.() -> Boolean, listener: (success: Boolean) -> Unit):
            CancellableJob
    interface CancellableJob {
        /**
         * Cancels the job
         * By norm, it should cancel the job and invoke the listener with false
         *
         */
        fun cancel()

        fun isDone(): Boolean
    }

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

        override fun Analytics.perform(
            work: Analytics.() -> Boolean, listener: (success: Boolean) -> Unit
        ): CancellableJob {
            val impl = WeakReference(ExponentialRetryImpl(this, maxAttempts,work, listener))
            impl.get()?.start()
            return object : CancellableJob {
                override fun cancel() {
                    impl.get()?.cancel()
                    listener(false)
                }

                override fun isDone(): Boolean {
                    return impl.get()?.isDone == true
                }
            }
        }


        inner class ExponentialRetryImpl internal constructor(private val analytics: Analytics,
            private val maxAttempts: Int, private val work: Analytics.() -> Boolean, private val
                listener: (
                success: Boolean
            ) -> Unit
        ) {
            private var retryCount = AtomicInteger(0)
            private var lastWaitTime = AtomicLong(0L)
            private val isRunning = AtomicBoolean(false)
            private val executorService = ScheduledThreadPoolExecutor(0)
            private var lastFuture: WeakReference<Future<*>>? = null
            private val _isDone = AtomicBoolean(false)
            private val logger
                get() = analytics.currentConfiguration?.rudderLogger
            val isDone: Boolean
                get() = _isDone.get()
            fun start() {
                if (isDone) {
                    logger?.warn(
                        "ExponentialRetryStrategy:", "RetryStrategyImpl is already shutdown"
                    )
                    return
                }
                if (!isRunning.compareAndSet(false, true)) {
                    logger?.warn(
                        "ExponentialRetryStrategy:", "RetryStrategyImpl is already running"
                    )
                    return
                }
                check(maxAttempts >= 0) {
                    "Max attempts needs to be at least 1"
                }
                check(retryCount.get() < 1) {
                    "perform() can be called only once. Create another instance for a new job."
                }
                scheduleWork()
            }

            fun cancel() {
                lastFuture?.get()?.takeIf { !it.isCancelled && !it.isDone }?.cancel(false)
                executorService.shutdown()
                _isDone.set(true)
            }

            private fun scheduleWork() {
                executorService.schedule({
                    lastWaitTime.set((1 shl retryCount.getAndIncrement()) * 1000L) // 2 to the power of retry count

                    val workDone = work.invoke(analytics)
                    if (workDone) {
                        listener.invoke(true)
                        cancel()
                        return@schedule
                    } else {
                        if (retryCount.get() >= maxAttempts) {
                            listener.invoke(false)
                            cancel()
                            return@schedule
                        }
                        scheduleWork()
                    }
                }, lastWaitTime.get(), TimeUnit.MILLISECONDS).also {
                    lastFuture = WeakReference(it)
                }
            }

        }
    }
}
