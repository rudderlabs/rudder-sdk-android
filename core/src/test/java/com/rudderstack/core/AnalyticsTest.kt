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

import com.rudderstack.core.RudderUtils.getUTF8Length
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.core.internal.states.DestinationConfigState
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.*
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpResponse
import com.vagabond.testcommon.assertArgument
import com.vagabond.testcommon.generateTestAnalytics
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.math.ceil

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
        source = RudderServerConfig.RudderServerConfigSource(
            isSourceEnabled = true,
            destinations = listOf(
                RudderServerConfig.RudderServerDestination(
                    destinationName = "assert-destination",
                    isDestinationEnabled = true,
                    destinationId = "1234",
                    destinationConfig = mapOf("eventFilteringOption" to "disable"),
                    destinationDefinition = RudderServerConfig.RudderServerDestinationDefinition(
                        "enabled-destination", displayName = "enabled-destination"
                    )
                )
            ),
        )
    )
    private lateinit var mockedControlPlane : ConfigDownloadService
    private lateinit var mockedDataUploadService : DataUploadService

    @Before
    fun setup() {
        mockedControlPlane = mock(ConfigDownloadService::class.java).also {
            `when`(it.download(any())).then {

                it.getArgument<(success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit>(0)
                    .invoke(
                        true, mockServerConfig, null
                    )
            }
        }
        mockedDataUploadService = mock (com.rudderstack.core.DataUploadService::class.java)
        storage = BasicStorageImpl()
        val mockedResponse: HttpResponse<out Any> = HttpResponse(200, "OK", null)
        mockedDataUploadService.let {

            whenever(it.upload(any(), any(), any())).then {
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
                jsonAdapter,
                shouldVerifySdk = false
            ),
            storage = storage,
            initializationListener = { success, message ->
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        println("setup done with analytics: $analytics, shouldVerifySdk = ${analytics.currentConfiguration?.shouldVerifySdk}")
    }

    @After
    fun destroy() {
        analytics.clearStorage()
        analytics.shutdown()
        println("After called on analytics $analytics")
    }

    @Test
    fun `test Analytics initialization listener call with correct write key`() {
        println("running test test Analytics initialization listener call with correct write key")
        analytics.shutdown()
        val isDone = AtomicBoolean(false)
        analytics = Analytics(
            writeKey, Configuration(
                jsonAdapter, shouldVerifySdk = true
            ), initializationListener = { success, message ->
                assertThat(success, `is`(true))
                assertThat(message, nullValue())
                isDone.set(true)
            }, configDownloadService = mockedControlPlane
        )
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilTrue(isDone)
    }

    @Test
    fun `test Analytics initialization listener call with incorrect write key`() {
        println("running test test Analytics initialization listener call with incorrect write key")
        val isDone = AtomicBoolean(false)
        analytics.shutdown()
        val mockedControlPlane = mock(ConfigDownloadService::class.java).also {
            `when`(it.download(any())).then {

                it.getArgument<(success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit>(
                    0
                ).invoke(
                    false, null, "some error"
                )
            }
        }
        analytics = Analytics(
            "some wrong write key", Configuration(
                jsonAdapter,
                sdkVerifyRetryStrategy = RetryStrategy.exponential(1),
                shouldVerifySdk = true
            ), initializationListener = { success, message ->
                assertThat(success, `is`(false))
                assertThat(message, `is`("Downloading failed, Shutting down some error"))
            }, configDownloadService = mockedControlPlane,
            shutdownHook = {  isDone.set(true) }
        )


        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilTrue(isDone)
    }

    @Test
    fun `test config service not called if shouldVerifySdk is false`() {
        println("running test test config service not called if shouldVerifySdk is false")
        // default shouldVerifySdk is false
        // we wait for some time
        // on setup, analytics is initialized
        busyWait(200)
        verify(mockedControlPlane, never()).download(
            any()
        )
    }
    @Test
    fun `test configuration has proper retry strategy`() {
        println("running test test configuration has proper retry strategy")
        analytics.shutdown()
        val retryStrategy = mock<RetryStrategy>()
        analytics = Analytics(
            writeKey, Configuration(
                jsonAdapter, sdkVerifyRetryStrategy = retryStrategy, shouldVerifySdk = true
            ), configDownloadService = mockedControlPlane
        )
        assertThat(analytics.currentConfiguration?.sdkVerifyRetryStrategy, `is`(retryStrategy))
    }

    @Test
    fun `test identify`() {
        println("running test test identify")
        analytics.shutdown()
        analytics = generateTestAnalytics(Configuration(jsonAdapter))
        analytics.identify("user_id", mapOf("trait-1" to "t-1", "trait-2" to "t-2"))
        analytics.assertArgument { input, output ->
            println("Input: $input\nOutput: $output")
            assertThat(
                output, allOf(
                    notNullValue(),
                    instanceOf(IdentifyMessage::class.java),
                    hasProperty("userId", `is`("user_id")),
                    hasProperty(
                        "context", allOf(
                            hasEntry(
                                equalTo("traits"), allOf(
                                    notNullValue(),
                                    aMapWithSize(3),
                                    hasEntry("trait-1", "t-1"),
                                    hasEntry("trait-2", "t-2"),
                                )
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test track event`() {
        println("running test test track event")
        analytics.shutdown()
        analytics = generateTestAnalytics(Configuration(jsonAdapter))
        analytics.track {
            event("event-1")
            userId("user_id")
            trackProperties {
                add("prop-1" to "p-1")
                add("prop-2" to "p-2")
                add("trait-1" to "t-1")
            }
        }
        analytics.assertArgument { input, output ->
            println("Input: $input\nOutput: $output")
            assertThat(
                output, allOf(
                    notNullValue(),
                    instanceOf(TrackMessage::class.java),
                    hasProperty("userId", `is`("user_id")),
                    hasProperty(
                        "properties", allOf(
                            aMapWithSize<String, Any>(3),
                            hasEntry("trait-1", "t-1"),
                            hasEntry("prop-1", "p-1"),
                            hasEntry("prop-2", "p-2"),
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test alias event`() {
        println("running test test alias event")
        analytics.shutdown()
        analytics = generateTestAnalytics(Configuration(jsonAdapter))
        analytics.alias {
            userId("user_id")
            newId("new_id")
        }
        analytics.assertArgument { input, output ->
            println("Input: $input\nOutput: $output")
            assertThat(
                output, allOf(
                    notNullValue(),
                    instanceOf(AliasMessage::class.java),
                    hasProperty("userId", `is`("new_id")),
                    hasProperty("previousId", `is`("user_id")),
                )
            )
        }
    }

    fun `test group event`() {

    }


    @Test
    fun `test with later initialized destinations`() {
        println("running test test with later initialized destinations")
        analytics.shutdown()
        analytics = generateTestAnalytics(
            Configuration(jsonAdapter, shouldVerifySdk = true), mockedControlPlane
        )
        val laterInitDestPlugin = mock(DestinationPlugin::class.java)
        whenever(laterInitDestPlugin.name).thenReturn("enabled-destination")
        whenever(laterInitDestPlugin.isReady).thenReturn(true)
        analytics.track { event("lost-track") }
        busyWait(100)
        analytics.addPlugin(laterInitDestPlugin)
        val chainCaptor = argumentCaptor<Plugin.Chain>()
        analytics.track(
            eventName = "track",
            userId = "user_id",
            trackProperties = mapOf("prop-1" to "p-1", "prop-2" to "p-2")
        )
        busyWait(100)//since it is submitted to executor

        verify(laterInitDestPlugin).intercept(chainCaptor.capture())
        val message = chainCaptor.firstValue.message()
        assertThat(message.userId, `is`("user_id"))
        assertThat(
            message, hasProperty(
                "properties", allOf(
                    aMapWithSize<String, Any>(2),
                    hasEntry("prop-1", "p-1"),
                    hasEntry("prop-2", "p-2"),
                )
            )
        )
        assertThat(
            message, allOf(
                instanceOf(TrackMessage::class.java), hasProperty("eventName", `is`("track"))
            )
        )

    }

    @Test
    fun `test with rudder option`() {
        println("running test test with rudder option")
        //given
        analytics.shutdown()
        analytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter, shouldVerifySdk = true
            ),
            storage = storage,
            initializationListener = { success, message ->
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        while(analytics.retrieveState<DestinationConfigState>()?.value == null){}
        busyWait(300L) // enough for server config to be downloaded
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
        val trackMessage = TrackMessage.create(eventName = "some", timestamp = RudderUtils.timeStamp)
        analytics.track(trackMessage, options =
        rudderOptions)
        val waitUntil = AtomicBoolean(false)
        analytics.addCallback(object : Callback{
            override fun success(message: Message?) {
                waitUntil.set(message is TrackMessage && message.eventName == "some")
            }

            override fun failure(message: Message?, throwable: Throwable?) {
                waitUntil.set(message is TrackMessage && message.eventName == "some")
            }
        })
        while(waitUntil.get().not()){}
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
                notNullValue(), iterableWithSize(1), containsInAnyOrder(mapOf("some_id" to "id"))
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test messages flushed when sent from different threads based on flushQ size`() {
        println("running test test messages flushed when sent from different threads based on flushQ size")
        analytics.applyConfiguration {
            copy(
                flushQueueSize = 300, maxFlushInterval = 10_000
            )
        }

        (1..2).map {
            thread {
                analytics.track("${Thread.currentThread().name}:$it-1")
                analytics.track("${Thread.currentThread().name}:$it-2")
            }
        }
        val storageCount = AtomicInteger(0)
        while (storageCount.get() < 4) {
            storageCount.set(storage.getDataSync().count())
        }
        analytics.applyConfiguration {
            copy(
                flushQueueSize = 3
            )
        }
        while(storage.getDataSync().isNotEmpty()){}
        busyWait(300)
        val listCaptor = argumentCaptor<List<Message>>()
        verify(mockedDataUploadService, atLeast(1)).uploadSync(listCaptor.capture(), anyOrNull())
        val allMsgsUploaded = listCaptor.allValues.flatten()
        assertThat(allMsgsUploaded, allOf(notNullValue(), iterableWithSize(4)))
    }
    @Test
    fun `test messages flushed when sent from different threads based on periodic timeout`() {
        println("running test test messages flushed when sent from different threads based on periodic timeout")
        analytics.applyConfiguration {
            copy(
                flushQueueSize = 100, maxFlushInterval = 10000 // so no flush takes place while
            )
        }
        assertThat(analytics.currentConfiguration?.maxFlushInterval, `is`(10000))
//        val trackedMsgCount = AtomicInteger(0)

        (1..2).map {
            thread {
                analytics.track("${Thread.currentThread().name}:$it-1")
                analytics.track("${Thread.currentThread().name}:$it-2")
//                trackedMsgCount.addAndGet(2)
            }
        }
        val dataStoredCount = AtomicInteger(0)
        while (dataStoredCount.get() < 4) {
            dataStoredCount.set(storage.getDataSync().count())
        }

        dataStoredCount.set(0)

        while (dataStoredCount.get() > 0) {
            dataStoredCount.set(storage.getDataSync().count())
        }
        analytics.applyConfiguration {
            copy(
                flushQueueSize = 100, maxFlushInterval = 100 // so that there is flush called
            )
        }
        assertThat(analytics.currentConfiguration?.maxFlushInterval, `is`(100))
        val timeNow = System.currentTimeMillis()
        while(storage.getDataSync().isNotEmpty()){
            if(System.currentTimeMillis() - timeNow > 10000) break
        }
        busyWait(200) // enough time for one flush
        val listCaptor = argumentCaptor<List<Message>>()
        verify(mockedDataUploadService, times(1)).uploadSync(listCaptor.capture(), anyOrNull())
        val allMsgsUploaded = listCaptor.allValues.flatten()
        assertThat(allMsgsUploaded, allOf(notNullValue(), iterableWithSize(4)))
    }

    @Test
    fun `test multiple messages ordering`() {
        println("running test test multiple messages ordering")
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
        println("running test test no item is tracked after shutdown")
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
        println("running test test blocking flush")
        //we will track few events, less than flush_queue_size,
        // call flush and wait for sometime to check the storage count.
        analytics.applyConfiguration {
            copy(
                flushQueueSize = 200, maxFlushInterval = 10_000_00
            )
        }

        val events = (1..10).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        for (i in events) {
            analytics.track(i)
        }
        while ((analytics.storage.getDataSync().size) < 10) {
        }
        analytics.blockingFlush()
        assertThat(storage.getDataSync(), anyOf(nullValue(), iterableWithSize(0)))
        verify(mockedDataUploadService, atLeast(1)).uploadSync(anyList(), anyOrNull())
    }

    @Test
    fun `given collective batch size is more than MAX_BATCH_SIZE, when blockingFlush is called, then messages are flushed in multiple batches`() {
        println("running test given collective batch size is more than MAX_BATCH_SIZE, when blockingFlush is called, then messages are flushed in multiple batches")

        val totalMessages = 100
        val properties = mutableMapOf<String, Any>()
        properties["property"] = generateDataOfSize(1024 * 30)

        analytics.applyConfiguration {
            copy(
                flushQueueSize = 500, maxFlushInterval = 10_000_00
            )
        }

        val events = (1..totalMessages).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp, properties = properties)
        }
        val numberOfBatches = calculateNumberOfBatches(events.first(), totalMessages)

        for (i in events) {
            analytics.track(i)
        }
        while ((analytics.storage.getDataSync().size) < totalMessages) {}

        analytics.blockingFlush()

        assertThat(storage.getDataSync(), anyOf(nullValue(), iterableWithSize(0)))
        verify(mockedDataUploadService, times(numberOfBatches)).uploadSync(anyList(), anyOrNull())
    }


    @Test
    fun `test back pressure strategies`() {
        println("running test test back pressure strategies")
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
        println("running test test should verify sdk")
        val spyControlPlane = spy(ConfigDownloadService::class.java)
        Analytics(
            writeKey, Configuration(
                jsonAdapter
            ), configDownloadService = spyControlPlane
        ).shutdown()
        verify(spyControlPlane, times(0)).download(
            any()
        )
    }

    @Test
    fun `test flush after shutdown`() {
        println("running test test flush after shutdown")
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
        println("running test test no force flush after shutdown")
        analytics.shutdown()
        val events = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        val spyDataUploadService = spy(DataUploadService::class.java)

        analytics = Analytics(
            writeKey, Configuration(
                jsonAdapter
            ), storage = storage, initializationListener = { success, message ->
                assertThat(success, `is`(true))
            }, dataUploadService = spyDataUploadService, configDownloadService = mockedControlPlane
        )
        analytics.shutdown()
        busyWait(500)
        assertThat(analytics.isShutdown, `is`(true))
        //inserting some data to storage

        storage.saveMessage(*events.toTypedArray())

        analytics.blockingFlush()
//        busyWait(/250)
        verify(spyDataUploadService, times(0)).uploadSync(any(), anyOrNull())
    }

    @Test
    fun `test shutdown`() {
        println("running test test shutdown")
        val events = (1..5).map {
            TrackMessage.create("event:$it", RudderUtils.timeStamp)
        }
        val someAnalytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter
            ),
            storage = storage,
            initializationListener = { success, message ->
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
    fun `test custom plugin`() {
        println("running test test custom plugin")
        val isDone = AtomicBoolean(false)
        val customPlugin = Plugin {
            println("inside custom plugin")
            isDone.set(true)
            it.proceed(it.message())
        }
        val waitUntil = AtomicBoolean(false)
        analytics.addCallback(object : Callback{
            override fun success(message: Message?) {
                analytics.removeCallback(this)
                waitUntil.set(message is TrackMessage && message.eventName == "event")
            }

            override fun failure(message: Message?, throwable: Throwable?) {
                analytics.removeCallback(this)
                waitUntil.set(message is TrackMessage && message.eventName == "event")
            }
        })
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
        val timeStarted = System.currentTimeMillis()
        while(waitUntil.get().not() && !isDone.get()){
            if (System.currentTimeMillis() - timeStarted > 10000) {
                assert(false)
                break
            }
        }
//        Awaitility.await().atMost(4, TimeUnit.SECONDS).untilTrue(isDone)

    }

    @Test
    fun `test flush throttling`() {
        println("running test test flush throttling")
        analytics.shutdown()
        Thread.sleep(500)
        val isDone = AtomicBoolean(false)
        //settings to make sure auto dump is off

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
        analytics = Analytics(
            writeKey,
            Configuration(
                jsonAdapter,
                maxFlushInterval = 15_000,
                flushQueueSize = 100,

                ),
            storage = spyStorage,
            initializationListener = { success, message ->
                assertThat(success, `is`(true))
            },
            dataUploadService = mockedDataUploadService,
            configDownloadService = mockedControlPlane
        )
        events.forEach {
            analytics.track(it)
        }
        Thread.sleep(1000) // let messages sink in
        thread {
            repeat((1..10).count()) {
                analytics.flush()
            }
        }
        thread {
            repeat((1..20).count()) {
                analytics.flush()
            }
        }
        Awaitility.await().atMost(100, TimeUnit.SECONDS).untilTrue(isDone)
    }

    @Test
    fun `assert reset called on infrastructure plugins`() {
        println("running test assert reset called on infrastructure plugins")
        val infraPlugin = mock<InfrastructurePlugin>()
        analytics.addInfrastructurePlugin(infraPlugin)
        analytics.reset()
        verify(infraPlugin, times(1)).reset()
    }

    /**
     * This method is used to generate the data of the given size.
     * This method could be used to generate the message of required size (e.g., 32 KB).
     * @param msgSizeInBytes The size of the message in bytes.
     */
    private fun generateDataOfSize(msgSizeInBytes: Int): String {
        return CharArray(msgSizeInBytes).apply { fill('a') }.joinToString("")
    }

    /**
     * This method is used to calculate the number of batches required to processes all the messages.
     *
     * Given that MAX_BATCH_SIZE is 500KB.
     * Suppose we are sending 100 messages and each message is of around 31 KB,
     * then we need to have 6 batches where each batch will have 16 messages and the last batch will have 4 messages.
     * So this method will return 7 as the number of batches.
     *
     * @param message This is required to calculate the size of the message.
     * @param totalNumberOfMessages This is the total number of messages that are to be sent.
     */
    private fun calculateNumberOfBatches(message: Message?, totalNumberOfMessages: Int): Int {
        val messageJSON = message?.let {
            analytics.currentConfiguration?.jsonAdapter?.writeToJson(it, object : RudderTypeAdapter<Message>() {})
        } ?: return 0

        val individualMessageSize = messageJSON.getUTF8Length()
        if (individualMessageSize == 0) {
            return 0
        }

        val messagesPerBatch = RudderUtils.MAX_BATCH_SIZE / individualMessageSize
        return ceil(totalNumberOfMessages.toDouble() / messagesPerBatch).toInt()
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

// Currently, we are not supporting Moshi adapter.
//class MoshiAnalyticsTest : AnalyticsTest() {
//    override val jsonAdapter: JsonAdapter
//        get() = MoshiAdapter()
//
//}

@RunWith(Suite::class)
@Suite.SuiteClasses(/*MoshiAnalyticsTest::class, JacksonAnalyticsTest::class, */GsonAnalyticsTest::class
)
class AnalyticsTestSuite : TestSuite() {}
