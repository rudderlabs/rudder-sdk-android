/*
 * Creator: Debanjan Chatterjee on 28/12/21, 11:53 PM Last modified: 28/12/21, 11:53 PM
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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.*
import com.rudderstack.android.core.internal.plugins.*
import com.rudderstack.android.core.internal.plugins.AnonymousIdPlugin
import com.rudderstack.android.core.internal.plugins.GDPRPlugin
import com.rudderstack.android.core.internal.plugins.RudderOptionPlugin
import com.rudderstack.android.core.internal.plugins.WakeupActionPlugin
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.Message
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class AnalyticsDelegate(
    private val writeKey: String,
    settings : Settings,
    private val analyticsExecutor: Executor = Executors.newCachedThreadPool(),
    private val networkExecutor : Executor = Executors.newSingleThreadExecutor()
) : Controller {
    private val customPlugins : MutableList<Plugin> = ArrayList()
    private val destinationPlugins : MutableList<DestinationPlugin<*>> = ArrayList()
    init {
        SettingsState.update(settings)
        wakeupAction()
    }


    override fun applySettings(settings: Settings) {
        SettingsState.update(settings)
    }

    override fun applyClosure(closure: Plugin.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun setAnonymousId(anonymousId: String) {
        TODO("Not yet implemented")
    }

    override fun optOut(optOut: Boolean) {
        TODO("Not yet implemented")
    }

    override val isOptedOut: Boolean
        get() = TODO("Not yet implemented")

    override fun putAdvertisingId(advertisingId: String) {
        TODO("Not yet implemented")
    }

    override fun putDeviceToken(token: String) {
        TODO("Not yet implemented")
    }

    override fun addPlugin(vararg plugin: Plugin) {
        if (plugin.isEmpty()) return
        plugin.forEach {
            if(it is DestinationPlugin<*>) destinationPlugins.add(it)
            else
                customPlugins.add(it)
        }
    }

    override fun processMessage(message: Message, options: RudderOptions?, lifecycleController: LifecycleController?) {
       /* val operatePlugins = listOf(GDPRPlugin(),
            RudderOptionPlugin(options?: SettingsState.value?.options?: return),
        AnonymousIdPlugin()
        ) + customPlugins + WakeupActionPlugin(storage = )

        val lcc = lifecycleController?: message.let { msg ->

            LifecycleControllerImpl(msg, operatePlugins )
        }*/

    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

    private fun wakeupAction() {
        TODO("Not yet implemented")
    }
}