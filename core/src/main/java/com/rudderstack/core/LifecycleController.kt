/*
 * Creator: Debanjan Chatterjee on 27/12/21, 5:23 PM Last modified: 27/12/21, 5:23 PM
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

import com.rudderstack.models.Message

/**
 * Handles the lifecycle of a message.
 * Most importantly aligns the plugins.
 * Acts as a bridge between application layer and internal business layer.
 * Might also be referred as LCC later on
 *
 */
interface LifecycleController {
    /**
     * Each message is connected to it's Lifecycle Controller. Returns the associated message
     */
    val message : Message

    /**
     * Separate options can be added for each message, null if there are no specific options
     *//*
    val options : RudderOptions?*/

    /**
     * Associated list of plugins
     */
    val plugins : List<Plugin>

    /**
     * The message is up for processing.
     * Plugins will be applied to it.
     *
     */
    fun process()
}