/*
 * Creator: Debanjan Chatterjee on 18/01/22, 9:59 AM Last modified: 18/01/22, 9:58 AM
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

package com.rudderstack.android.core.internal.plugins

import com.rudderstack.android.core.*
import com.rudderstack.android.core.internal.CentralPluginChain
import com.rudderstack.android.models.TrackMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.Test

class RudderOptionPluginTest {

    //for test we create 3 destinations
    private val dest1 = BaseDestinationPlugin<Any>("dest-1"){
        return@BaseDestinationPlugin it.proceed(it.message())
    }

    private val dest2 = BaseDestinationPlugin<Any>("dest-2"){
        return@BaseDestinationPlugin it.proceed(it.message())
    }
    private val dest3 = BaseDestinationPlugin<Any>("dest-3"){
        return@BaseDestinationPlugin it.proceed(it.message())
    }
    private val message = TrackMessage.create(
        "ev-1", Utils.timeStamp,
        traits = mapOf(
            "age" to 31,
            "office" to "Rudderstack"
        ),
        externalIds = listOf(
            mapOf("some_id" to "s_id"),
            mapOf("amp_id" to "amp_id"),
        ),
        customContextMap = null
    )

    @Test
    fun `test all true for empty integrations`(){
        //assertion plugin
        val assertPlugin = Plugin{
            //must contain all plugins
            assertThat(it.plugins, allOf(
                iterableWithSize(5),
                hasItems(dest1,dest2,dest3)
            ))
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(message, listOf(
            RudderOptionPlugin(RudderOptions.default()),assertPlugin, dest1, dest2, dest3
        ))
        chain.proceed(message)
    }

    @Test
    fun `test all false for integrations`(){
        //assertion plugin
        val assertPlugin = Plugin{
            //must contain all plugins
            assertThat(it.plugins, allOf(
                iterableWithSize(2),
                everyItem(not(`in`(arrayOf(dest1,dest2,dest3))))
            ))
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(message, listOf(
            RudderOptionPlugin(RudderOptions.Builder()
                .withIntegrations(mapOf("All" to false))
                .build()),assertPlugin, dest1, dest2, dest3
        ))
        chain.proceed(message)
    }

    @Test
    fun `test custom integrations with false`(){
        //assertion plugin
        val assertPlugin = Plugin{
            //must contain all plugins
            assertThat(it.plugins, allOf(
                iterableWithSize(3),
                everyItem(not(`in`(arrayOf(dest2,dest3)))),
                hasItem(dest1)
            ))
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(message, listOf(
            RudderOptionPlugin(RudderOptions.Builder()
                .withIntegrations(mapOf("dest-2" to false,"dest-3" to false))
                .build()),assertPlugin, dest1, dest2, dest3
        ))
        chain.proceed(message)
    }

    @Test
    fun `test custom integrations with true`(){
        //assertion plugin
        val assertPlugin = Plugin{
            //must contain all plugins
            assertThat(it.plugins, allOf(
                iterableWithSize(3),
                everyItem(not(`in`(arrayOf(dest1,dest3)))),
                hasItem(dest2)
            ))
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(message, listOf(
            RudderOptionPlugin(RudderOptions.Builder()
                .withIntegrations(mapOf("All" to false, "dest-2" to true,"dest-3" to false))
                .build()),assertPlugin, dest1, dest2, dest3
        ))
        chain.proceed(message)
    }

}