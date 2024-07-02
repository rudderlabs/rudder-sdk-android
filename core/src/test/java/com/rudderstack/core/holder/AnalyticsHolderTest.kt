/*
 * Creator: Debanjan Chatterjee on 08/02/24, 10:10 am Last modified: 08/02/24, 10:09 am
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.core.holder

import com.rudderstack.core.Controller
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class AnalyticsHolderTest {
    @Mock
    private lateinit var controller: Controller

    @Before
    fun setup() {
        whenever(controller.writeKey).thenReturn("writeKey")
    }
    @After
    fun tearDown() {
        controller.clearAll()
    }
    @Test
    fun testStoreAndRetrieve() {
        // Setup
        val identifier = "testIdentifier"
        val value = "testValue"

        // Execute
        controller.store(identifier, value)

        // Verify
        assertEquals(value, controller.retrieve(identifier))
    }

    @Test
    fun testRemove() {
        // Setup
        val identifier = "testIdentifier"
        val value = "testValue"
        controller.store(identifier, value)
        // Execute
        controller.remove(identifier)

        // Verify
        assertNull(controller.retrieve(identifier))
    }

    @Test
    fun testMultipleControllerAccess() {
        // Setup
        val identifier = "testIdentifier"
        val valueForController1 = "testValue"
        val valueForController2 = "testValue2"
        val controller2 = mock<Controller>()
        whenever(controller2.writeKey).thenReturn("writeKey2")

        // Execute
        controller.store(identifier, valueForController1)
        controller2.store(identifier, valueForController2)

        // Verify
        assertEquals(valueForController1, controller.retrieve(identifier))
        assertEquals(valueForController2, controller2.retrieve(identifier))
    }
}
