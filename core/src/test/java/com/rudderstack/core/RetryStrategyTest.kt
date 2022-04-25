/*
 * Creator: Debanjan Chatterjee on 04/04/22, 5:10 PM Last modified: 04/04/22, 5:10 PM
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

import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RetryStrategyTest {
    @Test
    fun `test exponential retry strategy`(){
        val isComplete = AtomicBoolean(false)

        val successOn = 3 //success on 3rd try
        var retryCount = 0
        val startedAt = System.currentTimeMillis()
        val expectedTime = 1000L /*first*/ + 2000L /*second*/ + 4000L /*third*/
        RetryStrategy.exponential().perform({
            retryCount ++ == successOn
        }, {
            assertThat(it, Matchers.`is`(true))
            assertThat(retryCount, Matchers.`is`(4))
            //on 3rd retry, probable 8 seconds


            isComplete.set(true)
        })
        //50 milliseconds, max buffer for processing
        Awaitility.await().atLeast(expectedTime, TimeUnit.MILLISECONDS).atMost((expectedTime + 200L),
            TimeUnit.MILLISECONDS).untilTrue(isComplete)
    }


}