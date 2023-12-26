/*
 * Creator: Debanjan Chatterjee on 04/04/22, 1:29 PM Last modified: 04/04/22, 1:29 PM
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

package com.rudderstack.core.internal

import com.rudderstack.core.Configuration
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.Storage
import com.rudderstack.core.busyWait
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.models.Message
import com.rudderstack.models.TrackMessage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Timer

@RunWith(MockitoJUnitRunner::class)
class FlushSchedulerTest {
    @Mock
    private lateinit var mockFlushSchedulerDataChangeListener: FlushScheduler.Listener

    @Mock
    private lateinit var mockStorage: Storage

    @Mock
    private lateinit var mockConfiguration: Configuration

//    @Mock
//    private lateinit var mockTimer: Timer

    private lateinit var flushScheduler: FlushScheduler
    private var storageDataChangeListener: Storage.DataListener? = null
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

    @Before
    fun setUp() {
        doReturn(300L).`when`(mockConfiguration).maxFlushInterval
        doReturn(mockStorage).`when`(mockConfiguration).storage
        doReturn(RudderUtils.defaultBase64Generator).`when`(mockConfiguration).base64Generator
        doAnswer {
            storageDataChangeListener = it.arguments[0] as Storage.DataListener
            Unit
        }.`when`(mockStorage).addDataListener(any())
        if(ConfigurationsState == null) throw Exception("ConfigurationsState is null")
        if(mockConfiguration == null) throw Exception("Mock Config is null")
        ConfigurationsState.update(mockConfiguration)
        flushScheduler = FlushScheduler(mockFlushSchedulerDataChangeListener)
    }

    @After
    fun tearDown() {
        ConfigurationsState.update(null)
    }

    @Test
    fun `test listener called when events count cross threshold`() {
        doAnswer {
            (it.getArgument(0) as (Long) -> Unit)(
                (ConfigurationsState.value?.flushQueueSize ?: 0) + 1L
            )
            Unit
        }.`when`(mockStorage).getCount(any())
        storageDataChangeListener?.onDataChange()
        verify(mockFlushSchedulerDataChangeListener, times(1)).onDataChange()
    }

    @Test
    fun `test listener called when maxFlushInterval is crossed`() {
//        doAnswer {
//            (it.getArgument(0) as (Long) -> Unit)(
//                (ConfigurationsState.value?.flushQueueSize ?: 0) - 1L // flush queue size not
//            // reached
//            )
//            Unit
//        }.`when`(mockStorage).getCount(any())

        //maxFlushInterval is 300L we will wait double of it to see it's called only once
        busyWait(590L)
        verify(mockFlushSchedulerDataChangeListener, times(1)).onDataChange()
    }

    @Test
    fun `test timer rescheduled when events count cross threshold`() {
        //500L is the default value of maxFlushInterval
        doAnswer {
            (it.getArgument(0) as (Long) -> Unit)(
                (ConfigurationsState.value?.flushQueueSize ?: 0) + 1L
            )
            Unit
        }.`when`(mockStorage).getCount(any())
        storageDataChangeListener?.onDataChange()
        // timer should be rescheduled and no call should take place in 150-300ms
        busyWait(250L)
        //should be called once
        verify(mockFlushSchedulerDataChangeListener, times(1)).onDataChange()
    }


}