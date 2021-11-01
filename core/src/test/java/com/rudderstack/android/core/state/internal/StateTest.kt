/*
 * Creator: Debanjan Chatterjee on 31/10/21, 12:09 AM Last modified: 31/10/21, 12:09 AM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.android.core.state.internal

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test

class StateTest {

    companion object {
        private const val INITIAL = "initial"
    }

    //a dummy State
    object DummyState : State<String>(INITIAL) {
    }

    /**
     * Test if observers work as expected
     *
     */
    @Test
    fun testStateObservers() {
        val firstMessage = "first"
        val secondMessage = "second"
        val thirdMessage = "third"

        var isObserver3Called = false
        var isObserver2Called = false
        var isObserver1Called = false


        //should get only first message
        val observer1 = object : State.Observer<String> {
            override fun onStateChange(state: String?) {
                println("obs1: $state")
                assertThat(state, anyOf(equalTo(INITIAL), equalTo(firstMessage)))
                isObserver1Called = true
            }
        }
        //should get only first and second messages
        val observer2 = object : State.Observer<String> {
            override fun onStateChange(state: String?) {
                println("obs2: $state")
                assertThat(
                    state,
                    anyOf(equalTo(INITIAL), equalTo(firstMessage), equalTo(secondMessage))
                )
                isObserver2Called = true
            }
        }
        //should get first, second and third messages
        val observer3 = object : State.Observer<String> {
            override fun onStateChange(state: String?) {
                println("obs3: $state")
                assertThat(
                    state, anyOf(
                        equalTo(INITIAL),
                        equalTo(firstMessage),
                        equalTo(secondMessage), equalTo(thirdMessage), emptyOrNullString()
                    )
                )
                //this observer is to be called always
                isObserver3Called = true

            }
        }

        DummyState.subscribe(observer1)
        observer2?.apply {
            DummyState.subscribe(this)
        }
        DummyState.subscribe(observer3)

        //test
        DummyState.update(firstMessage)
        assertThat(isObserver3Called, equalTo(true))
        assertThat(isObserver2Called, equalTo(true))
        assertThat(isObserver1Called, equalTo(true))

        //remove first
        DummyState.removeObserver(observer1)

        DummyState.update(secondMessage)
        assertThat(isObserver3Called, equalTo(true))
        assertThat(isObserver2Called, equalTo(true))

        DummyState.removeObserver(observer2)
        DummyState.update(thirdMessage)
        assertThat(isObserver3Called, equalTo(true))

        DummyState.update(null)
    }
}