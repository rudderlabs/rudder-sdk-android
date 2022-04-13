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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.BasicStorageImpl
import com.rudderstack.android.core.Settings
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.Message
import com.rudderstack.android.models.TrackMessage
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class StorageDecoratorTest {
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
    fun `test storage listener for time`(){
        val isComplete = AtomicBoolean(false)
        //setting flush queue size to a greater one, so it doesn't affect flush
        SettingsState.update((SettingsState.value?: Settings()).copy(
            flushQueueSize = testMessagesList.size + 1,
            maxFlushInterval = 500L
        ))
        var lastTime = 0L
        //we try to insert data and check for the time difference
        val storageDecorator = StorageDecorator(
            BasicStorageImpl(logger = KotlinLogger)
        ) {
            assertThat(it, allOf(
                Matchers.iterableWithSize(testMessagesList.size),
                Matchers.containsInAnyOrder(*testMessagesList.toTypedArray())

            ))
            assertThat(
                (System.currentTimeMillis() - lastTime), Matchers.allOf(
                    Matchers.greaterThanOrEqualTo(500L),
                    Matchers.lessThanOrEqualTo(500L + 10L)
                )
            )
            isComplete.set(true)
            println("list got - $it")
        }
        lastTime = System.currentTimeMillis()
        storageDecorator.saveMessage(*testMessagesList.toTypedArray())
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isComplete)

    }
}