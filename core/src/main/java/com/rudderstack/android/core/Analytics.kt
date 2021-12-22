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

import com.rudderstack.android.core.settings.Settings
import com.rudderstack.android.core.state.State
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Analytics(
    private val writeKey: String,
    private val settingsState : State<Settings>,
    private val analyticsExecutor: Executor = Executors.newCachedThreadPool(),
    private val networkExecutor : Executor = Executors.newSingleThreadExecutor()
    ) {

    fun track(){}
    fun identify(){}
    fun screen(){}
    fun group(){}
    fun processEvent(){}
    // This applies a closure(a block of code) to each plugin
    fun applyClosure(){}

}