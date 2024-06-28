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

package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Storage
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.TrackMessage
import com.rudderstack.testcommon.VerificationStorage
import com.rudderstack.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.iterableWithSize
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class StoragePluginTest {

    private val testMessagesList = listOf<TrackMessage>(
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
    private lateinit var analytics: Analytics
    private lateinit var storage: Storage
    @Before
    fun setup() {
        storage = VerificationStorage()
        analytics = generateTestAnalytics(mock(),
            mockConfiguration = Configuration(shouldVerifySdk = false), storage = storage
        )
    }
    @After
    fun tearDown() {
        analytics.shutdown()
    }

    @Test
    fun testStoragePluginWithQueueSize() {

        val eventNames = testMessagesList.map {
            it.eventName
        }
        val storagePlugin = StoragePlugin()
        storagePlugin.setup(analytics)
//        storagePlugin.updateConfiguration()
        testMessagesList.forEach { msg ->
            CentralPluginChain(msg, listOf(storagePlugin), originalMessage = msg).proceed(msg)
        }
        val dataOfNames = storage.getDataSync().map {
            (it as TrackMessage).eventName
        }
        assertThat(
            dataOfNames, allOf(
                iterableWithSize(testMessagesList.size), contains(*eventNames.toTypedArray())
            )
        )
    }


    /*@Test
    fun testStoragePluginWithFlushInterval() {
        val isComplete = AtomicBoolean(false)
        val flushInterval = 3 * 1000L
        val configuration = Configuration(
            jsonAdapter = JacksonAdapter(),
            flushQueueSize = Integer.MAX_VALUE, // setting it to a large value ensures
            // there is no chance of flushing due to queue size
            maxFlushInterval = flushInterval
        )
        ConfigurationsState.update(configuration)
        val storagePluginCreated = System.currentTimeMillis()
        val storagePlugin = StoragePlugin(*//*FlushScheduler(BasicStorageImpl(logger = KotlinLogger)) {

            assertThat(
                (System.currentTimeMillis() - storagePluginCreated), allOf(
                    greaterThanOrEqualTo(flushInterval)*//**//*,
                    lessThan(2 * flushInterval )*//**//*
                )
            )
            println("done")
            isComplete.set(true)
        }*//*)
        testMessagesList.forEach { msg ->
            CentralPluginChain(msg, listOf(storagePlugin)).proceed(msg)
        }
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isComplete)

    }*/
}