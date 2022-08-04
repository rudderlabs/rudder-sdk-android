/*
 * Creator: Debanjan Chatterjee on 28/12/21, 11:51 PM Last modified: 28/12/21, 11:51 PM
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

package com.rudderstack.core

import com.rudderstack.core.Storage.BackPressureStrategy
import com.rudderstack.core.Storage.Companion.MAX_FETCH_LIMIT
import com.rudderstack.models.Message

/**
 * Handles all messages, assorting the plugins, keeping track of cache, to name a few of it's
 * duties
 *
 */
interface Controller {
    /**
     * Apply changed settings to plugins
     *
     * @param settings [Settings] object representing global settings
     */
    fun applySettings(settings: Settings)

    /**
     * Applies a closure method to all available Plugins
     * Can break the system if not properly constructed.
     * Check for the plugin type and apply only to plugins that seem necessary
     *
     * @param closure A method to be run on each plugin
     */
    fun applyClosure(closure : Plugin.() -> Unit)

    /**
     * Anonymous id to be used for all consecutive calls.
     * Anonymous id is mostly used for messages sent prior to user identification or in case of
     * anonymous usage.
     *
     * @param anonymousId String to be used as anonymousId
     */
    fun setAnonymousId(anonymousId : String)

    /**
     * Opt out from analytics and usage monitoring. No further data will be sent once set true
     * @param optOut True to stop analytics data collection, false otherwise
     */
    fun optOut(optOut : Boolean)

    /**
     * Is opted out from analytics
     */
    val isOptedOut : Boolean

   /* *//**
     * Advertising id(if any) to be sent over to destinations
     *
     * @param advertisingId Advertising id for sending to destinations
     *//*
    fun putAdvertisingId(advertisingId: String)

    *//**
     * Set FCM device token
     *
     * @param token device token for FCM
     *//*
    fun putDeviceToken(token : String)
*/ //android
    /**
     * Custom plugins to be added.
     * These can be used to log/transform messages
     *
     * @param plugins  [Plugin] objects
     */
    fun addPlugin(vararg plugins: Plugin)
    /**
     * Custom plugins to be removed.
     *
     * @param plugin  [Plugin] object
     * @return true if successfully removed false otherwise
     */
    fun removePlugin(plugin: Plugin) : Boolean
    /**
     * Submit a [Message] for processing.
     * The message is taken up by the controller and it passes through the set of timelines defined.
     * Refrain from using this unless you are sure about it. Use other utility methods for
     * [Analytics.track], [Analytics.screen], [Analytics.identify], [Analytics.alias], [Analytics.group]
     * In case of external ids, custom contexts and integrations passed in message as well as in options,
     * the ones in options will replace those of message.
     *
     * @param message A [Message] object up for submission
     * @param options Individual [RudderOptions] for this message. Only applicable for this message
     * @param lifecycleController LifeCycleController related to this message, null for default implementation
     */
    fun processMessage(message: Message, options: RudderOptions?, lifecycleController: LifecycleController? = null)

    /**
     * Add a [Callback] for getting notified when a message is processed
     *
     * @param callback An object of [Callback]
     */
    fun addCallback(callback: Callback)

    /**
     * Removes an added [Callback]
     * @see addCallback
     *
     * @param callback The callback to be removed
     */
    fun removeCallback(callback: Callback)

    /**
     * Removes all added [Callback]
     * @see addCallback
     *
     */
    fun removeAllCallbacks()

    //fun reset()
    /**
     * Shuts down the Analytics. Once shutdown, a new instance needs to be created.
     *
     */
    fun shutdown()

    /**
     * true if [shutdown] is called, false otherwise
     */
    val isShutdown : Boolean

    /**
     * clears the storage of all data
     *
     */
    fun clearStorage()
    /**
     * The max number of [Message] that can be fetched and uploaded at one go.
     * Delegates to [Storage.setMaxFetchLimit]
     * @param limit The number of messages to be set as limit, defaults to [MAX_FETCH_LIMIT]
     */
    fun setMaxFetchLimit(limit : Int)

    /**
     * The maximum number of [Message] that can be stored. Beyond which, messages will be
     * discarded based on [BackPressureStrategy] defaults to [BackPressureStrategy.Drop]
     * Delegates to [Storage.setStorageCapacity] and [Storage.setBackpressureStrategy]
     * @param limit The max number of [Message] that can be stored
     */
    fun setMaxStorageCapacity(limit: Int, backPressureStrategy: BackPressureStrategy = BackPressureStrategy.Drop)
}