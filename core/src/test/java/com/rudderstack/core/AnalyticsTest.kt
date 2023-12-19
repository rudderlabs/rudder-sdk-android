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

import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.*
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.web.HttpResponse
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
import org.mockito.Answers
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
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
    private lateinit var storage: Storage
    private val mockServerConfig = RudderServerConfig(
        source = RudderServerConfig.RudderServerConfigSource(isSourceEnabled =
        true,
            destinations = listOf(RudderServerConfig.RudderServerDestination(
                destinationName = "assert-destination",
                isDestinationEnabled = true, destinationId = "1234",
                destinationConfig = mapOf("eventFilteringOption" to "disable"),
                destinationDefinition = RudderServerConfig.RudderServerDestinationDefinition(
                    "enabled-destination", displayName = "enabled-destination")
            )),
        )
    )
    private val mockedControlPlane = mock(ConfigDownloadService::class.java).also {
        `when`(it.download(anyString(), anyString(), anyString(), any(), any())).then {

            it.getArgument<(success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit>(4)
                .invoke(
                    true, mockServerConfig, null
                    )
        }
    }
    private val mockedDataUploadService = mock(DataUploadService::class.java)

    @Before
    fun setup() {
        storage = BasicStorageImpl()
        val mockedResponse: HttpResponse<out Any> = HttpResponse(200, "OK", null)
        mockedDataUploadService.let {

            whenever(it.upload(any(), any(), any())).then {
                val data = it.getArgument<List<Message>>(0)
//            storage.deleteMessages(data)
                it.getArgument<(response: HttpResponse<out Any>) -> Unit>(2).invoke(
                    mockedResponse
                )
            }
            `when`(it.uploadSync(any<List<Message>>(), anyOrNull())).thenReturn(
                mockedResponse
            )
        }
        analytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter, storage = storage
            ),
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
    }

    @After
    fun destroy() {
        analytics.clearStorage()
        analytics.shutdown()

    }

    @Test
    fun `test Analytics initialization listener call with correct write key`() {
        analytics.shutdown()
        val isDone = AtomicBoolean(false)
        analytics = Analytics(writeKey, Configuration(
            jsonAdapter, shouldVerifySdk = true
        ), initializationListener = { success, message ->
            assertThat(success, `is`(true))
            assertThat(message, nullValue())
            isDone.set(true)
        }, configDownloadService = mockedControlPlane)
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilTrue(isDone)
    }

    @Test
    fun `test Analytics initialization listener call with incorrect write key`() {
        val isDone = AtomicBoolean(false)
        analytics.shutdown()
        val mockedControlPlane = mock(ConfigDownloadService::class.java).also {
            `when`(it.download(anyString(), anyString(), anyString(), any(), any())).then {

                it.getArgument<(success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit>(4)
                    .invoke(
                        false, null, "some error"
                    )
            }
        }
        analytics = Analytics("some wrong write key", Configuration(
            jsonAdapter, sdkVerifyRetryStrategy = RetryStrategy.exponential(1), shouldVerifySdk =
            true
        ), initializationListener = { success, message ->
            assertThat(success, `is`(false))
            assertThat(message, `is`("Downloading failed, Shutting down some error"))
            isDone.set(true)
        }, configDownloadService = mockedControlPlane)

        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilTrue(isDone)
    }
    @Test
    fun `test config service not called if shouldVerifySdk is false`(){
        // default shouldVerifySdk is false
        // we wait for some time
        // on setup, analytics is initialized
        busyWait(200)
        verify(mockedControlPlane, never()).download(anyString(), anyString(), anyString(), any(),
            any())
    }

    @Test
    fun `test config download called with proper retry strategy`(){
        analytics.shutdown()
        val retryStrategy = RetryStrategy.exponential(3)
        analytics = Analytics(writeKey, Configuration(
            jsonAdapter, sdkVerifyRetryStrategy = retryStrategy, shouldVerifySdk =
            true
        ), configDownloadService = mockedControlPlane)
        busyWait(200)
        val retryStrategyCaptor = argumentCaptor<RetryStrategy>()
        verify(mockedControlPlane, times(1)).download(anyString(), anyString(), anyString(),
            retryStrategyCaptor.capture(),
            any())
        assertThat(retryStrategyCaptor.firstValue, `is`(retryStrategy))
    }
    @Test
    fun `test identify`() {
//        val isDone = AtomicBoolean(false)
        //TODO mock storage to test
        /*val assertDestinationPlugin = object : BaseDestinationPlugin<Any>("enabled-destination") {
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
                val currentSettings = ConfigurationsState.value
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

        Awaitility.await().atMost(10, TimeUnit.MINUTES).untilTrue(isDone)*/
    }

    @Test
    fun `test track event`() {

    }

    fun `test alias event`() {

    }

    fun `test group event`() {

    }


    @Test
    fun `test with later initialized destinations`() {
       /* val isDone = AtomicBoolean(false)

        val assertDestinationPlugin = object : BaseDestinationPlugin<Any>("enabled-destination") {
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
            eventName = "track",
            userID = "user_id_track",
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
        analytics.removeAllCallbacks()*/

    }

    @Test
    fun `test with rudder option`() {
        //given
        analytics.shutdown()
        analytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter, storage = storage, shouldVerifySdk = true
            ),
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        busyWait(500L) // enough for server config to be downloaded
        val rudderOptions = RudderOptions.Builder().withExternalIds(
            listOf(mapOf("some_id" to "id"))
        ).withIntegrations(mapOf("enabled-destination" to true, "All" to false)).build()

        val dummyPlugin = mock(BaseDestinationPlugin::class.java)
        whenever(dummyPlugin.name).thenReturn("dummy")
        whenever(dummyPlugin.isReady).thenReturn(true)
        val assertdestination = mock(DestinationPlugin::class.java)
        whenever(assertdestination.name).thenReturn("enabled-destination")
        whenever(assertdestination.isReady).thenReturn(true)
        analytics.addPlugin(assertdestination, dummyPlugin)

        //when
        analytics.track(eventName = "some", trackProperties = mapOf(), options = rudderOptions)
        busyWait(500L)

        //then
        verify(dummyPlugin, never()).intercept(any())
        val chainArgCaptor = argumentCaptor<Plugin.Chain>()
        verify(assertdestination, times(1)).intercept(chainArgCaptor.capture())
        val msg = chainArgCaptor.firstValue.message()
        assertThat(
            msg.integrations, allOf(
                aMapWithSize(2),
                hasEntry("enabled-destination", true),
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
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test flush schedule`() {
        //will spy new analytics instance
//        analytics.shutdown()
        val isDone = AtomicBoolean(false)
        //we change settings to change the flush queue size
        val newConfiguration = Configuration(jsonAdapter, flushQueueSize = 3)
        analytics.applyConfiguration {
            storage.setMaxFetchLimit(3)
            copy(
                flushQueueSize = 3
            )
        }
        val items = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        val itemNamesThatShouldBePresent = items.take(newConfiguration.flushQueueSize).map {
            it.eventName
        }
        val itemNamesThatShouldNotBePresent = items.map {
            it.eventName
        } - itemNamesThatShouldBePresent
        analytics.addCallback(object : Callback {
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

                if (incomingMsgs > newConfiguration.flushQueueSize) {
                    assert(false)
                    isDone.set(true)
                }
                if (incomingMsgs == newConfiguration.flushQueueSize) thread {
                    Thread.sleep(100) // the time should be good enough to check if number of messages is
                    //crossing max_fetch size, but should be less than it takes for an API call. since
                    // after a success, the next batch will be fetched and uploaded
                    isDone.set(true)
                }
            }

            override fun failure(message: Message?, throwable: Throwable?) {
                assert(false)
            }

        })

        for (i in items) {
            analytics.track(i)
        }
        Awaitility.await().atMost(newConfiguration.maxFlushInterval, TimeUnit.SECONDS)
            .untilTrue(isDone)
    }

    @Test
    fun `test multiple messages ordering`() {
        // add a plugin to check, add some delay to it
        val events = (1..10).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
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
            if (events.size < msgCounter) isDone.set(true)
            it.proceed(it.message())
        }

        analytics.addPlugin(assertionPlugin)
        for (i in events) {
            analytics.track(i)
        }
        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilTrue(isDone)
    }

    @Test
    fun `test no item is tracked after shutdown`() {
        //flush should be called prior to shutdown. and data uploaded
        //we will track few events, less than flush_queue_size,
        // call shutdown and wait for sometime to check the storage count.

        analytics.applyConfiguration {
            copy(
                flushQueueSize = 20
            )
        }
        val events = (1..10).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        analytics.shutdown()
        for (i in events) {
            analytics.track(i)
        }
        verify(mockedDataUploadService, never()).uploadSync(anyList(), anyOrNull())
//        assertThat(storage.getDataSync(), anyOf(nullValue(), iterableWithSize(0)))
    }

    @Test
    fun `test blocking flush`() {
        //we will track few events, less than flush_queue_size,
        // call flush and wait for sometime to check the storage count.
        analytics.applyConfiguration {
            copy(

                flushQueueSize = 200, maxFlushInterval = 10_000_00
            )
        }

        val events = (1..100).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        for (i in events) {
            analytics.track(i)
        }
        while ((analytics.currentConfiguration?.storage?.getDataSync()?.size ?: 0) < 100) {
        }

        analytics.blockingFlush()
        assertThat(storage.getDataSync(), anyOf(nullValue(), iterableWithSize(0)))
        verify(mockedDataUploadService, times(1)).uploadSync(anyList(), anyOrNull())
    }

    @Test
    fun `test back pressure strategies`() {
        //we check the storage directly
        val events = (1..20).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        storage.setStorageCapacity(10)
        storage.setBackpressureStrategy(Storage.BackPressureStrategy.Drop) // first 10 will be there
        storage.saveMessage(*events.toTypedArray())
        val first10Events = events.take(10).map { it.eventName }
        val last10Events = events.takeLast(10).map { it.eventName }

        assertThat(
            storage.getDataSync(), allOf(
                iterableWithSize(10), everyItem(
                    allOf(
                        Matchers.isA(TrackMessage::class.java), hasProperty(
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
                iterableWithSize(10), everyItem(
                    allOf(
                        Matchers.isA(TrackMessage::class.java), hasProperty(
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
            writeKey, Configuration(
                jsonAdapter
            ), configDownloadService = spyControlPlane
        ).shutdown()
        verify(spyControlPlane, times(0)).download(
            any(), any(), any(), any(), any()
        )
    }

    @Test
    fun `test flush after shutdown`() {
        analytics.applyConfiguration {
            copy(flushQueueSize = 100, maxFlushInterval = 10000)
        }
        val events = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        val events2 = (6..10).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
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
        verify(mockedDataUploadService, times(0)).uploadSync(any(), anyOrNull())
    }

    @Test
    fun `test no force flush after shutdown`() {
        val events = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        val someAnalytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter, storage = storage
            ),
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        someAnalytics.shutdown()
        Thread.sleep(500)
        //inserting some data to storage

        storage.saveMessage(*events.toTypedArray())

        val spyDataUploadService = spy(DataUploadService::class.java)
        someAnalytics.forceFlush(alternateDataUploadService = spyDataUploadService)
        Thread.sleep(250)
        verify(spyDataUploadService, times(0)).uploadSync(any(), anyOrNull())
    }

    @Test
    fun `test shutdown`() {
        val events = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        val someAnalytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter, storage = storage
            ),
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        someAnalytics.shutdown()
        //we try pushing in events
        events.forEach {
            someAnalytics.track(it)
        }
        //all data should have been rejected
        assertThat(
            storage.getDataSync(), allOf(
                iterableWithSize(0)
            )
        )
    }

    @Test
    fun `test periodic flush`() {
       /* val isDone = AtomicBoolean(false)
//        val newConfiguration = ConfigurationsState.value?.copy(
//            maxFlushInterval = 1000,
//            flushQueueSize = 100
//        ) ?: Configuration(anonymousId = "anon_id", maxFlushInterval = 1000, flushQueueSize = 100)
        analytics.applyConfiguration {
            copy(
                maxFlushInterval = 1000, flushQueueSize = 100
            )
        }
        val events = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        analytics.addCallback(object : Callback {
            override fun success(message: Message?) {
                assertThat(
                    message, allOf(
                        notNullValue(), hasProperty("eventName", `is`(events[0].eventName))
                    )
                )
                isDone.set(true)
            }

            override fun failure(message: Message?, throwable: Throwable?) {
            }

        })

        events.forEach {
            analytics.track(it)
        }

        Awaitility.await().atLeast(
            ConfigurationsState.value?.maxFlushInterval ?: 0L, TimeUnit.MILLISECONDS
        ).untilTrue(isDone)
        analytics.removeAllCallbacks()*/
    }

    @Test
    fun `test custom plugin`() {
        val isDone = AtomicBoolean(false)
        val customPlugin = Plugin {
            isDone.set(true)
            it.proceed(it.message())
        }
        analytics.addPlugin(customPlugin)

        analytics.track {
            event { +"event" }
            //or event("event")
            trackProperties {
                //use any of these
                +("property1" to "value1")
                +mapOf("property2" to "value2")
                add("property3" to "value3")
                add(mapOf("property4" to "value4"))
            }
            userId("user_id")
            rudderOptions {
                customContexts {
                    +("cc1" to "cp1")
                    +("cc2" to "cp2")
                }
                externalIds {
                    +(mapOf("ext-1" to "ex1"))
                    +(mapOf("ext-2" to "ex2"))
                    +listOf(mapOf("ext-3" to "ex3"))
                }
                integrations {
                    +("firebase" to true)
                    +("amplitude" to false)
                }
            }
        }

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilTrue(isDone)

    }

    @Test
    fun `test flush throttling`() {
        analytics.shutdown()
        Thread.sleep(500)
        println("analytics shut down")
        val isDone = AtomicBoolean(false)
        //settings to make sure auto dump is off
//        val newConfiguration = ConfigurationsState.value?.copy(
//            maxFlushInterval = 15_000,
//            flushQueueSize = 100
//        ) ?: Configuration(anonymousId = "anon_id", maxFlushInterval = 15_000, flushQueueSize = 100)
//        analytics.applyConfiguration{
//            copy(
//                maxFlushInterval = 15_000,
//                flushQueueSize = 100
//            )
//        }
        //spy upload service to check for throttling
        val counter = AtomicInteger(0) //will check number of times called
        val spyUploadService = mock(DataUploadService::class.java)
        `when`(spyUploadService.uploadSync(any(), anyOrNull())).then {
            Thread.sleep(500)
            HttpResponse(200, null, null)
        }
        val events = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        val spyStorage = mock(Storage::class.java)
        `when`(spyStorage.getDataSync(anyInt())).then {
            val t = counter.incrementAndGet()
            Thread.sleep(100)
            assertThat(t, `is`(counter.get()))
            if (counter.get() == 3) isDone.set(true)
            events
        }
        val someAnalytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter,
                maxFlushInterval = 15_000,
                flushQueueSize = 100,
                storage = spyStorage,
            ),
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
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
    MoshiAnalyticsTest::class, JacksonAnalyticsTest::class, GsonAnalyticsTest::class
)
class AnalyticsTestSuite : TestSuite() {}