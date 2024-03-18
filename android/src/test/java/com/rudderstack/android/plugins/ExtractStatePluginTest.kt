/*
 * Creator: Debanjan Chatterjee on 04/04/22, 1:36 PM Last modified: 04/04/22, 1:36 PM
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

package com.rudderstack.android.plugins

import com.rudderstack.android.internal.plugins.ExtractStatePlugin
import com.rudderstack.core.RudderOptions
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.BasicStorageImpl
import com.rudderstack.models.IdentifyMessage
import org.junit.Test

/**
 * ExtractStatePlugin works for Identify and AliasMessage
 * Checking the storage validates the usage
 *
 */

class ExtractStatePluginTest {

    @Test
    fun `test identify with traits userId`(){
        val storage = BasicStorageImpl( )
        val options = RudderOptions.Builder().withExternalIds(listOf(mapOf(
            "dest-1" to "dest-1-id"
        ))).build()
        val extractStatePlugin = ExtractStatePlugin()
        val identifyMsg = IdentifyMessage.create(timestamp = RudderUtils.timeStamp,
        traits = mapOf("userId" to "userId"));
//        val centralPluginChain = CentralPluginChain(identifyMsg, listOf(RudderOptionPlugin(options),
//            extractStatePlugin))
//        centralPluginChain.proceed(identifyMsg)

        //TODO(add tests. currently we are not using core)
//        assertThat(storage.traits, allOf(aMapWithSize(1),
//        hasEntry("userId", "userId")))
//        assertThat(storage.externalIds, iterableWithSize(1))
//        assertThat(
//            storage.externalIds!![0], allOf(aMapWithSize(1),
//            hasEntry("dest-1", "dest-1-id")))
    }
    @Test
    fun `test identify with both options and message external ids`(){
        val storage = BasicStorageImpl( )
        val options = RudderOptions.Builder().withExternalIds(listOf(mapOf(
            "dest-1" to "dest-1-id"
        ),mapOf(
            "dest-2" to "dest-2-id"
        )
        )).build()
        val extractStatePlugin = ExtractStatePlugin()
        val identifyMsg = IdentifyMessage.create(timestamp = RudderUtils.timeStamp,
        traits = mapOf("userId" to "userId"), externalIds = listOf(mapOf(
                "dest-3" to "dest-3-id"
            ), mapOf(
                "dest-2" to "not-dest-2-id"
            ) ))
//        val centralPluginChain = CentralPluginChain(identifyMsg, listOf(RudderOptionPlugin(options),
//            extractStatePlugin))
//        centralPluginChain.proceed(identifyMsg)
//        assertThat(storage.traits, allOf(aMapWithSize(1),
//        hasEntry("userId", "userId")))
//        assertThat(storage.externalIds, allOf(iterableWithSize(3),
//        everyItem(
//            aMapWithSize(1)
//        ), containsInAnyOrder(
//                mapOf(
//                    "dest-1" to "dest-1-id"
//                ),mapOf(
//                    "dest-2" to "not-dest-2-id"
//                ),
//                mapOf(
//                    "dest-3" to "dest-3-id"
//                )
//        )
//        ))
    }
    @Test
    fun `test identify with message external ids`(){
        val storage = BasicStorageImpl( )
        val options = RudderOptions.defaultOptions()
        val extractStatePlugin = ExtractStatePlugin(
        )
        val identifyMsg = IdentifyMessage.create(timestamp = RudderUtils.timeStamp,
        traits = mapOf("userId" to "userId"), externalIds = listOf(mapOf(
                "dest-1" to "dest-1-id"
            )))
//        val centralPluginChain = CentralPluginChain(identifyMsg, listOf(RudderOptionPlugin(options),
//            extractStatePlugin))
//        centralPluginChain.proceed(identifyMsg)

//        assertThat(storage.traits, allOf(aMapWithSize(1),
//        hasEntry("userId", "userId")))
//        assertThat(storage.externalIds, iterableWithSize(1))
//        assertThat(
//            storage.externalIds!![0], allOf(aMapWithSize(1),
//            hasEntry("dest-1", "dest-1-id")))
    }
}