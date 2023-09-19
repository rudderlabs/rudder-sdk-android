/*
 * Creator: Debanjan Chatterjee on 19/09/23, 2:40 pm Last modified: 19/09/23, 2:40 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

package com.rudderstack.rudderjsonadapter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test

class RudderTypeAdapterTest {
    @Test
    fun `test rudder type adapter returns correct type with Map`(){
        val typeAdapter = RudderTypeAdapter<Map<String, Any>>{}
        println(typeAdapter.type)
        assertThat(typeAdapter, Matchers.hasToString(Matchers.containsString("Map")))
    }
    @Test
    fun `test rudder type adapter returns correct type with List`(){
        val typeAdapter = RudderTypeAdapter<Map<String, Any>>{}
        println(typeAdapter.type)
        assertThat(typeAdapter, Matchers.hasToString(Matchers.containsString("List")))
    }
}