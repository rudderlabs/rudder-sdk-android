/*
 * Creator: Debanjan Chatterjee on 08/02/24, 10:12 am Last modified: 08/02/24, 10:12 am
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
import com.rudderstack.core.State
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class StatesHolderTest {
    @Mock
    private lateinit var controller: Controller

    @Before
    fun setup() {
        whenever(controller.writeKey).thenReturn("testInstance")
    }
    @After
    fun tearDown() {
        controller.clearAll()
    }
    class TestState : State<String>()
    @Test
    fun testAssociateAndRetrieveState() {
        // Setup
        val state = TestState()

        // Execute
        controller.associateState(state)

        // Verify
        assertThat(controller.retrieveState<TestState>(), Matchers.equalTo(state))
    }
    @Test
    fun testLastAssociatedStateCanOnlyBeRetrieved() {
        // Setup
        val state1 = TestState()
        val state2 = TestState()

        // Execute
        controller.associateState(state1)
        controller.associateState(state2)

        // Verify
        assertThat(controller.retrieveState<TestState>(), Matchers.equalTo(state2))
    }
    @Test
    fun testStateRemoval() {
        // Setup
        val state = TestState()
        // Execute
        controller.associateState(state)
        controller.removeState<TestState>()

        // Verify
        assertThat(controller.retrieveState<TestState>(), Matchers.nullValue())
    }
}
