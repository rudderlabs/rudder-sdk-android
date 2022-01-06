/*
 * Creator: Debanjan Chatterjee on 26/11/21, 12:24 AM Last modified: 26/11/21, 12:24 AM
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

package com.rudderstack.android.core

import com.rudderstack.android.core.internal.AnalyticsDelegate
import com.rudderstack.android.models.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Analytics(
    writeKey: String,
    settings: Settings,
    analyticsExecutor: Executor = Executors.newCachedThreadPool(),
    networkExecutor: Executor = Executors.newSingleThreadExecutor()
) : Controller by AnalyticsDelegate(
    writeKey,
    settings,
    analyticsExecutor,
    networkExecutor
) {


    fun track(message: TrackMessage, options: RudderOptions? = null) {

    }
    fun track(eventName: String, properties : Map<String, Any>, options: RudderOptions? = null) {}
    fun screen(message: ScreenMessage, options: RudderOptions? = null) {}
    fun screen(screenName: String, category: String, properties: Map<String, Any>, options: RudderOptions? = null) {}
    fun identify(message: IdentifyMessage, options: RudderOptions? = null) {
    }
    fun identify(userID: String, traits: Map<String, Any>? = null, options: RudderOptions? = null) {
    }
    fun alias(message: AliasMessage, options: RudderOptions? = null) {}
    fun alias(newId: String, options: RudderOptions? = null) {}

    fun group(message: GroupMessage, options: RudderOptions? = null) {}
    fun group(groupID: String, traits: Map<String, Any>? = null, options: RudderOptions? = null) {}
    private fun processMessage(message: Message) {

    }

}