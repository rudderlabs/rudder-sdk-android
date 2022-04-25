/*
 * Creator: Debanjan Chatterjee on 04/04/22, 1:38 PM Last modified: 04/04/22, 1:38 PM
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

import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.core.internal.states.SettingsState
import com.rudderstack.android.gsonrudderadapter.GsonAdapter
import com.rudderstack.android.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.android.models.*
import com.rudderstack.android.moshirudderadapter.MoshiAdapter
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.web.HttpResponse
import junit.framework.TestSuite
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

abstract class AnalyticsTest {
    //test track, alias, identify, group
    //wake up working,
    //rudder options work
    //test anon_id
    //complete flow
    protected abstract val jsonAdapter: JsonAdapter
    private lateinit var analytics: Analytics
    private lateinit var storage : Storage
    private val mockedControlPlane = mock(ConfigDownloadService::class.java).also {
        `when`(it.download(anyString(), anyString(), anyString(), any(), any())).then {

            it.getArgument<(success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit>(4)
                .invoke(
                    true, RudderServerConfig(
                        source =
                        RudderServerConfig.RudderServerConfigSource(isSourceEnabled = true)
                    ), null
                )
        }
    }
    private val mockedDataUploadService = mock(DataUploadService::class.java)

    @Before
    fun setup() {
        storage =  BasicStorageImpl(logger = com.rudderstack.core.internal.KotlinLogger)
        val mockedResponse: HttpResponse<out Any> = HttpResponse(200, "OK", null)
        mockedDataUploadService.let {

            whenever(it.upload(any(), any(), any())).then {
                val data = it.getArgument<List<Message>>(0)
//            storage.deleteMessages(data)
                it.getArgument<(response: HttpResponse<out Any>) -> Unit>(2).invoke(
                    mockedResponse
                )
            }
            `when`(it.uploadSync(any() as List<Message>, anyOrNull())).thenReturn(
                mockedResponse
            )
        }
        analytics = Analytics(
            writeKey,
            Settings(anonymousId = "anon_id"),
            jsonAdapter,
            storage = storage,
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            }, dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
    }

    @After
    fun destroy() {
        analytics.clearStorage()
        analytics.shutdown()

    }

    @Test
    fun `test Analytics initialization with correct write key`() {
        val isDone = AtomicBoolean(false)
        analytics = Analytics(
            writeKey,
            Settings(),
            jsonAdapter,
            initializationListener = { success, message ->
                assertThat(success, `is`(true))
                isDone.set(true)
            })
        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilTrue(isDone)
    }

    @Test
    fun `test Analytics initialization with incorrect write key`() {
        val isDone = AtomicBoolean(false)
        analytics.shutdown()
        analytics = Analytics(
            "some wrong write key",
            Settings(),
            jsonAdapter,
            initializationListener = { success, message ->
                assertThat(success, `is`(false))
                isDone.set(true)
            },
            sdkVerifyRetryStrategy = RetryStrategy.exponential(2)
        )
        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilTrue(isDone)
    }

    @Test
    fun `test identify`() {
        val isDone = AtomicBoolean(false)

        val assertDestinationPlugin = object : BaseDestinationPlugin<Any>("assert-destination") {
            override fun intercept(chain: Plugin.Chain): Message {
                val message = chain.message()
                assertThat(message.userId, `is`("user_id"))
                assertThat(
                    message.context?.traits, allOf(
                        notNullValue(),
                        aMapWithSize(3),
                        hasEntry("trait-1", "t-1"),
                        hasEntry("trait-2", "t-2"),
                    )
                )
                //check settings and storage
                val currentSettings = SettingsState.value
                assertThat(
                    currentSettings, allOf(
                        notNullValue(),
                        hasProperty("userId", `is`("user_id")),

                        )
                )
                isDone.set(true)
                return chain.proceed(message)
            }

            override fun updateRudderServerConfig(config: RudderServerConfig) {
                super.updateRudderServerConfig(config)
                setReady(true)
            }
        }
        analytics.addPlugin(assertDestinationPlugin)
        analytics.identify("user_id", mapOf("trait-1" to "t-1", "trait-2" to "t-2"))

        Awaitility.await().atMost(10, TimeUnit.MINUTES).untilTrue(isDone)
    }

    @Test
    fun `test track event`() {

    }

    fun `test alias event`() {

    }

    fun `test group event`() {

    }

    @Test
    fun `test put anonymous id`() {
        analytics.setAnonymousId("anon_id")
        assertThat(
            SettingsState.value?.anonymousId, allOf(
                notNullValue(),
                `is`("anon_id")
            )
        )
    }

    @Test
    fun `test with later initialized destinations`() {
        val isDone = AtomicBoolean(false)

        val assertDestinationPlugin = object : BaseDestinationPlugin<Any>("assert-destination") {
            override fun intercept(chain: Plugin.Chain): Message {
                val message = chain.message()
                assertThat(message.userId, `is`("user_id"))
                assertThat(
                    message.context?.traits, allOf(
                        notNullValue(),
                        aMapWithSize(3),
                        hasEntry("prop-1", "p-1"),
                        hasEntry("prop-2", "p-2"),
                    )
                )
                assertThat(
                    message, allOf(
                        instanceOf(TrackMessage::class.java),
                        hasProperty("eventName", `is`("track"))
                    )
                )
//                assertThat(messages, instanceOf(TrackMessage::class.java))
//                isDone.set(true)
                return chain.proceed(message)
            }

        }
        analytics.addPlugin(assertDestinationPlugin)
        analytics.track(
            eventName = "track", userID = "user_id_track",
            trackProperties = mapOf("prop-1" to "p-1", "prop-2" to "p-2")
        )
        val callback = object : Callback {
            override fun success(message: Message?) {
                isDone.set(true)
            }

            override fun failure(message: Message?, throwable: Throwable?) {
                assert(false)
                println(throwable?.message)
                isDone.set(true)
            }
        }
        analytics.addCallback(callback)
        Thread {
            Thread.sleep(1000L)
            assertDestinationPlugin.setReady(true)
        }.start()
        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilTrue(isDone)
        analytics.removeAllCallbacks()

    }

    @Test
    fun `test with rudder option`() {
        val isDone = AtomicBoolean(false)
        val rudderOptions = RudderOptions.Builder().withExternalIds(
            listOf(mapOf("some_id" to "id"))
        ).withIntegrations(mapOf("dest-1" to true, "All" to false)).build()

        val dummyPlugin = BaseDestinationPlugin<Any>("dummy") {
            assert(false)
            it.proceed(it.message())
        }
        val assertdestination = BaseDestinationPlugin<Any>("dest-1") {
            val msg = it.message()
            assertThat(
                msg.integrations, allOf(
                    aMapWithSize(2),
                    hasEntry("dest-1", true),
                    hasEntry("All", false),
                )
            )
            assertThat(
                msg.context?.externalIds, allOf(
                    notNullValue(),
                    iterableWithSize(1),
                    containsInAnyOrder(mapOf("some_id" to "id"))
                )
            )
            isDone.set(true)
            it.proceed(it.message())
        }
        analytics.addPlugin(assertdestination, dummyPlugin)
        assertdestination.setReady(true)
        dummyPlugin.setReady(true)
        analytics.track(eventName = "some", trackProperties = mapOf(), options = rudderOptions)
        Awaitility.await().atMost(20, TimeUnit.MINUTES).untilTrue(isDone)


    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test flush schedule`() {
        //will spy new analytics instance
//        analytics.shutdown()
        val isDone = AtomicBoolean(false)
        //we change settings to change the flush queue size
        val newSettings = Settings(flushQueueSize = 3, anonymousId = "anon_id")
        analytics.applySettings(newSettings)
        analytics.setMaxFetchLimit(3)
        val items = (1..5).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        val itemNamesThatShouldBePresent = items.take(newSettings.flushQueueSize)
            .map {
                it.eventName
            }
        val itemNamesThatShouldNotBePresent = items.map {
            it.eventName
        } - itemNamesThatShouldBePresent
        analytics.addCallback(
            object : Callback {
                private var incomingMsgs = 0
                override fun success(message: Message?) {
                    ++incomingMsgs

                    assertThat(
                        message, allOf(
                            notNullValue(),
                            hasProperty(
                                "eventName", allOf(
                                    `in`(itemNamesThatShouldBePresent.toTypedArray()),
                                    not(`in`(itemNamesThatShouldNotBePresent))
                                )
                            ),

                            )
                    )

                    if (incomingMsgs > newSettings.flushQueueSize) {
                        assert(false)
                        isDone.set(true)
                    }
                    if (incomingMsgs == newSettings.flushQueueSize)
                        thread {
                            Thread.sleep(100) // the time should be good enough to check if number of messages is
                            //crossing max_fetch size, but should be less than it takes for an API call. since
                            // after a success, the next batch will be fetched and uploaded
                            isDone.set(true)
                        }
                }

                override fun failure(message: Message?, throwable: Throwable?) {
                    assert(false)
                }

            }
        )

        for (i in items) {
            analytics.track(i)
        }
        Awaitility.await().atMost(newSettings.maxFlushInterval, TimeUnit.SECONDS)
            .untilTrue(isDone)
    }

    @Test
    fun `test multiple messages ordering`() {
        // add a plugin to check, add some delay to it
        val events = (1..10).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        val isDone = AtomicBoolean(false)
        var msgCounter = 1
        val assertionPlugin = Plugin {
            val msg = it.message()
            assertThat(
                msg, allOf(
                    Matchers.isA(TrackMessage::class.java),
                    hasProperty("eventName", `is`("event:${msgCounter++}"))
                )
            )
            Thread.sleep(20L) //a minor delay
            if (events.size < msgCounter)
                isDone.set(true)
            it.proceed(it.message())
        }

        analytics.addPlugin(assertionPlugin)
        for (i in events) {
            analytics.track(i)
        }
        Awaitility.await().atMost(20, TimeUnit.SECONDS)
            .untilTrue(isDone)
    }

    @Test
    fun `test flush before shutdown`() {
        //flush should be called prior to shutdown. and data uploaded
        //we will track few events, less than flush_queue_size,
        // call shutdown and wait for sometime to check the storage count.
        val newSettings = SettingsState.value?.copy(
            flushQueueSize = 20
        ) ?: Settings(anonymousId = "anon_id", flushQueueSize = 20)
        analytics.applySettings(newSettings)

        val events = (1..10).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        for (i in events) {
            analytics.track(i)
        }
        Thread.sleep(1000)
        assertThat(storage.getDataSync(), iterableWithSize(10))
        analytics.shutdown()
        Thread.sleep(5000)
        val response = mockedDataUploadService.uploadSync(listOf(), null)
        assertThat(storage.getDataSync(), anyOf(nullValue(), iterableWithSize(0)))
    }

    @Test
    fun `test back pressure strategies`() {
        //we check the storage directly
        val events = (1..20).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        storage.setStorageCapacity(10)
        storage.setBackpressureStrategy(Storage.BackPressureStrategy.Drop) // first 10 will be there
        storage.saveMessage(*events.toTypedArray())
        val first10Events = events.take(10).map { it.eventName }
        val last10Events = events.takeLast(10).map { it.eventName }

        assertThat(
            storage.getDataSync(), allOf(
                iterableWithSize(10),
                everyItem(
                    allOf(
                        Matchers.isA(TrackMessage::class.java),
                        hasProperty(
                            "eventName", allOf(
                                `in`(first10Events), not(`in`(last10Events))
                            )
                        )
                    )
                )
//            contains(last10Events)
            )
        )
        storage.clearStorage()
        storage.setBackpressureStrategy(Storage.BackPressureStrategy.Latest) // last 10 will be there
        storage.saveMessage(*events.toTypedArray())
        assertThat(
            storage.getDataSync(), allOf(
                iterableWithSize(10),
                everyItem(
                    allOf(
                        Matchers.isA(TrackMessage::class.java),
                        hasProperty(
                            "eventName", allOf(
                                `in`(last10Events), not(`in`(first10Events))
                            )
                        )
                    )
                )
//            contains(last10Events)
            )
        )
    }

    @Test
    fun `test should verify sdk`() {
        val spyControlPlane = spy(ConfigDownloadService::class.java)
        Analytics(
            writeKey,
            Settings(),
            jsonAdapter,
            false,
            configDownloadService = spyControlPlane
        ).shutdown()
        verify(spyControlPlane, times(0)).download(
            any(), any(),
            any(), any(), any()
        )

    }

    @Test
    fun `test flush after shutdown`() {
        val newSettings = SettingsState.value?.copy(flushQueueSize = 100, maxFlushInterval = 10000)?:
        Settings(flushQueueSize = 100, maxFlushInterval = 10000, anonymousId = "anon_id")
        SettingsState.update(newSettings)
        val events = (1..5).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        val events2 = (6..10).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }

        events.forEach {
            analytics.track(it)
        }
        Thread.sleep(500)
        analytics.shutdown()
        Thread.sleep(200) // so that data doesn't get inserted while flushing
        // This won't happen in production code, as we are specifically doing this through storage.
        //In real-world scenario, we would use analytics.saveMessage, which won't work if  shutdown is called
        //inserting some data
        events2.forEach {
            storage.saveMessage(it)
        }
//        Thread.sleep(500)
        analytics.flush()
        Thread.sleep(200)
        verify(mockedDataUploadService, times(1)).uploadSync(any(), anyOrNull())
        // 1 for shutdown call

    }
    @Test
    fun `test force flush after shutdown`() {
        val events = (1..5).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        val someAnalytics = Analytics(
            writeKey,
            Settings(anonymousId = "anon_id"),
            jsonAdapter,
            storage = storage,
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            }, dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        someAnalytics.shutdown()
        Thread.sleep(500)
        //inserting some data to storage

            storage.saveMessage(*events.toTypedArray())

        val spyDataUploadService = spy(DataUploadService::class.java)
        someAnalytics.forceFlush(spyDataUploadService)
        Thread.sleep(250)
        verify(spyDataUploadService, times(1)).uploadSync(any(), anyOrNull())
    }

    @Test
    fun `test shutdown`() {
        val events = (1..5).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        val someAnalytics = Analytics(
            writeKey,
            Settings(anonymousId = "anon_id"),
            jsonAdapter,
            storage = storage,
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            }, dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        someAnalytics.shutdown()
        //we try pushing in events
        events.forEach {
            someAnalytics.track(it)
        }
        //all data should have been rejected
        assertThat(storage.getDataSync(), allOf(
            iterableWithSize(0)
        ))
    }
    @Test
    fun `test periodic flush`() {
        val isDone = AtomicBoolean(false)
        val newSettings = SettingsState.value?.copy(maxFlushInterval = 1000,
        flushQueueSize = 100)?: Settings(anonymousId = "anon_id", maxFlushInterval = 1000, flushQueueSize = 100)
        analytics.applySettings(newSettings)
        val events = (1..5).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        analytics.addCallback(object : Callback{
            override fun success(message: Message?) {
                assertThat(message, allOf(
                    notNullValue(), hasProperty("eventName", `is`(events[0].eventName) )
                ))
                isDone.set(true)
            }

            override fun failure(message: Message?, throwable: Throwable?) {
            }

        })

        events.forEach {
            analytics.track(it)
        }

        Awaitility.await().atLeast(newSettings.maxFlushInterval, TimeUnit.MILLISECONDS).untilTrue(isDone)
        analytics.removeAllCallbacks()
    }
    @Test
    fun `test custom plugin`() {
        val isDone = AtomicBoolean(false)
        val customPlugin = Plugin {
            isDone.set(true)
            it.proceed(it.message())
        }
        analytics.addPlugin(customPlugin)
        analytics.track("event", mapOf())
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilTrue(isDone)

    }
    @Test
    fun `test flush throttling`(){
        analytics.shutdown()
        Thread.sleep(500)
        println("analytics shut down")
        val isDone = AtomicBoolean(false)
        //settings to make sure auto dump is off
         val newSettings = SettingsState.value?.copy(maxFlushInterval = 15_000,
            flushQueueSize = 100)?: Settings(anonymousId = "anon_id", maxFlushInterval = 15_000, flushQueueSize = 100)
        //spy upload service to check for throttling
        val counter = AtomicInteger(0) //will check number of times called
        val spyUploadService = mock(DataUploadService::class.java)
        `when`(spyUploadService.uploadSync(any(), anyOrNull())).then {
            Thread.sleep(500)
            HttpResponse(200, null, null)
        }
        val events = (1..5).map {
            TrackMessage.create("event:$it", Utils.timeStamp)
        }
        val spyStorage = mock(Storage::class.java)
        `when`(spyStorage.getDataSync(anyInt())).then {
            val t = counter.incrementAndGet()
            Thread.sleep(100)
            assertThat(t, `is`(counter.get()))
            if(counter.get() == 3) isDone.set(true)
            events
        }
        val someAnalytics = Analytics(
            writeKey,
            newSettings,
            jsonAdapter,
            storage = spyStorage,
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            }, dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        events.forEach {
            someAnalytics.track(it)
        }
        Thread.sleep(1000) // let messages sink in
        thread {
            repeat((1..10).count()) {
                someAnalytics.flush()
            }
        }
        thread {
            repeat((1..20).count()) {
                someAnalytics.flush()
            }
        }
        Awaitility.await().atMost(100, TimeUnit.SECONDS).untilTrue(isDone)
    }

}

class GsonAnalyticsTest : AnalyticsTest() {
    override val jsonAdapter: JsonAdapter
        get() = GsonAdapter()

}

class JacksonAnalyticsTest : AnalyticsTest() {
    override val jsonAdapter: JsonAdapter
        get() = JacksonAdapter()

}

class MoshiAnalyticsTest : AnalyticsTest() {
    override val jsonAdapter: JsonAdapter
        get() = MoshiAdapter()

}

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MoshiAnalyticsTest::class,
    JacksonAnalyticsTest::class,
    GsonAnalyticsTest::class
)
class AnalyticsTestSuite : TestSuite() {
}