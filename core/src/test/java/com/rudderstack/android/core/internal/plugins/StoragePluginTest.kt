/*
 * Creator: Debanjan Chatterjee on 10/01/22, 6:02 PM Last modified: 10/01/22, 6:02 PM
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

package com.rudderstack.android.core.internal.plugins

import com.rudderstack.android.core.Settings
import com.rudderstack.android.core.internal.CentralPluginChain
import com.rudderstack.android.core.BasicStorageImpl
import com.rudderstack.android.core.internal.KotlinLogger
import com.rudderstack.android.core.internal.StorageDecorator
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.Message
import com.rudderstack.android.models.TrackMessage
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class StoragePluginTest {

    private val testMessagesList = listOf<Message>(
        TrackMessage.create("m-1", anonymousId = "anon-1", timestamp = "09-01-2022"),
        TrackMessage.create("m-2", anonymousId = "anon-2", timestamp = "09-01-2022"),
        TrackMessage.create("m-3", anonymousId = "anon-3", timestamp = "09-01-2022"),
        TrackMessage.create("m-4", anonymousId = "anon-4", timestamp = "09-01-2022"),
        TrackMessage.create("m-5", anonymousId = "anon-5", timestamp = "09-01-2022"),
        TrackMessage.create("m-6", anonymousId = "anon-6", timestamp = "09-01-2022"),
        TrackMessage.create("m-7", anonymousId = "anon-7", timestamp = "09-01-2022"),
        TrackMessage.create("m-8", anonymousId = "anon-8", timestamp = "09-01-2022"),
        TrackMessage.create("m-9", anonymousId = "anon-9", timestamp = "09-01-2022"),
        TrackMessage.create("m-10", anonymousId = "anon-10", timestamp = "09-01-2022"),
        TrackMessage.create("m-11", anonymousId = "anon-11", timestamp = "09-01-2022"),
    )

    @Test
    fun testStoragePluginWithQueueSize() {
        val settings = Settings(flushQueueSize = 11)
        SettingsState.update(settings)
        val storagePlugin = StoragePlugin(StorageDecorator(
            BasicStorageImpl(logger = KotlinLogger),
        ) {
            assertThat(
                it,
                allOf(
                    iterableWithSize(testMessagesList.size),
                    contains(*testMessagesList.toTypedArray())
                )
            )
        })
        testMessagesList.forEach { msg ->
            CentralPluginChain(msg, listOf(storagePlugin)).proceed(msg)
        }
    }

    @Test
    fun testStoragePluginWithFlushInterval() {
        val isComplete = AtomicBoolean(false)
        val flushInterval = 10 * 1000L
        val settings = Settings(
            flushQueueSize = Integer.MAX_VALUE, // setting it to a large value ensures
            // there is no chance of flushing due to queue size
            maxFlushInterval = flushInterval
        )
        SettingsState.update(settings)
        val storagePluginCreated = System.currentTimeMillis()
        val storagePlugin = StoragePlugin(StorageDecorator(BasicStorageImpl(logger = KotlinLogger)) {

            assertThat(
                (System.currentTimeMillis() - storagePluginCreated), allOf(
                    greaterThanOrEqualTo(flushInterval),
                    lessThanOrEqualTo(flushInterval + 100L)
                )
            ) //100 millis we consider as a max buffer for all calculations
            println("done")
            isComplete.set(true)
        })
        testMessagesList.forEach { msg ->
            CentralPluginChain(msg, listOf(storagePlugin)).proceed(msg)
        }
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isComplete)

    }
}