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

package com.rudderstack.android.core

import com.rudderstack.android.core.internal.KotlinLogger
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.gsonrudderadapter.GsonAdapter
import com.rudderstack.android.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.android.models.*
import com.rudderstack.android.moshirudderadapter.MoshiAdapter
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.web.HttpResponse
import junit.framework.TestSuite
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.mockito.kotlin.any
import kotlin.concurrent.thread

abstract class AnalyticsTest {
    //test track, alias, identify, group
    //wake up working,
    //rudder options work
    //test anon_id
    //complete flow
    protected abstract val jsonAdapter: JsonAdapter
    private lateinit var analytics: Analytics
    private val storage = BasicStorageImpl(logger = KotlinLogger)
    private val mockedControlPlane = mock(ConfigDownloadService::class.java).also {
        `when`(it.download(anyString(), anyString(), anyString(), any(), any())).then {

            it.getArgument<(success : Boolean, RudderServerConfig?, lastErrorMsg : String?) -> Unit>(4)
                .invoke(true, RudderServerConfig(source =
                RudderServerConfig.RudderServerConfigSource(isSourceEnabled = true)), null
                )
        }
    }
    private val mockedDataUploadService = mock(DataUploadService::class.java).also {
        `when`(it.uploadSync(any(), any())).then {
            val data = it.getArgument<List<Message>>(0)
            storage.deleteMessages(data)
            HttpResponse(200,null,null )
        }
        `when`(it.upload(any(), any(), any())).then {
            val data = it.getArgument<List<Message>>(0)
            storage.deleteMessages(data)
            println("\nupload mock: $data \nthread: ${Thread.currentThread().name}\n")
            it.getArgument<(response: HttpResponse<out Any>) -> Unit>(2).invoke(
                HttpResponse(200, "OK", null, null)
            )
        }
    }
    @Before
    fun setup() {
        analytics = Analytics(
            writeKey,
            Settings(),
            jsonAdapter,
            storage = storage,
            initializationListener = { success, message ->
                println("success")
                assertThat(success, `is`(true))
            }, dataUploadService = mockedDataUploadService,
        configDownloadService = mockedControlPlane)
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

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilTrue(isDone)
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

        val dummyPlugin = BaseDestinationPlugin<Any>("dummy"){
            assert(false)
            it.proceed(it.message())
        }
        val assertdestination = BaseDestinationPlugin<Any>("dest-1"){
            val msg = it.message()
            assertThat(msg.integrations, allOf(
                aMapWithSize(2),
                hasEntry("dest-1", true),
                hasEntry("All", false),
            ))
            assertThat(msg.context?.externalIds, allOf(
                notNullValue(),
                iterableWithSize(1),
                containsInAnyOrder(mapOf("some_id" to "id"))
            ))
            isDone.set(true)
            it.proceed(it.message())
        }
        analytics.addPlugin(assertdestination, dummyPlugin)
        analytics.track(eventName = "some", trackProperties =  mapOf(), options = rudderOptions)
        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilTrue(isDone)


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
            object : Callback{
                private var incomingMsgs = 0
                override fun success(message: Message?) {
                    ++incomingMsgs
                    println("incoming - $incomingMsgs, msg- ${message}\n time: " +
                            "${System.currentTimeMillis()}, this: $this, thread: ${Thread.currentThread().name}\n")
                    assertThat(message, allOf(
                        notNullValue(), hasProperty("eventName", allOf(`in`(itemNamesThatShouldBePresent.toTypedArray()),
                        not(`in`(itemNamesThatShouldNotBePresent)))),

                    ))

                    if (incomingMsgs > newSettings.flushQueueSize) {
                        assert(false)
                        isDone.set(true)
                    }
                    if(incomingMsgs == newSettings.flushQueueSize)
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

        for (i in items){
            analytics.track(i)
            println("\ntrack $i\n")
        }
//        Thread.sleep(newSettings.maxFlushInterval)
//        val listArgumentCaptor : ArgumentCaptor<List<Message>> = ArgumentCaptor.forClass(List::class.java)
//                as ArgumentCaptor<List<Message>>
//        Thread.sleep(newSettings.maxFlushInterval + 100)
//        verify(spyDataUploadService, times(1)).upload(listArgumentCaptor.capture(), any(), any())

//        assertThat(listArgumentCaptor.value, allOf(
//            iterableWithSize(3), everyItem(notNullValue()),
//            contains(*items.toTypedArray())
//        ))
        Awaitility.await().atMost(newSettings.maxFlushInterval , TimeUnit.SECONDS)
            .untilTrue(isDone)
    }
    @Test
    fun `test multiple messages ordering`() {

    }

    fun `test flush before shutdown`() {

    }
    fun `test back pressure strategies`(){

    }
    fun `test should verify sdk`(){

    }
    fun `test flush after shutdown`() {

    }

    fun `test shutdown`() {}
    fun `test periodic flush`() {

    }

    fun `test message queue overflow`(){

    }

    fun `test custom plugin`() {}
    fun `test custom sub plugin`() {

    }

    fun `test message thread safety`() {}
    fun `test shutdown thread safety`() {

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